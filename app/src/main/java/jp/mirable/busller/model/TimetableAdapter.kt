package jp.mirable.busller.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.mirable.busller.R
import jp.mirable.busller.databinding.TimetableItemBinding
import jp.mirable.busller.ui.MyDialogFragment
import jp.mirable.busller.ui.top.TopFragmentDirections
import jp.mirable.busller.viewmodel.TopViewModel

class TimetableAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val viewModel: TopViewModel
) : ListAdapter<ListData, TimetableAdapter.TimeViewHolder>(DiffCallback) {

    class TimeViewHolder(private val binding: TimetableItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListData, viewLifecycleOwner: LifecycleOwner, viewModel: TopViewModel) {
            binding.run {
                lifecycleOwner = viewLifecycleOwner
                listData = item
                this.topVM = viewModel

                executePendingBindings()
            }
        }
    }

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TimeViewHolder(TimetableItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        holder.bind(getItem(position), viewLifecycleOwner, viewModel)
        holder.itemView.setOnClickListener {
            it.findNavController().navigate(
                TopFragmentDirections.actionTopPageToListDialog(position)
            )
        }
    }
}

private object DiffCallback: DiffUtil.ItemCallback<ListData>() {
    override fun areItemsTheSame(oldItem: ListData, newItem: ListData): Boolean {
        return oldItem.timeData.id == newItem.timeData.id
    }

    override fun areContentsTheSame(oldItem: ListData, newItem: ListData): Boolean {
        return oldItem == newItem
    }
}
