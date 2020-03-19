package ru.spbstu

import ru.spbstu.Rational.Companion.HALF
import ru.spbstu.Rational.Companion.ZERO
import ru.spbstu.Rational.Companion.ONE
import kotlin.test.*

class RationalTest {
    @Test
    fun smokeTest() {
        assertEquals(Rational(1, 2),1.toRational() / 2)
        assertEquals(Rational(1, 4), 4.toRational() / 16)
        assertEquals(Rational(6, 9), 6.toRational() / 9)
        assertEquals(Rational(2, 3),6.toRational() / 9)

        assertEquals(Rational(-24, 36),6.toRational() / -9)

        assertEquals(Rational(8, -256).num, -1)
        assertEquals(Rational(8, -256).den, 32)

        assertFailsWith<IllegalArgumentException> { Rational(1, 0) }
    }

    @Test
    fun comparisonTest() {
        assertTrue { Rational(1, 2) < Rational(3, 4) }
        assertTrue { Rational(10, 18) > Rational(10, 19) }
    }

    @Test
    fun inverseTest() {
        assertEquals(2.toRational(), HALF.inverse())
        assertEquals(Rational(3, -4), Rational(4, -3).inverse())
    }

    @Test
    fun arithmeticsTest() {
        assertEquals(-HALF, Rational(-1, 2))
        assertEquals(-HALF, ZERO - HALF)
        assertEquals(ONE,HALF + HALF)
        assertEquals(ONE, HALF * 2)
        assertEquals(ONE, HALF / HALF)
        assertEquals(Rational(1, 4), HALF / 2)
        assertEquals(Rational(1, 4), HALF * HALF)
        assertEquals(Rational(1, -4), -HALF * HALF)
        assertEquals(Rational(3, 2), HALF + 1)
        assertEquals(ZERO, HALF - HALF)
        assertEquals(-HALF, HALF - 1)
        assertEquals(Rational(1, 32),Rational(7, 256) + Rational(1, 256))

        assertEquals(Rational(1, 4), HALF pow 2)
        assertEquals(Rational(1, 8), HALF pow 3)
        assertEquals(Rational(8), HALF pow -3)
        assertEquals(ONE, HALF pow 0)
        assertEquals(HALF, HALF pow 1)
        assertEquals(2L.toRational(), HALF pow -1)
    }

    @Test
    fun miscTest() {
        assertTrue { (HALF * 4).isWhole() }
        assertTrue { (HALF * -4).isWhole() }
        assertFalse { (HALF * 3).isWhole() }
        assertTrue { (Rational(1, 4) + Rational(3, 4)).isWhole() }

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
        val map = hashMapOf<Rational, Int>()
        map[HALF] = 1
        map[Rational(4, 8)] = 2
        map[Rational(-4, 8)] = 3

        assertEquals(mapOf(HALF to 2, -HALF to 3), map)
    }

    @Test
    fun toStringTest() {
        val lst = listOf(HALF, Rational(3, -4), ONE * 2, ZERO, Rational.ONE)
        assertEquals("[1/2, -3/4, 2, 0, 1]", "$lst")
    }

}
