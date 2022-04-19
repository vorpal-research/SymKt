package ru.spbstu

import ru.spbstu.SymRational.Companion.HALF
import ru.spbstu.SymRational.Companion.ZERO
import ru.spbstu.SymRational.Companion.ONE
import kotlin.test.*

class RationalTest {
    @Test
    fun smokeTest() {
        assertEquals(SymRational(1, 2),1.toRational() / 2)
        assertEquals(SymRational(1, 4), 4.toRational() / 16)
        assertEquals(SymRational(6, 9), 6.toRational() / 9)
        assertEquals(SymRational(2, 3),6.toRational() / 9)

        assertEquals(SymRational(-24, 36),6.toRational() / -9)

        assertEquals(SymRational(8, -256).num, -1)
        assertEquals(SymRational(8, -256).den, 32)

        assertFailsWith<IllegalArgumentException> { SymRational(1, 0) }
    }

    @Test
    fun comparisonTest() {
        assertTrue { SymRational(1, 2) < SymRational(3, 4) }
        assertTrue { SymRational(10, 18) > SymRational(10, 19) }
    }

    @Test
    fun inverseTest() {
        assertEquals(2.toRational(), HALF.inverse())
        assertEquals(SymRational(3, -4), SymRational(4, -3).inverse())
    }

    @Test
    fun arithmeticsTest() {
        assertEquals(-HALF, SymRational(-1, 2))
        assertEquals(-HALF, ZERO - HALF)
        assertEquals(ONE,HALF + HALF)
        assertEquals(ONE, HALF * 2)
        assertEquals(ONE, HALF / HALF)
        assertEquals(SymRational(1, 4), HALF / 2)
        assertEquals(SymRational(1, 4), HALF * HALF)
        assertEquals(SymRational(1, -4), -HALF * HALF)
        assertEquals(SymRational(3, 2), HALF + 1)
        assertEquals(ZERO, HALF - HALF)
        assertEquals(-HALF, HALF - 1)
        assertEquals(SymRational(1, 32),SymRational(7, 256) + SymRational(1, 256))

        assertEquals(SymRational(1, 4), HALF pow 2)
        assertEquals(SymRational(1, 8), HALF pow 3)
        assertEquals(SymRational(8), HALF pow -3)
        assertEquals(ONE, HALF pow 0)
        assertEquals(HALF, HALF pow 1)
        assertEquals(2L.toRational(), HALF pow -1)
    }

    @Test
    fun miscTest() {
        assertTrue { (HALF * 4).isWhole() }
        assertTrue { (HALF * -4).isWhole() }
        assertFalse { (HALF * 3).isWhole() }
        assertTrue { (SymRational(1, 4) + SymRational(3, 4)).isWhole() }

        assertEquals(4L, (ONE * 4).wholePart)
        assertEquals(2L, (HALF * 4).wholePart)
        assertEquals(2L, (HALF * 5).wholePart)
        assertEquals(ZERO, (ONE * 4).fractionalPart)
        assertEquals(ZERO, (HALF * 4).fractionalPart)
        assertEquals(HALF, (HALF * 5).fractionalPart)

        assertEquals(0.5, HALF.toDouble())
        assertEquals(-1.0, (-ONE).toDouble())

    }

    @Test
    fun hashCodeTest() {
        val map = hashMapOf<SymRational, Int>()
        map[HALF] = 1
        map[SymRational(4, 8)] = 2
        map[SymRational(-4, 8)] = 3

        assertEquals(mapOf(HALF to 2, -HALF to 3), map)
    }

    @Test
    fun toStringTest() {
        val lst = listOf(HALF, SymRational(3, -4), ONE * 2, ZERO, SymRational.ONE)
        assertEquals("[1/2, -3/4, 2, 0, 1]", "$lst")
    }

}
