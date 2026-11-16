package me.nathanfallet.snapchatclone.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class FilterTest {
    @Test
    fun nextGoesThroughEveryFilterAndWrapsAround() {
        val visited = generateSequence(Filter.NORMAL) { it.next() }.take(Filter.entries.size + 1).toList()
        assertEquals(Filter.entries + Filter.NORMAL, visited)
    }

    @Test
    fun previousIsTheInverseOfNext() {
        Filter.entries.forEach { assertEquals(it, it.next().previous()) }
    }

    @Test
    fun previousOfTheFirstFilterIsTheLastOne() {
        assertEquals(Filter.entries.last(), Filter.NORMAL.previous())
    }

    @Test
    fun fromNameFindsFiltersAndFallsBackToNormal() {
        assertEquals(Filter.VINTAGE, Filter.fromName("VINTAGE"))
        assertEquals(Filter.NORMAL, Filter.fromName("REMOVED_FILTER"))
    }
}
