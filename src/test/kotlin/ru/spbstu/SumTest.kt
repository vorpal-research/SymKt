package ru.spbstu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SumTest {
    @Test
    fun normalizationTest() {
        val x by SymbolicScope
        val y by SymbolicScope
        assertEquals(x + 2 + y, y + 2 + x)
        assertEquals(x + x, x * 2)
        assertEquals((x + y + 1) * 2, x * 2 + y * 2 + 2)
        assertEquals((x + 1) * (x + 1), x * x + x * 2 + 1)
        assertEquals(x, x + 1 - 1)
        assertTrue { (x + 1 - 1) is Var }

    }
}