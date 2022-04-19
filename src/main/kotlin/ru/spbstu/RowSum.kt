package ru.spbstu

import kotlin.math.abs
import ru.spbstu.wheels.mapToArray

data class RowSum(
    val index: Var,
    val lowerBound: Symbolic,
    val upperBound: Symbolic,
    val body: Symbolic
) : Apply("RowSum", index, lowerBound, upperBound, body) {

    override fun copy(arguments: List<Symbolic>): Symbolic {
        check(arguments.size == 4)
        val (index, lowerBound, upperBound, body) = arguments
        check(index == this.index)
        return of(index as Var, lowerBound, upperBound, body)
    }

    override fun simplify(): Symbolic = of(index, lowerBound, upperBound, body)

    companion object {
        private fun powRowSum(index: Var,
                              lowerBound: Symbolic,
                              upperBound: Symbolic,
                              base: Symbolic,
                              power: Long): Symbolic? {
            return when {
                power >= 0 -> {
                    (bernoulli(power + 1, upperBound + 1) - bernoulli(power + 1, lowerBound)) / (power + 1)
                }
                lowerBound is Const && upperBound is Const
                        && lowerBound.value > 1 && upperBound.value >= 0 -> {
                    val upper = upperBound.value.toLong()
                    val lower = lowerBound.value.toLong()
                    if(power == -1L) Const(harmonic(upper) - harmonic(lower - 1))
                    else Const(harmonic(upper, abs(power)) - harmonic(lower - 1, abs(power)))
                }
                else -> null
            }
        }
        private fun symbolicRowSum(index: Var,
                                   lowerBound: Symbolic,
                                   upperBound: Symbolic,
                                   range: Symbolic, /* = upperBound - lowerBound + 1 */
                                  body: Symbolic): Symbolic? {
            if(body is Sum) {
                return Sum.of(
                    Const(body.constant) * range,
                    *body.parts.mapToArray { (s, c) ->
                        val rs = symbolicRowSum(index, lowerBound, upperBound, range, s)
                            ?: RowSum(index, lowerBound, upperBound, body)
                        Const(c) * rs
                    }
                )
            }

            if(body == index) return powRowSum(index, lowerBound, upperBound, body, 1)

            if(!body.containsVariable(index)) return body * range

            if(body is Product) {
                if(body.parts.size == 1) {
                    val (base, ratPower) = body.parts.entries.single()
                    if(!ratPower.isWhole()) return null
                    return powRowSum(index, lowerBound, upperBound, base, ratPower.toLong())?.times(body.constant)
                }

                val (lhv, rhv) = body.parts.partitionTo(mutableMapOf(), mutableMapOf()) { s, _ -> !s.containsVariable(index) }
                if(lhv.isEmpty()) return null

                val left = body.copy(parts = lhv).simplify()
                if(left == Const.ONE) return null

                if(rhv.isEmpty()) return left * range

                val right = Product(parts = rhv).simplify()
                return left * (symbolicRowSum(index, lowerBound, upperBound, range, right)
                    ?: RowSum(index, lowerBound, upperBound, right))
            }
            return null
        }

        fun of(index: Var, lowerBound: Symbolic, upperBound: Symbolic, body: Symbolic): Symbolic {
            if(body == Const.ZERO) return Const.ZERO

            val range = upperBound - lowerBound + 1

            if(range is Const && range.value < SymRational(100)) {
                require(range.value.isWhole() && range.value >= SymRational.ZERO)
                return Sum.of(*(0 until range.value.toLong()).toList().mapToArray {
                    body.subst(mapOf(index to lowerBound + it))
                })
            }

            return symbolicRowSum(index, lowerBound, upperBound, range, body)
                ?: RowSum(index, lowerBound, upperBound, body)
        }

        fun of(lowerBound: Symbolic, upperBound: Symbolic, body: (Var) -> Symbolic) =
            Var.fresh().let { of(it, lowerBound, upperBound, body(it)) }
    }
}

fun finiteDifference(body: Symbolic, variable: Var): Symbolic =
    body - body.subst(mapOf(variable to (variable - 1)))

