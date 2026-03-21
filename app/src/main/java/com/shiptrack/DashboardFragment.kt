package com.shiptrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shiptrack.databinding.FragmentDashboardBinding
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var vm: TaskViewModel
    private lateinit var recentAdapter: TaskAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        recentAdapter = TaskAdapter(onCardClick = { t -> TaskDetailActivity.start(requireContext(), t.id) }, onStatusChange = { t, s -> vm.updateStatus(t.id, s) })
        binding.rvRecent.adapter = recentAdapter
        binding.tvDate.text = SimpleDateFormat("EEE, d MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        vm.allTasks.observe(viewLifecycleOwner) { tasks ->
            updateStats(tasks)
            recentAdapter.submitList(tasks.sortedByDescending { it.created }.take(5))
        }
    }
    private fun updateStats(tasks: List<Task>) {
        val total = tasks.size; val open = tasks.count { it.status == "Open" }
        val inProg = tasks.count { it.status == "In Progress" }; val done = tasks.count { it.status == "Done" }
        val crit = tasks.count { it.priority == "Critical" }; val photos = tasks.count { it.photos.isNotEmpty() }
        setCard(R.id.cardTotal, "TOTAL", total.toString(), "all zones")
        setCard(R.id.cardCrit, "CRITICAL", crit.toString(), "$crit critical")
        setCard(R.id.cardOpen, "OPEN", open.toString(), "awaiting")
        setCard(R.id.cardInProg, "IN PROGRESS", inProg.toString(), "active")
        setCard(R.id.cardDone, "DONE", done.toString(), "${if (total > 0) done * 100 / total else 0}%")
        setCard(R.id.cardPhotos, "WITH PHOTOS", photos.toString(), "documented")
    }
    private fun setCard(cardId: Int, label: String, value: String, sub: String) {
        val card = binding.root.findViewById<View>(cardId) ?: return
        card.findViewById<TextView>(R.id.tvStatLabel)?.text = label
        card.findViewById<TextView>(R.id.tvStatValue)?.text = value
        card.findViewById<TextView>(R.id.tvStatSub)?.text = sub
    }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
