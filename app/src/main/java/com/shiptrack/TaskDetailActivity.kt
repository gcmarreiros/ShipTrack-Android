package com.shiptrack

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.shiptrack.databinding.ActivityTaskDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var vm: TaskViewModel
    private var taskId: String = ""

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        fun start(context: Context, taskId: String) {
            context.startActivity(Intent(context, TaskDetailActivity::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm = ViewModelProvider(this)[TaskViewModel::class.java]
        taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: run { finish(); return }

        binding.btnDetailClose.setOnClickListener { finish() }

        vm.allTasks.observe(this) { tasks ->
            tasks.find { it.id == taskId }?.let { bindTask(it) }
        }
    }

    private fun bindTask(task: Task) {
        binding.tvDetailTitle.text = task.title

        val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        val ts = task.createdTs.takeIf { it > 0 } ?: task.created
        val tsStr = if (ts > 0) " \u00B7 ${sdf.format(Date(ts))}" else ""
        binding.tvDetailSub.text = "${task.id} \u00B7 ${task.type}$tsStr"

        val zones = vm.settings.zones
        val taskZones = if (task.zones.isNotEmpty()) task.zones else listOf(task.zone)
        val zoneNames = taskZones.mapNotNull { code -> zones.find { it.code == code }?.name ?: code }
        binding.tvDetailZone.text = zoneNames.joinToString("\n\u2022 ", prefix = if (zoneNames.size > 1) "\u2022 " else "")
            .ifBlank { "\u2014" }

        binding.tvDetailRef.text = task.ref.ifBlank { "\u2014" }
        binding.tvDetailDue.text = task.due.ifBlank { "\u2014" }

        if (task.notes.isNotBlank()) {
            binding.tvNotesSection.visibility = View.VISIBLE
            binding.tvDetailNotes.visibility = View.VISIBLE
            binding.tvDetailNotes.text = task.notes
        } else {
            binding.tvNotesSection.visibility = View.GONE
            binding.tvDetailNotes.visibility = View.GONE
        }

        val (prioBg, prioFg) = when (task.priority) {
            "Critical" -> Pair(0x33E05020, 0xFFE07050.toInt())
            "High"     -> Pair(0x26C0401A, 0xFFD06040.toInt())
            "Medium"   -> Pair(0x1AE8A000, 0xFFC09020.toInt())
            "Low"      -> Pair(0x1A2A7A6A, 0xFF5AAA90.toInt())
            else       -> Pair(0x1AE8A000, 0xFFC09020.toInt())
        }
        binding.tvPriorityBadge.setBackgroundColor(prioBg)
        binding.tvPriorityBadge.setTextColor(prioFg)
        binding.tvPriorityBadge.text = "${task.priority} \u25BE"
        binding.tvPriorityBadge.setOnClickListener { showPriorityPicker(task) }

        highlightStatus(task.status)
        val statusHandler = { status: String ->
            if (task.status != status) {
                vm.updateStatus(task.id, status)
                toast("\u2192 $status")
            }
        }
        binding.btnStatusOpen.setOnClickListener   { statusHandler("Open") }
        binding.btnStatusInProg.setOnClickListener { statusHandler("In Progress") }
        binding.btnStatusDone.setOnClickListener   { statusHandler("Done") }
        binding.btnStatusHold.setOnClickListener   { statusHandler("Hold On") }

        binding.tvPhotosLabel.text = "PHOTOS (${task.photos.size})"
        if (task.photos.isEmpty()) {
            binding.rvDetailPhotos.visibility = View.GONE
            binding.tvNoPhotos.visibility = View.VISIBLE
        } else {
            binding.tvNoPhotos.visibility = View.GONE
            binding.rvDetailPhotos.visibility = View.VISIBLE
            val adapter = DetailPhotoAdapter(task.photos) { src -> showPhotoViewer(src, task.title) }
            binding.rvDetailPhotos.layoutManager = GridLayoutManager(this, 3)
            binding.rvDetailPhotos.adapter = adapter
        }

        binding.btnDetailEdit.setOnClickListener { TaskFormActivity.start(this, task.id) }
        binding.btnAddPhoto.setOnClickListener { TaskFormActivity.start(this, task.id); toast("Open edit to add photos") }
        binding.btnDetailShare.setOnClickListener { shareTask(task) }
    }

    private fun highlightStatus(status: String) {
        val btns = mapOf("Open" to binding.btnStatusOpen, "In Progress" to binding.btnStatusInProg, "Done" to binding.btnStatusDone, "Hold On" to binding.btnStatusHold)
        val activeBg = mapOf("Open" to Pair(0x26FF6B3D, 0xFFFF8C63.toInt()), "In Progress" to Pair(0x2E388BFD, 0xFF58A6FF.toInt()), "Done" to Pair(0x262EC4B6, 0xFF3DDDD0.toInt()), "Hold On" to Pair(0x26E63946, 0xFFFF6B77.toInt()))
        btns.forEach { (s, btn) -> if (s == status) { val (bg, fg) = activeBg[s]!!; btn.setBackgroundColor(bg); btn.setTextColor(fg) } else { btn.setBackgroundResource(R.drawable.status_btn_bg); btn.setTextColor(getColor(R.color.muted)) } }
    }

    private fun showPriorityPicker(task: Task) {
        val prios = arrayOf("Critical", "High", "Medium", "Low")
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Change Priority").setItems(prios) { _, i -> vm.updatePriority(task.id, prios[i]); toast("Priority \u2192 ${prios[i]}") }.show()
    }

    private fun shareTask(task: Task) {
        val zones = vm.settings.zones; val taskZones = if (task.zones.isNotEmpty()) task.zones else listOf(task.zone)
        val zoneNames = taskZones.mapNotNull { code -> zones.find { it.code == code }?.name }
        val text = buildString { appendLine("\uD83D\uDCCB ${task.id}"); appendLine("Title: ${task.title}"); appendLine("Status: ${task.status}"); appendLine("Priority: ${task.priority}"); appendLine("Category: ${task.type}"); if (zoneNames.isNotEmpty()) appendLine("Zone: ${zoneNames.joinToString(", ")}"); if (task.due.isNotBlank()) appendLine("Due: ${task.due}"); if (task.ref.isNotBlank()) appendLine("Ref: ${task.ref}"); if (task.notes.isNotBlank()) appendLine("Notes: ${task.notes}") }
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share Task"))
    }

    private fun showPhotoViewer(src: String, caption: String) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen); dialog.setContentView(R.layout.dialog_photo_viewer)
        val iv = dialog.findViewById<android.widget.ImageView>(R.id.ivPhoto); val tv = dialog.findViewById<android.widget.TextView>(R.id.tvCaption); val btnClose = dialog.findViewById<android.widget.Button>(R.id.btnClose)
        Glide.with(this).load(src).into(iv); tv.text = caption; iv.setOnClickListener { dialog.dismiss() }; btnClose.setOnClickListener { dialog.dismiss() }; dialog.show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

class DetailPhotoAdapter(private val photos: List<String>, private val onClick: (String) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<DetailPhotoAdapter.VH>() {
    inner class VH(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) { val img: android.widget.ImageView = view.findViewById(R.id.ivPhoto); val taskId: android.widget.TextView = view.findViewById(R.id.tvTaskId) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH { val v = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false); return VH(v) }
    override fun onBindViewHolder(holder: VH, position: Int) { Glide.with(holder.img.context).load(photos[position]).centerCrop().into(holder.img); holder.taskId.visibility = View.GONE; holder.itemView.setOnClickListener { onClick(photos[position]) } }
    override fun getItemCount() = photos.size
}
