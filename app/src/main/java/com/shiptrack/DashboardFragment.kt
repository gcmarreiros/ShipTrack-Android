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
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _binding = FragmentDashboardBinding.inflate(i, c, false); return binding.root }
    override fun onViewCreated(view: View, s: Bundle?) {
        vm = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        recentAdapter = TaskAdapter({ task -> TaskDetailActivity.start(requireContext(), task.id) }, { task, status -> vm.updateStatus(task.id, status) })
        binding.rvRecent.adapter = recentAdapter
        binding.tvDate.text = SimpleDateFormat("EEE, d MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        vm.allTasks.observe(viewLifecycleOwner) { tasks ->
            updateStats(tasks)
            recentAdapter.submitList(tasks.sortedByDescending { it.created }.take(5))
            binding.btnViewAll.visibility = if (tasks.size > 5) View.VISIBLE else View.GONE
            binding.btnViewAll.text = "View all ${tasks.size} tasks "
        }
        binding.btnViewAll.setOnClickListener { (activity as? MainActivity)?.binding?.bottomNav?.selectedItemId = R.id.nav_tasks }
        binding.btnTheme.setOnClickListener { val s = vm.settings; s.theme = if (s.theme == "bluegrey") "light" else "bluegrey"; binding.btnTheme.text = if (s.theme == "bluegrey") "" else "" }
        binding.btnTheme.text = if (vm.settings.theme == "bluegrey") "" else ""
    }
    private fun updateStats(tasks: List<Task>) {
        val total=tasks.size; val open=tasks.count{it.status=="Open"}; val inProg=tasks.count{it.status=="In Progress"}; val done=tasks.count{it.status=="Done"}; val hold=tasks.count{it.status=="Hold On"}; val crit=tasks.count{it.priority=="Critical"}; val photos=tasks.count{it.photos.isNotEmpty()}
        fun sc(id:Int,l:String,v:String,sub:String){val c=binding.root.findViewById<View>(id)?:return; c.findViewById<TextView>(R.id.tvStatLabel)?.text=l; c.findViewById<TextView>(R.id.tvStatValue)?.text=v; c.findViewById<TextView>(R.id.tvStatSub)?.text=sub}
        sc(R.id.cardTotal,"TOTAL","$total","all zones"); sc(R.id.cardCrit,"CRITICAL/HOLD","${crit+hold}","$crit crit"); sc(R.id.cardOpen,"OPEN","$open","awaiting"); sc(R.id.cardInProg,"IN PROGRESS","$inProg","active"); sc(R.id.cardDone,"DONE","$done","${if(total>0)done*100/total else 0}%"); sc(R.id.cardPhotos,"WITH PHOTOS","$photos","documented")
        val byType=tasks.groupBy{it.type}.mapValues{it.value.size}; val mx=(byType.values.maxOrNull()?:1)
        fun pb(c:ViewGroup,rows:List<Pair<String,Int>>,mx:Int){c.removeAllViews(); rows.forEach{(l,n)->val r=layoutInflater.inflate(R.layout.item_bar_row,c,false); r.findViewById<TextView>(R.id.tvBarLabel).text=l; r.findViewById<TextView>(R.id.tvBarCount).text="$n"; r.findViewById<ProgressBar>(R.id.progressBar).apply{this.max=mx;this.progress=n}; c.addView(r)}}
        pb(binding.catBarsContainer,byType.entries.sortedByDescending{it.value}.map{"${DefaultData.TYPE_ICONS[it.key]} ${it.key}"to it.value},mx)
        val bz=mutableMapOf<String,Int>(); tasks.forEach{t->(if(t.zones.isNotEmpty())t.zones else listOf(t.zone)).forEach{c->if(c.isNotBlank())bz[c]=(bz[c]?:0)+1}}
        val mz?(bz.values.maxOrNull()?:1); val zk=vm.settings.zones
        pb(binding.zoneBarsContainer,bz.entries.sortedByDescending{it.value}.take(5).map{(code,n)->(zk.find{it.code==code}?.name?:code)to n},mz)
    }
    override fun onDestroyView() { super.onDestroyView(); _binding=null }
}
