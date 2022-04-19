package ru.spbstu

import ru.spbstu.wheels.joinTo
import ru.spbstu.wheels.mapToArray
import kotlin.math.abs

// represents a builder for the `sum(v * k)` or `product(k ^ v)` for each (k, v) in the map
// compacted not to store zero values
// this allow to do things like `builder[x] += 3` which is not possible for a simple map
inline class PartsBuilder<K>(val data: MutableMap<K, SymNumber> = mutableMapOf()) {
    operator fun get(key: K): SymNumber = data[key] ?: SymRational.ZERO
    operator fun set(key: K, value: SymNumber) {
        if(value == SymRational.ZERO) data.remove(key)
        else data[key] = value
    }
}

sealed class Symbolic {
    abstract fun simplify(): Symbolic
    abstract fun subst(substitution: Map<Var, Symbolic>): Symbolic
    abstract fun containsVariable(variable: Var): Boolean
    abstract fun <C: MutableCollection<Var>> vars(mutableCollection: C): C
}
fun Symbolic.vars(): Set<Var> = vars(mutableSetOf())
fun Symbolic.subst(vararg mapping: Pair<Var, Symbolic>) = subst(mapOf(*mapping))

sealed class SumLike: Symbolic() {
    abstract fun asSum(): Sum
}
sealed class ProductLike: SumLike() {
    abstract fun asProduct(): Product
}
sealed class AtomLike: ProductLike() {
    override fun asSum(): Sum = Sum(parts = mapOf(this to SymRational.ONE))
    override fun asProduct(): Product = Product(parts = mapOf(this to SymRational.ONE))
    override fun simplify(): Symbolic = this
}

data class Const(val value: SymNumber): AtomLike() {
    constructor(value: Long): this(SymRational(value))
    constructor(value: Int): this(SymRational(value))
    constructor(value: Double): this(SymDouble(value))

    override fun asSum(): Sum = Sum(constant = value)
    override fun asProduct(): Product = Product(constant = value)

    override fun subst(substitution: Map<Var, Symbolic>) = this
    override fun containsVariable(variable: Var): Boolean = false

    override fun <C : MutableCollection<Var>> vars(mutableCollection: C): C = mutableCollection

    override fun toString(): String = "$value"

    companion object {
        val ZERO = Const(SymRational.ZERO)
        val ONE = Const(SymRational.ONE)
        val MINUS_ONE = Const(-SymRational.ONE)
    }
}
data class Var(val name: String): AtomLike() {
    override fun toString(): String = name

    override fun subst(substitution: Map<Var, Symbolic>) = substitution[this] ?: this
    override fun containsVariable(variable: Var): Boolean = this.name == variable.name

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
        copy(arguments = listOf(arguments.first()) + ( arguments.drop(1).map { it.subst(substitution) }))

    override fun containsVariable(variable: Var): Boolean = arguments.any { it.containsVariable(variable) }

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

    override fun toString(): String = "$function(${arguments.joinToString()})"
}

// a unified representation for constant + sum[(k, v) in parts](v * k)
data class Sum(val constant: SymNumber = SymRational.ZERO,
               val parts: Map<ProductLike, SymNumber> = mapOf()
): SumLike() {
    override fun asSum(): Sum = this

    override fun simplify(): Symbolic = simplifyData(constant, parts)

    override fun subst(substitution: Map<Var, Symbolic>): Symbolic =
        of(Const(constant), *parts.mapToArray { (c, r) -> c.subst(substitution) * r })

    override fun containsVariable(variable: Var): Boolean =
        variable in parts || parts.keys.any { it.containsVariable(variable) }

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
        else -> of(factor * constant, *parts.mapToArray { (ek, ev) -> ek * factor * ev })
    }

    companion object {
        private fun simplifyData(constant: SymNumber, parts: Map<ProductLike, SymNumber
                >): Symbolic {
            if(parts.isEmpty())
                return Const(constant)
            if(parts.size == 1 && constant == SymRational.ZERO) {
                val (p, c) = parts.entries.first()
                return p * c
            }
            // XXX: check if map contains zero values?
            return Sum(constant, parts)
        }

        fun of(vararg parts: Symbolic): Symbolic {
            if(parts.isEmpty()) return Const.ZERO
            if(parts.size == 1) return parts.first()

            var constantBuilder: SymNumber = SymRational.ZERO
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
        if(constant != SymRational.ZERO) builder.append("$constant + ")
        fun elementToString(p: ProductLike, c: SymNumber): String = when(c) {
            SymRational.ONE -> "$p"
            else -> "$c * $p"
        }
        parts.joinTo(builder, separator = " + ", transform = ::elementToString)
        return "$builder"
    }
}

// a unified representation for constant * product[(k, v) in parts](k ^ v)
data class Product(val constant: SymNumber = SymRational.ONE,
                   val parts: Map<AtomLike, SymNumber> = mapOf()
): ProductLike() {
    override fun asSum(): Sum = Sum(parts = mapOf(copy(constant = SymRational.ONE) to constant))
    override fun simplify(): Symbolic = simplifyData(constant, parts)

    override fun asProduct(): Product = this

    override fun subst(substitution: Map<Var, Symbolic>): Symbolic =
        of(Const(constant), *parts.mapToArray { (c, r) -> c.subst(substitution) pow r })

    override fun containsVariable(variable: Var): Boolean =
        variable in parts || parts.keys.any { it.containsVariable(variable) }

    override fun <C : MutableCollection<Var>> vars(mutableCollection: C): C =
        mutableCollection.apply {
            parts.forEach { (k, _) -> k.vars(mutableCollection) }
        }

    override fun toString(): String {
        val builder = StringBuilder()
        if(constant != SymRational.ONE) builder.append("$constant * ")
        fun elementToString(p: AtomLike, c: SymNumber): String = when(c) {
            SymRational.ONE -> "$p"
            else -> "($p^$c)"
        }
        parts.joinTo(builder, separator = " * ", transform = ::elementToString)
        return "$builder"
    }

    companion object {
        private fun simplifyData(constant: SymNumber, parts: Map<AtomLike, SymNumber>): Symbolic = when {
            constant == SymRational.ZERO -> Const.ZERO
            parts.isEmpty() -> Const(constant)
            parts.size == 1 -> {
                val (p, c) = parts.entries.first()
                when {
                    c == SymRational.ONE && constant == SymRational.ONE -> p
                    c == SymRational.ONE -> Sum(parts = mapOf(p to constant))
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

            var constantBuilder: SymNumber = SymRational.ONE
            val partsBuilder: PartsBuilder<AtomLike> = PartsBuilder()
            for(part in prods) {
                val prod = part.asProduct()
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
operator fun Symbolic.plus(constant: SymNumber): Symbolic =
    Sum.of(this, Const(constant))
operator fun Symbolic.plus(other: Symbolic): Symbolic =
    Sum.of(this, other)
operator fun Symbolic.times(constant: Long): Symbolic =
    Product.of(this, Const(constant))
operator fun Symbolic.times(constant: Int): Symbolic =
    Product.of(this, Const(constant))
operator fun Symbolic.times(constant: SymNumber): Symbolic =
    Product.of(this, Const(constant))
operator fun Symbolic.times(other: Symbolic): Symbolic =
    Product.of(this, other)

operator fun Symbolic.unaryMinus(): Symbolic = this * -1

operator fun Symbolic.minus(other: Symbolic) = plus(-other)
operator fun Symbolic.minus(other: SymNumber) = plus(-other)
operator fun Symbolic.minus(other: Long) = plus(-other)
operator fun Symbolic.minus(other: Int) = plus(-other)

operator fun Symbolic.div(other: Symbolic) = times(other pow -1)
operator fun Symbolic.div(other: SymNumber) = times(other.inverse())
operator fun Symbolic.div(other: Long) = times(SymRational(1, other))
operator fun Symbolic.div(other: Int) = times(SymRational(1, other.toLong()))

fun Symbolic.reciprocal() : Symbolic {
    return Const(1) / this
}

open class Shift(base: Symbolic, shift: Symbolic, val type : Char) : Apply("sh$type", base, shift) {
    override fun copy(arguments: List<Symbolic>): Symbolic {
        check(arguments.size == 2)
        val (base, shift) = arguments
        return ShiftLeft.of(base, shift)
    }
}

data class ShiftLeft(val base : Symbolic, val shift : Symbolic): Shift(base, shift, 'l') {
    override fun copy(arguments: List<Symbolic>): Symbolic {
        check(arguments.size == 2)
        val (base, shift) = arguments
        return of(base, shift)
    }

    override fun subst(substitution: Map<Var, Symbolic>): Symbolic = of(base.subst(substitution), shift.subst(substitution))

    override fun toString(): String = "shl($base, $shift)"

    companion object{
        fun of(base: Symbolic, shift: Symbolic) : Symbolic = base shl shift
    }
}

infix fun Symbolic.shl(shift : Long) : Symbolic  {
    if (shift == 0L) return this
    return when (this) {
        is ShiftLeft -> ShiftLeft.of(base, this.shift + shift)
        is Const -> when (this.value) {
            is SymRational -> if (value.isWhole()) Const(value.num shl shift.toInt()) else ShiftLeft(this, Const(shift))
            is SymDouble -> ShiftLeft(this, Const(shift))
        }
        else -> ShiftLeft(this, Const(shift))
    }
}

infix fun Symbolic.shl(shift : SymNumber) : Symbolic {
    if (shift is SymRational && shift.isWhole()) return shl(shift.num)
    if (this is ShiftLeft) return ShiftLeft.of(base, this.shift + shift)
    return ShiftLeft(this, Const(shift))
}

infix fun Symbolic.shl(shift : Symbolic) : Symbolic {
    if (shift is Const) return shl(shift.value)
    if (this is ShiftLeft) return ShiftLeft.of(base, this.shift + shift)
    return ShiftLeft(this, shift)
}

data class ShiftRight(val base : Symbolic, val shift : Symbolic): Shift(base, shift, 'r') {

    override fun copy(arguments: List<Symbolic>): Symbolic {
        check(arguments.size == 2)
        val (base, shift) = arguments
        return of(base, shift)
    }

    override fun toString(): String = "shr($base, $shift)"

    companion object{
        fun of(base: Symbolic, shift: Symbolic) : Symbolic = base shr shift
    }
}

infix fun Symbolic.shr(shift : Long) : Symbolic  {
    if (shift == 0L) return this
    return when (this) {
        is ShiftRight -> ShiftRight.of(base, this.shift + shift)
        is Const -> when (this.value) {
            is SymRational -> if (value.isWhole()) Const(value.num shr shift.toInt()) else ShiftRight(this, Const(shift))
            is SymDouble -> ShiftRight(this, Const(shift))
        }
        else -> ShiftRight(this, Const(shift))
    }
}

infix fun Symbolic.shr(shift : SymNumber) : Symbolic {
    if (shift is SymRational && shift.isWhole()) return shr(shift.num)
    if (this is ShiftRight) return ShiftRight.of(base, this.shift + shift)
    return ShiftRight(this, Const(shift))
}

infix fun Symbolic.shr(shift : Symbolic) : Symbolic {
    if (shift is Const) return shr(shift.value)
    if (this is ShiftRight) return ShiftRight.of(base, this.shift + shift)
    return ShiftRight(this, shift)
}

data class Pow(val base: Symbolic, val power: Symbolic): Apply("pow", base, power) {
    override fun copy(arguments: List<Symbolic>): Symbolic {
        check(arguments.size == 2)
        val (base, power) = arguments
        return of(base, power)
    }

    override fun toString(): String = "pow($base, $power)"

    companion object {
        fun of(base: Symbolic, power: Symbolic): Symbolic = base pow power
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
                if(k is Const && newPower is SymRational && newPower.isWhole()) constantBuilder *= k.value pow newPower.wholePart
                else partsBuilder[k] = v * power
            }
            Product(
                constant = constantBuilder,
                parts = partsBuilder.data
            )
        }
        is AtomLike -> Product(parts = mapOf(this to SymRational(power)))
        is Sum -> when (power) {
            in 0..100 -> {
                var res = this
                repeat(power.toInt() - 1) { res *= this }
                res
            }
            !in -100..100 -> Pow(this, Const(power))
            else -> Pow(this, -Const.ONE) pow abs(power)
        }
    }
}

infix fun Symbolic.pow(power: SymNumber): Symbolic = when {
    power is SymRational && power.isWhole() -> pow(power.wholePart)
    else -> when(this) {
        is Product -> {
            val interm = PartsBuilder<AtomLike>(parts.mapValuesTo(mutableMapOf()) { (_, v) -> v * power })
            interm[Const(this.constant)] += power
            Product(parts = interm.data)
        }
        is AtomLike -> Product(parts = mapOf(this to power))
        is Sum -> if (power is SymRational) {
            if(power.num == 1L || power.num == -1L) Pow(this, Const(power))
            else Pow(this, Const(power / abs(power.num))) pow abs(power.num)
        } else {
            Pow(this, Const(power))
        }
    }
}

infix fun Symbolic.pow(power: Symbolic): Symbolic = when {
    this == Const.ZERO && power == Const.ZERO -> throw ArithmeticException("Zero to the power of zero")
    power is Const -> pow(power.value)
    this == Const.ZERO -> Const.ZERO
    this == Const.ONE -> Const.ONE
    power is Sum -> {
        Product.of(this pow power.constant, *power.parts.mapToArray { (x, m) ->
            this pow x pow m
        })
    }
/* any other options? */
    else -> Pow(this, power)
}
