package news.androidtv.subchannel.services;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.telecom.Call;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.google.android.media.tv.companionlibrary.BaseTvInputService;
import com.google.android.media.tv.companionlibrary.TvPlayer;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.model.RecordedProgram;

import news.androidtv.libs.player.YouTubePlayerView;
import news.androidtv.subchannel.utils.YoutubeUtils;

/**
 * Created by Nick on 2/16/2017.
 */

public class TifPlaybackService extends BaseTvInputService {
    private static final String TAG = TifPlaybackService.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final long EPG_SYNC_DELAYED_PERIOD_MS = 1000 * 2; // 2 Seconds

    public static final String IPD_KEY_SUBREDDIT = "subreddit_name";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "About to set recording");
            TvInputInfo info = new TvInputInfo.Builder(getApplicationContext(),
                    new ComponentName(getApplicationContext(), TifPlaybackService.class))
                    .setCanRecord(true)
                    .setTunerCount(1) // This could in theory be any number.
                    .build();
            ((TvInputManager) getSystemService(Context.TV_INPUT_SERVICE)).updateTvInputInfo(info);
            Log.d(TAG, "This app can record");
        }
    }

    @Nullable
    @Override
    public TvInputService.Session onCreateSession(String s) {
        return new RedditTifService(this, s);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public TvInputService.RecordingSession onCreateRecordingSession(String inputId) {
        return new RedditRecordingSession(this, inputId);
    }

    class RedditTifService extends BaseTvInputService.Session {
        private YouTubePlayerView mYouTubePlayerView;
        private Context mContext;
        private String mInputId;
        private Uri mChannelUri;

        RedditTifService(Context context, String inputId) {
            super(context, inputId);
            mContext = context;
            mInputId = inputId;
            onCreateOverlayView();
            TvInputManager manager = (TvInputManager) getSystemService(TV_INPUT_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
            }
        }

        @Override
        public TvPlayer getTvPlayer() {
            mYouTubePlayerView.setVolume(0.95f);
            return mYouTubePlayerView;
        }

        @Override
        public boolean onSetSurface(Surface surface) {
            return true;
        }

        @Override
        public boolean onTune(Uri channelUri) {
            mChannelUri = channelUri;
            return super.onTune(channelUri);
        }

        @Override
        public boolean onPlayProgram(final Program program, long startPosMs) {
            if (program == null) {
                notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
                requestEpgSync(mChannelUri);
                return false;
            }
            notifyVideoAvailable();
            setOverlayViewEnabled(true);
            final TvPlayer.Callback[] callback = new TvPlayer.Callback[1];
            callback[0] = new TvPlayer.Callback() {
                @Override
                public void onCompleted() {
                    super.onCompleted();
                    try {
                        Log.i(TAG, "Program ended, obtain the next");
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mYouTubePlayerView.unregisterCallback(callback[0]);
                                onPlayProgram(getNextProgram(mChannelUri, program), 0);
                            }
                        });
                    } catch (IllegalStateException e) {
                        // Handler (android.os.Handler) {30477c7c} sending message to a Handler on a dead thread
                        // Restart the thread
                        TifPlaybackService.this.onCreate();
                    }
                }

                @Override
                public void onStarted() {
                    super.onStarted();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mYouTubePlayerView.setVolume(0.95f);
                        }
                    });
                }
            };
            mYouTubePlayerView.registerCallback(callback[0]);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String videoId = YoutubeUtils.parseVideoId(program.getInternalProviderData()
                            .getVideoUrl());
                    mYouTubePlayerView.loadVideo(videoId);
                }
            });
            return true;
        }

        @Override
        public boolean onPlayRecordedProgram(final RecordedProgram recordedProgram) {
            notifyVideoAvailable();
            setOverlayViewEnabled(false);
            setOverlayViewEnabled(true);
            onCreateOverlayView();
            mYouTubePlayerView.registerCallback(new TvPlayer.Callback() {
                @Override
                public void onStarted() {
                    super.onStarted();
                    mYouTubePlayerView.setVolume(0.99f);
                }
            });
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String videoId = YoutubeUtils.parseVideoId(recordedProgram.
                            getInternalProviderData().getVideoUrl());
                    mYouTubePlayerView.loadVideo(videoId);
                }
            });
            return true;
        }

        @Override
        public void onSetCaptionEnabled(boolean b) {

        }

        @Override
        public void onTimeShiftPlay(Uri recordedProgramUri) {

        }

        @Override
        public View onCreateOverlayView() {
            if (mYouTubePlayerView == null) {
                mYouTubePlayerView = new YouTubePlayerView(getApplicationContext());
            }
            return mYouTubePlayerView;
        }

        private void requestEpgSync(final Uri channelUri) {
            SubredditJobService.requestImmediateSync1(TifPlaybackService.this, mInputId,
                    SubredditJobService.DEFAULT_IMMEDIATE_EPG_DURATION_MILLIS,
                    new ComponentName(TifPlaybackService.this, SubredditJobService.class));
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    onTune(channelUri);
                }
            }, EPG_SYNC_DELAYED_PERIOD_MS);
        }

        private Program getNextProgram(Uri channelUri, Program currentProgram) {
            ContentResolver contentResolver = getContentResolver();
            Cursor query = contentResolver.query(TvContract.buildProgramsUriForChannel(channelUri),
                    Program.PROJECTION, null, null, null);
            if (query == null) {
                return null;
            }
            if (DEBUG) {
                Log.d(TAG, "Program " + currentProgram.getTitle() + " ended");
            }
            while (query.moveToNext()) {
                Program program = Program.fromCursor(query);
                if (DEBUG) {
                    Log.i(TAG, "* " + program.getTitle() + "==" + currentProgram.getTitle() + ", " + query.getPosition() + "/" + query.getCount());
                }
                if (program.equals(currentProgram)) {
                    // Get next
                    while (program.equals(currentProgram) && query.moveToNext()) {
                        program = Program.fromCursor(query);
                        if (DEBUG) {
                            Log.i(TAG, program.getTitle() + "==" + currentProgram.getTitle() + ", " + query.getPosition() + "/" + query.getCount());
                        }
                    }
                    if (!query.isLast()) {
                        if (query.getPosition() == query.getCount()) {
                            query.moveToPrevious();
                        }
                        Program next = Program.fromCursor(query);
                        query.close();
                        if (DEBUG) {
                            Log.i(TAG, "Selected next program " + next.getTitle());
                        }
                        return next;
                    } else {
                        // Get first
                        query.moveToFirst();
                        Program next = Program.fromCursor(query);
                        query.close();
                        if (DEBUG) {
                            Log.i(TAG, "Selected first program " + next.getTitle());
                        }
                        return next;
                    }
                }
            }
            query.close();
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private class RedditRecordingSession extends BaseTvInputService.RecordingSession {
        private String mInputId;
        private Uri mChannelUri;
        private long mRecordingStarted;
        private long mRecordingStopped;

        public RedditRecordingSession(Context context, String inputId) {
            super(context, inputId);
            mInputId = inputId;
            Log.d(TAG, "Recording Session Created");
        }

        @Override
        public void onTune(Uri channelUri) {
            // Right now we only have one channel
            notifyTuned(channelUri);
            mChannelUri = channelUri;
        }

        @Override
        public void onStartRecording(Uri programUri) {
            // Don't bother with program uri
            Log.d(TAG, "Recording started");
            mRecordingStarted = System.currentTimeMillis();
        }

        @Override
        public void onStopRecording(Program programToRecord) {
            RecordedProgram recordedProgram = new RecordedProgram.Builder(programToRecord)
                    .setRecordingDataBytes(1024)
                    .setRecordingDurationMillis(1000 * 60) // FIXME need to get durations
                    .setInputId(mInputId)
                    .build();
            notifyRecordingStopped(recordedProgram);
        }

        @Override
        public void onStopRecordingChannel(Channel channelToRecord) {
            // Need to get the program right now
            ContentResolver contentResolver = getContentResolver();
            mRecordingStopped = System.currentTimeMillis();
            Cursor cursor = contentResolver.query(TvContract.buildProgramsUriForChannel(
                    Long.parseLong(mChannelUri.getLastPathSegment()),
                    mRecordingStarted, mRecordingStopped),
                    Program.PROJECTION, null, null, null);
            if (cursor != null) {
                // Obtain first program
                Program program = Program.fromCursor(cursor);
                onStopRecording(program);
            } else {
                notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
            }
        }

        @Override
        public void onRelease() {

        }
    }
}
