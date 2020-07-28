package uz.mnsh.buyuklar.ui.fragment

import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Status
import com.mnsh.sayyidsafo.R
import kotlinx.coroutines.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import uz.mnsh.buyuklar.App
import uz.mnsh.buyuklar.data.db.model.AudioModel
import uz.mnsh.buyuklar.ui.activity.MainActivity.Companion.listAudios
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext

class InfoFragment : Fragment(R.layout.info_fragment), CoroutineScope, KodeinAware {

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): InfoFragment {
            return InfoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override val kodein by closestKodein()
    private val viewModelFactory: InfoViewModelFactory by instance<InfoViewModelFactory>()
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var viewModel: InfoViewModel
    private lateinit var tvInfo: TextView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlay: AppCompatImageView
    private lateinit var imgInfo: AppCompatImageView
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var textInfo: TextView
    private var startTime: Int = 0
    private var endTime: Int = 0
    private var isStop: Boolean = false
    private var downloadID: Int = 0;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        job = Job()
        tvInfo = view.findViewById(R.id.tv_info)
        textInfo = view.findViewById(R.id.text_info)
        btnPlay = view.findViewById(R.id.info_play)
        imgInfo = view.findViewById(R.id.img_info)
        tvStartTime = view.findViewById(R.id.tv_start_time)
        tvEndTime = view.findViewById(R.id.tv_end_time)
        seekBar = view.findViewById(R.id.seekBar)
        seekBar.isClickable = false
        progressBar = view.findViewById(R.id.progress)
        mediaPlayer = MediaPlayer()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(InfoViewModel::class.java).apply {
            setIndex(arguments?.getInt(InfoFragment.ARG_SECTION_NUMBER) ?: 1)
        }
        viewModel.text.observe(viewLifecycleOwner, Observer {
            loadData(it)
        })
    }

    private fun loadData(index: Int) = launch{
        if (index == 1){
            viewModel.getFirst(733).value.await().observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                bindUI(it, index)
            })
        }else{
            viewModel.getFirst(850).value.await().observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                bindUI(it, index)
            })
        }

    }

    private fun bindUI(model: AudioModel, index: Int){
        try {
            val inputStream: InputStream? = if (index == 1){
                imgInfo.setImageResource(R.drawable.info_1)
                context?.assets?.open("info_text_one.txt")
            }else{
                imgInfo.setImageResource(R.drawable.info_2)
                context?.assets?.open("info_text_two.txt")
            }
            val buffer = ByteArray(inputStream?.available()!!)
            inputStream.read(buffer)
            tvInfo.text = String(buffer)
            textInfo.text = model.name
        }catch (e: IOException){

        }

        if (listAudios.contains(model.getFileName())){
            btnPlay.setImageResource(R.drawable.play)
            bindCard(model.getFileName())
        }else{
            btnPlay.setImageResource(R.drawable.download)
        }

        btnPlay.setOnClickListener {
            if (listAudios.contains(model.getFileName())){
                if (isStop){
                    btnPlay.setImageResource(R.drawable.play)
                    mediaPlayer.pause()
                    isStop = false
                }else{
                    btnPlay.setImageResource(R.drawable.stop)
                    isStop = true
                    mediaPlayer.start()
                    updateSong()
                }
            }else{
                if (PRDownloader.getStatus(downloadID) == Status.RUNNING){
                    PRDownloader.cancel(downloadID)
                    progressBar.visibility = View.GONE
                    btnPlay.setImageResource(R.drawable.download)
                }else {
                    progressBar.visibility = View.VISIBLE
                    btnPlay.setImageResource(R.drawable.cancel)
                    downloadID = PRDownloader.download(
                        App.BASE_URL + model.location,
                        App.DIR_PATH,
                        model.getFileName()
                    ).build()
                        .setOnProgressListener {
                            progressBar.progress = (it.currentBytes * 100 / it.totalBytes).toInt()
                        }
                        .start(object : OnDownloadListener {
                            override fun onDownloadComplete() {
                                progressBar.visibility = View.GONE
                                btnPlay.setImageResource(R.drawable.play)
                                listAudios.add(model.getFileName())
                                bindCard(model.getFileName())
                            }

                            override fun onError(error: Error?) {
                                Log.d("BAG", error?.responseCode.toString())
                            }
                        })
                }
            }
        }
    }

    private fun bindCard(name: String){
        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(File(App.DIR_PATH+name)))
        startTime = mediaPlayer.currentPosition / 1000
        endTime = mediaPlayer.duration / 1000
        Log.d("BAG", mediaPlayer.duration.toString())
        tvStartTime.text = getFormattedTime(startTime)
        tvEndTime.text = getFormattedTime(endTime)
        seekBar.progress = mediaPlayer.currentPosition / 100
    }

    private fun updateSong(){
        startTime = mediaPlayer.currentPosition / 1000
        tvStartTime.text = getFormattedTime(startTime)
        seekBar.progress = (mediaPlayer.currentPosition) / endTime / 10
        if ((mediaPlayer.currentPosition / 1000) == endTime){
            btnPlay.setImageResource(R.drawable.play)
        }else{
            Handler().postDelayed(Runnable { updateSong() }, 1000)
        }
    }


    private fun getFormattedTime(seconds: Int): String {
        val minutes = seconds / 60
        return String.format("%d:%02d", minutes, seconds % 60)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        job.cancel()
    }

}