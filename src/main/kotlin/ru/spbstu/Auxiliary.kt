package ru.spbstu

private val bernoulliCache = mutableMapOf(
    0L to Rational.ONE,
    2L to Rational(1, 6),
    4L to Rational(-1, 30)
)
private val bernoulliHighest = mutableMapOf(
    0L to 0L, 2L to 2L, 4L to 4L
)

fun bernoulli(n: Long, sym: Symbolic? = null): Symbolic {
    require(n >= 0)
    return when (n) {
        0L -> Const.ONE
        1L -> when (sym) {
            null -> -Const(Rational.HALF)
            else -> sym - Const(Rational.HALF)
        }
        else -> when (sym) {
            null -> {
                if (n % 2 == 1L) Const.ZERO
                else {
                    val case = n % 6
                    val highestCached = bernoulliHighest[case] ?: 0
                    if (n <= highestCached) return Const(bernoulliCache[n] ?: Rational.ZERO)

                    var b: Const = Const.ZERO
                    for (i in (highestCached + 6) until (n + 6) step 6) {
                        b = bernoulli(i) as Const
                        bernoulliCache[i] = b.value
                        bernoulliHighest[case] = i
                    }
                    b
                }
            }
            else -> {
                Sum.of(*(0..n).toList().mapToArray { k ->
                    val binom = binomial(n, k)
                    val recur = bernoulli(k) ?: Const.ONE
                    Product.of(Const(binom), recur, sym pow (n - k))
                })
            }
        }
    }
}

private data class HarmonicFunction(val m: Long, val cache: MutableList<Rational> = mutableListOf(Rational.ZERO)): (Long) -> Rational {
    override fun invoke(n: Long): Rational {
        while(cache.size <= n) {
            cache += cache.last() + Rational(1, pow(n, m))
        }
        return cache[n.toInt()]
    }

}
private val harmonicCache = mutableMapOf<Long, (Long) -> Rational>()

fun harmonic(n: Long, mOrNull: Long? = null): Rational {
    require(n >= 0)

    val m = mOrNull ?: 1

    if(m == 0L) return Rational(n)
    if(n == 0L) return Rational.ZERO

    val hf = harmonicCache.getOrPut(m){ HarmonicFunction(m) }
    return hf(n)
}
