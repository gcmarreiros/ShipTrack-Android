package com.shiptrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shiptrack.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var vm: TaskViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm = ViewModelProvider(this)[TaskViewModel::class.java]
        lifecycleScope.launch { vm.seedIfEmpty() }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, DashboardFragment()).commit()
            binding.bottomNav.selectedItemId = R.id.nav_dashboard
        }
        binding.bottomNav.setOnItemSelectedListener { item ->
            val frag = when (item.itemId) { R.id.nav_dashboard -> DashboardFragment(); R.id.nav_tasks -> TasksFragment(); R.id.nav_gallery -> GalleryFragment(); R.id.nav_settings -> SettingsFragment(); else -> DashboardFragment() }
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, frag).commit()
            true
        }
        binding.fabAdd.setOnClickListener { TaskFormActivity.start(this, null) }
    }
}
