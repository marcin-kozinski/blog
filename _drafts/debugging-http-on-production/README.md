# Debugging HTTP on production

## Problem

We had a problem with a server not tracking some events we reported via firing GET requests. We could verify that we're sending the request and they look valid on debug and internal builds. It was easy with Logcat, the debugger and any other tool at our disposal. But to be absolutely sure the problem wasn't on the app side we wanted to check the same thing on our production builds.

tl;dr We wanted to capture and inspect an HTTP request coming from a production build. And when I say HTTP I actually mean it was over HTTPS of course.

## Solution

I tried a bunch of solutions, but thanks to some great privacy and security improvements modern Android versions make it pretty hard to snoop on HTTPS traffic. Here is *a* solution I came up with, there totally might be an easier or nicer way, but here's what worked for me.

I found [a blog post](https://medium.com/@rotxed/how-to-debug-http-s-traffic-on-android-7fbe5d2a34) recommending using a tool called mitmproxy (I've usually used Charles in the past for that, but wanted to try something else, since Charles could sometimes be a pain, especially to make it work with recent Android versions). Then in mitmproxy documentation I found [instructions](https://docs.mitmproxy.org/stable/howto-install-system-trusted-ca-android/) for the exact thing I was trying to do. These two links tell almost everything there is to know, but here are some things I had to figure out by myself:

* Here's the emulator setup that worked for me:
  * Use an emulator image without Google Play support as these come with a production build, root commands don't work and you can't write to the system partition.
  * Also I recommend using Android P/Android 9.0/API 28. mitmproxy documentation mentions different instructions for <=9.0 and >9.0, but I couldn't make it work on an Android 10 image, so decided to stick with 9.0.
* Don't start the emulator from Android Studio (or.. close it if you already have), you have to start it from the terminal with the extra `-writable-system` option, like the mitmproxy page mentions in step 3.
* Go through the [mitmproxy CA certificate instructions](https://docs.mitmproxy.org/stable/howto-install-system-trusted-ca-android/).
* Connect your phone and your computer to the same WiFi or local network.
* Then you can just start mitmproxy with a simple `mitmproxy`. You don't have to specify an IP and a port. By default it uses your computer's IP and port 8080.
* On macos you can find your IP in Network Preferences > WiFi  > Advanced > TCP/IP > IPv4 Address.
* You can set up your proxy settings in emulator settings, you don't have to go through Android system WiFi settings inside the emulator. Much easier!
* Inside mitmproxy you can use up and down arrows to select requests from the list and hit enter to view details. Then `q` to go back. At any point you can hit `?` to see contextual keyboard commands help.

## References

* https://medium.com/@rotxed/how-to-debug-http-s-traffic-on-android-7fbe5d2a34
* https://docs.mitmproxy.org/stable/howto-install-system-trusted-ca-android/
