package com.sonianicoletti.progettolam.ui.game.cards

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sonianicoletti.progettolam.databinding.ListItemCardBinding

class CardsAdapter : RecyclerView.Adapter<CardsAdapter.ViewHolder>() {

    private val cards = mutableListOf<CardItem>()
    private val accusationCards = mutableListOf<CardItem>()

    // creo il ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemCardBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    // dove i dati vengono applicati alla view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(cards: List<CardItem>) {
        this.cards.clear()
        this.cards.addAll(cards)
        notifyDataSetChanged()
    }

    fun setAccusationCards(cards: List<CardItem>) {
        this.accusationCards.apply {
            clear()
            addAll(cards)
            notifyDataSetChanged()
        }
    }

    // Il ViewHolder e' il contenitore per un elemento della lista, in questo caso una carta
    // la classe ListItemCardBinding e' generata automaticamente grazie al ViewBinding
    // ogni layout puo' essere preso con binding.root
    inner class ViewHolder(var binding : ListItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cardItem: CardItem) {
            binding.card.setImageResource(cardItem.imageRes)
            binding.cardName.text = cardItem.card.name

            if (accusationCards.contains(cardItem)) {
                ObjectAnimator.ofInt(binding.cardLayout.foreground, "alpha", 0, 100).apply {
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                    duration = 500
                    start()
                }
            } else {
                binding.cardLayout.foreground.alpha = 0
            }
        }
    }
}
