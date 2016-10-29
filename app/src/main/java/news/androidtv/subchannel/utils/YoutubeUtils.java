package news.androidtv.subchannel.utils;

/**
 * Created by Nick on 10/28/2016.
 */

public class YoutubeUtils {
    public static String parseVideoId(String videoUrl) {
        if (videoUrl.contains("youtube.com/watch?v=")) {
            return videoUrl.substring(videoUrl.indexOf("youtube.com/watch?v=") + 20, videoUrl.indexOf("youtube.com/watch?v=") + 33);
        } else if (videoUrl.contains("youtu.be/")) {
            return videoUrl.substring(videoUrl.indexOf("youtu.be/") + 9, videoUrl.indexOf("youtu.be/") + 24);
        }
        return "";
    }
}
