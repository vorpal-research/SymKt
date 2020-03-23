package ru.spbstu

import ru.spbstu.wheels.joinTo
import kotlin.math.abs
import kotlin.reflect.KProperty

// represents a builder for the `sum(v * k)` or `product(k ^ v)` for each (k, v) in the map
// compacted not to store zero values
// this allow to do things like `builder[x] += 3` which is not possible for a simple map
inline class PartsBuilder<K>(val data: MutableMap<K, Rational> = mutableMapOf()) {
    operator fun get(key: K): Rational = data[key] ?: Rational.ZERO
    operator fun set(key: K, value: Rational) {
        if(value == Rational.ZERO) data.remove(key)
        else data[key] = value
    }
}

sealed class Symbolic {
    abstract fun simplify(): Symbolic
    abstract fun subst(substitution: Map<Var, Symbolic>): Symbolic
    abstract fun <C: MutableCollection<Var>> vars(mutableCollection: C): C
}
fun Symbolic.vars(): Set<Var> = vars(mutableSetOf())
fun Symbolic.subst(vararg mapping: Pair<Var, Symbolic>) = subst(mapOf(*mapping))

sealed class SumLike: Symbolic() {
    abstract fun asSum(): Sum
}
sealed class ProductLike: SumLike() {
    abstract fun asProd(): Product
}
sealed class AtomLike: ProductLike() {
    override fun asSum(): Sum = Sum(parts = mapOf(this to Rational.ONE))
    override fun asProd(): Product = Product(parts = mapOf(this to Rational.ONE))
    override fun simplify(): Symbolic = this
}
data class Const(val value: Rational): AtomLike() {
    constructor(value: Long): this(Rational(value))
    constructor(value: Int): this(Rational(value))

    override fun asSum(): Sum = Sum(constant = value)
    override fun asProd(): Product = Product(constant = value)

    override fun subst(substitution: Map<Var, Symbolic>) = this
    override fun <C : MutableCollection<Var>> vars(mutableCollection: C): C = mutableCollection

    override fun toString(): String = "$value"

    companion object {
        val ZERO = Const(Rational.ZERO)
        val ONE = Const(Rational.ONE)
    }
}
data class Var(val name: String): AtomLike() {
    override fun toString(): String = name

    override fun subst(substitution: Map<Var, Symbolic>) = substitution[this] ?: this
    override fun <C : MutableCollection<Var>> vars(mutableCollection: C): C =
        mutableCollection.apply { add(this@Var) }

    companion object {
        private var counter = 0
        fun fresh(prefix: String): Var {
            return Var(prefix + ++counter)
        }
        fun fresh(): Var = fresh("%")
    }
}
open class Apply(val function: String, val arguments: List<Symbolic>): AtomLike() {

    constructor(function: String, vararg arguments: Symbolic): this(function, arguments.asList())

    open fun copy(arguments: List<Symbolic>): Symbolic =
        Apply(function, arguments)

    override fun subst(substitution: Map<Var, Symbolic>) =
        copy(arguments = arguments.map { it.subst(substitution) })
    override fun <C : MutableCollection<Var>> vars(mutableCollection: C): C =
        mutableCollection.apply {
            arguments.forEach { it.vars(mutableCollection) }
        }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Apply -> false
        function != other.function -> false
        arguments != other.arguments -> false
        else -> true
    }

    override fun hashCode(): Int =
        31 * function.hashCode() + arguments.hashCode()
}

// a unified representation for constant + sum[(k, v) in parts](v * k)
data class Sum(val constant: Rational = Rational.ZERO,
               val parts: Map<ProductLike, Rational> = mapOf()
): SumLike() {
    override fun asSum(): Sum = this

    override fun simplify(): Symbolic = simplifyData(constant, parts)

    override fun subst(substitution: Map<Var, Symbolic>): Symbolic =
        of(Const(constant), *parts.mapToArray { c, r -> c.subst(substitution) * r })

    override fun <C : MutableCollection<Var>> vars(mutableCollection: C): C =
        mutableCollection.apply {
            parts.forEach { (k, _) -> k.vars(mutableCollection) }
        }

    internal fun timesImpl(factor: Symbolic): Symbolic = when(factor) {
        Const.ZERO -> Const.ZERO
        Const.ONE -> this
        is Const -> copy(
            constant = constant * factor.value,
            parts = parts.mapValues { (_, v) -> v * factor.value }
        )
        else -> of(factor * constant, *parts.mapToArray { ek, ev -> ek * factor * ev })
    }

    companion object {
        private fun simplifyData(constant: Rational, parts: Map<ProductLike, Rational>): Symbolic {
            if(parts.isEmpty())
                return Const(constant)
            if(parts.size == 1 && constant == Rational.ZERO) {
                val (p, c) = parts.entries.first()
                return p * c
            }
            // XXX: check if map contains zero values?
            return Sum(constant, parts)
        }

        fun of(vararg parts: Symbolic): Symbolic {
            if(parts.isEmpty()) return Const.ZERO
            if(parts.size == 1) return parts.first()

            var constantBuilder: Rational = Rational.ZERO
            val partsBuilder: PartsBuilder<ProductLike> = PartsBuilder()
            for(part in parts) {
                part as SumLike
                val sum = part.asSum()
                constantBuilder += sum.constant
                for ((k, v) in sum.parts) {
                    partsBuilder[k] += v
                }
            }
            return simplifyData(constantBuilder, partsBuilder.data)
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        if(constant != Rational.ZERO) builder.append("$constant + ")
        fun elementToString(p: ProductLike, c: Rational): String = when(c) {
            Rational.ONE -> "$p"
            else -> "$c * $p"
        }
        parts.joinTo(builder, separator = " + ", transform = ::elementToString)
        return "$builder"
    }
}

// a unified representation for constant * product[(k, v) in parts](k ^ v)
data class Product(val constant: Rational = Rational.ONE,
                   val parts: Map<AtomLike, Rational> = mapOf()
): ProductLike() {
    override fun asSum(): Sum = Sum(parts = mapOf(copy(constant = Rational.ONE) to constant))
    override fun simplify(): Symbolic = simplifyData(constant, parts)

    override fun asProd(): Product = this

    override fun subst(substitution: Map<Var, Symbolic>): Symbolic =
        of(Const(constant), *parts.mapToArray { c, r -> c.subst(substitution) pow r })
    override fun <C : MutableCollection<Var>> vars(mutableCollection: C): C =
        mutableCollection.apply {
            parts.forEach { (k, _) -> k.vars(mutableCollection) }
        }

    override fun toString(): String {
        val builder = StringBuilder()
        if(constant != Rational.ONE) builder.append("$constant * ")
        fun elementToString(p: AtomLike, c: Rational): String = when(c) {
            Rational.ONE -> "$p"
            else -> "($p^$c)"
        }
        parts.joinTo(builder, separator = " * ", transform = ::elementToString)
        return "$builder"
    }

    companion object {
        private fun simplifyData(constant: Rational, parts: Map<AtomLike, Rational>): Symbolic = when {
            constant == Rational.ZERO -> Const.ZERO
            parts.isEmpty() -> Const(constant)
            parts.size == 1 -> {
                val (p, c) = parts.entries.first()
                when {
                    c == Rational.ONE && constant == Rational.ONE -> p
                    c == Rational.ONE -> Sum(parts = mapOf(p to constant))
                    else -> Product(constant, parts)
                }
            }
            // XXX: check if map contains zero values?
            else -> Product(constant, parts)
        }

        fun of(vararg parts: Symbolic): Symbolic {
            if(parts.isEmpty()) return Const.ONE
            if(parts.size == 1) return parts.first()

            val sums: MutableList<Sum> = mutableListOf()
            val prods: MutableList<ProductLike> = mutableListOf()
            for(part in parts) when(part) {
                is Sum -> sums += part
                is ProductLike -> prods += part
            }.sealed()

            var constantBuilder: Rational = Rational.ONE
            val partsBuilder: PartsBuilder<AtomLike> = PartsBuilder()
            for(part in prods) {
                val prod = part.asProd()
                constantBuilder *= prod.constant
                for ((k, v) in prod.parts) {
                    partsBuilder[k] += v
                }
            }
            val totalProduct = simplifyData(constantBuilder, partsBuilder.data)

            var result: Symbolic = totalProduct
            for(sum in sums) {
                result = sum.timesImpl(result)
            }
            return result
        }
    }

}

operator fun Symbolic.plus(constant: Long): Symbolic =
    Sum.of(this, Const(constant))
operator fun Symbolic.plus(constant: Int): Symbolic =
    Sum.of(this, Const(constant))
operator fun Symbolic.plus(constant: Rational): Symbolic =
    Sum.of(this, Const(constant))
operator fun Symbolic.plus(other: Symbolic): Symbolic =
    Sum.of(this, other)
operator fun Symbolic.times(constant: Long): Symbolic =
    Product.of(this, Const(constant))
operator fun Symbolic.times(constant: Int): Symbolic =
    Product.of(this, Const(constant))
operator fun Symbolic.times(constant: Rational): Symbolic =
    Product.of(this, Const(constant))
operator fun Symbolic.times(other: Symbolic): Symbolic =
    Product.of(this, other)

operator fun Symbolic.unaryMinus(): Symbolic = this * -1

operator fun Symbolic.minus(other: Symbolic) = plus(-other)
operator fun Symbolic.minus(other: Rational) = plus(-other)
operator fun Symbolic.minus(other: Long) = plus(-other)
operator fun Symbolic.minus(other: Int) = plus(-other)

operator fun Symbolic.div(other: Symbolic) = times(other pow -1)
operator fun Symbolic.div(other: Rational) = times(other.inverse())
operator fun Symbolic.div(other: Long) = times(Rational(1, other))
operator fun Symbolic.div(other: Int) = times(Rational(1, other.toLong()))

data class Pow(val base: Symbolic, val power: Rational): Apply("pow", base, Const(power)) {
    override fun copy(arguments: List<Symbolic>): Symbolic {
        check(arguments.size == 2)
        val (base, power) = arguments
        check(power is Const)
        return of(base, power.value)
    }

    override fun toString(): String = "pow($base, $power)"

    companion object {
        fun of(base: Symbolic, power: Rational): Symbolic = base pow power
    }
}

infix fun Symbolic.pow(power: Long): Symbolic = when(power) {
    0L -> Const.ONE
    1L -> this
    else -> when(this) {
        is Const -> Const(value.pow(power))
        is Product -> {
            var constantBuilder = constant pow power
            val partsBuilder = PartsBuilder<AtomLike>()
            for((k, v) in parts) {
                val newPower = v * power
                if(k is Const && newPower.isWhole()) constantBuilder *= k.value pow newPower.wholePart
                else partsBuilder[k] = v * power
            }
            Product(
                constant = constantBuilder,
                parts = partsBuilder.data
            )
        }
        is AtomLike -> Product(parts = mapOf(this to Rational(power)))
        is Sum -> when (power) {
            in 0..100 -> {
                var res = this
                repeat(power.toInt() - 1) { res *= this }
                res
            }
            !in -100..100 -> Pow(this, Rational(power))
            else -> Pow(this, -Rational.ONE) pow abs(power)
        }
    }
}

infix fun Symbolic.pow(power: Rational): Symbolic = when {
    power.isWhole() -> pow(power.wholePart)
    else -> when(this) {
        is Product -> {
            val interm = PartsBuilder<AtomLike>(parts.mapValuesTo(mutableMapOf()) { (_, v) -> v * power })
            interm[Const(this.constant)] += power
            Product(parts = interm.data)
        }
        is AtomLike -> Product(parts = mapOf(this to power))
        is Sum -> {
            if(power.num == 1L || power.num == -1L) Pow(this, power)
            else Pow(this, power / abs(power.num)) pow abs(power.num)
        }
    }
}

object SymbolicScope {
    operator fun getValue(self: Any?, prop: KProperty<*>): Var = Var(prop.name)

    operator fun Rational.invoke() = Const(this)
    operator fun Long.invoke() = Const(Rational(this))
    operator fun Int.invoke() = Const(Rational(this.toLong()))
}
