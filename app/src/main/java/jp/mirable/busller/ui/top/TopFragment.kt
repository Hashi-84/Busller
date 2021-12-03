package jp.mirable.busller.ui.top

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import jp.mirable.busller.R
import jp.mirable.busller.databinding.FragmentTopBinding
import jp.mirable.busller.model.TimetableAdapter
import jp.mirable.busller.viewmodel.TopViewModel

class TopFragment : Fragment() {
    private val topVM: TopViewModel by activityViewModels()
    private lateinit var timetableAdapter: TimetableAdapter

    var handler: Handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run() {
            topVM.setLeftSec()
//            Log.d("Hoge!", "250ミリ秒ごとの処理だよ!")
            if (topVM.nextData.value != null) handler.postDelayed(this, 250)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentTopBinding.inflate(inflater, container, false).apply {
            this.topVM = this@TopFragment.topVM
            lifecycleOwner = viewLifecycleOwner

            timeTableRV.run {
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = TimetableAdapter(viewLifecycleOwner, this@TopFragment.topVM).also {
                    timetableAdapter = it
                }
            }
        }.run { root }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topVM.run {
            forSchool.observe(viewLifecycleOwner, {
                handler.removeCallbacks(runnable)
                load()
                if (nextData.value != null) handler.post(runnable)
            })
            rvTimeList.observe(viewLifecycleOwner, {
                timetableAdapter.submitList(it)
            })
            nextData.observe(viewLifecycleOwner, {
                if (nextData.value == null) handler.removeCallbacks(runnable)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (topVM.nextData.value != null) handler.post(runnable)
    }

    override fun onPause() {
        handler.removeCallbacks(runnable)
        super.onPause()
    }
}