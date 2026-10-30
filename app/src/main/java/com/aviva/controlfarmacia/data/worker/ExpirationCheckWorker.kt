package com.aviva.controlfarmacia.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aviva.controlfarmacia.data.notification.MedicationNotifier
import com.aviva.controlfarmacia.domain.model.ExpiryStatus
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.YearMonth

/** Daily check that notifies about expired medications and those expiring this month. */
@HiltWorker
class ExpirationCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MedicationRepository,
    private val notifier: MedicationNotifier
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = YearMonth.now()
        val byStatus = repository.observeAll().first().groupBy { it.expiryStatus(today) }
        notifier.showExpirationSummary(
            expired = byStatus[ExpiryStatus.EXPIRED].orEmpty(),
            expiringSoon = byStatus[ExpiryStatus.EXPIRING_THIS_MONTH].orEmpty()
        )
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "ExpirationCheckWork"
    }
}
