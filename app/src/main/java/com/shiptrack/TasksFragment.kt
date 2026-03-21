package com.shiptrack

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shiptrack.databinding.FragmentTasksBinding

class TasksFragment : Fragment() {
    private var _binding: FragmentTasksBinding? = null; private val binding get() = _binding!!; private lateinit var vm: TaskViewModel; private lateinit var adapter: TaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View { _binding = FragmentTasksBinding.inflate(inflater, container, false); return binding.root }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        adapter = TaskAdapter(onCardClick = { task -> TaskDetailActivity.start(requireContext(), task.id) }, onStatusChange = { task, status -> vm.updateStatus(task.id, status) })
        binding.rvTasks.adapter = adapter
        vm.filteredTasks.observe(viewLifecycleOwner) { tasks -> adapter.submitList(tasks); binding.tvTaskCount.text = "${tasks.size} task${if (tasks.size != 1) "s" else ""}"; binding.emptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE }
        binding.etSearch.addTextChangedListener(object : TextWatcher { override fun afterTextChanged(s: Editable?) { vm.filterSearch.value = s?.toString() ?: "" }; override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}; override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {} })
        binding.btnToggleDone.setOnClickListener { val cur = vm.showDone.value ?: true; vm.showDone.value = !cur; binding.btnToggleDone.text = if (!cur) "\u25CF SHOW DONE" else "\u25CF HIDE DONE"; binding.btnToggleDone.isSelected = !cur }
        binding.btnSort.setOnClickListener { showSortMenu() }; binding.btnFilterCat.setOnClickListener { showCategoryFilter() }; binding.btnFilterPrio.setOnClickListener { showPriorityFilter() }
    }

    private fun showSortMenu() { val options = arrayOf("Newest first", "Oldest first","By priority","By due date"); val modes = arrayOf("new", "old", "prio", "due"); androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Sort tasks").setItems(options) { _, i -> vm.sortMode.value = modes[i] }.show() }

    private fun showCategoryFilter() { val cats = vm.settings.categories; val checked = cats.map { vm.filterTypes.value?.contains(it) == true }.toBooleanArray(); androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Filter by Category").setMultiChoiceItems(cats.toTypedArray(), checked) { _, i, isChecked -> val cur = vm.filterTypes.value?.toMutableList() ?: mutableListOf(); if (isChecked) cur.add(cats[i]) else cur.remove(cats[i]); vm.filterTypes.value = cur }.setPositiveButton("Done") { d, _ -> d.dismiss() }.setNeutralButton("Clear") { d, _ -> vm.filterTypes.value = emptyList(); d.dismiss() }.show() }

    private fun showPriorityFilter() { val prios = arrayOf("Critical", "High", "Medium", "Low"); val checked = prios.map { vm.filterPrios.value?.contains(it) == true }.toBooleanArray(); androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Filter by Priority").setMultiChoiceItems(prios, checked) { _, i, isChecked -> val cur = vm.filterPrios.value?.toMutableList() ?: mutableListOf(); if (isChecked) cur.add(prios[i]) else cur.remove(prios[i]); vm.filterPrios.value = cur }.setPositiveButton("Done") { d, _ -> d.dismiss() }.setNeutralButton("Clear") { d, _ -> vm.filterPrios.value = emptyList(); d.dismiss() }.show() }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
