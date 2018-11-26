package com.dvoroncov.arcore.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dvoroncov.arcore.R

class ModelsAdapter : RecyclerView.Adapter<ModelsAdapter.ViewHolder>() {

    var modelsImagesList = mutableListOf(R.drawable.amenemhat, R.drawable.model, R.drawable.planetary_crawler)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.model_item, parent, false))
    }

    override fun getItemCount(): Int {
        return modelsImagesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.modelImageView)

        fun onBind(position: Int) {
            imageView.setImageDrawable(itemView.context.getDrawable(modelsImagesList[position]))
        }
    }
}