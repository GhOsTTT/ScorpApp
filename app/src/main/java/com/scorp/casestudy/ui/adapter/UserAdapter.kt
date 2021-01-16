package com.scorp.casestudy.ui.adapter


import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.scorp.casestudy.databinding.MovieItemLayoutBinding
import com.scorp.casestudy.util.Person

class UserAdapter (private val listener: (personModel: Person) -> Unit) :
    ListAdapter<Person, RecyclerView.ViewHolder>(MovieDiffCallback()) {

    var  personModelList: MutableList<Person>? = null

    private class DataViewHolder(
        private val binding: MovieItemLayoutBinding,
        var listener: (personModel: Person) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {

            }
        }

        fun bind(itemResponse: Person) {
            binding.apply {
                personModel = itemResponse
                executePendingBindings()
            }
        }
    }


    override fun getItemCount(): Int = personModelList?.size ?: 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = MovieItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DataViewHolder(binding, listener)
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewHolder = holder as DataViewHolder
        personModelList?.get(position)?.let { itemViewHolder.bind(it) }
    }
}

private class MovieDiffCallback : DiffUtil.ItemCallback<Person>() {
    override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Person, newItemResponse: Person): Boolean {
        return oldItem == newItemResponse
    }
}