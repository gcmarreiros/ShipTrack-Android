package com.shiptrack

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shiptrack.databinding.FragmentTasksBinding
class TasksFragment:Fragment(){private var _b:FragmentTasksBinding?=null;private val b get()=_b!!;private lateinit var vm:TaskViewModel;private lateinit var adapter:TaskAdapter
	override fun onCreateView(inflater:LayoutInflater,c:ViewGroup?,s:Bundle?):View({_b=FragmentTasksBinding.inflate(inflater,c,false);return b.root}
	override fun onViewCreated(view:View,s:Bundle?){vm=ViewModelProvider(requireActivity())[TaskViewModel::class.java]
	adapter=TaskAdapter({t->TaskDetailActivity.start(requireContext(),t.id)},{t,s,vm.updateStatus(t.id,s)})
	b.rvTasks.adapter=adapter
	vm.filteredTasks.observe(viewLifecycleOwner){tasks->adapter.submitList(tasks);b.tvTaskCount.text="${tasks.size} task${if(tasks.size!=1)"s" else ""}";b.emptyState.visibility=if(tasks.isEmpty())View.VISIBLE else View.GONE}
	b.etSearch.addTextChangedListener(object:TextWatcher{override fun afterTextChanged(s:Editable?){vm.filterSearch.value=s?.toString()?:""};override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){};override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){}})
	b.btnToggleDone.setOnClickListener{val c=vm.showDone.value?:true;vm.showDone.value=!c;b.btnToggleDone.text=if(!c)" SHOW DONE" else " HIDE DONE";b.btnToggleDone.isSelected=!c}
	b.btnSort.setOnClickListener{val o=arrayOf("Newest first","Oldest first","By priority","By due date");val m=arrayOf("new","old","prio","due");androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Sort tasks").setItems(o){_,i->vm.sortMode.value=m[i]}.show()}
	b.btnFilterCat.setOnClickListener{val cats=vm.settings.categories;val ch=cats.map{vm.filterTypes.value?.contains(it)==true}.toBooleanArray();androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Filter by Category").setMultiChoiceItems(cats.toTypedArray(),ch){_,i,c->val cur=vm.filterTypes.value?.toMutableList()?:mutableListOf();if(c)cur.add(cats[i]) else cur.remove(cats[i]);vm.filterTypes.value=cur}.setPositiveButton("Done"){d,_->d.dismiss()}.setNeutralButton("Clear"){d,_->vm.filterTypes.value=emptyList();d.dismiss()}.show()}
	b.btnFilterPrio.setOnClickListener{val prios=arrayOf("Critical","High","Medium","Low");val ch=prios.map{vm.filterPrios.value?.contains(it)==true}.toBooleanArray();androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Filter by Priority").setMultiChoiceItems(prios,ch){_,i,c->val cur=vm.filterPrios.value?.toMutableList()?:mutableListOf();if(c)cur.add(prios[i]) else cur.remove(prios[i]);vm.filterPrios.value=cur}.setPositiveButton("Done"){d,_->d.dismiss()}.setNeutralButton("Clear"){d,_->vm.filterPrios.value=emptyList();d.dismiss()}.show()}}
	override fun onDestroyView(){super.onDestroyView();_b=null}}
