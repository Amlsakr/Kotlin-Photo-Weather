package com.example.kotlinweatheramlsakr.view.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinweatheramlsakr.databinding.RecyclerItemViewBinding

class MainAdapter(
    var pictureItems: List<Uri>,
    var recyclerViewItemClickListener: RecyclerViewItemClickListener
): RecyclerView.Adapter<MainAdapter.ViewHolder>() {

     inner   class ViewHolder(var recyclerItemViewBinding: RecyclerItemViewBinding) :
        RecyclerView.ViewHolder(recyclerItemViewBinding.getRoot()) {
        init {
            recyclerItemViewBinding.recyclerViewIV.setOnClickListener(View.OnClickListener {
                recyclerViewItemClickListener.onClick(
                    adapterPosition
                )
            })
        }
    }

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         val recyclerItemViewBinding =
             RecyclerItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
         return ViewHolder(recyclerItemViewBinding)
     }

     override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         holder.recyclerItemViewBinding.recyclerViewIV.setImageURI(pictureItems[position])
     }

     override fun getItemCount() = pictureItems.size
 }