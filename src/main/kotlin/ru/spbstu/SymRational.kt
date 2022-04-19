package ru.spbstu

import kotlin.math.abs
import kotlin.math.sign

class SymRational(num: Long, den: Long = 1) : SymNumber() {
    val num: Long
    val den: Long

    init {
        require(den != 0L) { "Rational denominator may not be zero" }
        val gc = gcd(num, den)
        this.num = den.sign * num / gc
        this.den = abs(den) / gc
    }

    constructor(value: Int) : this(value.toLong(), 1)

    val wholePart: Long get() = num / den
    val fractionalPart: SymRational get() = SymRational(num % den, den)

    override fun compareTo(other: SymNumber): Int = when (other) {
        is SymRational -> (this.num * other.den).compareTo(this.den * other.num)
        is SymDouble -> this.toDouble().compareTo(other.toDouble())
    }

    override operator fun compareTo(other: Long): Int =
        this.num.compareTo(this.den * other)

    override operator fun compareTo(other: Int): Int =
        this.num.compareTo(this.den * other)

    override operator fun compareTo(other: Double): Int =
        this.toDouble().compareTo(other)

    override operator fun plus(other: SymNumber) = when (other) {
        is SymRational -> SymRational(num * other.den + other.num * den, den * other.den)
        is SymDouble -> SymDouble(this).plus(other)
    }

    override operator fun plus(other: Long) =
        plus(SymRational(other))

    override operator fun plus(other: Double) =
        plus(SymDouble(other))

    override operator fun minus(other: SymNumber) = when (other) {
        is SymRational -> SymRational(num * other.den - other.num * den, den * other.den)
        is SymDouble -> SymDouble(this).minus(other)
    }

    override operator fun minus(other: Long) =
        minus(SymRational(other))

    override operator fun minus(other: Double) =
        minus(SymDouble(other))

    override operator fun times(other: SymNumber) = when (other) {
        is SymRational -> SymRational(num * other.num, den * other.den)
        is SymDouble -> SymDouble(this).times(other)
    }

    override operator fun times(other: Long) =
        SymRational(num * other, den)

    override operator fun times(other: Double) =
        SymDouble(this).times(other)

    override operator fun div(other: SymNumber) = when (other) {
        is SymRational -> SymRational(num * other.den, den * other.num)
        is SymDouble -> SymDouble(this).div(other)
    }

    override operator fun div(other: Long) =
        SymRational(num, den * other)

    override operator fun div(other: Double) =
        SymDouble(this).div(other)

    override operator fun unaryMinus() =
        SymRational(-num, den)

    override fun inverse() = when {
        num < 0 -> SymRational(-den, -num)
        else -> SymRational(den, num)
    }

    override infix fun pow(power: Long): SymRational = when {
        power < 0 -> pow(abs(power)).inverse()
        else -> SymRational(pow(num, power), pow(den, power))
    }

    override fun isWhole() = den == 1L

    override fun toDouble() = num.toDouble() / den.toDouble()

    override fun equals(other: Any?): Boolean =
        this === other || other is SymRational && num == other.num && den == other.den

    override fun hashCode(): Int {
        var result = num.hashCode()
        result = 31 * result + den.hashCode()
        return result
    }

    override fun toFloat(): Float = toDouble().toFloat()
    override fun toInt(): Int = wholePart.toInt()
    override fun toLong(): Long = wholePart
    override fun toShort(): Short = wholePart.toShort()
    override fun toByte(): Byte = wholePart.toByte()
    override fun toChar(): Char = toInt().toChar()

    override fun toString(): String = when (den) {
        1L -> "$num"
        else -> "$num/$den"
    }

    companion object {
        val ZERO = SymRational(0)
        val ONE = SymRational(1)
        val HALF = SymRational(1, 2)
    }
}

fun Long.toRational() = SymRational(this)
fun Int.toRational() = SymRational(this)
