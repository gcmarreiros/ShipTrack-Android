package com.shiptrack

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.shiptrack.databinding.ActivityTaskDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var vm: TaskViewModel
    private var taskId: String = ""
    companion object {
        const val EXTRA_TASK_ID = "task_id"
        fun start(context: Context, taskId: String) {
            context.startActivity(Intent(context, TaskDetailActivity::class.java).apply { putExtra(EXTRA_TASK_ID, taskId) })
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm = ViewModelProvider(this)[TaskViewModel::class.java]
        taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: run { finish(); return }
        binding.btnDetailClose.setOnClickListener { finish() }
        vm.allTasks.observe(this) { tasks -> tasks.find { it.id == taskId }?.let { bindTask(it) } }
    }
    private fun bindTask(task: Task) {
        binding.tvDetailTitle.text = task.title
        val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        val ts = task.createdTs.takeIf { it > 0 } ?: task.created
        binding.tvDetailSub.text = "${task.id}  ${task.type}${if (ts > 0) "  ${sdf.format(java.util.Date(ts))}" else ""}"
        val zones = vm.settings.zones
        val taskZones = if (task.zones.isNotEmpty()) task.zones else listOf(task.zone)
        binding.tvDetailZone.text = taskZones.mapNotNull { c -> zones.find { it.code == c }?.name ?: c }.joinToString("\n").ifBlank { "" }
        binding.tvDetailRef.text = task.ref.ifBlank { "" }
        binding.tvDetailDue.text = task.due.ifBlank { "" }
        if (task.notes.isNotBlank()) { binding.tvNotesSection.visibility = View.VISIBLE; binding.tvDetailNotes.visibility = View.VISIBLE; binding.tvDetailNotes.text = task.notes
        } else { binding.tvNotesSection.visibility = View.GONE; binding.tvDetailNotes.visibility = View.GONE }
        binding.tvPriorityBadge.text = "${task.priority} v"
        highlightStatus(task.status)
        val sh = { s: String -> if (task.status != s) { vm.updateStatus(task.id, s); toast(" $s") } }
        binding.btnStatusOpen.setOnClickListener { sh("Open") }
        binding.btnStatusInProg.setOnClickListener { sh("In Progress") }
        binding.btnStatusDone.setOnClickListener { sh("Done") }
        binding.btnStatusHold.setOnClickListener { sh("Hold On") }
        binding.tvPhotosLabel.text = "PHOTOS (${task.photos.size})"
        if (task.photos.isEmpty()) { binding.rvDetailPhotos.visibility = View.GONE; binding.tvNoPhotos.visibility = View.VISIBLE
        } else { binding.tvNoPhotos.visibility = View.GONE; binding.rvDetailPhotos.visibility = View.VISIBLE; binding.rvDetailPhotos.layoutManager = GridLayoutManager(this, 3) }
        binding.btnDetailEdit.setOnClickListener { TaskFormActivity.start(this, task.id) }
        binding.btnDetailShare.setOnClickListener { shareTask(task) }
    }
    private fun highlightStatus(status: String) {
        mapOf("Open" to binding.btnStatusOpen, "In Progress" to binding.btnStatusInProg, "Done" to binding.btnStatusDone, "Hold On" to binding.btnStatusHold).forEach { (s, btn) -> btn.isSelected = s == status }
    }
    private fun shareTask(task: Task) {
        val text = "${task.id}\n${task.title}\nStatus: ${task.status}\nPriority: ${task.priority}"
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share Task"))
    }
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

class DetailPhotoAdapter(private val photos: List<String>, private val onClick: (String) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<DetailPhotoAdapter.VI>() {
    inner class VH(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) { val img: android.widget.ImageView = view.findViewById(R.id.ivPhoto) }
    override fun onCreateViewHolder(parent: android.view.ViewGroup, vT: Int): VH { val v = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_photo_preview, parent, false); return VH(v) }
    override fun onBindViewHolder(h: VH, pos: Int) { Glide.with(h.img.context).load(photos[pos]).centerCrop().into(h.img); h.itemView.setOnClickListener { onClick(photos[pos]) } }
    override fun getItemCount() = photos.size
}
