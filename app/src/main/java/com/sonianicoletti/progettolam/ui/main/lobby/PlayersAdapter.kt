package com.sonianicoletti.progettolam.ui.main.lobby

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sonianicoletti.entities.Player
import com.sonianicoletti.progettolam.databinding.ListItemPlayerBinding

class PlayersAdapter(private val players: List<Player>) : RecyclerView.Adapter<PlayersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemPlayerBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    // Mostra i dati in una posizione specifica
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(players[position])
    }

    override fun getItemCount() = players.size

    class ViewHolder(private val binding: ListItemPlayerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(player: Player) {
            binding.username.text = player.displayName
        }
    }


}