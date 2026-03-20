package com.shiptrack

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shiptrack.databinding.FragmentSettingsBinding
class SettingsFragment:Fragment(){private var _b:FragmentSettingsBinding?=null;private val b get()=_b!!;private lateinit var vm:TaskViewModel
override fun onCreateView(inflater:LayoutInflater,c:ViewGroup?,s:Bundle?):View{_b=FragmentSettingsBinding.inflate(inflater,c,false);return b.root}
override fun onViewCreated(view:View,s:Bundle?){vm=ViewModelProvider(requireActivity())[TaskViewModel::class.java];refreshUI()
b.btnAddCat.setOnClickListener{val name=b.etNewCat.text?.toString()?.trim()?:"";if(name.isBlank()){toast("Enter a category name");return@setOnClickListener};val cats=vm.settings.categories.toMutableList();if(cats.contains(name)){toast("Category already exists");return@setOnClickListener};cats.add(name);vm.settings.categories=cats;b.etNewCat.setText("");refreshUI();toast("Category added")}
b.btnAddZone.setOnClickListener{val name=b.etNewZoneName.text?.toString()?.trim()?:"";val code=b.etNewZoneCode.text?.toString()?.trim()?.uppercase()?:"";if(name.isBlank()||code.isBlank()){toast("Enter zone name and code");return@setOnClickListener};val zones=vm.settings.zones.toMutableList();if(zones.any{it.code==code}){toast("Zone code already exists");return@setOnClickListener};zones.add(Zone(code,name,"⪡",""));vm.settings.zones=zones;b.etNewZoneName.setText("");b.etNewZoneCode.setText("");refreshUI();toast("Zone added")}
b.btnResetCats.setOnClickListener{vm.settings.categories=DefaultData.CATEGORIES.toList();refreshUI();toast("Categories reset")}
b.btnResetZones.setOnClickListener{vm.settings.zones=DefaultData.ZONES.toList();refreshUI();toast("Zones reset")}
b.tvVersion.text="ShipTrack v1.0 · Offline · Room DB"}
private fun refreshUI(){b.tvCatList.text=vm.settings.categories.joinToString("\n"){"• $it"};b.tvZoneList.text=vm.settings.zones.joinToString("\n"){"${it.icon} ${it.name} (${it.code})"}}
private fun toast(msg:String)=Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
override fun onDestroyView(){super.onDestroyView();_b=null}}
