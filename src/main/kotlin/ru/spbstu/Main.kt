package ru.spbstu

fun main(args: Array<String>) {
    with(SymbolicScope) {
        val x by this
        val y by this
        val z by this
        println(x pow Rational(1, 2) pow 2)
        println((x * x + y).subst(mapOf(x to 23())))
        println((x + y) * (x + y) * (x + z) * 2)
        println((x + y - z) pow 2)
        println(x pow -2)
        println((x + x + y) / 2 + y / 2)

        println(RowSum.of(-x, x) { y })
        println(RowSum.of(1(), 12()) { it })
        println(RowSum.of(1(), y + 1) { it }.subst(mapOf(y to 11())))

        println(RowSum.of(1(), x) { it * it })

        println(((x pow 2) / 2 + x / 2).subst(x to y + 1))
        println(finiteDifference((x pow 2) / 2 + x / 2, x))

        println((x + y) * ((x + y) pow -2))
    }
}