package news.androidtv.subchannel.utils;

import news.androidtv.subchannel.model.SuggestedSubreddit;

import static news.androidtv.subchannel.model.SuggestedSubreddit.CATEGORY_EDUCATION;
import static news.androidtv.subchannel.model.SuggestedSubreddit.CATEGORY_MEME;

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

    /**
     * Gets an array of suggested Subreddits with lots of YouTube videos
     * @return
     */
    public static SuggestedSubreddit[] getSuggestedSubreddits() {
        return new SuggestedSubreddit[] {
            new SuggestedSubreddit(CATEGORY_EDUCATION, "lectures"),
            new SuggestedSubreddit(CATEGORY_MEME, "deepintoyoutube"),
            new SuggestedSubreddit(CATEGORY_MEME, "youtubehaiku")
        };
    }
}
