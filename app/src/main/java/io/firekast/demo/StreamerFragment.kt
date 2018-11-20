package io.firekast.demo

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import io.firekast.*
import kotlinx.android.synthetic.main.fragment_streamer.*

var gLatestStream: FKStream? = null

class StreamerFragment : Fragment(), View.OnClickListener, FKStreamer.StreamingCallback {

    private val sTAG = "StreamerFragment"

    private lateinit var cameraFragment: FKCameraFragment
    private lateinit var camera: FKCamera
    private lateinit var streamer: FKStreamer

    private lateinit var streamStateViewHolder: StreamStateViewHolder

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

        streamStateViewHolder = StreamStateViewHolder(blockStreamState)
        streamStateViewHolder.visibility = View.INVISIBLE
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
        stream?.let { textViewStreamId.text = getString(R.string.stream_id, it.id) }
        if (error != null) {
            Toast.makeText(this.context, "Error: $error", Toast.LENGTH_LONG).show()
            return
        }
        streamStateViewHolder.visibility = View.VISIBLE
        streamStateViewHolder.state = FKStream.State.WAITING
        gLatestStream = stream
        isStreaming = true
    }

    override fun onStreamDidBecomeLive(stream: FKStream) {
        streamStateViewHolder.state = FKStream.State.LIVE
    }

    override fun onStreamDidStop(stream: FKStream?, error: FKError?) {
        textViewStreamId.text = ""
        error?.let { Toast.makeText(this.context, "Error: $error", Toast.LENGTH_LONG).show() }
        streamStateViewHolder.visibility = View.GONE
        isLoading = false
        isStreaming = false
    }

    override fun onStreamingUpdateAvailable(p0: Boolean) {
    }

}

class StreamStateViewHolder(val view: ConstraintLayout) {
    val textView: TextView = view.findViewById(R.id.textViewLive)
    val progressBar: ProgressBar = view.findViewById(R.id.progressBarLive)

    var visibility: Int = View.VISIBLE
        set(value) {
            field = value
            view.visibility = value
        }

    var state: FKStream.State? = null
        set(value) {
            field = value
            if (value == FKStream.State.WAITING) {
                progressBar.visibility = View.VISIBLE
                view.setBackgroundColor(Color.GRAY)
            } else if (value == FKStream.State.LIVE) {
                progressBar.visibility = View.GONE
                view.setBackgroundColor(Color.RED)
            }
        }

}