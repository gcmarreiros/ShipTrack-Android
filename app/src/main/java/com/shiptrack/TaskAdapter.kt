package com.shiptrack

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shiptrack.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onCardClick: (Task) -> Unit,
    private val onStatusChange: (Task, String) -> Unit
) : ListAdapter<Task, TaskAdapter.VH>(DIFF) {
    inner class VH(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH	 {
        val b = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = getItem(position)
        val b = holder.binding
        b.tvTaskId.text = task.id
        b.tvTitle.text = task.title
        b.tvPriority.text = task.priority
        val zones = if (task.zones.isNotEmpty()) task.zones else listOf(task.zone)
        val zoneLabel = when { zones.isEmpty() || zones.all { it.isBlank() } -> ""; zones.size == 1 -> zones[0]; else -> "${zones[0]} +${zones.size - 1}" }
        b.tvMeta.text = "${task.type}  $zoneLabel"
        val borderColor = when (task.priority) { "Critical" -> 0xFFE05020.toInt(); "High" -> 0xFFE63946.toInt(); "Medium" -> 0xFF00BFFF.toInt(); "Low" -> 0xFF2EC4B6.toInt(); else -> Color.TRANSPARENT }
        b.priorityBar.setBackgroundColor(borderColor)
        val statuses = listOf("Open" to b.btnOpen, "In Progress" to b.btnInProgress, "Done" to b.btnDone, "Hold On" to b.btnHoldOn)
        statuses.forEach { (s, btn) -> btn.isSelected = task.status == s; btn.setOnClickListener { if (task.status != s) onStatusChange(task, s) } }
        b.cardTop.setOnClickListener { onCardClick(task) }
    }
    companion object { val DIFF = object : DiffUtil.ItemCallback<Task>() { override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id; override fun areContentsTheSame(a: Task, b: Task) = a == b } }
}
