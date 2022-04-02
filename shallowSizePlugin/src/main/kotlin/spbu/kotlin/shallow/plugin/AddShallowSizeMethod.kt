package spbu.kotlin.shallow.plugin

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isShort
import org.jetbrains.kotlin.ir.types.isUByte
import org.jetbrains.kotlin.ir.types.isUInt
import org.jetbrains.kotlin.ir.types.isULong
import org.jetbrains.kotlin.ir.types.isUShort
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.properties

const val DEFAULT_SIZE = 8
const val UNIT_SIZE = 8
const val BOOLEAN_SIZE = 1

@Suppress("complexity.ComplexMethod")
fun IrType.byteSize(): Int = when {
    this.isChar() -> Char.SIZE_BYTES
    this.isByte() -> Byte.SIZE_BYTES
    this.isShort() -> Short.SIZE_BYTES
    this.isInt() -> Int.SIZE_BYTES
    this.isLong() -> Long.SIZE_BYTES
    this.isUByte() -> UByte.SIZE_BYTES
    this.isUShort() -> UShort.SIZE_BYTES
    this.isUInt() -> UInt.SIZE_BYTES
    this.isULong() -> ULong.SIZE_BYTES
    this.isFloat() -> Float.SIZE_BYTES
    this.isDouble() -> Double.SIZE_BYTES
    this.isBoolean() -> BOOLEAN_SIZE
    this.isUnit() -> UNIT_SIZE
    else -> DEFAULT_SIZE
}

fun IrClass.findFunctionByNameAndArguments(name: String, nArguments: Int = 0): IrSimpleFunction? =
    this.functions.find { it.name.asString() == name && it.valueParameters.size == nArguments }

fun IrClass.calculateShallowSizeOfFields(): Int = this.properties.sumOf { it.backingField?.type?.byteSize() ?: 0 }

val Meta.GenerateShallowSize: CliPlugin
    get() = "Generate shallowSize method" {
        meta(
            classDeclaration(this, { element.isData() }) { declaration ->
                Transform.replace(
                    replacing = declaration.element,
                    newDeclaration =
                    """|$`@annotations` $modality $visibility $kind $name $`(typeParameters)` $`(params)` $superTypes {
                       |    $body
                       |    fun shallowSize(): Int = TODO("Body will be replaced at compile time.")
                       |}""".trimMargin().`class`
                )
            },
            irClass { clazz ->
                if (clazz.isData) {
                    val shallowSizeFunction = clazz.findFunctionByNameAndArguments("shallowSize")
                        ?: throw NoSuchElementException("shallowSize() wasn't added to data class")
                    val size = clazz.calculateShallowSizeOfFields()
                    DeclarationIrBuilder(pluginContext, shallowSizeFunction.symbol).irBlockBody {
                        shallowSizeFunction.body = irBlockBody {
                            +irReturn(
                                irInt(size)
                            )
                        }
                    }
                }
                clazz
            }
        )
    }
