package ru.spbstu

import kotlinx.warnings.Warnings

inline fun <A, reified B> Collection<A>.mapToArray(body: (A) -> B): Array<B> {
    val res = arrayOfNulls<B>(size)
    var i = 0
    for(e in this) res[i++] = body(e)
    @Suppress(Warnings.UNCHECKED_CAST)
    return res as Array<B>
}

inline fun <K, V, reified B> Map<K, V>.mapToArray(body: (K, V) -> B): Array<B> {
    val res = arrayOfNulls<B>(size)
    var i = 0
    for((k, v) in this) res[i++] = body(k, v)
    @Suppress(Warnings.UNCHECKED_CAST)
    return res as Array<B>
}

inline fun <T> T.sealed() = this

inline fun <T, C1: MutableCollection<in T>, C2: MutableCollection<in T>>
        Iterable<T>.partitionTo(c1: C1, c2: C2, predicate: (T) -> Boolean): Pair<C1, C2> {
    for (element in this) {
        if (predicate(element)) {
            c1.add(element)
        } else {
            c2.add(element)
        }
    }
    return Pair(c1, c2)
}

inline fun <K, V, C1: MutableMap<K, V>, C2: MutableMap<K, V>>
        Map<K, V>.partitionTo(c1: C1, c2: C2, predicate: (K, V) -> Boolean): Pair<C1, C2> {
    for ((k, v) in this) {
        if (predicate(k, v)) {
            c1.put(k, v)
        } else {
            c2.put(k, v)
        }
    }
    return Pair(c1, c2)
}
