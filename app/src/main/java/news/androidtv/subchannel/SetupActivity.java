package news.androidtv.subchannel;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Nick on 10/28/2016.
 */

public class SetupActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getActionBar().hide();
    }
}
