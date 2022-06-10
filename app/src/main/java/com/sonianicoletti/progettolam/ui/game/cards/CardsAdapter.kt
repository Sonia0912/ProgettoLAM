package com.sonianicoletti.progettolam.ui.game.cards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sonianicoletti.progettolam.databinding.ListItemCardBinding

class CardsAdapter(private val cards: MutableList<CardItem>) : RecyclerView.Adapter<CardsAdapter.ViewHolder>() {

    // Il ViewHolder e' il contenitore per un elemento della lista, in questo caso una carta
    // la classe ListItemCardBinding e' generata automaticamente grazie al ViewBinding
    // ogni layout puo' essere preso con binding.root
    class ViewHolder(var binding : ListItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cardItem: CardItem) {
            binding.card.setImageResource(cardItem.imageRes)
            binding.cardName.text = cardItem.card.name
        }
    }

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

}