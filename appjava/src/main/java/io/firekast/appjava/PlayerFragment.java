package io.firekast.appjava;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.firekast.FKError;
import io.firekast.FKPlayer;
import io.firekast.FKPlayerView;
import io.firekast.FKStream;

/**
 * Created by Francois Rouault on 01/11/2016.
 */
public class PlayerFragment extends Fragment implements FKPlayer.Callback, View.OnClickListener {

    private static final String TAG = PlayerFragment.class.getSimpleName();

    private EditText mEditTextUrl;
    private FKPlayerView mPlayerView;
    private TextView mTextViewState;

    private ProgressDialog mLoading;
    private FKPlayer mPlayer;

    public static PlayerFragment newInstance() {
        return new PlayerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mEditTextUrl = view.findViewById(R.id.editText_url);
        mPlayerView = view.findViewById(R.id.playerView);
        mTextViewState = view.findViewById(R.id.textViewState);
        view.findViewById(R.id.button_get_time).setOnClickListener(this);
        view.findViewById(R.id.button_play).setOnClickListener(this);
        view.findViewById(R.id.pauseButton).setOnClickListener(this);
        view.findViewById(R.id.resumeButton).setOnClickListener(this);

        mPlayer = mPlayerView.getPlayer();
        mPlayer.setCallback(this);
        mPlayer.setShowPlaybackControls(true);
        mEditTextUrl.setHint("Stream id (ex. H1N_D8eex)");
        mEditTextUrl.setText(App.latestStreamId);
        mTextViewState.setText("" + mPlayer.getState());
    }

    @Override
    public void onPause() {
        super.onPause();
        mPlayer.release();
    }

    public void showLoading() {
        mLoading = ProgressDialog.show(getContext(), null, "Loading stream...");
    }

    public void hideLoading() {
        if (mLoading == null) {
            return;
        }
        mLoading.dismiss();
    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d:%02d", h, m, s);
    }

    // ------
    // ON CLICK
    // ------

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_play:
                onClickPlay();
                break;
            case R.id.button_get_time:
                onClickGetTime();
                break;
            case R.id.resumeButton:
                mPlayer.resume();
                break;
            case R.id.pauseButton:
                mPlayer.pause();
                break;

        }
    }

    void onClickGetTime() {
        long position = mPlayer.getCurrentPosition() / 1000L;
        String toHMmSs = convertSecondsToHMmSs(position);
        Toast.makeText(this.getContext(), "Position: " + toHMmSs, Toast.LENGTH_SHORT).show();
    }

    void onClickPlay() {
        String streamId = mEditTextUrl.getText().toString().trim();
        FKStream current = mPlayer.getCurrentStream();
        showLoading();
        if (TextUtils.isEmpty(streamId)) {
            mEditTextUrl.setError("Need a StreamID");
        } else if (current == null || !current.getId().equals(streamId)) {
            FKStream stream = FKStream.newEmptyInstance(streamId);
            mPlayer.play(stream);
        } else {
            mPlayer.play(current);
        }
    }

    // --------
    // FKPLayer Callback
    // --------

    @Override
    public void onPlayerWillPlay(@NonNull FKPlayer player, @NonNull FKStream stream, @Nullable FKError error) {
        Log.v(TAG, "onPlayerWillPlay: stream " + stream + " unless error: " + error);
        if (error != null) {
            Toast.makeText(getContext(), "FKError: " + error, Toast.LENGTH_LONG).show();
        }
        hideLoading();
    }

    @Override
    public void onPlayerStateChanged(@NonNull FKPlayer player, FKPlayer.State state) {
        Log.v(TAG, "onPlayerStateChanged: " + state);
        mTextViewState.setText("" + mPlayer.getState());
    }

}
