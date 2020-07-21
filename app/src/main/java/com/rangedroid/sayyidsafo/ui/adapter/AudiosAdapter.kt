package com.rangedroid.sayyidsafo.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Status
import com.google.android.exoplayer2.util.Util
import com.rangedroid.sayyidsafo.App
import com.rangedroid.sayyidsafo.R
import com.rangedroid.sayyidsafo.data.db.model.UnitAudiosModel
import com.rangedroid.sayyidsafo.ui.activity.MainActivity.Companion.connection
import com.rangedroid.sayyidsafo.ui.activity.MainActivity.Companion.listAudios
import com.rangedroid.sayyidsafo.utils.AudioPlayerService

class AudiosAdapter(audiosModel: List<UnitAudiosModel>) : RecyclerView.Adapter<AudiosAdapter.AudiosViewHolder>(){

    private val listModel: ArrayList<UnitAudiosModel> = ArrayList(audiosModel)
    private var downloadID: Int = 0

    class AudiosViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvTitle: TextView = view.findViewById(R.id.title)
        val tvDuration: TextView = view.findViewById(R.id.duration)
        val tvSize: TextView = view.findViewById(R.id.size)
        val progressBar: ProgressBar = view.findViewById(R.id.progress)
        val download: AppCompatImageView = view.findViewById(R.id.download)
        val constraintLayout: ConstraintLayout = view.findViewById(R.id.constraintLayout)
        val mContext: Context = view.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudiosViewHolder {
        return AudiosViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_audios_container,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return listModel.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AudiosViewHolder, position: Int) {
        holder.tvTitle.text = listModel[position].name
        holder.tvSize.text = String.format("%.2f", listModel[position].size / 1024.0) + "Мб"
        holder.tvDuration.text = getFormattedTime(listModel[position].duration)

        if (listAudios.contains(listModel[position].getFileName())){
            holder.download.setImageResource(R.drawable.play)
            holder.download.visibility =  View.VISIBLE
            holder.progressBar.visibility =  View.GONE
        }else{
            holder.download.setImageResource(R.drawable.download)
            holder.download.visibility =  View.VISIBLE
            holder.progressBar.visibility =  View.GONE
        }

        holder.constraintLayout.setOnClickListener {
            startPlay(position, holder)
        }
        holder.download.setOnClickListener {
            startPlay(position, holder)
        }
        holder.progressBar.setOnClickListener {
            startPlay(position, holder)
        }
    }

    private fun startPlay(index: Int, holder: AudiosViewHolder){
        if (!listAudios.contains(listModel[index].getFileName())){
            startDownload(index, holder)
        }else{
            listAudios.forEachIndexed { i, it ->
                if (it == listModel[index].getFileName()){
                    val intent = Intent(holder.mContext, AudioPlayerService::class.java)
                    intent.putExtra(AudioPlayerService.INDEX, i)
                    intent.putExtra(AudioPlayerService.BINDING_SERVICE, true)
                    holder.mContext.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
                    Util.startForegroundService(holder.mContext, intent)
                }
            }
        }
    }

    private fun startDownload(index: Int, holder: AudiosViewHolder){
        if (PRDownloader.getStatus(downloadID) == Status.RUNNING){
            PRDownloader.cancel(downloadID)
            notifyItemChanged(index)
        }else {
            holder.progressBar.visibility = View.VISIBLE
            holder.download.setImageResource(R.drawable.cancel)
            downloadID = PRDownloader.download(
                App.BASE_URL + listModel[index].location,
                App.DIR_PATH,
                listModel[index].getFileName()
            ).build()
                .setOnProgressListener {
                    holder.progressBar.progress = (it.currentBytes * 100 / it.totalBytes).toInt()
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        listAudios.add(listModel[index].name+".mp3")
                        notifyItemChanged(index)
                    }

                    override fun onError(error: Error?) {
                        Log.d("BAG", error?.responseCode.toString())
                        notifyItemChanged(index)
                    }
                })
        }

    }

    private fun getFormattedTime(seconds: Long): String {
        val minutes = seconds / 60
        return String.format("%d:%02d", minutes, seconds % 60)
    }
}