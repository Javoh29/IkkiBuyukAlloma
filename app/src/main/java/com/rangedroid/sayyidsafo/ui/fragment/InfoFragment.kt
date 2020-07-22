package com.rangedroid.sayyidsafo.ui.fragment

import android.media.MediaPlayer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.rangedroid.sayyidsafo.R
import java.io.IOException
import java.io.InputStream

class InfoFragment : Fragment(R.layout.info_fragment) {

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

    private lateinit var viewModel: InfoViewModel
    private lateinit var tvInfo: TextView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlay: AppCompatImageView
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private var startTime: Int = 0
    private var endTime: Int = 0
    private var isStop: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvInfo = view.findViewById(R.id.tv_info)
        mediaPlayer = MediaPlayer.create(context, R.raw.sound)
        btnPlay = view.findViewById(R.id.info_play)
        tvStartTime = view.findViewById(R.id.tv_start_time)
        tvEndTime = view.findViewById(R.id.tv_end_time)
        seekBar = view.findViewById(R.id.seekBar)
        seekBar.isClickable = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(InfoViewModel::class.java)
        bindUI()
    }

    private fun bindUI(){
        startTime = mediaPlayer.currentPosition / 1000
        endTime = mediaPlayer.duration / 1000
        Log.d("BAG", mediaPlayer.duration.toString())
        tvStartTime.text = getFormattedTime(startTime)
        tvEndTime.text = getFormattedTime(endTime)
        seekBar.progress = mediaPlayer.currentPosition / 100
        try {
            val inputStream: InputStream? = context?.assets?.open("info_text_one.txt")
            val buffer = ByteArray(inputStream?.available()!!)
            inputStream.read(buffer)
            tvInfo.text = String(buffer)
        }catch (e: IOException){

        }

        btnPlay.setOnClickListener {
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
        }

    }

    private fun updateSong(){
        startTime = mediaPlayer.currentPosition / 1000
        tvStartTime.text = getFormattedTime(startTime)
        seekBar.progress = (mediaPlayer.currentPosition) / endTime / 10
        Handler().postDelayed(Runnable { updateSong() }, 200)
    }


    private fun getFormattedTime(seconds: Int): String {
        val minutes = seconds / 60
        return String.format("%d:%02d", minutes, seconds % 60)
    }

}