package com.rangedroid.sayyidsafo.ui.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.exoplayer2.ui.PlayerControlView
import com.rangedroid.sayyidsafo.App
import com.rangedroid.sayyidsafo.App.Companion.binder
import com.rangedroid.sayyidsafo.R
import com.rangedroid.sayyidsafo.ui.adapter.SectionsPagerAdapter
import com.rangedroid.sayyidsafo.utils.AudioPlayerService
import java.io.File

class MainActivity : AppCompatActivity() {

    private var mService: AudioPlayerService? = null
    private var mBound: Boolean = false
    private lateinit var playerView: PlayerControlView

    companion object {
        var listAudios: ArrayList<String> = ArrayList()
        var connection: ServiceConnection? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_main)
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
        requestPermissions()
        setConnect()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
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
        connection = object : ServiceConnection{
            override fun onServiceDisconnected(p0: ComponentName?) {
                mBound = false
            }

            override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
                binder = service as AudioPlayerService.LocalBinder
                mService = binder?.getService()
                playerView.player = mService?.mExoPlayer
                mBound = true
//            mService?.currentTitle?.observe(this@MainActivity, Observer {
//                binding?.audioTitle?.text = it
//            })
//            mService?.isPlaying?.observe(this@MainActivity, Observer {
//                if (it) {
//                    binding?.playButton?.setImageResource(R.drawable.ic_pause_circled)
//                } else {
//                    binding?.playButton?.setImageResource(R.drawable.ic_play_circled)
//                }
//            })
            }

        }
        if (binder != null){
            playerView.player = binder?.getService()?.mExoPlayer
        }
    }

    override fun onStop() {
        super.onStop()
        if (mBound) {
            unbindService(connection!!)
            mBound = false
        }
    }
}