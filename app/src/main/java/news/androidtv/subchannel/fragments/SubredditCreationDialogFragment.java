package news.androidtv.subchannel.fragments;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import news.androidtv.subchannel.R;
import news.androidtv.subchannel.utils.SubchannelSettingsManager;

/**
 * Created by Nick on 2/20/2017.
 */
public class SubredditCreationDialogFragment extends DialogFragment {
    private View mDialogView;
    private Callback mCallback;

    public SubredditCreationDialogFragment() {}

    @SuppressLint("ValidFragment")
    public SubredditCreationDialogFragment(Callback callback) {
        mCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
             Bundle savedInstanceState) {
        mDialogView = inflater.inflate(R.layout.dialog_add, null);
        mDialogView.findViewById(R.id.edittext_name).requestFocus();
        mDialogView.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubchannelSettingsManager settingsManager =
                        new SubchannelSettingsManager(getActivity());
                settingsManager.addSubreddit(((EditText) mDialogView
                        .findViewById(R.id.edittext_name)).getText().toString());
                dismiss();
                if (mCallback != null) {
                    mCallback.onDismiss();
                }
            }
        });
        return mDialogView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle("Add a Subreddit channel");
        getDialog().getWindow().setLayout(900, 400);
    }

    public interface Callback {
        void onDismiss();
    }
}
