package me.eugeniomarletti.kotlin.visitors

import kotlinx.metadata.*
import me.eugeniomarletti.kotlin.metadata.fqName
import me.eugeniomarletti.kotlin.metadata.isNullableType

class KmTypeInfoVisitor(
        private val flags: Flags,
        private val typeResolver: (Int) -> String?,
        private val onVisitEnd: (KmTypeInfoVisitor) -> Unit
) : KmTypeVisitor() {
    lateinit var fqName: String
        private set

    private val typeArgs = arrayListOf<String>()

    override fun visitClass(name: ClassName) {
        fqName = name.fqName()
    }

    override fun visitTypeAlias(name: ClassName) {
        fqName = name.fqName()
    }

    override fun visitAbbreviatedType(flags: Flags): KmTypeVisitor? {
        return KmTypeInfoVisitor(flags, typeResolver) {
            fqName = it.fqName
            typeArgs.clear()
        }
    }

    override fun visitTypeParameter(id: Int) {
        fqName = typeResolver(id) ?: throw IllegalStateException(
                "Visiting TypeParameter with id=$id, but it's missing in the TypeParameter registry")
    }

    override fun visitArgument(flags: Flags, variance: KmVariance): KmTypeVisitor? {
        return KmTypeInfoVisitor(flags, typeResolver) {
            typeArgs += it.fqName
        }
    }

    override fun visitStarProjection() {
        typeArgs += "*"
    }

    override fun visitEnd() {
        if (typeArgs.isNotEmpty()) {
            fqName += typeArgs.joinToString(prefix = "<", postfix = ">")
            typeArgs.clear()
        }

        if (flags.isNullableType) {
            fqName += '?'
        }

        onVisitEnd(this)
    }
}
