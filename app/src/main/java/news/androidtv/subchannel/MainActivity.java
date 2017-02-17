package news.androidtv.subchannel;

import android.content.Intent;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.jreddit.entity.Submission;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

import org.sonatype.guice.bean.containers.Main;
import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RestClient restClient = new PoliteHttpRestClient();
    private Submissions subs;
    private TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restClient.setUserAgent("bot/1.0 by name");

        subs = new Submissions(restClient);

        Log.d(TAG, "Get some posts");
        logView = (TextView) findViewById(R.id.logs);
        findViewById(R.id.livechannels).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, TvContract.Channels.CONTENT_URI));
            }
        });
        getPosts();
        getSupportActionBar().hide();
    }

    public void getPosts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Submission> posts = subs.ofSubreddit("youtubehaiku", null, -1, 100, null, null, true);
                appendLog("Popular posts retrieved");
                Log.d(TAG, "Posts gotten: " + posts.size());
                for (Submission s : posts) {
                    Log.d(TAG, s.getTitle());
                    Log.d(TAG, "    " + s.getUrl());
                    appendLog(s.getTitle() + "   " + s.getUrl());
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    Cursor cursor = getContentResolver().query(TvContract.RecordedPrograms.CONTENT_URI, null, null, null, null);
                    Log.d(TAG, "Recorded Programs: " + cursor.getCount());
                    appendLog("Found " + cursor.getCount() + " recorded programs");
                    while (cursor.moveToNext()) {
                        Log.d(TAG, cursor.getString(cursor.getColumnIndex(TvContract.RecordedPrograms.COLUMN_TITLE)));
                        appendLog("RP: " + cursor.getString(cursor.getColumnIndex(TvContract.RecordedPrograms.COLUMN_TITLE)));
                    }
                }
            }
        }).start();
    }

    private void appendLog(final String data) {
        new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                logView.setText(data + "\n" + logView.getText());
            }
        }.sendEmptyMessage(0);
    }
}
