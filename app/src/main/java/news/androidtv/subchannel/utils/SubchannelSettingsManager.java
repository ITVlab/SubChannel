package news.androidtv.subchannel.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;

import com.felkertech.settingsmanager.SettingsManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import news.androidtv.subchannel.services.SubredditJobService;

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
        // Resync
        SubredditJobService.requestImmediateSync1(getContext(), "news.androidtv.subchannel/.services.TifPlaybackService",
                SubredditJobService.DEFAULT_IMMEDIATE_EPG_DURATION_MILLIS,
                new ComponentName(getContext(), SubredditJobService.class));
    }

    public void deleteSubreddit(int index) {
        List<String> subredditsList = getList(getSubreddits());
        subredditsList.remove(index);
        saveSubreddits(subredditsList.toArray(new String[subredditsList.size()]));
    }

    public void addSubreddit(String name) {
        List<String> subredditsList = getList(getSubreddits());
        subredditsList.add(name);
        saveSubreddits(subredditsList.toArray(new String[subredditsList.size()]));
    }

    private List<String> getList(String[] array) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String item : array) {
            arrayList.add(item);
        }
        return arrayList;
    }
}
