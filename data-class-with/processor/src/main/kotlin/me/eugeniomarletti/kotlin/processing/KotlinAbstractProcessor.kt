package me.eugeniomarletti.kotlin.processing

import me.eugeniomarletti.kotlin.metadata.kaptGeneratedOption
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

/**
 * An [AbstractProcessor] that overrides every method to provide correct Kotlin types.
 *
 * Implements [KotlinProcessingEnvironment] for ease of use and extension.
 */
abstract class KotlinAbstractProcessor : AbstractProcessor(), KotlinProcessingEnvironment {

    override abstract fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean

    /** @see [AbstractProcessor.processingEnv] **/
    override val processingEnv: ProcessingEnvironment get() = super.processingEnv

    /**
     * Returns the directory where generated Kotlin sources should be placed in order to be compiled.
     *
     * If `null`, then this processor is probably not being run through kapt.
     */
    val generatedDir: File? get() = options[kaptGeneratedOption]?.let(::File)
}
