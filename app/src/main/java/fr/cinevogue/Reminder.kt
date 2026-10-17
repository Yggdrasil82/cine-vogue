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
import fr.cinevogue.data.ProfileRepo
import fr.cinevogue.data.ProgressStore
import fr.cinevogue.data.Settings
import java.time.LocalDate
import java.util.Calendar

/** Rappel quotidien commun : une alarme par jour, reprogrammée à chaque fois. */
object Reminder {
    const val ACTION = "fr.cinevogue.REMINDER"
    private const val CHANNEL = "tournage"

    private val MESSAGES = listOf(
        "Une scène de 3 minutes ? Le plateau est prêt.",
        "Moteur… action ! La prochaine scène t'attend.",
        "Les étoiles ne se gagnent pas toutes seules !",
        "Ta série de tournage compte sur toi aujourd'hui.",
    )

    fun sync(context: Context, s: Settings) {
        if (s.reminderOn) schedule(context, s.reminderHour, s.reminderMinute) else cancel(context)
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

    /** Prévient les profils qui n'ont pas encore tourné aujourd'hui. */
    fun show(context: Context, names: List<String>) {
        if (names.isEmpty()) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Rappel de tournage", NotificationManager.IMPORTANCE_DEFAULT),
        )
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val title = if (names.size == 1) "Ton tournage t'attend, ${names[0]} !" else "Le tournage vous attend !"
        val text = if (names.size == 1) MESSAGES.random()
        else names.dropLast(1).joinToString(", ") + " et " + names.last() + " n'ont pas encore tourné aujourd'hui."

        val open = PendingIntent.getActivity(
            context, 2,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
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
        val repo = ProfileRepo(context)
        val settings = repo.settings()
        if (intent.action == Reminder.ACTION && settings.reminderOn) {
            val today = LocalDate.now().toEpochDay()
            val late = repo.profiles().filter { ProgressStore(context, it.prefs).load().lastDay != today }
            Reminder.show(context, late.map { it.name })
        }
        Reminder.sync(context, settings)
    }
}
