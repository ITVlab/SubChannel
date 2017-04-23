package news.androidtv.subchannel.model;

/**
 * Created by Nick on 4/22/2017.
 */

public class SuggestedSubreddit {
    public static final String CATEGORY_ART = "Artistic";
    public static final String CATEGORY_CINEMA = "Cinema";
    public static final String CATEGORY_EDUCATION = "Educational";
    public static final String CATEGORY_FUNNY = "Funny";
    public static final String CATEGORY_MEME = "Memes";
    public static final String CATEGORY_MISC = "Community";
    public static final String CATEGORY_NSFW = "NSFW";

    private final String mCategory;
    private final String mSubreddit;

    public SuggestedSubreddit(String category, String subreddit) {
        this.mCategory = category;
        this.mSubreddit = subreddit;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getSubreddit() {
        return mSubreddit;
    }
}
