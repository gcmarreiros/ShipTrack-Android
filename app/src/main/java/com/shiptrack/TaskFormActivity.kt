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
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success) cameraUri?.let { uri -> addPhotoFromUri(uri) } }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { addPhotoFromUri(it) } }
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) launchCamera() else toast("Camera permission denied") }
    companion object { const val EXTRA_TASK_ID = "task_id"; fun start(ctx: Context, id: String?) { ctx.startActivity(Intent(ctx, TaskFormActivity::class.java).apply { id?.let { putExtra(EXTRA_TASK_ID, it) } }) } }
    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
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
        if (editTaskId != null) { binding.tvFormTitle.text = "Edit Task"; binding.btnDeleteTask.visibility = View.VISIBLE; binding.btnDeleteTask.setOnClickListener { confirmDelete() }; loadExistingTask() } else { binding.tvFormTitle.text = "Register Task"; binding.tvFormSub.text = "NEW ENTRY" }
    }
    private fun setupSpinners() {
        binding.spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vm.settings.categories).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Critical", "High", "Medium", "Low")).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerPriority.setSelection(2)
    }
    private var selectedZones = mutableListOf<String>()
    private fun showZonePicker() {
        val zones = vm.settings.zones
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Select Zone(s)").setMultiChoiceItems(zones.map { "${it.name} (${it.code})" }.toTypedArray(), zones.map { selectedZones.contains(it.code) }.toBooleanArray()) { _, i, chk -> val code = zones[i].code; if (chk) { if (!selectedZones.contains(code)) selectedZones.add(code) } else selectedZones.remove(code) }.setPositiveButton("Done") { d, _ -> updateZoneLabel(); d.dismiss() }.setNeutralButton("Clear") { d, _ -> selectedZones.clear(); updateZoneLabel(); d.dismiss() }.show()
    }
    private fun updateZoneLabel() {
        if (selectedZones.isEmpty()) { binding.btnPickZone.text = "Tap to select zones"; binding.tvSelectedZones.visibility = View.GONE } else { binding.btnPickZone.text = "${selectedZones.size} zone(s) selected"; binding.tvSelectedZones.visibility = View.VISIBLE }
    }
    private fun setupPhotoPreview() {
        photoAdapter = PhotoPreviewAdapter(pendingPhotos) { i -> pendingPhotos.removeAt(i); photoAdapter.notifyDataSetChanged() }
        binding.rvPhotoPreview.layoutManager = GridLayoutManager(this, 3)
        binding.rvPhotoPreview.adapter = photoAdapter
    }
    private fun checkCameraPermission() { if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) launchCamera() else permLauncher.launch(Manifest.permission.CAMERA) }
    private fun launchCamera() {
        val f = File(cacheDir, "photos/cam_${System.currentTimeMillis()}.jpg").also { it.parentFile?.mkdirs() }
        cameraUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
        cameraLauncher.launch(cameraUri)
    }
    private fun addPhotoFromUri(uri: Uri) { try { val bmp = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri); val sc = android.graphics.Bitmap.createScaledBitmap(bmp, minOf(bmp.width, 800), minOf(bmp.height, 800), true); val baos = java.io.ByteArrayOutputStream(); sc.compress(android.graphics.Bitmap.CompressFormat.JPEG,  75, baos); pendingPhotos.add("data:image/jpeg;base64,"+android.util.Base64.encodeToString(baos.toByteArray(),android.util.Base64.NO_WRAP)); photoAdapter.notifyDataSetChanged() } catch (e: Exception) { toast("Error: ${e.message}") } }
    private fun loadExistingTask() { lifecycleScope.launch { val task = AppDatabase.getInstance(this@TaskFormActivity).taskDao().getById(editTaskId!!) ?: return@launch; binding.etTitle.setText(task.title); binding.etDue.setText(task.due); binding.etRef.setText(task.ref); binding.etNotes.setText(task.notes); selectedZones = (task.zones.ifEmpty { listOf(task.zone) }).toMutableList(); updateZoneLabel(); pendingPhotos.clear(); pendingPhotos.addAll(task.photos); photoAdapter.notifyDataSetChanged() } }
    private fun saveTask() { val title = binding.etTitle.text?.toString()?.trim() ?: ""; if (title.isBlank()) { toast("Title required"); return }; val id = editTaskId ?: vm.genId(); val now = System.currentTimeMillis(); lifecycleScope.launch { val existing = if (editTaskId != null) AppDatabase.getInstance(this@TaskFormActivity).taskDao().getById(editTaskId!!) else null; val task = Task(id=id,title=title,type=vm.settings.categories.getOrElse(binding.spinnerType.selectedItemPosition){"Other"},zone=selectedZones.firstOrNull()?:"",zones=selectedZones.toList(),priority=listOf("Critical","High","Medium","Low").getOrElse(binding.spinnerPriority.selectedItemPosition){"Medium"},status=existing?.status?:"Open",due=binding.etDue.text?.toString()?.trim()?:"",ref=binding.etRef.text?.toString()?.trim()?:"",notes=binding.etNotes.text?.toString()?.trim()?:"",photos=pendingPhotos.toList(),created=existing?.created?:now,createdTs=existing?.createdTs?:now); vm.saveTask(task); toast(if (editTaskId != null) "Updated!" else "Registered!"); finish() } }
    private fun confirmDelete() { androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Delete Task").setMessage("Delete $editTaskId? Cannot be undone.").setPositiveButton("Delete") { _, _ -> editTaskId?.let { vm.deleteTask(it) }; toast("Deleted"); finish() }.setNegativeButton("Cancel",null).show() }
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
class PhotoPreviewAdapter(private val photos: List<String>, private val onRemove: (Int) -> Unit) : RecyclerView.Adapter<PhotoPreviewAdapter.VH>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) { val img: ImageView = view.findViewById(R.id.ivPreview); val btnRemove: TextView = view.findViewById(R.id.btnRemovePhoto) }
    override fun onCreateViewHolder(parent: ViewGroup, t: Int): VH = VH(oldf { LayoutInflater.from(parent.context).inflate(R.layout.item_photo_preview, parent, false) })
    private fun oldf(b: () -> View): View = b()
    override fun onBindViewHolder(h: VH, pos: Int) { Glide.with(h.img.context).load(photos[pos]).centerCrop().into(h.img); h.btnRemove.setOnClickListener { onRemove(pos) } }
    override fun getItemCount() = photos.size
}
