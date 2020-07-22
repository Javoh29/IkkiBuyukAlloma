package com.rangedroid.sayyidsafo.utils

import  android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.rangedroid.sayyidsafo.App
import java.io.File
import com.rangedroid.sayyidsafo.R
import com.rangedroid.sayyidsafo.ui.activity.MainActivity
import com.rangedroid.sayyidsafo.ui.activity.MainActivity.Companion.listAudios


class AudioPlayerService : MediaBrowserServiceCompat() {

    companion object {
        const val PLAYBACK_CHANNEL_ID = "playback_channel"
        const val PLAYBACK_NOTIFICATION_ID = 1
        const val PENDING_INTENT_REQ_CODE = 100
        const val BINDING_SERVICE = "BINDING_SERVICE"
        const val INDEX = "INDEX"
    }

    private var mMediaSession: MediaSessionCompat? = null
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder
    var mExoPlayer: SimpleExoPlayer? = null
    private var notificationManager: PlayerNotificationManager? = null

    var currentTitle: MutableLiveData<String> = MutableLiveData()
    var isPlaying: MutableLiveData<Boolean> = MutableLiveData()

    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {


    }

    override fun onCreate() {
        super.onCreate()
        val context = this

        mExoPlayer = ExoPlayerFactory.newSimpleInstance(
            this, DefaultRenderersFactory(baseContext)
            , DefaultTrackSelector()
        )

        mExoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == STATE_READY) {
                    Log.d("EXO_COMMAND", "Playin")
                    isPlaying.postValue(true)
                } else if (playWhenReady) {
                    Log.d("EXO_COMMAND", "Buffered")
                    isPlaying.postValue(true)
                } else {
                    updatePlaybackState(PlaybackState.STATE_PAUSED)
                    isPlaying.postValue(false)
                }
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray?,
                trackSelections: TrackSelectionArray?
            ) {
                super.onTracksChanged(trackGroups, trackSelections)
                try {
                    listAudios[mExoPlayer?.currentWindowIndex ?: 0].let {
                        currentTitle.postValue(it)
                    }

                } catch (ex: IndexOutOfBoundsException) {

                }
            }
        })

        setupNotification(context)

        initializeExtractor()
        initializeAttributes()

        mMediaSession = MediaSessionCompat(context, "tag for debugging").apply {
            // Enable callbacks from MediaButtons and TransportControls

            mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(mStateBuilder.build())

            // methods that handle callbacks from a media controller
            setCallback(mMediaSessionCallback)
            // Set the session's token so that client activities can communicate with it
            setSessionToken(sessionToken)
            notificationManager?.setMediaSessionToken(sessionToken)
            isActive = true
        }
    }

    private fun setupNotification(context: Context) {

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            PLAYBACK_CHANNEL_ID,
            R.string.app_name,
            PLAYBACK_NOTIFICATION_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = Intent(context, MainActivity::class.java)
                    return PendingIntent.getActivity(
                        context,
                        PENDING_INTENT_REQ_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

                override fun getCurrentContentText(player: Player): String? {
                    return null
                }

                override fun getCurrentContentTitle(player: Player): String {

                    return listAudios[player.currentWindowIndex]
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    return context.getDrawable(R.drawable.splashlogo)?.toBitmap()
                }
            }
        )

        notificationManager?.setNotificationListener(object :
            PlayerNotificationManager.NotificationListener {
            override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
                startForeground(notificationId, notification)

            }

            override fun onNotificationCancelled(notificationId: Int) {
                stopSelf()
            }

        })
        notificationManager?.setPlayer(mExoPlayer)
    }

    private var mAttrs: AudioAttributes? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    private fun updatePlaybackState(state: Int) {
        // You need to change the state because the action taken in the controller depends on the state !!!
        mMediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                state // this state is handled in the media controller
                , 0L
                , 1.0f // Speed playing
            ).build()
        )
    }


    private fun initializeAttributes() {
        mAttrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
    }

    private lateinit var mExtractorFactory: ExtractorMediaSource.Factory

    private fun initializeExtractor() {
        val userAgent = Util.getUserAgent(baseContext, "Sayyid Safo")
        mExtractorFactory = ExtractorMediaSource.Factory(
            DefaultDataSourceFactory(
                this, userAgent
            )
        ).setExtractorsFactory(DefaultExtractorsFactory())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BAG", "onStop")
        stop()
    }

    private fun stop() {
        mMediaSession?.release()
        notificationManager?.setPlayer(null)
        mExoPlayer?.release()
        mExoPlayer = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && listAudios.isNotEmpty()){
            handleIntent(index = intent.getIntExtra(INDEX, 0))
        }
        return Service.START_STICKY
    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): AudioPlayerService {
            return this@AudioPlayerService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        val isFromService = intent?.getBooleanExtra(BINDING_SERVICE, false)
        if (isFromService == true) {
            return binder
        }
        return super.onBind(intent)
    }

    fun handleIntent(index: Int) {
        val concatMS = ConcatenatingMediaSource()
        listAudios.forEach {
            val ms = mExtractorFactory
                .createMediaSource(Uri.fromFile(File(App.DIR_PATH + it)))
            concatMS.addMediaSource(ms)
        }
        mExoPlayer?.prepare(concatMS)
        mExoPlayer?.playWhenReady = true
        mExoPlayer?.seekToDefaultPosition(index)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(getString(R.string.root_id), null)
    }
}