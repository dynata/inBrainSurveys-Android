package com.inbrain.example

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.inbrain.sdk.model.Survey

class NativeSurveysAdapter(
    private val onStartClick: (Survey) -> Unit
) : RecyclerView.Adapter<NativeSurveysAdapter.ViewHolder>() {

    private val items = mutableListOf<Survey>()

    fun submit(surveys: List<Survey>) {
        items.clear()
        items.addAll(surveys)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_native_survey, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rewardLabel: TextView = itemView.findViewById(R.id.rewardLabel)
        private val durationLabel: TextView = itemView.findViewById(R.id.durationLabel)
        private val categoryLabel: TextView = itemView.findViewById(R.id.categoryLabel)
        private val btnStart: MaterialButton = itemView.findViewById(R.id.btnStart)
        private val stars = listOf<ImageView>(
            itemView.findViewById(R.id.star1),
            itemView.findViewById(R.id.star2),
            itemView.findViewById(R.id.star3),
            itemView.findViewById(R.id.star4),
            itemView.findViewById(R.id.star5)
        )

        fun bind(survey: Survey) {
            rewardLabel.text = buildRewardText(survey.value, survey.multiplier)
            durationLabel.text = itemView.context.getString(R.string.duration_min, survey.time)
            categoryLabel.text = survey.categories
                ?.joinToString(", ") { it.name }
                .orEmpty()
            categoryLabel.isSelected = true

            stars.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index <= survey.rank) R.drawable.star_filled else R.drawable.star_empty
                )
            }

            btnStart.setOnClickListener { onStartClick(survey) }
        }

        private fun buildRewardText(reward: Float, multiplier: Float): CharSequence {
            val context = itemView.context
            if (multiplier == 1f) {
                return context.getString(R.string.reward_points, reward)
            }

            val oldReward = String.format("%.0f", reward / multiplier)
            val full = "$oldReward ${context.getString(R.string.reward_points, reward)}"
            val spannable = SpannableString(full)
            val end = oldReward.length
            spannable.setSpan(StrikethroughSpan(), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(
                ForegroundColorSpan(Color.LTGRAY),
                0,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }
    }
}
