package io.firekast.demo

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.firekast.FKError
import io.firekast.FKPlayer
import io.firekast.FKStream
import kotlinx.android.synthetic.main.fragment_player.*

class PlayerFragment : Fragment(), FKPlayer.Callback {

    private lateinit var player: FKPlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editText.setText(gLatestStream?.id)
        progressBar.visibility = View.GONE
        buttonPlay.setOnClickListener(onClickPlayButton)
        buttonPause.setOnClickListener(onClickPauseButton)
        buttonResume.setOnClickListener(onClickResumeButton)

        player = playerView.player
        player.setCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private val onClickPlayButton = View.OnClickListener {
        progressBar.visibility = View.VISIBLE
        val streamId = editText.text.toString().trim()
        val current = player.currentStream
        if (TextUtils.isEmpty(streamId)) {
            editText.error = "StreamId needed here."
        } else if (current == null || current.id != streamId) {
            // no stream playing yet or wants to play another stream
            val stream = FKStream.newEmptyInstance(streamId)
            player.play(stream)
        } else {
            // restart the stream from the beginning
            player.play(current)
        }
    }

    private val onClickPauseButton = View.OnClickListener {
        player.pause()
    }

    private val onClickResumeButton = View.OnClickListener {
        player.resume()
    }

    // -----
    // FKPlayerCallback
    // -----

    override fun onPlayerWillPlay(stream: FKStream, error: FKError?) {
        progressBar.visibility = View.GONE
        error?.let {
            Toast.makeText(this.context, "Error: $it", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPlayerStateChanged(state: FKPlayer.State) {
        textViewState.text = "$state"
    }
}