package com.thedancercodes.exoplayerintro

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MainActivity : AppCompatActivity() {

    /* Member Variables */
    // Instance of Exoplayer
    private lateinit var exoPlayer: SimpleExoPlayer

    // Instance of EventLogger
    private lateinit var eventLogger: EventLogger

    // Instance of SimpleExoPlayerView
    lateinit var simpleExoPlayerView: SimpleExoPlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Sets up the Exoplayer for playback
    fun initializeExoPlayer() {

        // A renderer Object decodes media & allows you to play different media formats
        val renderersFactory = DefaultRenderersFactory(this,
                null, //DrmSessionManager
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

        // Provides logic for selecting a track to give to the renderer
        val trackSelector = DefaultTrackSelector()

        // Initialize ExoPlayer
        exoPlayer = ExoPlayerFactory.newSimpleInstance(
                renderersFactory,
                trackSelector
        )

        // Initialize EventLogger
        eventLogger = EventLogger(trackSelector)
        exoPlayer.addListener(eventLogger)
        exoPlayer.setAudioDebugListener(eventLogger)
        exoPlayer.setMetadataOutput(eventLogger)


        // Pass in a Media Source;
        // Use an ExtractorMediaSource as we are playing a mp3 file stored in the assets folder
        val userAgent = Util.getUserAgent(this, "ExoplayerIntro")

        val mediaSource = ExtractorMediaSource(
                Uri.parse("asset:///gawvi-high-note.mp3"),
                DefaultDataSourceFactory(this, userAgent),
                DefaultExtractorsFactory(),
                null, // eventHandler: Handler > To post messages about events
                null) // eventListener: ExtractorMediaSource > To get callbacks about events

        // Preparing Exoplayer
        exoPlayer.prepare(mediaSource)
        exoPlayer.playWhenReady = true

        // Initialize the View
        simpleExoPlayerView = findViewById(R.id.player_view) as SimpleExoPlayerView

        // Pass in Exoplayer > So we can respond to events & control the player
        simpleExoPlayerView.player = exoPlayer
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        // Show Playback controls > controls on any key event
        simpleExoPlayerView.showController()

        // If event was not handled, check whether the player view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchMediaKeyEvent(event)
    }

    // Release Exoplayer when you are done with it to release system resources
    fun releaseExoplayer() = exoPlayer.release()

    override fun onStart() {
        super.onStart()

        // Account for multi-window introduced in nougat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initializeExoPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            initializeExoPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            releaseExoplayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            releaseExoplayer()
        }
    }
}