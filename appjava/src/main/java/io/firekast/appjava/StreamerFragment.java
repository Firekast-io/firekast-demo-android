package io.firekast.appjava;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.firekast.FKCamera;
import io.firekast.FKCameraFragment;
import io.firekast.FKError;
import io.firekast.FKStream;
import io.firekast.FKStreamer;

public class StreamerFragment extends Fragment implements View.OnClickListener, FKStreamer.StreamingCallback, FKCameraFragment.OnCameraReadyCallback {

    private Button mButton;
    private TextView mTextViewLive;
    private ProgressBar mProgressBarCreateStream;
    private ProgressBar mProgressBarLive;
    private LinearLayout mLayoutLive;

    @NonNull
    private FKCamera mCamera;
    @NonNull
    private FKStreamer mStreamer;

    /**
     * The current stream
     */
    @Nullable
    private FKStream mStream;
    private boolean mIsCreatingStream = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streamer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FKCameraFragment cameraFragment = new FKCameraFragment();
        cameraFragment.getCameraAsync(this);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.camera_container, cameraFragment)
                .commit();

        mButton = view.findViewById(R.id.button);
        mButton.setOnClickListener(this);
        mButton.setEnabled(false);
        mTextViewLive = view.findViewById(R.id.textViewLive);
        mLayoutLive = view.findViewById(R.id.layoutLive);
        mProgressBarLive = view.findViewById(R.id.progressBarLive);
        mProgressBarLive.setVisibility(View.GONE);
        mProgressBarCreateStream = view.findViewById(R.id.progressBarCreateStream);
        mProgressBarCreateStream.setVisibility(View.GONE);
    }

    private void updateUI() {
        mButton.setText(mStreamer.isStreaming() ? R.string.streaming_stop : R.string.streaming_start);
        mButton.setEnabled(!mIsCreatingStream);
        mProgressBarCreateStream.setVisibility(mIsCreatingStream ? View.VISIBLE : View.GONE);
        mLayoutLive.setVisibility(mStreamer.isStreaming() ? View.VISIBLE : View.GONE);
        if (mStream != null) {
            mLayoutLive.setBackgroundColor(mStream.getState() == FKStream.State.LIVE ? Color.RED : Color.GRAY);
            mProgressBarLive.setVisibility(mStream.getState() == FKStream.State.LIVE ? View.GONE : View.VISIBLE);
            mTextViewLive.setText(mStream.getState().toString());
        }
    }

    @Override
    public void onClick(View v) {
        if (mStreamer.isStreaming()) {
            handleStop();
        } else {
            handleStart();
        }
    }

    private void handleStart() {
        mIsCreatingStream = true;
        updateUI();
        // First request a stream
        mStreamer.createStream(new FKStreamer.CreateStreamCallback() {
            @Override
            public void done(@Nullable FKStream stream, @Nullable FKError error) {
                mIsCreatingStream = false;
                if (error != null) {
                    new AlertDialog.Builder(StreamerFragment.this.getContext())
                            .setTitle("Create stream error")
                            .setMessage(error.toString())
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                } else {
                    // Then start streaming on that stream
                    mButton.setText(R.string.streaming_stop);
                    mStream = stream;
                    App.latestStreamId = stream.getId();
                    mStreamer.startStreaming(stream, StreamerFragment.this);
                }
                updateUI();
            }
        });
    }

    private void handleStop() {
        mStream = null;
        mStreamer.stopStreaming();
        updateUI();
    }

    @Override
    public void onCameraReady(@Nullable FKCamera camera, @Nullable FKStreamer streamer, @Nullable FKError error) {
        if (error != null) {
            new AlertDialog.Builder(StreamerFragment.this.getContext())
                    .setTitle("Camera error")
                    .setMessage(error.toString())
                    .show();
            return;
        }
        mCamera = camera;
        mStreamer = streamer;
        updateUI();
    }

    @Override
    public void onSteamWillStartUnless(@Nullable FKStream stream, @Nullable FKError error) {
        updateUI();
        if (error != null) {
            new AlertDialog.Builder(StreamerFragment.this.getContext())
                    .setTitle("Streaming not started")
                    .setMessage(error.toString())
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
            return;
        }
    }

    @Override
    public void onStreamDidStop(@Nullable FKStream stream, FKError error) {
        updateUI();
    }

    @Override
    public void onStreamHealthDidUpdate(boolean freezing, float health) {

    }

    @Override
    public void onStreamDidBecomeLive(@NonNull FKStream stream) {
        updateUI();
    }

}
