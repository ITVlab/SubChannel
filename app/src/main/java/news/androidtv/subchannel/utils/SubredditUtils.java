package news.androidtv.subchannel.utils;

/**
 * Created by Nick on 4/22/2017.
 */

public class SubredditUtils {
    public static String sanitizeSubreddit(String submission) {
        // If subreddit string contains spaces or '/r/' remove.
        submission = submission.trim();
        if (submission.substring(0, 3).equals("/r/")) {
            submission = submission.substring(3);
        } else if (submission.substring(0, 2).equals("r/")) {
            submission = submission.substring(2);
        } else if (submission.substring(0, 1).equals("/")) {
            submission = submission.substring(1);
        }
        return submission;
    }
}
