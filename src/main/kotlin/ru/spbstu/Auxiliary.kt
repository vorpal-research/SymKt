package ru.spbstu

import ru.spbstu.wheels.mapToArray

private val bernoulliCache = mutableMapOf(
    0L to SymRational.ONE,
    2L to SymRational(1, 6),
    4L to SymRational(-1, 30)
)
private val bernoulliHighest = mutableMapOf(
    0L to 0L, 2L to 2L, 4L to 4L
)

fun bernoulli(n: Long, sym: Symbolic? = null): Symbolic {
    require(n >= 0)
    return when (n) {
        0L -> Const.ONE
        1L -> when (sym) {
            null -> -Const(SymRational.HALF)
            else -> sym - Const(SymRational.HALF)
        }
        else -> when (sym) {
            null -> {
                if (n % 2 == 1L) Const.ZERO
                else {
                    val case = n % 6
                    val highestCached = bernoulliHighest[case] ?: 0
                    if (n <= highestCached) return Const(bernoulliCache[n] ?: SymRational.ZERO)

                    var b: Const = Const.ZERO
                    for (i in (highestCached + 6) until (n + 6) step 6) {
                        b = bernoulli(i) as Const
                        bernoulliCache[i] = b.value as SymRational
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

private data class HarmonicFunction(val m: Long, val cache: MutableList<SymRational> = mutableListOf(SymRational.ZERO)): (Long) -> SymRational {
    override fun invoke(n: Long): SymRational {
        while(cache.size <= n) {
            cache += (cache.last() + SymRational(1, pow(n, m))) as SymRational
        }
        return cache[n.toInt()]
    }

}
private val harmonicCache = mutableMapOf<Long, (Long) -> SymRational>()

fun harmonic(n: Long, mOrNull: Long? = null): SymRational {
    require(n >= 0)

    val m = mOrNull ?: 1

    if(m == 0L) return SymRational(n)
    if(n == 0L) return SymRational.ZERO

    val hf = harmonicCache.getOrPut(m){ HarmonicFunction(m) }
    return hf(n)
}
