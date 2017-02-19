# SubChannel
Videos from various subreddits that can be played through the Live Channels App, on Android TV

Download the app from [Google Play](https://play.google.com/store/apps/details?id=news.androidtv.subchannel).

You can also sign up [for the beta](https://play.google.com/apps/testing/news.androidtv.subchannel?authuser=null).

## Background
Reddit is a popular source for curated content. Users are able to pick what submissions are the best. This content can be found in the "top" or "hot" sections of Reddit.

What this app does is integrates this hot content with the TV Input Framework. A `JobService` runs periodically to pull the top content from each selected Subreddit, extract all of the videos, and place them into the program guide.

At this moment, only YouTube videos are playable. Some good examples of Subreddits are **/r/YouTubeHaiku**.

### How Does This Work?
Since the standard Android YouTube SDK doesn't work for video playback, this uses a `WebView` and loads the YouTube URLs. Then, playback is controlled through JavaScript. The code for doing so is available as a separate library, the [YouTube TV Player](https://github.com/itvlab/youtube-tv-player).

### DVR
If your Android TV is running Android 7.0 and up, you will have access to the DVR feature. This will allow certain programs to be saved. Programs are not stored on the device. It is more like a bookmark, where the metadata is saved. Playing that video will start playing the YouTube video.

## Compiling
If you encounter any issues in getting the app to build and compile on a device, try running **Clean Project** first. Then building should work. You'll still see a number of errors but they're benign.

## Contributing
If you have any feedback, feel free to write an issue or contribute to the code. Pull requests are accepted.

## Known Issues

* Program guide doesn't match what's playing
    * Yes, I currently don't have the duration of each video, so the guide may be out of sync. However, I do pull from that same guide to get every video.
* I can't edit the Subreddits
    * A future update will give users more control over what content is available
* General playback issues
    * Yes, the [YouTube Tv Player](https://github.com/itvlab/youtube-tv-player) is still buggy. Refer to that project for issues and upadtes.