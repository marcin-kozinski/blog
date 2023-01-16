# You don't need a custom share sheet for that!

### Native share sheets have come a long way on Android and there's really no excuse for implementing custom share sheets anymore. Right?

At Pocket we sat down a couple years ago to retire our old custom share UI and switch to the native one, but we uncovered two product requirements that we thought would block us:

* We wanted to customise the text we included in the share intent based on the target app.
* We wanted to track which apps our users choose.

Fortunately we dug into the documentation and discovered that both of these are possible with the native share sheet! Let me show you how!

## Basic share sheet intent

But first, so we're all on the same page, here's our starting point—a way to create a basic native share sheet intent without any customisation:

```kotlin
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
```

## Per-app customisation with `EXTRA_REPLACEMENT_EXTRAS`

Intent extras are an essential part of talking to the Android framework. This API goes the *extra* mile and lets you provide extra extras. No, really, meet [Intent.EXTRA_REPLACEMENT_EXTRAS](https://developer.android.com/reference/kotlin/android/content/Intent#extra_replacement_extras).

It lets you attach a `Bundle` of replacements to a chooser intent. `Bundle` is essentially a map. In this case the keys are package names of the target apps you want customised share text for. And values are `Bundle`s of intent extras you want to add to or replace in the base intent. Yes, it's a `Bundle` of `Bundle`s.

*inception theme plays*

Putting it together, here's how you would send a custom message to the official Twitter app:

```kotlin
fun createShareIntentWithCustomMessage(context: Context): Intent {

    // snip

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
```

You can call `putBundle()` multiple times, to provide (same or different) replacements for more than one app.

Note: this was added in API 21, which should be low enough these days to just use directly in any modern app. If you're stuck on a lower `minSdkVersion`, well.. here's an argument I would try to make in front of my team: "It isn't worth it to roll an entire custom share sheet just to support a custom message for users on a 9+ year old Android version."

## Tracking share sheet clicks

Above we used a simple `Intent.createChooser()` call that accepts an intent and an optional title. But there's an [Intent.createChooser(intent, title, intentSender)](https://developer.android.com/reference/kotlin/android/content/Intent#createchooser_1) overload added in API 22, which accepts an optional `IntentSender`. It's basically a (pretty convoluted) way to add a callback when a user chooses an app from the sheet.

The callback lets you check the chosen app's package name and do anything you want with it. In the example below I'll call a (fake) analytics service to track the usage. Another use case—suggested by the docs—is remembering the last chosen app so you can provide it as a one touch share shortcut in your UI.

There's a couple of steps. First we need to create our "callback" in the form of a broadcast receiver that checks [Intent.EXTRA_CHOSEN_COMPONENT](https://developer.android.com/reference/kotlin/android/content/Intent#extra_chosen_component). Here's an example receiver (you can name the class anything you want):

```kotlin
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
```

The second step is passing an intent sender that points to this broadcast receiver when creating the share sheet intent:

```kotlin
fun createShareIntentWithCallback(context: Context): Intent {

    // snip

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ChosenComponentReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    return Intent.createChooser(sendIntent, null, pendingIntent.intentSender)
}
```

It's not the most straighforward of things, but once you're done with the special ceremony required by the framework, it works like a normal callback or rather a normal broadcast receiver.

## Conclusion

If you, your coworkers, your boss or—worse yet—your client thought you'll have to invest in building a custom share sheet, because otherwise there's no way to customise the shared text on a per-app basis or no way to track where your users share to, then you can be the hero that explains it is possible to save all this engineering and design effort and satisfy your requirements while showing the users a polished, familiar share experience.

Clients and bosses can be very creative, so there still might be some other reasons to implement a custom share sheet. But luckly the Android team handled at least these two common cases in the native one, which should greatly reduce how often you don't have a choice and have to go with a custom implementation. Thanks Android team!

You can see complete code examples from this post [here](https://github.com/marcin-kozinski/blog/tree/native-share-sheet-customisation/_drafts/native-share-sheet-customisation/NativeShareSheetCustomizations.kt). If you have any questions or comments, reply to this toot (TODO).
