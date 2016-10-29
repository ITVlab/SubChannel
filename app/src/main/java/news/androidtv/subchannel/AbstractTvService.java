package news.androidtv.subchannel;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.View;

import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.service.TvInputProvider;
import com.felkertech.channelsurfer.sync.SyncAdapter;
import com.felkertech.channelsurfer.utils.TvContractUtils;
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

public class AbstractTvService extends TvInputProvider {
    private List<Submission> submissions;
    private YouTubePlayerView youTubePlayerView;

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
        Program program = getProgramRightNow(channel);
        setOverlayEnabled(true);
        youTubePlayerView.setVideoEventsListener(new AbstractWebPlayer.VideoEventsListener() {
            @Override
            public void onVideoEnded() {
                onTune(channel);
            }
        });
        youTubePlayerView.loadVideo(YoutubeUtils.parseVideoId(program.getInternalProviderData()));
        return true;
    }
}
