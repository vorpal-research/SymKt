package ru.spbstu

import kotlin.reflect.KProperty


object SymbolicScope {
    operator fun getValue(self: Any?, prop: KProperty<*>): Var = Var(prop.name)

    operator fun SymRational.invoke() = Const(this)
    operator fun SymDouble.invoke() = Const(this)
    operator fun Long.invoke() = Const(SymRational(this))
    operator fun Int.invoke() = Const(SymRational(this.toLong()))

    operator fun SymRational.plus(that: Symbolic): Symbolic = Const(this).plus(that)
    operator fun Long.plus(that: Symbolic): Symbolic = Const(this).plus(that)
    operator fun Int.plus(that: Symbolic): Symbolic = Const(this).plus(that)

    operator fun SymRational.minus(that: Symbolic): Symbolic = Const(this).minus(that)
    operator fun Long.minus(that: Symbolic): Symbolic = Const(this).minus(that)
    operator fun Int.minus(that: Symbolic): Symbolic = Const(this).minus(that)

    operator fun SymRational.times(that: Symbolic): Symbolic = Const(this).times(that)
    operator fun Long.times(that: Symbolic): Symbolic = Const(this).times(that)
    operator fun Int.times(that: Symbolic): Symbolic = Const(this).times(that)

    operator fun SymRational.div(that: Symbolic): Symbolic = Const(this).div(that)
    operator fun Long.div(that: Symbolic): Symbolic = Const(this).div(that)
    operator fun Int.div(that: Symbolic): Symbolic = Const(this).div(that)
}
