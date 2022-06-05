package com.sonianicoletti.progettolam.ui.game.characters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sonianicoletti.entities.Character
import com.sonianicoletti.progettolam.databinding.ListItemCharacterBinding

class CharactersAdapter(
    private val characterItems: MutableList<SelectCharacterItem>,
    private val onCharacterClick: (Character) -> Unit,
) : RecyclerView.Adapter<CharactersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemCharacterBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(characterItems[position])
    }

    override fun getItemCount() = characterItems.size

    fun updateItems(characterItems: List<SelectCharacterItem>) {
        this.characterItems.apply {
            clear()
            addAll(characterItems)
        }
    }

    inner class ViewHolder(val binding: ListItemCharacterBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(characterItem: SelectCharacterItem) {
            binding.avatar.setImageResource(characterItem.avatarRes)
            binding.playerName.text = characterItem.assignedPlayer?.displayName.orEmpty()
            binding.root.setOnClickListener { onCharacterClick(characterItem.character) }
        }
    }
}
