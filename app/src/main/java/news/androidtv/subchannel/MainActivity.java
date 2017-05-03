package news.androidtv.subchannel;

import android.content.ComponentName;
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
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.felkertech.settingsmanager.SettingsManager;
import com.github.jreddit.entity.Submission;
import com.github.jreddit.entity.Subreddit;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;
import com.google.android.media.tv.companionlibrary.EpgSyncJobService;

import io.fabric.sdk.android.Fabric;
import news.androidtv.subchannel.fragments.SubredditCreationDialogFragment;
import news.androidtv.subchannel.model.SuggestedSubreddit;
import news.androidtv.subchannel.services.SubredditJobService;
import news.androidtv.subchannel.shims.Function;
import news.androidtv.subchannel.utils.SubchannelSettingsManager;
import news.androidtv.subchannel.utils.SubredditUtils;

import org.sonatype.guice.bean.containers.Main;
import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

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

        findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSubredditAdd();
            }
        });

        getPosts();
        getSupportActionBar().hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
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

    /**
     * Programmatically display a series of Subreddits that already exist, and some suggested.
     */
    private void updateList() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.things); // TODO Rename this.
        linearLayout.removeAllViews(); // Clear
        linearLayout.addView(getHeading("Recommended"));
        // Get your subreddits.
        List<String> subreddits = Arrays.asList(new SubchannelSettingsManager(this).getSubreddits());
        int i = 0;
        List<SuggestedSubreddit> suggestions = Arrays.asList(SubredditUtils.getSuggestedSubreddits());
        Collections.shuffle(suggestions); // Randomize items

        for (SuggestedSubreddit subreddit : SubredditUtils.getSuggestedSubreddits()) {
            if (i >= 2) {
                break; // Exit loop
            }
            if (!subreddits.contains(subreddit.getSubreddit())) {
                linearLayout.addView(getRecommendedButton(subreddit));
                i++;
            }
        }

        linearLayout.addView(getHeading("My Subreddits"));

        for (i = 0; i < subreddits.size(); i++) {
            linearLayout.addView(getMySubredditButton(subreddits.get(i), i));
        }
    }

    private void updateEpg() {
        String inputId =  new SettingsManager(this).getString(EpgSyncJobService.BUNDLE_KEY_INPUT_ID);
        SubredditJobService.requestImmediateSync1(this, inputId,
                SubredditJobService.DEFAULT_IMMEDIATE_EPG_DURATION_MILLIS,
                new ComponentName(this, SubredditJobService.class));
    }

    private void openSubredditAdd() {
        // Create an "Add Subreddit" dialog
        SubredditCreationDialogFragment dialogFragment =
                new SubredditCreationDialogFragment(new SubredditCreationDialogFragment.Callback() {
                    @Override
                    public void onDismiss() {
                        updateList();
                        updateEpg();
                    }
                });
        dialogFragment.show(getFragmentManager(), "SUBREDDIT");
    }

    private TextView getHeading(String txt) {
        TextView heading = new TextView(this);
        heading.setText(txt);
        heading.setTextSize(18);
        heading.setAllCaps(true);
        heading.setTextColor(getResources().getColor(R.color.whitish));
        heading.setPadding(0, 16, 0, 8);
        return heading;
    }

    private Button getListButton(final String txt) {
        Button myButton = new Button(this);
        myButton.setText(txt);
//        myButton.setBackgroundColor(getResources().getColor(android.R.color.white));
        return myButton;
    }

    private Button getRecommendedButton(final SuggestedSubreddit subreddit) {
        Button myButton = getListButton(subreddit.getSubreddit() + " - " +
                subreddit.getCategory().toUpperCase());
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubchannelSettingsManager settingsManager =
                        new SubchannelSettingsManager(MainActivity.this);
                settingsManager.addSubreddit(subreddit.getSubreddit());
                Toast.makeText(MainActivity.this, R.string.added_subreddit, Toast.LENGTH_SHORT).show();
                updateList();
                updateEpg();
            }
        });
        return myButton;
    }

    private Button getMySubredditButton(String subreddit, final int index) {
        Button myButton = getListButton(subreddit);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubchannelSettingsManager settingsManager = new SubchannelSettingsManager(MainActivity.this);
                if (settingsManager.getSubreddits().length == 1) {
                    Toast.makeText(MainActivity.this, R.string.warning_one_subreddit, Toast.LENGTH_SHORT).show();
                } else {
                    // TODO Show a manager dialog
                    settingsManager.deleteSubreddit(index);
                    Toast.makeText(MainActivity.this, R.string.deleted_subreddit, Toast.LENGTH_SHORT).show();
                    updateList();
                    updateEpg();
                }
            }
        });
        return myButton;
    }
}
