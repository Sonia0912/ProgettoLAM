package com.sonianicoletti.progettolam.ui.game.characters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sonianicoletti.progettolam.databinding.ListItemCharacterBinding

class CharactersAdapter(var characterItems: List<SelectCharacterItem>) : RecyclerView.Adapter<CharactersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemCharacterBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(characterItems[position])
    }

    override fun getItemCount() = characterItems.size

    class ViewHolder(val binding: ListItemCharacterBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(characterItem: SelectCharacterItem) {
            binding.avatar.setImageResource(characterItem.avatarRes)
            binding.playerName.text = characterItem.assignedPlayer?.displayName.orEmpty()
        }
    }
}
