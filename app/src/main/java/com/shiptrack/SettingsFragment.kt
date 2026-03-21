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
            if (name.isBlank()) { toast("Enter a category name"); return@setOnClickListener }
            val cats = vm.settings.categories.toMutableList()
            if (cats.contains(name)) { toast("Category already exists"); return@setOnClickListener }
            cats.add(name)
            vm.settings.categories = cats
            binding.etNewCat.setText("")
            refreshUI()
            toast("Category added")
        }
        binding.btnAddZone.setOnClickListener {
            val name = binding.etNewZoneName.text?.toString()?.trim() ?: ""
            val code = binding.etNewZoneCode.text?.toString()?.trim()?.uppercase() ?: ""
            if (name.isBlank() || code.isBlank()) { toast("Enter zone name and code"); return@setOnClickListener }
            val zones = vm.settings.zones.toMutableList()
            if (zones.any { it.code == code }) { toast("Zone code already exists"); return@setOnClickListener }
            zones.add(Zone(code, name, "\u2B21", ""))
            vm.settings.zones = zones
            binding.etNewZoneName.setText("")
            binding.etNewZoneCode.setText("")
            refreshUI()
            toast("Zone added")
        }
        binding.btnResetCats.setOnClickListener {
            vm.settings.categories = DefaultData.CATEGORIES.toList()
            refreshUI(); toast("Categories reset")
        }
        binding.btnResetZones.setOnClickListener {
            vm.settings.zones = DefaultData.ZONES.toList()
            refreshUI(); toast("Zones reset")
        }
        binding.tvVersion.text = "ShipTrack v1.0 \u00B7 Offline \u00B7 Room DB"
    }

    private fun refreshUI() {
        val cats = vm.settings.categories
        binding.tvCatList.text = cats.joinToString("\n") { "\u2022 $it" }
        val zones = vm.settings.zones
        binding.tvZoneList.text = zones.joinToString("\n") { "${it.icon} ${it.name} (${it.code})" }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
