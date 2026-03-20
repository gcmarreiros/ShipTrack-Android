package com.shiptrack

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shiptrack.databinding.FragmentGalleryBinding
import com.shiptrack.databinding.ItemPhotoBinding
class GalleryFragment:Fragment(){private var _b:FragmentGalleryBinding?=null;private val b get()=_b!!;private lateinit var vm:TaskViewModel
data class PhotoItem(val src:String,val taskId:String,val taskTitle:String)
override fun onCreateView(i:LayoutInflater,c:ViewGroup?,s:Bundle?):View{_b=FragmentGalleryBinding.inflate(i,c,false);return b.root}
override fun onViewCreated(view:View,s:Bundle?){vm=ViewModelProvider(requireActivity())[TaskViewModel::class.java]
val adapter=PhotoAdapter{item->showPhotoViewer(item)};b.rvPhotos.layoutManager=GridLayoutManager(requireContext(),3);b.rvPhotos.adapter=adapter
vm.allTasks.observe(viewLifecycleOwner){tasks->val photos=mutableListOf<PhotoItem>();tasks.forEach{t->t.photos.forEach{src->photos.add(PhotoItem(src,t.id,t.title))}};adapter.items=photos+adapter.notifyDataSetChanged();b.tvPhotoCount.text="${photos.size} photo${if(photos.size!=1)"s" else ""} across ${tasks.count{it.photos.isNotEmpty()}} tasks";b.emptyState.visibility=if(photos.isEmpty())View.VISIBLE else View.GONE;b.rvPhotos.visibility=if(photos.isEmpty())View.GONE else View.VISIBLE}}
private fun showPhotoViewer(item:PhotoItem){val dialog=android.app.Dialog(requireContext(),android.R.style.Theme_Black_NoTitleBar_Fullscreen);dialog.setContentView(R.layout.dialog_photo_viewer);val iv=dialog.findViewById<android.widget.ImageView>(R.id.ivPhoto);val tv=dialog.findViewById<android.widget.TextView>(R.id.tvCaption);Glide.with(this).load(item.src).into(iv);tv.text="${item.taskId} · ${item.taskTitle}";iv.setOnClickListener{dialog.dismiss()};dialog.show()}
inner class PhotoAdapter(val onClick:(PhotoItem)->Unit):RecyclerView.Adapter<PhotoAdapter.VH>(){var items:List<PhotoItem>=emptyList();inner class VH(val binding:ItemPhotoBinding):RecyclerView.ViewHolder(binding.root);override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):VH{val b=ItemPhotoBinding.inflate(LayoutInflater.from(parent.context),parent,false);return VH(b)};override fun onBindViewHolder(holder:VH,position:Int){val item=items[position];Glide.with(holder.itemView.context).load(item.src).centerCrop().into(holder.binding.ivPhoto);holder.binding.tvTaskId.text=item.taskId;holder.itemView.setOnClickListener{onClick(item)}};override fun getItemCount()=items.size}
override fun onDestroyView(){super.onDestroyView();_b=null}}
