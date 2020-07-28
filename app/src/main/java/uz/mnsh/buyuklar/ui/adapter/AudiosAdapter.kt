package uz.mnsh.buyuklar.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Status
import com.google.android.exoplayer2.util.Util
import uz.mnsh.buyuklar.App.Companion.binder
import uz.mnsh.buyuklar.App.Companion.connection
import com.mnsh.sayyidsafo.R
import uz.mnsh.buyuklar.App
import uz.mnsh.buyuklar.data.db.model.AudioModel
import uz.mnsh.buyuklar.ui.activity.MainActivity.Companion.listAudios
import uz.mnsh.buyuklar.utils.AudioPlayerService

class AudiosAdapter(audiosModel: List<AudioModel>) :
    RecyclerView.Adapter<AudiosAdapter.AudiosViewHolder>() {

    private val listModel: ArrayList<AudioModel> = ArrayList(audiosModel)
    private var isPlay: Int = 1000
    private var isStart: Boolean = true
    private var idList: HashMap<Int, Int> = HashMap()

    init {
        if (isStart) {
            listModel.forEachIndexed { index, audioModel ->
                if (audioModel.getFileName() == binder?.getService()?.currentTitle?.value) {
                    changeAudio(index)
                }
            }
        }
    }

    class AudiosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        holder.tvSize.text = String.format("%.2f", listModel[position].size / 1000.0) + "Мб"
        holder.tvDuration.text = getFormattedTime(listModel[position].duration)

        if (listAudios.contains(listModel[position].getFileName())) {
            holder.download.setImageResource(R.drawable.play)
            holder.download.visibility = View.VISIBLE
            holder.progressBar.visibility = View.GONE
            if (isPlay == position) {
                holder.download.setImageResource(R.drawable.stop)
            }
        } else {
            holder.download.setImageResource(R.drawable.download)
            holder.download.visibility = View.VISIBLE
            holder.progressBar.visibility = View.GONE
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

    private fun startPlay(index: Int, holder: AudiosViewHolder) {
        if (!listAudios.contains(listModel[index].getFileName())) {
            startDownload(index, holder)
        } else {
            if (binder?.getService()?.currentTitle?.value == listModel[index].getFileName()) {
                binder?.getService()?.mExoPlayer?.let {
                    it.playWhenReady = !it.playWhenReady
                }
            } else {
                isPlay = 1000
                binder?.getService()
                    ?.handleIntent(listAudios.indexOf(listModel[index].getFileName()))
            }
            changeAudio(index)
        }
    }

    private fun changeAudio(index: Int) {
        isStart = false
        binder?.getService()?.isPlaying?.observeForever {
            if (it == null) return@observeForever
            if (it) {
                if (listModel[index].getFileName() == binder?.getService()?.currentTitle?.value) {
                    if (isPlay != index) {
                        isPlay = index
                    }
                    notifyDataSetChanged()
                } else {
                    isPlay = 1000
                    notifyDataSetChanged()
                }
            } else {
                isPlay = 1000
                notifyDataSetChanged()
            }
        }
    }

    private fun startDownload(index: Int, holder: AudiosViewHolder) {

        if (idList[index] != null && PRDownloader.getStatus(idList[index]!!) == Status.RUNNING) {
            PRDownloader.cancel(idList[index]!!)
            notifyItemChanged(index)
        } else {
            holder.progressBar.visibility = View.VISIBLE
            holder.download.setImageResource(R.drawable.cancel)
            idList[index] = PRDownloader.download(
                App.BASE_URL + listModel[index].location,
                App.DIR_PATH,
                listModel[index].getFileName()
            ).build()
                .setOnProgressListener {
                    holder.progressBar.progress = (it.currentBytes * 100 / it.totalBytes).toInt()
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        listAudios.add(listModel[index].getFileName())
                        notifyItemChanged(index)
                        idList.remove(index)
                        if (listAudios.size == 1) {
                            val intent = Intent(holder.mContext, AudioPlayerService::class.java)
                            intent.putExtra(AudioPlayerService.INDEX, 0)
                            intent.putExtra(AudioPlayerService.BINDING_SERVICE, true)
                            holder.mContext.bindService(
                                intent,
                                connection!!,
                                Context.BIND_AUTO_CREATE
                            )
                            Util.startForegroundService(holder.mContext, intent)
                        }
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