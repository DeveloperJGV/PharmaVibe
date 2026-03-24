package com.aviva.controlfarmacia.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aviva.controlfarmacia.data.repository.MedicationRepository
import com.aviva.controlfarmacia.util.AppNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class ExpirationCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MedicationRepository,
    private val notificationManager: AppNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val medications = repository.getAllMedications().first()
        val now = LocalDate.now()
        val currentMonth = now.monthValue
        val currentYear = now.year

        val expiredMedications = medications.filter { 
            it.expiryYear < currentYear || (it.expiryYear == currentYear && it.expiryMonth < currentMonth)
        }
        
        val expiringMedications = medications.filter { 
            it.expiryMonth == currentMonth && it.expiryYear == currentYear 
        }

        if (expiredMedications.isNotEmpty()) {
            val names = expiredMedications.joinToString(", ") { it.name }
            val message = if (expiredMedications.size == 1) {
                "Medication '$names' has expired."
            } else {
                "${expiredMedications.size} medications have expired: $names"
            }
            notificationManager.showExpirationNotification(
                title = "Expired Medications",
                message = message,
                notificationId = 101
            )
        }

        if (expiringMedications.isNotEmpty()) {
            val names = expiringMedications.joinToString(", ") { it.name }
            val message = if (expiringMedications.size == 1) {
                "Medication '$names' expires this month."
            } else {
                "${expiringMedications.size} medications expire this month: $names"
            }
            notificationManager.showExpirationNotification(
                title = "Expiring Soon",
                message = message,
                notificationId = 102
            )
        }

        return Result.success()
    }
}
