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

        recentAdapter = TaskAdapter(
            onCardClick    = { task -> TaskDetailActivity.start(requireContext(), task.id) },
            onStatusChange = { task, status -> vm.updateStatus(task.id, status) }
        )
        binding.rvRecent.adapter = recentAdapter

        val sdf = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date())

        vm.allTasks.observe(viewLifecycleOwner) { tasks ->
            updateStats(tasks)
            val recent = tasks.sortedByDescending { it.created }.take(5)
            recentAdapter.submitList(recent)
            binding.btnViewAll.visibility = if (tasks.size > 5) View.VISIBLE else View.GONE
            binding.btnViewAll.text = "View all ${tasks.size} tasks \u2192"
        }

        binding.btnViewAll.setOnClickListener {
            (activity as? MainActivity)?.binding?.bottomNav?.selectedItemId = R.id.nav_tasks
        }

        binding.btnTheme.setOnClickListener {
            val s = vm.settings
            s.theme = if (s.theme == "bluegrey") "light" else "bluegrey"
            binding.btnTheme.text = if (s.theme == "bluegrey") "\uD83C\uDF19" else "\u2600\uFE0F"
        }
        binding.btnTheme.text = if (vm.settings.theme == "bluegrey") "\uD83C\uDF19" else "\u2600\uFE0F"
    }

    private fun updateStats(tasks: List<Task>) {
        val total  = tasks.size
        val open   = tasks.count { it.status == "Open" }
        val inProg = tasks.count { it.status == "In Progress" }
        val done   = tasks.count { it.status == "Done" }
        val hold   = tasks.count { it.status == "Hold On" }
        val crit   = tasks.count { it.priority == "Critical" }
        val photos = tasks.count { it.photos.isNotEmpty() }

        setCard(R.id.cardTotal,  "TOTAL",         total.toString(),        "all zones")
        setCard(R.id.cardCrit,   "CRITICAL/HOLD", "${crit + hold}",        "$crit crit \u00B7 $hold hold")
        setCard(R.id.cardOpen,   "OPEN",          open.toString(),         "awaiting")
        setCard(R.id.cardInProg, "IN PROGRESS",   inProg.toString(),       "active")
        setCard(R.id.cardDone,   "DONE",          done.toString(),         "${if (total > 0) done * 100 / total else 0}%")
        setCard(R.id.cardPhotos, "WITH PHOTOS",   photos.toString(),       "documented")

        val byType = tasks.groupBy { it.type }.mapValues { it.value.size }
        val maxType = byType.values.maxOrNull() ?: 1
        populateBars(binding.catBarsContainer,
            byType.entries.sortedByDescending { it.value }.map {
                "${DefaultData.TYPE_ICONS[it.key] ?: "\u25CC"} ${it.key}" to it.value
            }, maxType)

        val byZone = mutableMapOf<String, Int>()
        tasks.forEach { t ->
            val codes = if (t.zones.isNotEmpty()) t.zones else listOf(t.zone)
            codes.forEach { c -> if (c.isNotBlank()) byZone[c] = (byZone[c] ?: 0) + 1 }
        }
        val maxZone = byZone.values.maxOrNull() ?: 1
        val zoneList = vm.settings.zones
        populateBars(binding.zoneBarsContainer,
            byZone.entries.sortedByDescending { it.value }.take(5).map { (code, n) ->
                (zoneList.find { it.code == code }?.name ?: code) to n
            }, maxZone)
    }

    private fun setCard(cardId: Int, label: String, value: String, sub: String) {
        val card = binding.root.findViewById<View>(cardId) ?: return
        card.findViewById<TextView>(R.id.tvStatLabel)?.text = label
        card.findViewById<TextView>(R.id.tvStatValue)?.text = value
        card.findViewById<TextView>(R.id.tvStatSub)?.text   = sub
    }

    private fun populateBars(container: ViewGroup, rows: List<Pair<String, Int>>, max: Int) {
        container.removeAllViews()
        rows.forEach { (label, count) ->
            val row = layoutInflater.inflate(R.layout.item_bar_row, container, false)
            row.findViewById<TextView>(R.id.tvBarLabel).text    = label
            row.findViewById<TextView>(R.id.tvBarCount).text    = count.toString()
            row.findViewById<ProgressBar>(R.id.progressBar).apply {
                this.max      = max
                this.progress = count
            }
            container.addView(row)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
