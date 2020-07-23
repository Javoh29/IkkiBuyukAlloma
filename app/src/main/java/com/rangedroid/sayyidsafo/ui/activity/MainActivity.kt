package com.rangedroid.sayyidsafo.ui.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.ui.BuildConfig
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.util.Util
import com.rangedroid.sayyidsafo.App
import com.rangedroid.sayyidsafo.App.Companion.binder
import com.rangedroid.sayyidsafo.App.Companion.connection
import com.rangedroid.sayyidsafo.R
import com.rangedroid.sayyidsafo.data.provider.UnitProvider
import com.rangedroid.sayyidsafo.ui.adapter.SectionsPagerAdapter
import com.rangedroid.sayyidsafo.utils.AboutUsDialog
import com.rangedroid.sayyidsafo.utils.AudioPlayerService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.auto_mode.*
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import org.kodein.di.android.kodein
import java.io.File

class MainActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val unitProvider: UnitProvider by instance<UnitProvider>()
    private var mBound: Boolean = false
    private var isStart: Boolean = true
    private lateinit var playerView: PlayerControlView
    private lateinit var tvTitle: TextView
    private lateinit var audioTitle: TextView
    private lateinit var playButton: ImageView
    private lateinit var replayButton: ImageView
    private lateinit var forwardButton: ImageView

    companion object {
        var listAudios: ArrayList<String> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val sectionsPagerAdapter =
            SectionsPagerAdapter(
                this,
                supportFragmentManager
            )
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        playerView = findViewById(R.id.playerview)
        tvTitle = findViewById(R.id.tv_title)
        audioTitle = findViewById(R.id.audio_title)
        playButton = findViewById(R.id.play_button)
        replayButton = findViewById(R.id.replay)
        forwardButton = findViewById(R.id.forward)

        requestPermissions()
        setConnect()
        onClickListener()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
        App.DIR_PATH = getExternalFilesDir(null)?.path ?: ""
        App.DIR_PATH += "/"
    }

    private fun setConnect(){
        listAudios.clear()
        File(App.DIR_PATH).walkTopDown().forEach { file ->
            if (file.name.endsWith(".mp3")){
                listAudios.add(file.name)
            }
        }

        Log.d("BAG", unitProvider.getSavedAudio())
        if (unitProvider.getSavedAudio() != "not"){
            listAudios.forEachIndexed { i, it ->
                if (it == unitProvider.getSavedAudio()){
                    val intent = Intent(this, AudioPlayerService::class.java)
                    intent.putExtra(AudioPlayerService.INDEX, i)
                    intent.putExtra(AudioPlayerService.BINDING_SERVICE, true)
                    bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
                    Util.startForegroundService(this, intent)
                }
            }
        }

        connection = object : ServiceConnection{
            override fun onServiceDisconnected(p0: ComponentName?) {
                mBound = false
            }

            override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
                binder = service as AudioPlayerService.LocalBinder
                playerView.player = binder?.getService()?.mExoPlayer
                if (isStart){
                    playerView.player.let {
                        it.playWhenReady = !it.playWhenReady
                    }
                    isStart = false
                }
                mBound = true
                binder?.getService()?.currentTitle?.observe(this@MainActivity, Observer {
                tvTitle.text = it.substring(0, it.length-3)
                audioTitle.text = it.substring(0, it.length-3)
            })
                binder?.getService()?.isPlaying?.observe(this@MainActivity, Observer {
                if (it) {
                    playButton.setImageResource(R.drawable.ic_pause_circled)
                } else {
                    playButton.setImageResource(R.drawable.ic_play_circled)
                }
            })
            }

        }

        if (binder != null){
            playerView.player = binder?.getService()?.mExoPlayer
            binder?.getService()?.currentTitle?.observe(this@MainActivity, Observer {
                Log.d("BAG", it)
                tvTitle.text = it.substring(0, it.length-3)
                audioTitle.text = it.subSequence(0, it.length-3)
            })
            binder?.getService()?.isPlaying?.observe(this@MainActivity, Observer {
                if (it) {
                    playButton.setImageResource(R.drawable.ic_pause_circled)
                } else {
                    playButton.setImageResource(R.drawable.ic_play_circled)
                }
            })
        }
    }

    private fun onClickListener(){
        forwardButton.setOnClickListener {
            binder?.getService()?.mExoPlayer?.let {
                it.seekTo(it.currentPosition + 30_000L)
            }
        }

        replayButton.setOnClickListener {
            binder?.getService()?.mExoPlayer?.let {
                it.seekTo(it.currentPosition - 30_000L)
            }
        }

        playButton.setOnClickListener {
            binder?.getService()?.mExoPlayer?.let {
                it.playWhenReady = !it.playWhenReady
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.btn_rule -> {
                driving_mode_container.visibility = View.VISIBLE
            }
            R.id.tv_share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                var message = getString(R.string.about_us_text)
                message =
                    message + "\n" + getString(R.string.app_url) + BuildConfig.APPLICATION_ID + "\n\n"
                intent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(intent, "Улашиш"))
            }
            R.id.tv_telegram -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.telegram_url))
                startActivity(intent)
            }
            R.id.tv_about -> {
                val dialog = AboutUsDialog()
                dialog.show(supportFragmentManager, "ABOUT_US")
            }
            R.id.tv_other_app -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.our_app))
                startActivity(intent)
            }
            R.id.btn_question -> {
                startActivity(Intent(this@MainActivity, InfoActivity::class.java))
            }
        }
        return true
    }

    override fun onStop() {
        unitProvider.setSavedAudio(
            audio = tvTitle.text.toString()
        )
        super.onStop()
        if (mBound) {
            try {
                unbindService(connection!!)
                mBound = false
            }catch (e: Exception){

            }

        }
    }

    override fun onBackPressed() {
        if (driving_mode_container.visibility == View.VISIBLE){
            driving_mode_container.visibility = View.GONE
        }else{
            super.onBackPressed()
        }
    }
}