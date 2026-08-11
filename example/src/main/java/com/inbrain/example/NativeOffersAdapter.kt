package com.inbrain.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.inbrain.sdk.model.offers.InBrainNativeOffer

class NativeOffersAdapter(
    private val onOpenClick: (InBrainNativeOffer) -> Unit
) : RecyclerView.Adapter<NativeOffersAdapter.ViewHolder>() {

    private val items = mutableListOf<InBrainNativeOffer>()

    fun submit(offers: List<InBrainNativeOffer>) {
        items.clear()
        items.addAll(offers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_native_offer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rewardLabel: TextView = itemView.findViewById(R.id.rewardLabel)
        private val descriptionLabel: TextView = itemView.findViewById(R.id.descriptionLabel)
        private val categoryLabel: TextView = itemView.findViewById(R.id.categoryLabel)
        private val btnOpen: MaterialButton = itemView.findViewById(R.id.btnOpen)

        fun bind(offer: InBrainNativeOffer) {
            rewardLabel.text = offer.rewardString
            descriptionLabel.text = offer.description?.joinToString(", ").orEmpty()
            categoryLabel.text = offer.categories?.joinToString(", ").orEmpty()
            categoryLabel.isSelected = true
            btnOpen.setOnClickListener { onOpenClick(offer) }
        }
    }
}
