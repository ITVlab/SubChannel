package news.androidtv.subchannel.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;

import news.androidtv.subchannel.R;
import news.androidtv.subchannel.services.TifPlaybackService;

/**
 * Created by Nick on 4/29/2017.
 */

public class ProgramInfoActivity extends Activity {
    public static final String EXTRA_TIMEOUT = "timeout";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_info);

        Program p = TifPlaybackService.mCurrentProgram;
        try {
            ((TextView) findViewById(R.id.info_title)).setText(
                    p.getInternalProviderData().get(TifPlaybackService.IPD_KEY_POST_TITLE).toString());
            ((TextView) findViewById(R.id.info_submitted)).setText(
                    p.getInternalProviderData().get(TifPlaybackService.IPD_KEY_POST_BY).toString());
        } catch (InternalProviderData.ParseException e) {
            e.printStackTrace();
        }

        // Display info about this program into layout.

        // Setup automated timeout
        if (getIntent() != null && getIntent().hasExtra(EXTRA_TIMEOUT)) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, getIntent().getIntExtra(EXTRA_TIMEOUT, Integer.MAX_VALUE));
        }

        // Turn into side-panel
        // Sets the size and position of dialog activity.
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.side_panel_height);
        getWindow().setAttributes(layoutParams);
    }
}
