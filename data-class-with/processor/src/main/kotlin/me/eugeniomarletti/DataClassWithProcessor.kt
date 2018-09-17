package me.eugeniomarletti

import com.google.auto.service.AutoService
import kotlinx.metadata.*
import kotlinx.metadata.jvm.KotlinClassMetadata
import me.eugeniomarletti.Generator.Input
import me.eugeniomarletti.Generator.Parameter
import me.eugeniomarletti.Generator.TypeParameter
import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import me.eugeniomarletti.kotlin.visitors.KmTypeInfoVisitor
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic.Kind.ERROR

@AutoService(Processor::class)
@Suppress("unused")
class DataClassWithProcessor : KotlinAbstractProcessor() {

    private val annotationName = WithMethods::class.java.canonicalName

    override fun getSupportedAnnotationTypes() = setOf(annotationName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotationElement = elementUtils.getTypeElement(annotationName)
        @Suppress("LoopToCallChain")
        for (element in roundEnv.getElementsAnnotatedWith(annotationElement)) {
            val input = getInputFrom(element) ?: continue
            if (!input.generateAndWrite()) return true
        }
        return true
    }

    private fun getInputFrom(element: Element): Input? {
        val metadata = element.kotlinMetadata

        if (metadata !is KotlinClassMetadata.Class) {
            errorMustBeDataClass(element)
            return null
        }

        lateinit var fqClassName: String
        lateinit var `package`: String
        val typeArguments = mutableListOf<TypeParameter>()
        val typeArgsRegistry = mutableMapOf<Int, String>()
        val parameters = mutableListOf<Parameter>()

        var isDataClass = true

        metadata.accept(object : KmClassVisitor() {
            override fun visit(flags: Flags, name: ClassName) {
                if (!flags.isDataClass) {
                    isDataClass = false
                    return
                }

                `package` = name.substringBeforeLast('/').fqName()
                fqClassName = name.fqName()
            }

            override fun visitConstructor(flags: Flags): KmConstructorVisitor? {
                if (!flags.isPrimaryConstructor) {
                    return null
                }

                return object : KmConstructorVisitor() {
                    override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                        return object : KmValueParameterVisitor() {
                            override fun visitType(flags: Flags): KmTypeVisitor? {
                                return KmTypeInfoVisitor(flags, typeArgsRegistry::get) {
                                    parameters += Parameter(name, it.fqName)
                                }
                            }
                        }
                    }
                }
            }

            override fun visitTypeParameter(flags: Flags, name: String, id: Int, variance: KmVariance): KmTypeParameterVisitor? {
                return object : KmTypeParameterVisitor() {
                    val upperBoundsFqClassNames = arrayListOf<String>()

                    override fun visitUpperBound(flags: Flags): KmTypeVisitor? {
                        return KmTypeInfoVisitor(flags, typeArgsRegistry::get) {
                            upperBoundsFqClassNames += it.fqName
                        }
                    }

                    override fun visitEnd() {
                        typeArgsRegistry += id to name
                        typeArguments += TypeParameter(name, upperBoundsFqClassNames)
                    }
                }
            }
        })

        val extensionName = element.getAnnotation(WithMethods::class.java).extensionName

        if (!isDataClass) {
            errorMustBeDataClass(element)
            return null
        }

        return Input(
            fqClassName = fqClassName,
            `package` = `package`,
            typeArgumentList = typeArguments,
            parameterList = parameters,
            extensionName = extensionName)
    }

    private fun errorMustBeDataClass(element: Element) {
        messager.printMessage(ERROR,
            "@${WithMethods::class.java.simpleName} can't be applied to $element: must be a Kotlin data class", element)
    }

    private fun Input.generateAndWrite(): Boolean {
        val generatedDir = generatedDir ?: run {
            messager.printMessage(ERROR, "Can't find option '$kaptGeneratedOption'")
            return false
        }
        val dirPath = `package`.replace('.', File.separatorChar)
        val filePath = "DataClassWithExtensions_${fqClassName.substringAfter(`package`).replace('.', '_')}.kt"
        val dir = File(generatedDir, dirPath).also { it.mkdirs() }
        val file = File(dir, filePath)
        file.writeText(generate())
        return true
    }
}
