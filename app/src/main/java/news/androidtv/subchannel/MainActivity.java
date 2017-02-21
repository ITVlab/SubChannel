package news.androidtv.subchannel;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.github.jreddit.entity.Submission;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

import io.fabric.sdk.android.Fabric;
import news.androidtv.subchannel.fragments.SubredditCreationDialogFragment;
import news.androidtv.subchannel.utils.SubchannelSettingsManager;

import org.sonatype.guice.bean.containers.Main;
import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = false;

    private RestClient restClient = new PoliteHttpRestClient();
    private Submissions subs;
    private TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        restClient.setUserAgent("bot/1.0 by name");

        subs = new Submissions(restClient);
        final SubchannelSettingsManager settingsManager = new SubchannelSettingsManager(this);

        if (DEBUG) {
            Log.d(TAG, "Get some posts");
        }
        logView = (TextView) findViewById(R.id.logs);
        findViewById(R.id.livechannels).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, TvContract.Channels.CONTENT_URI));
            }
        });
        findViewById(R.id.button_edit_channels).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Selecting an item deletes it")
                        .setItems(settingsManager.getSubreddits(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (settingsManager.getSubreddits().length == 1) {
                                    Toast.makeText(MainActivity.this, "You must have at least" +
                                            " one subreddit.", Toast.LENGTH_SHORT).show();
                                } else {
                                    settingsManager.deleteSubreddit(i);
                                }
                            }
                        })
                        .setPositiveButton("Add Subreddit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Create an "Add Subreddit" dialog
                                SubredditCreationDialogFragment dialogFragment =
                                        new SubredditCreationDialogFragment();
                                dialogFragment.show(getFragmentManager(), "SUBREDDIT");
                            }
                        })
                        .show();
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
                if (DEBUG) {
                    Log.d(TAG, "Posts gotten: " + posts.size());
                }
                for (Submission s : posts) {
                    if (DEBUG) {
                        Log.d(TAG, s.getTitle());
                        Log.d(TAG, "    " + s.getUrl());
                    }
                    appendLog(s.getTitle() + "   " + s.getUrl());
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    Cursor cursor = getContentResolver().query(TvContract.RecordedPrograms.CONTENT_URI, null, null, null, null);
                    if (DEBUG) {
                        Log.d(TAG, "Recorded Programs: " + cursor.getCount());
                    }
                    appendLog("Found " + cursor.getCount() + " recorded programs");
                    while (cursor.moveToNext()) {
                        if (DEBUG) {
                            Log.d(TAG, cursor.getString(cursor.getColumnIndex(TvContract.RecordedPrograms.COLUMN_TITLE)));
                        }
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
