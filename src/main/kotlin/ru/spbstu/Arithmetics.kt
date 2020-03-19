package ru.spbstu

import kotlin.math.abs

fun gcd(a: Long, b: Long): Long {
    val a = abs(a)
    val b = abs(b)
    return tailGCD(maxOf(a, b), minOf(a, b))
}

private tailrec fun tailGCD(a: Long, b: Long): Long =
    if(b == 0L) a else tailGCD(b, a % b)

fun pow(a: Long, b: Long): Long = when {
    b == 0L -> 1L
    b == 1L -> a
    b % 2 == 1L -> pow(a, b - 1L) * a
    else -> {
        val sqrt = pow(a, b / 2)
        sqrt * sqrt
    }
}

private val binomialCache = mutableMapOf<Long, MutableMap<Long, Long>>()

fun binomial(n: Long, k: Long) = binomialCache[k]?.get(n)
    ?: binomialUncached(n, k).also {
        binomialCache.getOrPut(k) { mutableMapOf() }[n] = it
    }

private fun binomialUncached(n: Long, k: Long): Long {
    require(k <= n)
    if (n == k || k == 0L) {
        return 1
    }
    if (k == 1L || k == n - 1) {
        return n
    }
    // Use symmetry for large k
    if (k > n / 2) {
        return binomial(n, n - k)
    }
    // (n choose k) == (n-1 choose k-1) * n / k
    return (Rational(binomial(n - 1, k - 1), k) * n).wholePart
}
