package ru.spbstu

class Factorial(base: Symbolic): Apply("factorial", base) {
    override fun copy(arguments: List<Symbolic>): Symbolic {
        check(arguments.size == 1)
        return Factorial(arguments.single())
    }
}
