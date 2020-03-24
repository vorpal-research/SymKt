package ru.spbstu

import kotlin.reflect.KProperty


object SymbolicScope {
    operator fun getValue(self: Any?, prop: KProperty<*>): Var = Var(prop.name)

    operator fun Rational.invoke() = Const(this)
    operator fun Long.invoke() = Const(Rational(this))
    operator fun Int.invoke() = Const(Rational(this.toLong()))

    operator fun Rational.plus(that: Symbolic): Symbolic = Const(this).plus(that)
    operator fun Long.plus(that: Symbolic): Symbolic = Const(this).plus(that)
    operator fun Int.plus(that: Symbolic): Symbolic = Const(this).plus(that)

    operator fun Rational.minus(that: Symbolic): Symbolic = Const(this).minus(that)
    operator fun Long.minus(that: Symbolic): Symbolic = Const(this).minus(that)
    operator fun Int.minus(that: Symbolic): Symbolic = Const(this).minus(that)

    operator fun Rational.times(that: Symbolic): Symbolic = Const(this).times(that)
    operator fun Long.times(that: Symbolic): Symbolic = Const(this).times(that)
    operator fun Int.times(that: Symbolic): Symbolic = Const(this).times(that)

    operator fun Rational.div(that: Symbolic): Symbolic = Const(this).div(that)
    operator fun Long.div(that: Symbolic): Symbolic = Const(this).div(that)
    operator fun Int.div(that: Symbolic): Symbolic = Const(this).div(that)
}
