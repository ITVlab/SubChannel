package news.androidtv.subchannel.utils;

import android.util.Log;

/**
 * Created by Nick on 10/28/2016.
 */

public class YoutubeUtils {
    private static final String TAG = YoutubeUtils.class.getSimpleName();

    public static String parseVideoId(String videoUrl) {
        if (videoUrl.contains("youtube.com/watch?v=")) {
            Log.d(TAG, videoUrl);
            return videoUrl.substring(videoUrl.indexOf("youtube.com/watch?v=") + 20, videoUrl.indexOf("youtube.com/watch?v=") + 31);
        } else if (videoUrl.contains("youtu.be/")) {
            return videoUrl.substring(videoUrl.indexOf("youtu.be/") + 9, videoUrl.indexOf("youtu.be/") + 20);
        }
        return "";
    }
}
