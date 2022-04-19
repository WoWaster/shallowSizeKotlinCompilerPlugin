package spbu.kotlin.shallow.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

const val DEFAULT_SIZE = 8

class AddShallowSizeMethodTest {
    @ParameterizedTest(name = "{0}Test")
    @MethodSource("testInputData")
    fun `test shallowSize()`(expected: Int, actual: Int) {
        assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        fun testInputData() = listOf(
            Arguments.of(Named.of("baseShallow", DEFAULT_SIZE), BaseClass("Hello").shallowSize()),
            Arguments.of(Named.of("internalModifier", 1), InternalClass(true).shallowSize()),
            Arguments.of(Named.of("inheritInterfaces", Int.SIZE_BYTES), InheritInterfaces(3).shallowSize()),
            Arguments.of(Named.of("inheritClass", Int.SIZE_BYTES), InheritClass(3).shallowSize()),
            Arguments.of(Named.of("noBackField", 2), NoBackField('c').shallowSize()),
            Arguments.of(Named.of("privateFields", Long.SIZE_BYTES + Int.SIZE_BYTES), PrivateFields(3).shallowSize()),
            Arguments.of(
                Named.of(
                    "multipleFieldsInConstructor",
                    Byte.SIZE_BYTES + Short.SIZE_BYTES + Int.SIZE_BYTES + Long.SIZE_BYTES
                ),
                MultipleFieldsInConstructor(1, 2, 3, 4).shallowSize()
            ),
            Arguments.of(
                Named.of("nullablePrimitives", 4 * DEFAULT_SIZE),
                NullablePrimitives(1f, 1.0, 'c', true).shallowSize()
            ),
            Arguments.of(Named.of("javaCharacter", DEFAULT_SIZE), JavaCharacter(Character('3')).shallowSize()),
            Arguments.of(Named.of("noExplicitType", Int.SIZE_BYTES + Long.SIZE_BYTES), NoExplicitType(3).shallowSize()),
            Arguments.of(Named.of("overrideFieldFromClass", Int.SIZE_BYTES), OverrideFieldFromClass(4).shallowSize()),
            Arguments.of(
                Named.of("overrideFieldFromInterfaceTest", Int.SIZE_BYTES),
                OverrideFieldFromInterface(4).shallowSize()
            ),
        )
    }
}
