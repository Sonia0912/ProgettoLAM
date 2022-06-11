package com.sonianicoletti.progettolam.ui.game.accusation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.sonianicoletti.progettolam.databinding.ListItemAccusationCardBinding
import com.sonianicoletti.progettolam.ui.game.cards.CardItem

class AccusationCardsAdapter(
    private val cardItems: List<CardItem>,
    private val onCardSelected: (CardItem) -> Unit
) : RecyclerView.Adapter<AccusationCardsAdapter.ViewHolder>() {

    private var selectedCard: CardItem? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemAccusationCardBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cardItems[position])
    }

    override fun getItemCount() = cardItems.size

    fun setSelectedCard(selectedCard: CardItem) {
        this.selectedCard = selectedCard
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ListItemAccusationCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cardItem: CardItem) {
            binding.cardImage.setImageResource(cardItem.imageRes)
            binding.selectedView.isVisible = selectedCard == cardItem
            binding.card.setOnClickListener {
                setSelectedCard(cardItem)
                onCardSelected(cardItem)
            }
        }
    }
}
