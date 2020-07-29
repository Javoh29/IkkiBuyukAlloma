package uz.mnsh.buyuklar.ui.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import uz.mnsh.buyuklar.App.Companion.binder
import uz.mnsh.buyuklar.App.Companion.connection
import com.mnsh.sayyidsafo.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.auto_mode.*
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import org.kodein.di.android.kodein
import uz.mnsh.buyuklar.App
import uz.mnsh.buyuklar.data.provider.UnitProvider
import uz.mnsh.buyuklar.ui.adapter.SectionsPagerAdapter
import uz.mnsh.buyuklar.utils.AboutUsDialog
import uz.mnsh.buyuklar.utils.AudioPlayerService
import java.io.File

@Suppress("DEPRECATION")
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
        App.DIR_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        App.DIR_PATH += "/Ikki buyuk alloma/"
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
                playerView.player = binder?.getService()?.mExoPlayer
                if (unitProvider.getSavedTime() != "not"){
                    playerView.player.seekTo(unitProvider.getSavedTime().toLong())
                }
                if (isStart){
                    playerView.player.let {
                        it.playWhenReady = !it.playWhenReady
                    }
                    isStart = false
                }
                mBound = true
                binder?.getService()?.currentTitle?.observe(this@MainActivity, Observer {
                tvTitle.text = it.substring(0, it.length-4)
                audioTitle.text = it.substring(0, it.length-4)
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
                tvTitle.text = it.substring(0, it.length-4)
                audioTitle.text = it.subSequence(0, it.length-4)
            })
            binder?.getService()?.isPlaying?.observe(this@MainActivity, Observer {
                if (it) {
                    playButton.setImageResource(R.drawable.ic_pause_circled)
                } else {
                    playButton.setImageResource(R.drawable.ic_play_circled)
                }
            })
        }

        Log.d("BAG", unitProvider.getSavedAudio())
        if (unitProvider.getSavedAudio().length > 5){
            listAudios.forEachIndexed { i, it ->
                if (it == unitProvider.getSavedAudio()){
                    val intent = Intent(this, AudioPlayerService::class.java)
                    intent.putExtra(AudioPlayerService.INDEX, i)
                    intent.putExtra(AudioPlayerService.BINDING_SERVICE, true)
                    bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
                    Util.startForegroundService(this, intent)
                }
            }
        }else{
            if (listAudios.isNotEmpty()){
                val intent = Intent(this, AudioPlayerService::class.java)
                intent.putExtra(AudioPlayerService.INDEX, 1)
                intent.putExtra(AudioPlayerService.BINDING_SERVICE, true)
                bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
                Util.startForegroundService(this, intent)
            }
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
                playerView.player.playWhenReady = false
                startActivity(Intent(this@MainActivity, InfoActivity::class.java))
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        if (binder?.getService()?.mExoPlayer == null){
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
    }

    override fun onStop() {
        super.onStop()
        if (mBound) {
            try {
                unbindService(connection!!)
                mBound = false
            }catch (e: Exception){

            }

        }
    }

    override fun onDestroy() {
        unitProvider.setSavedAudio(
            audio = tvTitle.text.toString() + ".mp3"
        )
        unitProvider.setSavedTime(
            time = playerView.player.contentPosition.toString()
        )
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (driving_mode_container.visibility == View.VISIBLE){
            driving_mode_container.visibility = View.GONE
        }else{
            super.onBackPressed()
        }
    }
}