package io.firekast.demo

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.firekast.*
import kotlinx.android.synthetic.main.fragment_streamer.*

class StreamerFragment : Fragment(), View.OnClickListener, FKStreamer.StreamingCallback {

    private val sTAG = "StreamerFragment"

    private lateinit var cameraFragment: FKCameraFragment
    private lateinit var camera: FKCamera
    private lateinit var streamer: FKStreamer

    var isLoading: Boolean by observing(false, didSet = {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        button.isEnabled = !isLoading
    })

    var isStreaming: Boolean by observing(false, didSet = {
        button.setText(if (isStreaming) R.string.stop_streaming else R.string.start_streaming)
    })
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_streamer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraFragment = FKCameraFragment.Builder()
                .setCameraPosition(FKCamera.Position.FRONT)
                .build()
        cameraFragment.getCameraAsync { camera, streamer, error ->
            if (error != null) {
                Toast.makeText(view.context, "Error: $error", Toast.LENGTH_LONG).show()
                button.isEnabled = false
                return@getCameraAsync
            }
            this.camera = camera!!
            this.streamer = streamer!!
        }

        childFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, cameraFragment)
                .commit()

        button.setOnClickListener(this)

        isStreaming = false
        isLoading = false
    }

    override fun onClick(p0: View?) {
        if (streamer.isStreaming) {
            streamer.stopStreaming()
            isStreaming = false
        } else {
            isLoading = true
            streamer.requestStream { stream: FKStream?, error: FKError? ->
                if (error != null) {
                    Toast.makeText(this.context, "Error: $error", Toast.LENGTH_LONG).show()
                    isLoading = false
                    return@requestStream
                }
                streamer.startStreaming(stream!!, this)
            }
        }
    }

    override fun onSteamWillStartUnless(stream: FKStream?, error: FKError?) {
        isLoading = false
        if (error != null) {
            Toast.makeText(this.context, "Error: $error", Toast.LENGTH_LONG).show()
            return
        }
        isStreaming = true
    }

    override fun onStreamDidStop(stream: FKStream?, error: FKError?) {
        Toast.makeText(this.context, "Error: $error", Toast.LENGTH_LONG).show()
        isLoading = false
        isStreaming = false
    }

    override fun onStreamingUpdateAvailable(p0: Boolean) {
    }

}