package ru.spbstu

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.PI
import kotlin.math.pow
import kotlin.random.Random

class DoubleTest {
    @Test
    fun smokeTest() {
        assertEquals(SymDouble(PI), SymDouble(PI) / 1.0)
        assertEquals(SymDouble(PI), SymDouble(PI) *  1.0)
        assertEquals(SymDouble(PI), SymDouble(PI) * 1)
        assertEquals(SymDouble(PI), SymDouble(PI) * SymRational(1))
    }

    @Test
    fun comparisonTest() {
        assertTrue { SymDouble(0.1234) < SymDouble(0.234) }
        assertTrue{ SymDouble(0.1) < SymRational(1, 9)}
        assertTrue { SymDouble(0.1) > SymRational(1, 11) }
    }

    @Test
    fun inverseTest() {
        assertEquals(SymDouble(1.0), SymDouble(1.0).inverse())
        assertEquals(SymDouble(0.5), SymDouble(2.0).inverse())
    }

    @Test
    fun arithmeticsTest() {
        for (k in 1..100) {
            val test = Random.nextDouble()
            assertEquals(-SymDouble(test), SymDouble(-test))
            assertEquals(-SymDouble(test), SymRational.ZERO - SymDouble(test))
            assertEquals(SymDouble(test * 2), SymDouble(test) + SymDouble(test))
            assertEquals(SymDouble(test * 2), SymDouble(test) * 2)
            assertEquals(SymDouble(1), SymDouble(test) / SymDouble(test))
            assertEquals(SymDouble(test / 2), SymDouble(test) / 2)
            assertEquals(SymDouble(test * test), SymDouble(test) * SymDouble(test))
            assertEquals(SymDouble(- test * test), -SymDouble(test) * SymDouble(test))
            assertEquals(SymDouble(test + 1), SymDouble(test) + 1)
            assertEquals(SymDouble(0), SymDouble(test) - SymDouble(test))
            assertEquals(SymDouble(test - 1), SymDouble(test) - 1)

            assertEquals(SymDouble(test.pow(2)), SymDouble(test) pow 2)
            assertEquals(SymDouble(test.pow(3)), SymDouble(test) pow 3)
            assertEquals(SymDouble(test.pow(-3)), SymDouble(test) pow -3)
            assertEquals(SymDouble(1), SymDouble(test) pow 0)
            assertEquals(SymDouble(test), SymDouble(test) pow 1)
            assertEquals(SymDouble(test).inverse(), SymDouble(test) pow -1)
        }

    }

}