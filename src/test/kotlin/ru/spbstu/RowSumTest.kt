package ru.spbstu

import kotlin.test.Test
import kotlin.test.assertEquals

class RowSumTest {
    @Test
    fun smokeTest() {
        val n by SymbolicScope
        val k by SymbolicScope
        assertEquals(
            Const.ZERO,
            RowSum.of(Const.ONE, n) { Const.ZERO }
        )
        assertEquals(
            n * 2,
            RowSum.of(Const.ONE, n) { i -> Const(2) }
        )
        // sum[i=1..n](i) = n^2 / 2 + n / 2
        assertEquals(
            (n pow 2) / 2 + n / 2,
            RowSum.of(Const.ONE, n) { i -> i }
        )
        assertEquals(
            (n pow 2) + n,
            RowSum.of(Const.ONE, n) { i -> i * 2 }
        )
        assertEquals(
            (n pow 2),
            RowSum.of(Const.ONE, n) { i -> i * 2 - 1 }
        )
        assertEquals(
            (n pow 2) * k + n * k,
            RowSum.of(Const.ONE, n) { i -> i * 2 * k }
        )

        assertEquals(
            ((n pow 2) + n) * (k pow 2 - 5),
            RowSum.of(Const.ONE, n) { i -> i * 2 * (k pow 2 - 5) }
        )

        assertEquals(
            k * 3,
            RowSum.of(n, n + 2) { k }
        )

        assertEquals(
            n * k + n - (n pow 2),
            RowSum.of(n, k) { n }
        )
    }

    val i by SymbolicScope

    fun crossCheck(functionOfI: Symbolic) {
        val v = Var.fresh()
        val sum = RowSum.of(i, Const.ONE, v, functionOfI)
        assertEquals(finiteDifference(sum, v), functionOfI.subst(i to v))
    }

    @Test
    fun crossCheckTest() {
        crossCheck(i)
        crossCheck(i + 2)
        crossCheck(i * i)
        val k by SymbolicScope
        crossCheck(k)
        crossCheck(k * i)
        crossCheck(k + i)
        crossCheck(k + i / 34)
        crossCheck((k * i * i + k * k * 12))
    }
}