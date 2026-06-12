package com.aicaller.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aicaller.app.data.repository.CallRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that drains any call records with a transcript but no
 * AI summary yet. Runs after a call ends so the UI never blocks on the AI call.
 */
@HiltWorker
class SummarizationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val callRepository: CallRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        var processed = 0
        while (processed < MAX_PER_RUN) {
            val record = callRepository.summarizeNextPendingCall() ?: break
            processed++
        }
        return Result.success()
    }

    companion object {
        private const val MAX_PER_RUN = 5

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<SummarizationWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
