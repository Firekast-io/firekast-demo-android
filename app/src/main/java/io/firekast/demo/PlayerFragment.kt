package io.firekast.demo

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.firekast.FKError
import io.firekast.FKPlayerView
import io.firekast.FKStream
import kotlinx.android.synthetic.main.fragment_player.*

class PlayerFragment : Fragment(), View.OnClickListener, FKPlayerView.Callback {

    var isLoading: Boolean by observing(false, didSet = {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        button.isEnabled = !isLoading
    })

    var isPlaying: Boolean by observing(false, didSet = {
        button.setText(if (isPlaying) R.string.stop_playing else R.string.start_playing)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener(this)
        editText.setText(if (gLatestStream != null) gLatestStream!!.id else "")
        videoView.setPlayerListener(this)
        isLoading = false
        isPlaying = false
    }

    override fun onClick(p0: View?) {
        if (isPlaying) {
            videoView.stop()
            isLoading = false
            isPlaying = false
            return
        }
        val streamId = editText.text.trim()
        if (streamId.isEmpty()) {
            editText.error = "Fill a stream ID"
            return
        }
        isLoading = true
        videoView.play(streamId.toString())
    }

    override fun onPlayerWillPlay(stream: FKStream?, error: FKError?) {
        isLoading = false
        if (error != null) {
            isPlaying = false
            Toast.makeText(this.context, "Error: $error", Toast.LENGTH_SHORT).show()
            return
        }
        isPlaying = true
    }
}