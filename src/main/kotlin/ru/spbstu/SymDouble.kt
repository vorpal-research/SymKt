package ru.spbstu

import kotlin.math.pow
import kotlin.math.floor

class SymDouble(private val num: Double) : SymNumber() {
    constructor(rational: SymRational) : this(rational.toDouble())
    constructor(long: Long) : this(long.toDouble())
    constructor(int: Int) : this(int.toDouble())

    override fun compareTo(other: SymNumber): Int = when (other) {
        is SymRational -> num.compareTo(other.toDouble())
        is SymDouble -> num.compareTo(other.num)
    }

    override operator fun compareTo(other: Long): Int =
        toLong().compareTo(other)

    override operator fun compareTo(other: Int): Int =
        toInt().compareTo(other)

    override operator fun compareTo(other: Double): Int =
        toDouble().compareTo(other)

    override operator fun plus(other: SymNumber) = when (other) {
        is SymRational -> SymDouble(this.num + other.toDouble())
        is SymDouble -> SymDouble(this.num + other.num)
    }

    override operator fun plus(other: Long) =
        SymDouble(this.num + other)

    override operator fun plus(other: Double) =
        SymDouble(this.num + other)


    override operator fun minus(other: SymNumber) = when (other) {
        is SymRational -> SymDouble(this.num - other.toDouble())
        is SymDouble -> SymDouble(this.num - other.num)
    }

    override operator fun minus(other: Long) =
        SymDouble(this.num - other)

    override operator fun minus(other: Double) =
        SymDouble(this.num - other)

    override operator fun times(other: SymNumber) = when (other) {
        is SymRational -> SymDouble(this.num * other.toDouble())
        is SymDouble -> SymDouble(this.num * other.num)
    }

    override operator fun times(other: Long) =
        SymDouble(this.num * other)

    override operator fun times(other: Double) =
        SymDouble(this.num * other)

    override operator fun div(other: SymNumber) = when (other) {
        is SymRational -> SymDouble(this.num / other.toDouble())
        is SymDouble -> SymDouble(this.num / other.num)
    }

    override operator fun div(other: Long) =
        SymDouble(this.num / other)

    override operator fun div(other: Double) =
        SymDouble(this.num / other)

    override operator fun unaryMinus() =
        SymDouble(-num)

    override fun inverse() = SymDouble(1.0 / this.num)

    override fun isWhole() = false

    override infix fun pow(power: Long): SymDouble =
        SymDouble(this.num.pow(power.toDouble()))

    override fun toByte(): Byte = toInt().toByte()
    override fun toChar(): Char = toInt().toChar()
    override fun toDouble(): Double = num
    override fun toFloat(): Float = num.toFloat()
    override fun toInt(): Int = num.toInt()
    override fun toLong(): Long = num.toLong()
    override fun toShort(): Short = toInt().toShort()

    override fun equals(other: Any?): Boolean =
        this === other || other is SymDouble && num == other.num

}

fun Double.toSymDouble() = SymDouble(this)
fun Long.toSymDouble() = SymDouble(this)
fun Int.toSymDouble() = SymDouble(this)