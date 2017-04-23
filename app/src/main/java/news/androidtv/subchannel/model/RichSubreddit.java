package news.androidtv.subchannel.model;

import android.util.Log;

import com.github.jreddit.entity.Subreddit;

import org.json.simple.JSONObject;

/**
 * Created by Nick on 4/22/2017.
 */

public class RichSubreddit extends Subreddit {
    private static final String TAG = RichSubreddit.class.getSimpleName();
    private static final String KEY_ICON = "icon_img";
    private static final String KEY_HEADER = "header_img";

    private String mHeaderImg;
    private String mIconImg;

    /**
     * Create a Submission from a JSONObject
     *
     * @param obj The JSONObject to load Submission data from
     */
    public RichSubreddit(JSONObject obj) {
        super(obj);
        if (obj.containsKey(KEY_ICON)) {
            Log.d(TAG, obj.get(KEY_ICON) + "");
            mIconImg = obj.get(KEY_ICON).toString();
        }
        if (obj.containsKey(KEY_HEADER)) {
            Log.d(TAG, obj.get(KEY_HEADER) + "");
            mHeaderImg = obj.get(KEY_HEADER).toString();
        }
    }

    public String getHeaderImage() {
        return mHeaderImg;
    }

    public void setHeaderImage(String imageUrl) {
        mHeaderImg = imageUrl;
    }

    public String getIconImage() {
        return mIconImg;
    }

    public void setIconImage(String iconUrl) {
        mIconImg = iconUrl;
    }

    public String getBestIcon() {
        if (mIconImg != null) {
            return mIconImg;
        } else if (mHeaderImg != null) {
            return mHeaderImg;
        }
        // App icon as a URL
        return "https://raw.githubusercontent.com/ITVlab/SubChannel/master/store/app_icon.png";
    }
}
