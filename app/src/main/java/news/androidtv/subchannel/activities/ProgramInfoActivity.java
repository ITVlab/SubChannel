package news.androidtv.subchannel.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.media.tv.companionlibrary.model.Program;

import news.androidtv.subchannel.services.TifPlaybackService;

/**
 * Created by Nick on 4/29/2017.
 */

public class ProgramInfoActivity extends AppCompatActivity {
    public static final String EXTRA_TIMEOUT = "timeout";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Program p = TifPlaybackService.currentProgram;

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
    }
}
