package me.nathanfallet.snapchatclone.domain.usecases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class GetMemoriesUseCaseImplTest {
    @Test
    fun memoriesAreSortedNewestFirst() = runTest {
        val repository = FakeMemoriesRepository(
            listOf(
                memory("old", Instant.fromEpochMilliseconds(1_000)),
                memory("new", Instant.fromEpochMilliseconds(3_000)),
                memory("middle", Instant.fromEpochMilliseconds(2_000)),
            ),
        )
        val ids = GetMemoriesUseCaseImpl(repository)().first().map { it.id }
        assertEquals(listOf("new", "middle", "old"), ids)
    }

    @Test
    fun memoriesCapturedAtTheSameTimeHaveAStableOrder() = runTest {
        val date = Instant.fromEpochMilliseconds(1_000)
        val repository = FakeMemoriesRepository(listOf(memory("a", date), memory("c", date), memory("b", date)))
        val ids = GetMemoriesUseCaseImpl(repository)().first().map { it.id }
        assertEquals(listOf("c", "b", "a"), ids)
    }

    @Test
    fun updatesFollowTheRepository() = runTest {
        val repository = FakeMemoriesRepository()
        val useCase = GetMemoriesUseCaseImpl(repository)
        assertEquals(emptyList(), useCase().first())
        repository.state.value = listOf(memory("a"))
        assertEquals(listOf("a"), useCase().first().map { it.id })
    }
}
