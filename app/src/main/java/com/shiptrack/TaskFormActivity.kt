package com.shiptrack

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shiptrack.databinding.ActivityTaskFormBinding
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

class TaskFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskFormBinding
    private lateinit var vm: TaskViewModel
    private var editTaskId: String? = null
    private val pendingPhotos = mutableListOf<String>()
    private var cameraUri: Uri? = null
    private lateinit var photoAdapter: PhotoPreviewAdapter
    private var selectedZones = mutableListOf<String>()
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success) cameraUri?.let { uri -> addPhotoFromUri(uri) } }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { addPhotoFromUri(it) } }
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) launchCamera() else toast("Camera permission denied") }
    companion object {
        const val EXTRA_TASK_ID = "task_id"
        fun start(context: Context, taskId: String?) { context.startActivity(Intent(context, TaskFormActivity::class.java).apply { taskId?.let { putExtra(EXTRA_TASK_ID, it) } }) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm = ViewModelProvider(this)[TaskViewModel::class.java]
        editTaskId = intent.getStringExtra(EXTRA_TASK_ID)
        setupSpinners(); setupPhotoPreview()
        binding.btnClose.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnCamera.setOnClickListener { checkCameraPermission() }
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnPickZone.setOnClickListener { showZonePicker() }
        binding.btnSave.setOnClickListener { saveTask() }
        if (editTaskId != null) { binding.tvFormTitle.text = "Edit Task"; binding.tvFormSub.text = "EDITING $editTaskId"; binding.btnDeleteTask.visibility = View.VISIBLE; binding.btnDeleteTask.setOnClickListener { confirmDelete() }; loadExistingTask() }
    }
    private fun setupSpinners() {
        val cats = vm.settings.categories
        binding.spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cats).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Critical","High","Medium","Low")).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerPriority.setSelection(2)
    }
    private fun showZonePicker() {
        val zones = vm.settings.zones
        val names = zones.map { "${it.name} (${it.code})" }.toTypedArray()
        val checked = zones.map { selectedZones.contains(it.code) }.toBooleanArray()
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Select Zone(s)").setMultiChoiceItems(names, checked) { _, i, isChecked -> val code = zones[i].code; if (isChecked) { if (!selectedZones.contains(code)) selectedZones.add(code) } else selectedZones.remove(code) }.setPositiveButton("Done") { d, _ -> updateZoneLabel(); d.dismiss() }.setNeutralButton("Clear") { d, _ -> selectedZones.clear(); updateZoneLabel(); d.dismiss() }.show()
    }
    private fun updateZoneLabel() {
        if (selectedZones.isEmpty()) { binding.btnPickZone.text = "Tap to select zones"; binding.tvSelectedZones.visibility = View.GONE
        } else { binding.btnPickZone.text = "${selectedZones.size} zone(s) selected"; binding.tvSelectedZones.visibility = View.VISIBLE }
    }
    private fun setupPhotoPreview() {
        photoAdapter = PhotoPreviewAdapter(pendingPhotos) { index -> pendingPhotos.removeAt(index); photoAdapter.notifyDataSetChanged() }
        binding.rvPhotoPreview.layoutManager = GridLayoutManager(this, 3)
        binding.rvPhotoPreview.adapter = photoAdapter
    }
    private fun checkCameraPermission() { if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) launchCamera() else permLauncher.launch(Manifest.permission.CAMERA) }
    private fun launchCamera() {
        val f = File(cacheDir, "photos/cam_${System.currentTimeMillis()}.jpg").also { it.parentFile?.mkdirs() }
        cameraUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
        cameraLauncher.launch(cameraUri)
    }
    private fun addPhotoFromUri(uri: Uri) {
        try {
            val bmp = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val scaled = Bitmap.createScaledBitmap(bmp, minOf(bmp.width, 800), minOf(bmp.height, 800), true)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 75, baos)
            val b64 = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            pendingPhotos.add(b64); photoAdapter.notifyDataSetChanged()
        } catch (e: Exception) { toast("Could not load photo: ${e.message}") }
    }
    private fun loadExistingTask() {
        lifecycleScope.launch {
            val task = AppDatabase.getInstance(this@TaskFormActivity).taskDao().getById(editTaskId!!) ?: return@launch
            binding.etTitle.setText(task.title); binding.etDue.setText(task.due)
            binding.etRef.setText(task.ref); binding.etNotes.setText(task.notes)
            val cats = vm.settings.categories
            binding.spinnerType.setSelection(cats.indexOf(task.type).coerceAtLeast(0))
            binding.spinnerPriority.setSelection(listOf("Critical","High","Medium","Low").indexOf(task.priority).coerceAtLeast(0))
            selectedZones = (task.zones.ifEmpty { listOf(task.zone) }).toMutableList(); updateZoneLabel()
            pendingPhotos.clear(); pendingPhotos.addAll(task.photos); photoAdapter.notifyDataSetChanged()
        }
    }
    private fun saveTask() {
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        if (title.isBlank()) { toast("Task title is required"); return }
        val cats = vm.settings.categories
        val type = cats.getOrElse(binding.spinnerType.selectedItemPosition) { "Other" }
        val priority = listOf("Critical","High","Medium","Low").getOrElse(binding.spinnerPriority.selectedItemPosition) { "Medium" }
        val id = editTaskId ?: vm.genId(); val now = System.currentTimeMillis(); val zones = selectedZones.toList()
        lifecycleScope.launch {
            val existing = if (editTaskId != null) AppDatabase.getInstance(this@TaskFormActivity).taskDao().getById(editTaskId!!) else null
            val task = Task(id=id,title=title,type=type,zone=zones.firstOrNull()?:"",zones=zones,priority=priority,status=existing?.status?:"Open",due=binding.etDue.text?.toString()?.trim()?:"",ref=binding.etRef.text?.toString()?.trim()?:"",notes=binding.etNotes.text?.toString()?.trim()?:"",photos=pendingPhotos.toList(),created=existing?.created?:now,createdTs=existing?.createdTs?:now)
            vm.saveTask(task); toast(if (editTaskId != null) "Task updated" else "Task registered!"); finish()
        }
    }
    private fun confirmDelete() { androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Delete Task").setMessage("Delete $editTaskId?").setPositiveButton("Delete") { _, _ -> editTaskId?.let { vm.deleteTask(it) }; toast("Deleted"); finish() }.setNegativeButton("Cancel", null).show() }
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

class PhotoPreviewAdapter(private val photos: List<String>, private val onRemove: (Int) -> Unit) : RecyclerView.Adapter<PhotoPreviewAdapter.VH>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.ivPreview)
        val btnRemove: TextView = view.findViewById(R.id.btnRemovePhoto)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_preview, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        Glide.with(holder.img.context).load(photos[position]).centerCrop().into(holder.img)
        holder.btnRemove.setOnClickListener { onRemove(position) }
    }
    override fun getItemCount() = photos.size
}
