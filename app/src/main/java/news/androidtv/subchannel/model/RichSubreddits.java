package news.androidtv.subchannel.model;

import android.util.Log;

import com.github.jreddit.entity.Kind;
import com.github.jreddit.entity.Subreddit;
import com.github.jreddit.entity.User;
import com.github.jreddit.exception.RedditError;
import com.github.jreddit.exception.RetrievalFailedException;
import com.github.jreddit.retrieval.Subreddits;
import com.github.jreddit.utils.ApiEndpointUtils;
import com.github.jreddit.utils.ParamFormatter;
import com.github.jreddit.utils.restclient.RestClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

import static com.github.jreddit.utils.restclient.JsonUtils.safeJsonToString;

/**
 * Created by Nick on 4/22/2017.
 */

public class RichSubreddits extends Subreddits {
    private static final String TAG = RichSubreddits.class.getSimpleName();

    private RestClient restClient;
    private User user = null;

    public RichSubreddits(RestClient restClient) {
        super(restClient);
        this.restClient = restClient;
    }

    public RichSubreddit getRichSubreddit(String name) throws RetrievalFailedException, RedditError {
        // Retrieve submissions from the given URL
        return parseRich("/r/" + name + "/about.json");
    }

    /**
     * Parses a JSON feed from the Reddit (URL) into a nice list of Subreddit objects.
     *
     * @param url 	URL
     * @return 		Listing of submissions
     */
    public RichSubreddit parseRich(String url) throws RetrievalFailedException, RedditError {
        // Determine cookie
        Log.d(TAG, url);
        String cookie = (user == null) ? null : user.getCookie();

        // List of subreddits
        List<RichSubreddit> subreddits = new LinkedList<>();

        // Send request to reddit server via REST client
        Object response = restClient.get(url, cookie).getResponseObject();

        if (response instanceof JSONObject) {
            JSONObject object = (JSONObject) response;

            // Make sure it is of the correct kind
            String kind = safeJsonToString(object.get("kind"));
            if (kind != null) {
                if (kind.equals(Kind.SUBREDDIT.value())) {

                    // Create and add subreddit
                    object = ((JSONObject) object.get("data"));
                    Log.d(TAG, object.toJSONString());
                    subreddits.add(new RichSubreddit(object));
                }
            }
        } else {
            System.err.println("Cannot cast to JSON Object: '" + response.toString() + "'");
        }

        // Finally return list of subreddits
        Log.d(TAG, "Found " + subreddits.size() + " subreddits.");
        return subreddits.get(0); // Should only have one (at most)

    }
}
