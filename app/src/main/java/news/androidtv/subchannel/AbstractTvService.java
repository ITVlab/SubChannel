package news.androidtv.subchannel;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.media.PlaybackParams;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.felkertech.channelsurfer.interfaces.TimeShiftable;
import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.service.SimpleSessionImpl;
import com.felkertech.channelsurfer.service.TvInputProvider;
import com.felkertech.channelsurfer.sync.SyncAdapter;
import com.github.jreddit.entity.Submission;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.utils.restclient.PoliteHttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

import java.util.ArrayList;
import java.util.List;

import news.androidtv.libs.player.AbstractWebPlayer;
import news.androidtv.libs.player.YouTubePlayerView;
import news.androidtv.subchannel.utils.YoutubeUtils;

/**
 * Created by Nick on 10/28/2016.
 */

public class AbstractTvService extends TvInputProvider implements TimeShiftable {
    private static final String TAG = AbstractTvService.class.getSimpleName();

    private List<Submission> submissions;
    private YouTubePlayerView youTubePlayerView;
    private RedditTvSession redditTvSession;
    private boolean play;

    public AbstractTvService() {
    }

    @Override
    public void performCustomSync(final SyncAdapter syncAdapter, final String inputId) {
        submissions = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                RestClient restClient = new PoliteHttpRestClient();
                restClient.setUserAgent("bot/1.0 by name");
                Submissions subs = new Submissions(restClient);
                submissions = subs.ofSubreddit("youtubehaiku", null, -1, 100, null, null, true);
                syncAdapter.performSync(AbstractTvService.this, inputId);
            }
        }).start();
    }

    @Override
    public List<Channel> getAllChannels(Context context) {
        List<Channel> channelList = new ArrayList<>();
        channelList.add(new Channel()
                .setName("/r/YouTubeHaiku")
                .setLogoUrl("http://theawesomer.com/photos/2012/06/140612_youtube_haiku_t.jpg")
                .setNumber("1"));
        return channelList;
    }

    @Override
    public List<Program> getProgramsForChannel(Context context, Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        List<Program> programList = new ArrayList<>();
        for (int i = 0; i < submissions.size(); i++) {
            Submission s = submissions.get(i);
            programList.add(new Program.Builder()
                    .setTitle(s.getTitle())
                    .setThumbnailUri(s.getThumbnail())
                    .setDescription("Posted by " + s.getAuthor())
                    .setInternalProviderData(s.getUrl())
                    .setStartTimeUtcMillis(startTimeMs)
                    .setEndTimeUtcMillis(startTimeMs + 1000 * 60 * i) // Don't know the video duration
                    .build());
        }
        return programList;
    }

    @Override
    public boolean onSetSurface(Surface surface) {
        return true;
    }

    @Override
    public void onSetStreamVolume(float volume) {
        // Maybe
    }

    @Override
    public void onRelease() {
        play = false;
    }

    @Override
    public View onCreateOverlayView() {
        if (youTubePlayerView == null) {
            youTubePlayerView = new YouTubePlayerView(getApplicationContext());
        }
        return youTubePlayerView;
    }

    @Override
    public boolean onTune(final Channel channel) {
        play = true;
        final Program program = getProgramRightNow(channel);
        notifyVideoAvailable();
        setOverlayEnabled(true);
        youTubePlayerView.setVideoEventsListener(new AbstractWebPlayer.VideoEventsListener() {
            @Override
            public void onVideoEnded() {
                if (play) {
                    onTune(channel);
                }
            }
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                youTubePlayerView.loadVideo(YoutubeUtils.parseVideoId(program.getInternalProviderData()));
            }
        });
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public RecordingSession onCreateRecordingSession(String inputId) {
        Log.d(TAG, "Want to create a new recording session " + inputId);
        return new RedditRecordingSession(getApplicationContext());
    }

    @Override
    public void onMediaPause() {

    }

    @Override
    public void onMediaResume() {

    }

    @Override
    public void onMediaSeekTo(long timeMs) {

    }

    @Override
    public long mediaGetStartMs() {
        return 0;
    }

    @Override
    public long mediaGetCurrentMs() {
        return 10;
    }

    @Override
    public void onMediaSetPlaybackParams(PlaybackParams playbackParams) {

    }

    @Nullable
    @Override
    public Session onCreateSession(String inputId) {
        redditTvSession = new RedditTvSession(this);
        simpleSession = redditTvSession;
        return redditTvSession;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "About to set recording");
            TvInputInfo info = new TvInputInfo.Builder(getApplicationContext(), new ComponentName(getApplicationContext(), AbstractTvService.class))
                    .setCanRecord(true)
                    .setTunerCount(1)
                    .build();
            ((TvInputManager) getSystemService(Context.TV_INPUT_SERVICE)).updateTvInputInfo(info);
            Log.d(TAG, "This app can record");
        }
    }

    @Override
    public SimpleSessionImpl getSession() {
        return redditTvSession;
    }

    private class RedditTvSession extends SimpleSessionImpl {
        RedditTvSession(TvInputProvider tvInputProvider) {
            super(tvInputProvider);
        }

        @Override
        public void onTimeShiftPlay(Uri recordedProgramUri) {
            notifyVideoAvailable();
            setOverlayEnabled(true);
            youTubePlayerView.setVideoEventsListener(new AbstractWebPlayer.VideoEventsListener() {
                @Override
                public void onVideoEnded() {
                    // Video ended. That's it.
                }
            });
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    youTubePlayerView.loadVideo(YoutubeUtils.parseVideoId("q0P4SFrjA4Y"));
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private class RedditRecordingSession extends RecordingSession {
        /**
         * Creates a new RecordingSession.
         *
         * @param context The context of the application
         */
        public RedditRecordingSession(Context context) {
            super(context);
            Log.d(TAG, "Recording Session Created");
        }

        @Override
        public void onTune(Uri channelUri) {
            // Right now we only have one channel
            notifyTuned(channelUri);
        }

        @Override
        public void onStartRecording(Uri programUri) {
            // Don't bother with program uri
        }

        @Override
        public void onStopRecording() {
            // Just save a random program
            ContentValues recordedProgram = new ContentValues();
            recordedProgram.put(TvContract.RecordedPrograms.COLUMN_TITLE, "Saved Program");
            recordedProgram.put(TvContract.RecordedPrograms.COLUMN_THUMBNAIL_URI, "http://theawesomer.com/photos/2012/06/140612_youtube_haiku_t.jpg");
            recordedProgram.put(TvContract.RecordedPrograms.COLUMN_RECORDING_DURATION_MILLIS, 1000 * 60);
            recordedProgram.put(TvContract.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS, System.currentTimeMillis());
            recordedProgram.put(TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_DATA, "q0P4SFrjA4Y");
            recordedProgram.put(TvContract.RecordedPrograms.COLUMN_SEARCHABLE, 1);
            notifyRecordingStopped(getContentResolver().insert(TvContract.RecordedPrograms.CONTENT_URI, recordedProgram));

        }

        @Override
        public void onRelease() {

        }
    }
}
