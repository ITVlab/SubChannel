package news.androidtv.subchannel.utils;

import android.app.Activity;
import android.content.Context;

import com.felkertech.settingsmanager.SettingsManager;

import java.util.Arrays;

/**
 * Created by Nick on 2/20/2017.
 */

public class SubchannelSettingsManager extends SettingsManager {
    /**
     * Comma-separated string for all default subreddits included in this app.
     */
    private final static String DEFAULT_SUBREDDITS = "youtubehaiku";

    public SubchannelSettingsManager(Activity activity) {
        super(activity);
    }

    public SubchannelSettingsManager(Context context) {
        super(context);
    }

    public String[] getSubreddits() {
        String subreddits = getString(SettingConstants.KEY_SUBREDDITS_SAVED);
        if (subreddits == null || subreddits.isEmpty()) {
            subreddits = DEFAULT_SUBREDDITS;
        }
        return subreddits.split(",");
    }

    public void saveSubreddits(String[] subredditArray) {
        if (subredditArray.length > 0) {
            StringBuilder nameBuilder = new StringBuilder();

            for (String n : subredditArray) {
                nameBuilder.append(n).append(",");
            }

            nameBuilder.deleteCharAt(nameBuilder.length() - 1);

            setString(SettingConstants.KEY_SUBREDDITS_SAVED, nameBuilder.toString());
        } else {
            setString(SettingConstants.KEY_SUBREDDITS_SAVED, DEFAULT_SUBREDDITS);
        }
    }
}
