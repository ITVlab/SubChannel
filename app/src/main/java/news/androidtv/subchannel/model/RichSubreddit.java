package news.androidtv.subchannel.model;

import android.util.Log;

import com.github.jreddit.entity.Subreddit;

import org.json.simple.JSONObject;

/**
 * Created by Nick on 4/22/2017.
 */

public class RichSubreddit extends Subreddit {
    private static final String TAG = RichSubreddit.class.getSimpleName();

    private String mHeaderImg;
    private String mIconImg;

    /**
     * Create a Submission from a JSONObject
     *
     * @param obj The JSONObject to load Submission data from
     */
    public RichSubreddit(JSONObject obj) {
        super(obj);
        Log.d(TAG, obj.get("icon_img") + "");
        mIconImg = obj.get("icon_img").toString();
        Log.d(TAG, obj.get("header_img") + "");
        mHeaderImg = obj.get("header_img").toString();
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
