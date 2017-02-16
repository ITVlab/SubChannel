package news.androidtv.subchannel.services;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.google.android.media.tv.companionlibrary.BaseTvInputService;
import com.google.android.media.tv.companionlibrary.TvPlayer;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.model.RecordedProgram;

import news.androidtv.libs.player.AbstractWebPlayer;
import news.androidtv.libs.player.YouTubePlayerView;
import news.androidtv.subchannel.utils.YoutubeUtils;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Nick on 2/16/2017.
 */

public class TifPlaybackService extends BaseTvInputService {
    private static final String TAG = TifPlaybackService.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final long EPG_SYNC_DELAYED_PERIOD_MS = 1000 * 2; // 2 Seconds

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

    class RedditTifService extends BaseTvInputService.Session {
        private YouTubePlayerView mYouTubePlayerView;
        private Context mContext;
        private String mInputId;
        private Uri mChannelUri;

        RedditTifService(Context context, String inputId) {
            super(context, inputId);
            mContext = context;
            mInputId = inputId;
        }

        @Override
        public TvPlayer getTvPlayer() {
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
            notifyVideoAvailable();
            setOverlayViewEnabled(true);
            mYouTubePlayerView.setVideoEventsListener(new AbstractWebPlayer.VideoEventsListener() {
                @Override
                public void onVideoEnded() {
                    onTune(mChannelUri); // Need to reload
                }
            });
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
            mYouTubePlayerView.setVideoEventsListener(new AbstractWebPlayer.VideoEventsListener() {
                @Override
                public void onVideoEnded() {
                    // Video ended. That's it.
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

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Nullable
        @Override
        public TvInputService.RecordingSession onCreateRecordingSession(String inputId) {
            return new RedditRecordingSession(mContext, inputId);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        private class RedditRecordingSession extends BaseTvInputService.RecordingSession {
            private String mInputId;
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
                        channelToRecord.getId(), mRecordingStarted, mRecordingStopped),
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
}
