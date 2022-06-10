package com.sonianicoletti.progettolam.ui.game.accusation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sonianicoletti.entities.Card
import com.sonianicoletti.entities.Cards
import com.sonianicoletti.progettolam.databinding.FragmentAccusationBinding
import com.sonianicoletti.progettolam.ui.game.cards.CardItem

class AccusationFragment : Fragment() {

    private lateinit var binding: FragmentAccusationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAccusationBinding.inflate(inflater)
        initCardsLists()
        return binding.root
    }

    private fun initCardsLists() {
        val charactersAdapter = AccusationCardsAdapter(Cards.characters.mapToCardItems())
        val weaponsAdapter = AccusationCardsAdapter(Cards.weapons.mapToCardItems())
        val roomsAdapter = AccusationCardsAdapter(Cards.rooms.mapToCardItems())
        binding.characterCards.adapter = charactersAdapter
        binding.weaponCards.adapter = weaponsAdapter
        binding.roomCards.adapter = roomsAdapter
    }

    private fun List<Card>.mapToCardItems() = map { CardItem.fromCard(it) }
}
