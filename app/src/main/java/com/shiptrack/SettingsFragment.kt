package com.shiptrack

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shiptrack.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var vm: TaskViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        refreshUI()
        binding.btnAddCat.setOnClickListener {
            val name = binding.etNewCat.text?.toString()?.trim() ?: ""
            if (name.isBlank()) { return@setOnClickListener }
            val cats = vm.settings.categories.toMutableList()
            cats.add(name); vm.settings.categories = cats
            binding.etNewCat.setText(""); refreshUI()
        }
        binding.btnAddZone.setOnClickListener {
            val name = binding.etNewZoneName.text?.toString()?.trim() ?: ""
            val code = binding.etNewZoneCode.text?.toString()?.trim()?.uppercase() ?: ""
            if (name.isBlank() || code.isBlank()) { return@setOnClickListener }
            val zones = vm.settings.zones.toMutableList()
            zones.add(Zone(code, name, "", "")); vm.settings.zones = zones
            refreshUI()
        }
        binding.btnResetCats.setOnClickListener { vm.settings.categories = DefaultData.CATEGORIES.toList(); refreshUI() }
        binding.btnResetZones.setOnClickListener { vm.settings.zones = DefaultData.ZONES.toList(); refreshUI() }
        binding.tvVersion.text = "ShipTrack v1.0  Offline  Room DB"
    }
    private fun refreshUI() {
        binding.tvCatList.text = vm.settings.categories.joinToString("\n") { " $it" }
        binding.tvZoneList.text = vm.settings.zones.joinToString("\n") { "${it.name} (${it.code})" }
    }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
