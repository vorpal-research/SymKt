package ru.spbstu

import kotlin.test.Test
import kotlin.test.assertEquals

class ArithmeticsTest {

    @Test
    fun gcdTest() {
        assertEquals(1, gcd(1, 1))
        assertEquals(3, gcd(12, 9))
        assertEquals(3, gcd(9, 12))
        assertEquals(7, gcd(49, 7))

    }

    @Test
    fun binomialTest() {
        val triangle = mutableListOf<MutableList<Long>>()
        for (n in 0L..9L) {
            val row = mutableListOf<Long>()
            for (k in 0L..n)
                row += binomial(n, k)
            triangle += row
        }

        assertEquals(
            listOf<List<Long>>(
                listOf(1),
                listOf(1, 1),
                listOf(1, 2, 1),
                listOf(1, 3, 3, 1),
                listOf(1, 4, 6, 4, 1),
                listOf(1, 5, 10, 10, 5, 1),
                listOf(1, 6, 15, 20, 15, 6, 1),
                listOf(1, 7, 21, 35, 35, 21, 7, 1),
                listOf(1, 8, 28, 56, 70, 56, 28, 8, 1),
                listOf(1, 9, 36, 84, 126, 126, 84, 36, 9, 1)
            ), triangle
        )
    }

}