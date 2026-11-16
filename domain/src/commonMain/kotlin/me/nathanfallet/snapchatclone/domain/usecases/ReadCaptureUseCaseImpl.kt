package me.nathanfallet.snapchatclone.domain.usecases

import me.nathanfallet.snapchatclone.domain.repositories.CapturesRepository

/**
 * Reads captures with the [repository].
 */
class ReadCaptureUseCaseImpl(private val repository: CapturesRepository) : ReadCaptureUseCase {
    override suspend fun invoke(path: String): ByteArray = repository.readCapture(path)
}
