package ru.spbstu

import kotlin.math.abs
import ru.spbstu.wheels.mapToArray

data class RowProduct(
    val index: Var,
    val lowerBound: Symbolic,
    val upperBound: Symbolic,
    val body: Symbolic
) : Apply("RowProduct", index, lowerBound, upperBound, body) {

    override fun copy(arguments: List<Symbolic>): Symbolic {
        check(arguments.size == 4)
        val (index, lowerBound, upperBound, body) = arguments
        check(index == this.index)
        return of(index as Var, lowerBound, upperBound, body)
    }

    override fun simplify(): Symbolic = of(index, lowerBound, upperBound, body)

    companion object {
        fun symbolicRowProduct(index: Var, lowerBound: Symbolic, upperBound: Symbolic, body: Symbolic, range: Symbolic): Symbolic? {
            if(!body.containsVariable(index) && range is Const) return Pow.of(body, range.value)

            if(range is Const && body is Product) {
                val mapped = body.parts.mapToArray { (s, c) ->
                    val sym = symbolicRowProduct(index, lowerBound, upperBound, s, range) ?: return null
                    sym pow c
                }

                Product.of(Const(body.constant) pow range.value, *mapped)
            }

            // TODO: think about allowing "pow to symbolic"
            if(range is Const && range.value < Rational(100)) {
                require(range.value.isWhole() && range.value >= Rational.ZERO)
                return Product.of(*(0 until range.value.wholePart).toList().mapToArray {
                    body.subst(mapOf(index to lowerBound + it))
                })
            }

            return null
        }

        fun of(index: Var, lowerBound: Symbolic, upperBound: Symbolic, body: Symbolic): Symbolic {
            if(body == Const.ZERO) return Const.ZERO
            if(body == Const.ONE) return Const.ONE

            val range = upperBound - lowerBound + 1

            return symbolicRowProduct(index, lowerBound, upperBound, body, range)
                ?: RowProduct(index, lowerBound, upperBound, body)
        }

        fun of(lowerBound: Symbolic, upperBound: Symbolic, body: (Var) -> Symbolic) =
            Var.fresh().let { of(it, lowerBound, upperBound, body(it)) }
    }
}
