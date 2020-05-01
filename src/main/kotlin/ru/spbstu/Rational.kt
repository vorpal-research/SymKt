package ru.spbstu

import kotlin.math.abs
import kotlin.math.sign

class Rational(num: Long, den: Long = 1): Comparable<Rational>, Number() {
    val num: Long
    val den: Long

    init {
        require(den != 0L) { "Rational denominator may not be zero" }
        val gc = gcd(num, den)
        this.num = den.sign * num / gc
        this.den = abs(den) / gc
    }

    constructor(value: Int): this(value.toLong(), 1)

    val wholePart: Long get() = num / den
    val fractionalPart: Rational get() = Rational(num % den, den)

    override fun compareTo(other: Rational): Int =
        (this.num * other.den).compareTo(this.den * other.num)

    operator fun compareTo(other: Long): Int =
        this.num.compareTo(this.den * other)
    operator fun compareTo(other: Int): Int =
        this.num.compareTo(this.den * other)

    operator fun plus(other: Rational) =
        Rational(num * other.den + other.num * den, den * other.den)
    operator fun plus(other: Long) =
        plus(Rational(other))
    operator fun minus(other: Rational) =
        Rational(num * other.den - other.num * den, den * other.den)
    operator fun minus(other: Long) =
        minus(Rational(other))
    operator fun times(other: Rational) =
        Rational(num * other.num, den * other.den)
    operator fun times(other: Long) =
        Rational(num * other, den)
    operator fun div(other: Rational) =
        Rational(num * other.den, den * other.num)
    operator fun div(other: Long) =
        Rational(num, den * other)
    operator fun unaryMinus() =
        Rational(-num, den)

    fun inverse() = when {
        num < 0 -> Rational(-den, -num)
        else -> Rational(den, num)
    }

    infix fun pow(power: Long): Rational = when {
        power < 0 -> pow(abs(power)).inverse()
        else -> Rational(pow(num, power), pow(den, power))
    }

    fun isWhole() = den == 1L

    override fun toDouble() = num.toDouble() / den.toDouble()

    override fun equals(other: Any?): Boolean =
        this === other || other is Rational && num == other.num && den == other.den

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
    override fun toChar(): Char = wholePart.toChar()

    override fun toString(): String = when(den) {
        1L -> "$num"
        else -> "$num/$den"
    }

    companion object {
        val ZERO = Rational(0)
        val ONE = Rational(1)
        val HALF = Rational(1, 2)
    }
}

fun Long.toRational() = Rational(this)
fun Int.toRational() = Rational(this)
