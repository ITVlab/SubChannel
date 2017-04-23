package news.androidtv.subchannel.utils;

import news.androidtv.subchannel.model.SuggestedSubreddit;

import static news.androidtv.subchannel.model.SuggestedSubreddit.CATEGORY_ART;
import static news.androidtv.subchannel.model.SuggestedSubreddit.CATEGORY_CINEMA;
import static news.androidtv.subchannel.model.SuggestedSubreddit.CATEGORY_EDUCATION;
import static news.androidtv.subchannel.model.SuggestedSubreddit.CATEGORY_FUNNY;
import static news.androidtv.subchannel.model.SuggestedSubreddit.CATEGORY_MEME;
import static news.androidtv.subchannel.model.SuggestedSubreddit.CATEGORY_MISC;

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
                new SuggestedSubreddit(CATEGORY_ART, "ArtisanVideos"),
                new SuggestedSubreddit(CATEGORY_ART, "CookingVideos"),
                new SuggestedSubreddit(CATEGORY_CINEMA, "FullMoviesOnYouTube"),
                new SuggestedSubreddit(CATEGORY_CINEMA, "Trailers"),
                new SuggestedSubreddit(CATEGORY_EDUCATION, "lectures"),
                new SuggestedSubreddit(CATEGORY_EDUCATION, "Documentaries"),
                new SuggestedSubreddit(CATEGORY_EDUCATION, "EducativeVideos"),
                new SuggestedSubreddit(CATEGORY_FUNNY, "AccidentalComedy"),
                new SuggestedSubreddit(CATEGORY_FUNNY, "AmIBeingDetained"),
                new SuggestedSubreddit(CATEGORY_FUNNY, "AwfulCommercials"),
                new SuggestedSubreddit(CATEGORY_FUNNY, "Cringe"),
                new SuggestedSubreddit(CATEGORY_FUNNY, "CommercialCuts"),
                new SuggestedSubreddit(CATEGORY_FUNNY, "ContagiousLaughter"),
                new SuggestedSubreddit(CATEGORY_FUNNY, "IdiotsFightingThings"),
                new SuggestedSubreddit(CATEGORY_FUNNY, "PrematureCelebration"),
                new SuggestedSubreddit(CATEGORY_FUNNY, "UnexpectedThugLife"),
                new SuggestedSubreddit(CATEGORY_MEME, "deepintoyoutube"),
                new SuggestedSubreddit(CATEGORY_MEME, "youtubehaiku"),
                new SuggestedSubreddit(CATEGORY_MISC, "360video"),
                new SuggestedSubreddit(CATEGORY_MISC, "CuriousVideos"),
                new SuggestedSubreddit(CATEGORY_MISC, "FastWorkers"),
                new SuggestedSubreddit(CATEGORY_MISC, "HappyCrowds"),
                new SuggestedSubreddit(CATEGORY_MISC, "IMGXXXX"),
                new SuggestedSubreddit(CATEGORY_MISC, "MealTimeVideos"),
                new SuggestedSubreddit(CATEGORY_MISC, "MotivationVideos"),
                new SuggestedSubreddit(CATEGORY_MISC, "ObscureMedia"),
                new SuggestedSubreddit(CATEGORY_MISC, "PlayItAgainSam"),
                new SuggestedSubreddit(CATEGORY_MISC, "PublicFreakout"),
                new SuggestedSubreddit(CATEGORY_MISC, "Roadcam"),
                new SuggestedSubreddit(CATEGORY_MISC, "StreetFights"),
                new SuggestedSubreddit(CATEGORY_MISC, "SweetJustice"),
                new SuggestedSubreddit(CATEGORY_MISC, "TheWayWeWereOnVideo"),
                new SuggestedSubreddit(CATEGORY_MISC, "UnknownVideos"),
                new SuggestedSubreddit(CATEGORY_MISC, "Videos"),
                new SuggestedSubreddit(CATEGORY_MISC, "VideoPorn"),
                new SuggestedSubreddit(CATEGORY_MISC, "Vids"),
                new SuggestedSubreddit(CATEGORY_MISC, "VirtualFreakout"),
                new SuggestedSubreddit(CATEGORY_MISC, "WoahTube"),
        };
    }
}
