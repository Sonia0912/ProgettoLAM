package com.sonianicoletti.progettolam.ui.game.scores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sonianicoletti.entities.Score
import com.sonianicoletti.progettolam.databinding.ListItemScoreBinding

class ScoresAdapter : RecyclerView.Adapter<ScoresAdapter.ViewHolder>() {

    private val scores = mutableListOf<Score>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(scores[position])
    }

    override fun getItemCount() = scores.size

    fun updateList(scores: List<Score>) {
        this.scores.apply {
            clear()
            addAll(scores)
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ListItemScoreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(score: Score) {
            binding.playerName.text = score.player
            binding.ranking.text = score.score
        }
    }
}
