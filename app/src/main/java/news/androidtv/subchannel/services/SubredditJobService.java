package news.androidtv.subchannel.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.jreddit.entity.Submission;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;
import com.google.android.media.tv.companionlibrary.EpgSyncJobService;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 2/16/2017.
 */

public class SubredditJobService extends EpgSyncJobService {
    private static final String TAG = SubredditJobService.class.getSimpleName();
    public static final long DEFAULT_IMMEDIATE_EPG_DURATION_MILLIS = 1000 * 60 * 60; // 1 Hour

    private List<Submission> submissions;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Intent intent = new Intent(ACTION_SYNC_STATUS_CHANGED);
        intent.putExtra(BUNDLE_KEY_INPUT_ID, params.getExtras().getString(BUNDLE_KEY_INPUT_ID));
        Log.d(TAG, "Sync program data for " + params.getExtras().getString(BUNDLE_KEY_INPUT_ID));
        intent.putExtra(SYNC_STATUS, SYNC_STARTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Pull data from Reddit
        submissions = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                RestClient restClient = new PoliteHttpRestClient();
                restClient.setUserAgent("bot/1.0 by name");
                Submissions subs = new Submissions(restClient);
                // TODO get other subreddits in the future
                submissions = subs.ofSubreddit("youtubehaiku", null, -1, 100, null, null, true);
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        EpgSyncTask epgSyncTask = new EpgSyncTask(params);
                        epgSyncTask.execute();
                    }
                }.sendEmptyMessage(0);
            }
        }).start();
        return true;
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> channelList = new ArrayList<>();
        channelList.add(new Channel.Builder()
                .setDisplayName("/r/YouTubeHaiku")
                .setChannelLogo("https://raw.githubusercontent.com/ITVlab/SubChannel/master/store/haiku.PNG")
                .setDisplayNumber("1")
                .setOriginalNetworkId(0)
                .build());
        return channelList;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channel, long startMs, long endMs) {
        List<Program> programList = new ArrayList<>();
        for (int i = 0; i < submissions.size(); i++) {
            Submission s = submissions.get(i);
            InternalProviderData data = new InternalProviderData();
            data.setVideoUrl(s.getUrl());
            programList.add(new Program.Builder()
                    .setTitle(s.getTitle())
                    .setThumbnailUri(s.getThumbnail())
                    .setDescription("Posted by " + s.getAuthor())
                    .setInternalProviderData(data)
                    .setStartTimeUtcMillis(startMs + 1000 * 60 * i)
                    .setEndTimeUtcMillis(startMs + 1000 * 60 * (i + 1)) // FIXME Don't know the video duration
                    .build());
        }
        return programList;
    }

    @Deprecated
    public static void requestImmediateSync1(Context context, String inputId, long syncDuration,
            ComponentName jobServiceComponent) {
        if (jobServiceComponent.getClass().isAssignableFrom(EpgSyncJobService.class)) {
            throw new IllegalArgumentException("This class does not extend EpgSyncJobService");
        }
        PersistableBundle persistableBundle = new PersistableBundle();
        if (Build.VERSION.SDK_INT >= 22) {
            persistableBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            persistableBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        }
        persistableBundle.putString(EpgSyncJobService.BUNDLE_KEY_INPUT_ID, inputId);
        persistableBundle.putLong("bundle_key_sync_period", syncDuration);
        JobInfo.Builder builder = new JobInfo.Builder(1, jobServiceComponent);
        JobInfo jobInfo = builder
                .setExtras(persistableBundle)
                .setOverrideDeadline(1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();
        scheduleJob(context, jobInfo);
        Log.d(TAG, "Single job scheduled");
    }

    /** Send the job to JobScheduler. */
    private static void scheduleJob(Context context, JobInfo job) {
        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = jobScheduler.schedule(job);
        Assert.assertEquals(result, JobScheduler.RESULT_SUCCESS);
        Log.d(TAG, "Scheduling result is " + result);
    }
}
