package fr.cinevogue

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.cinevogue.data.Progress
import fr.cinevogue.data.ProgressStore
import java.time.LocalDate
import java.util.Calendar

/** Rappel quotidien : une alarme par jour, reprogrammée à chaque fois. */
object Reminder {
    const val ACTION = "fr.cinevogue.REMINDER"
    private const val CHANNEL = "tournage"

    private val MESSAGES = listOf(
        "Une scène de 3 minutes ? Le plateau est prêt.",
        "Moteur… action ! Ta prochaine scène t'attend.",
        "Les étoiles ne se gagnent pas toutes seules !",
        "Ta série de tournage compte sur toi aujourd'hui.",
    )

    fun sync(context: Context, p: Progress) {
        if (p.reminderOn) schedule(context, p.reminderHour, p.reminderMinute) else cancel(context)
    }

    private fun pending(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 1,
            Intent(context, ReminderReceiver::class.java).setAction(ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun schedule(context: Context, hour: Int, minute: Int) {
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis() + 1000) add(Calendar.DAY_OF_YEAR, 1)
        }
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pending(context))
    }

    private fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pending(context))
    }

    fun show(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Rappel de tournage", NotificationManager.IMPORTANCE_DEFAULT),
        )
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val open = PendingIntent.getActivity(
            context, 2,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle("Ton tournage t'attend !")
            .setContentText(MESSAGES.random())
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(10, notification)
        } catch (e: SecurityException) {
            // Notifications refusées : on ne fait rien.
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val p = ProgressStore(context).load()
        if (intent.action == Reminder.ACTION && p.reminderOn && p.lastDay != LocalDate.now().toEpochDay()) {
            Reminder.show(context)
        }
        Reminder.sync(context, p)
    }
}
