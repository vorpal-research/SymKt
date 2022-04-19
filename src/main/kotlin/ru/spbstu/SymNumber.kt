package ru.spbstu

sealed class SymNumber : Number(), Comparable<SymNumber> {
    abstract operator fun compareTo(other: Long) : Int
    abstract operator fun compareTo(other: Int): Int
    abstract operator fun compareTo(other: Double): Int

    abstract operator fun times(other: SymNumber): SymNumber
    abstract operator fun times(other: Long): SymNumber
    abstract operator fun times(other: Double): SymNumber

    abstract operator fun plus(other: SymNumber): SymNumber
    abstract operator fun plus(other: Long): SymNumber
    abstract operator fun plus(other: Double): SymNumber

    abstract operator fun minus(other: SymNumber): SymNumber
    abstract operator fun minus(other: Long): SymNumber
    abstract operator fun minus(other: Double): SymNumber

    abstract operator fun div(other: SymNumber): SymNumber
    abstract operator fun div(other: Long): SymNumber
    abstract operator fun div(other: Double): SymNumber

    abstract operator fun unaryMinus(): SymNumber

    abstract fun inverse(): SymNumber
    abstract infix fun pow(power : Long) : SymNumber
    abstract fun isWhole(): Boolean
}