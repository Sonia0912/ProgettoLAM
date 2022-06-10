package com.sonianicoletti.progettolam.ui.game.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TableRow
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sonianicoletti.entities.Game
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.databinding.FragmentNotesBinding
import com.sonianicoletti.progettolam.ui.game.GameViewModel
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private lateinit var binding: FragmentNotesBinding
    private val viewModel: NotesViewModel by viewModels()
    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater)
        // Check default notes (your cards + leftover cards)
        gameViewModel.gameState.observe(viewLifecycleOwner) {
            hideNonPlayerColumns(it.game)
            viewModel.handleGameState()
        }
        viewModel.cardsState.observe(viewLifecycleOwner) {
            disableDefaultNotes(it.defaultCards)
        }
        // Handle cards button
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.cardsFragment)
        }
        // Handle check button (make it stay)
        // TODO
        // Handle types of notes
        binding.buttonCharacters.setOnClickListener {
            binding.tableCharacters.tableLayoutCharacters.isVisible = true
            binding.tableWeapons.tableLayoutWeapons.visibility = View.GONE
            binding.tableRooms.tableLayoutRooms.visibility =  View.GONE
        }
        binding.buttonWeapons.setOnClickListener {
            binding.tableWeapons.tableLayoutWeapons.isVisible = true
            binding.tableRooms.tableLayoutRooms.visibility =  View.GONE
            binding.tableCharacters.tableLayoutCharacters.visibility = View.GONE
        }
        binding.buttonRooms.setOnClickListener {
            binding.tableRooms.tableLayoutRooms.isVisible = true
            binding.tableWeapons.tableLayoutWeapons.visibility = View.GONE
            binding.tableCharacters.tableLayoutCharacters.visibility = View.GONE
        }
        return binding.root
    }

    private fun hideNonPlayerColumns(game: Game) {
        val tables = listOf(binding.tableCharacters, binding.tableRooms, binding.tableWeapons)
        when (game.players.size) {
            3 -> {
                tables.forEach { (it.root as ViewGroup).findViewsWithTag("COLUMN_3").forEach { it.isVisible = false } }
                tables.forEach { (it.root as ViewGroup).findViewsWithTag("COLUMN_4").forEach { it.isVisible = false } }
                tables.forEach { (it.root as ViewGroup).findViewsWithTag("COLUMN_5").forEach { it.isVisible = false } }
            }
            4 -> {
                tables.forEach { (it.root as ViewGroup).findViewsWithTag("COLUMN_4").forEach { it.isVisible = false } }
                tables.forEach { (it.root as ViewGroup).findViewsWithTag("COLUMN_5").forEach { it.isVisible = false } }
            }
            5 -> {
                tables.forEach { (it.root as ViewGroup).findViewsWithTag("COLUMN_5").forEach { it.isVisible = false } }
            }
        }
    }

    private fun disableDefaultNotes(defaultCards: MutableList<CardItem>) {
        defaultCards.forEach { cardItem ->
            val tableRow = binding.root.findViewWithTag<TableRow>(cardItem.card.name)
            tableRow.children.forEach { child ->
                (child as? CheckBox)?.apply {
                    isEnabled = false
                    isChecked = true
                }
            }
        }
    }

    private fun ViewGroup.findViewsWithTag(tag: String): List<View> {
        val viewsWithTag = mutableListOf<View>()
        children.forEach {
            if (it.tag == tag) {
                viewsWithTag.add(it)
            }

            if (it is ViewGroup) {
                viewsWithTag.addAll(it.findViewsWithTag(tag))
            }
        }
        return viewsWithTag
    }
}