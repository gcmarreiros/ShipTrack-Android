package com.shiptrack

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = getItem(position)
        val b = holder.binding
        val ctx = holder.itemView.context
        b.tvTaskId.text = task.id
        b.tvTitle.text = task.title
        b.tvPriority.text = task.priority
        val ts = task.createdTs.takeIf { it > 0 } ?: task.created
        val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        b.tvTimestamp.text = if (ts > 0) sdf.format(Date(ts)) else ""
        val zones = if (task.zones.isNotEmpty()) task.zones else listOf(task.zone)
        val zl = when { zones.isEmpty() || zones.all { it.isBlank() } -> ""; zones.size == 1 -> zones[0]; else -> "${zones[0]} +${zones.size - 1}" }
        b.tvMeta.text = "${task.type}  $zl${if(task.photos.isNotEmpty())"   ${task.photos.size}"else""}"
        val bc = when (task.priority) { "Critical" -> 0xFFE05020.toInt(); "High" -> 0xFFE63946.toInt(); "Medium" -> 0xFF00BFFF.toInt(); "Low" -> 0xFF2EC4B6.toInt(); else -> android.graphics.Color.TRANSPARENT }
        b.priorityBar.setBackgroundColor(bc)
        val (pb, pf) = when (task.priority) { "Critical" -> Pair(0x33E05020, 0xFFE07050.toInt()); "High" -> Pair(0x26C0401A, 0xFFD06040.toInt()); "Medium" -> Pair(0x1AE8A000, 0xFFC09020.toInt()); else -> Pair(0x1A2A7A6A, 0xFF5AAA90.toInt()) }
        b.tvPriority.setBackgroundColor(pb)
        b.tvPriority.setTextColor(pf)
        listOf("Open" to b.btnOpen, "In Progress" to b.btnInProgress, "Done" to b.btnDone, "Hold On" to b.btnHoldOn).forEach { (s, btn) -> btn.isSelected = task.status == s; btn.setOnClickListener { if (task.status != s) onStatusChange(task, s) } }
        b.cardTop.setOnClickListener { onCardClick(task) }
    }
    companion object { val DIFF = object : DiffUtil.ItemCallback<Task>() { override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id; override fun areContentsTheSame(a: Task, b: Task) = a == b } }
}
