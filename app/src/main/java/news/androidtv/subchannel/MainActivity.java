package news.androidtv.subchannel;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.jreddit.entity.Submission;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

import org.sonatype.guice.bean.containers.Main;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RestClient restClient = new PoliteHttpRestClient();
    private Submissions subs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restClient.setUserAgent("bot/1.0 by name");

        subs = new Submissions(restClient);

        Log.d(TAG, "Get some posts");
        getPosts();
    }

    public void getPosts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Submission> posts = subs.ofSubreddit("youtubehaiku", null, -1, 100, null, null, true);
                Log.d(TAG, "Posts gotten: " + posts.size());
                for (Submission s : posts) {
                    Log.d(TAG, s.getTitle());
                    Log.d(TAG, "    " + s.getUrl());
                }
            }
        }).start();
    }
}
