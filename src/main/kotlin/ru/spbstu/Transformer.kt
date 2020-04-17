package ru.spbstu

import ru.spbstu.wheels.mapToArray

class TransformScope(val body: TransformScope.(Symbolic) -> Symbolic) {
    fun Symbolic.transformBase(): Symbolic = when(this) {
        is Const, is Var -> this
        is Apply -> copy(arguments = arguments.map { body(it) })
        is Product -> Product.of(Const(constant), *parts.mapToArray { (s, c) -> body(s) pow c })
        is Sum -> Sum.of(Const(constant), *parts.mapToArray { (s, c) -> body(s) * c })
    }
}

fun Symbolic.transform(body: TransformScope.(Symbolic) -> Symbolic): Symbolic {
    return TransformScope(body).body(this)
}
