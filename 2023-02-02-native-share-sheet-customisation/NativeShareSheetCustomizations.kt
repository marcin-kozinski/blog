import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.IntentCompat
import com.pocket.util.android.PendingIntentUtils

private const val REQUEST_CODE = 457952

class ChosenComponentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val component = IntentCompat.getParcelableExtra(
            intent,
            Intent.EXTRA_CHOSEN_COMPONENT,
            ComponentName::class.java
        )
        component?.let {
            analytics.track(it.packageName)
        }
    }
}

fun createBasicShareIntent(context: Context): Intent {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Subject")
        putExtra(Intent.EXTRA_TEXT, "Hello world!")
        addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    return Intent.createChooser(sendIntent, null)
}

fun createShareIntentWithCustomMessage(context: Context): Intent {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Subject")
        putExtra(Intent.EXTRA_TEXT, "Hello world!")
        addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    val replacements = Bundle().apply {
        putBundle(
            "com.twitter.android",
            Bundle().apply {
                putString(Intent.EXTRA_TEXT, "Bye Elon!")
            },
        )
    }
    return Intent.createChooser(sendIntent, null).apply {
        putExtra(Intent.EXTRA_REPLACEMENT_EXTRAS, replacements)
    }
}

fun createShareIntentWithCallback(context: Context): Intent {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Subject")
        putExtra(Intent.EXTRA_TEXT, "Hello world!")
        addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ChosenComponentReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    return Intent.createChooser(sendIntent, null, pendingIntent.intentSender)
}
