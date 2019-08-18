package io.firekast.demo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import io.firekast.*
import kotlinx.android.synthetic.main.fragment_streamer.*
import java.util.*

class StreamerFragment : Fragment(), View.OnClickListener, FKStreamer.StreamingCallback {

    private val sTAG = "StreamerFragment"

    private lateinit var cameraFragment: FKCameraFragment
    private lateinit var camera: FKCamera
    private lateinit var streamer: FKStreamer

    private lateinit var streamStateViewHolder: StreamStateViewHolder

    private val facebookLoginCallbackManager = CallbackManager.Factory.create()

    var isLoading: Boolean by observing(false, didSet = {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        buttonResume.isEnabled = !isLoading
    })

    var isStreaming: Boolean by observing(false, didSet = {
        buttonResume.setText(if (isStreaming) R.string.stop_streaming else R.string.start_streaming)
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
                buttonResume.isEnabled = false
                return@getCameraAsync
            }
            this.camera = camera!!
            this.streamer = streamer!!
        }

        childFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, cameraFragment)
                .commit()

        buttonResume.setOnClickListener(this)

        streamStateViewHolder = StreamStateViewHolder(blockStreamState)
        streamStateViewHolder.visibility = View.INVISIBLE
        isStreaming = false
        isLoading = false

        /*
        FACEBOOK: https://developers.facebook.com/docs/live-video-api/
        Publishing on a User
            - publish_video
        Publishing on a Page
            - publish_pages
            - manage_pages
        Publishing on a Group
            - publish_video
            - publish_to_groups
         */
        facebookRestream.setOnClickListener {
            facebookProgressBar.visibility = View.VISIBLE
            facebookRestream.isEnabled = false
            LoginManager.getInstance().registerCallback(facebookLoginCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            "/me",
                            null,
                            HttpMethod.GET,
                            GraphRequest.Callback {
                                val id = it.jsonObject.optString("id")
                                if (id.isNullOrEmpty()) {
                                    restoreUI()
                                } else {
                                    GraphRequest(
                                            AccessToken.getCurrentAccessToken(),
                                            "/$id/live_videos",
                                            null,
                                            HttpMethod.POST,
                                            GraphRequest.Callback {
                                                facebookRtmpsUrl.text = it.jsonObject.optString("secure_stream_url")
                                                restoreUI()
                                            }
                                    ).executeAsync()
                                }
                            }
                    ).executeAsync()
                }

                override fun onCancel() {
                    restoreUI()
                }

                override fun onError(error: FacebookException?) {
                    restoreUI()
                }

                private fun restoreUI() {
                    facebookProgressBar.visibility = View.GONE
                    facebookRestream.isEnabled = true
                }
            })
            LoginManager.getInstance().logIn(this, Arrays.asList("publish_video"))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookLoginCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(p0: View?) {
        if (streamer.isStreaming) {
            streamer.stopStreaming()
            isStreaming = false
        } else {
            isLoading = true
            val outputs = listOf(facebookRtmpsUrl.text)
                    .filterNot { TextUtils.isEmpty(it) }
                    .map { it.toString() }
            streamer.createStream(outputs) { stream: FKStream?, error: FKError? ->
                if (error != null) {
                    Toast.makeText(this.context, "Error: $error", Toast.LENGTH_LONG).show()
                    isLoading = false
                    return@createStream
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

    override fun onStreamHealthDidUpdate(freezing: Boolean, health: Float) {
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