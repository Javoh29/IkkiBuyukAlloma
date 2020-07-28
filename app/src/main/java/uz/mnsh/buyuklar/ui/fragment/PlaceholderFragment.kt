package uz.mnsh.buyuklar.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ybq.android.spinkit.SpinKitView
import com.mnsh.sayyidsafo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import uz.mnsh.buyuklar.data.db.model.AudioModel
import uz.mnsh.buyuklar.ui.adapter.AudiosAdapter
import kotlin.coroutines.CoroutineContext

class PlaceholderFragment : Fragment(R.layout.fragment_main), CoroutineScope, KodeinAware {

    override val kodein by closestKodein()
    private val viewModelFactory: PageViewModelFactory by instance<PageViewModelFactory>()
    private lateinit var pageViewModel: PageViewModel
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var recyclerView: RecyclerView
    private var spinKitView: SpinKitView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        job = Job()

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        spinKitView = view.findViewById(R.id.spin_kit)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pageViewModel = ViewModelProviders.of(this, viewModelFactory).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
        pageViewModel.text.observe(viewLifecycleOwner, Observer {
            loadData(it)
        })
    }

    private fun loadData(index: Int) = launch {
        if (index == 1){
            pageViewModel.getAudios(8).value.await().observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                if (it.isNotEmpty()) bindUI(it)
            })
        }else{
            pageViewModel.getAudios(9).value.await().observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                if (it.isNotEmpty()) bindUI(it)
            })
        }
    }

    private fun bindUI(audioModel: List<AudioModel>){
        recyclerView.adapter = AudiosAdapter(audioModel)
        recyclerView.visibility = View.VISIBLE
        spinKitView?.visibility = View.GONE
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}