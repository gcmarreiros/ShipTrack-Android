package com.shiptrack

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).taskDao()
    val settings = SettingsStore(application)
    val allTasks: LiveData<List<Task>> = dao.getAllLive()
    val filterTypes = MutableLiveData<List<String>>(emptyList())
    val filterPrios = MutableLiveData<List<String>>(emptyList())
    val filterSearch = MutableLiveData<String>("")
    val showDone = MutableLiveData<Boolean>(true)
    val sortMode = MutableLiveData<String>("new")
    val filteredTasks: LiveData<List<Task>> = MediatorLiveData<List<Task>>().apply {
        fun recompute() {
            val base = allTasks.value ?: return
            var result = base
            if (showDone.value != true) result = result.filter { it.status != "Done" }
            filterTypes.value?.takeIf { it.isNotEmpty() }?.let { types -> result = result.filter { it.type in types } }
            filterPrios.value?.takeIf { it.isNotEmpty() }?.let { prios -> result = result.filter { it.priority in prios } }
            filterSearch.value?.takeIf { it.isNotBlank() }?.let { q -> result = result.filter { it.title.contains(q, true) || it.id.contains(q, true) || it.notes.contains(q, true) } }
            result = when (sortMode.value) {
                "old" -> result.sortedBy { it.created }
                "prio" -> result.sortedBy { listOf("Critical","High","Medium","Low").indexOf(it.priority) }
                "due" -> result.sortedBy { it.due }
                else -> result.sortedByDescending { it.created }
            }
            value = result
        }
        listOf(allTasks, filterTypes, filterPrios, filterSearch, showDone, sortMode).forEach { addSource(it) { recompute() } }
    }
    fun saveTask(task: Task) = viewModelScope.launch { dao.insert(task) }
    fun deleteTask(id: String) = viewModelScope.launch { dao.deleteById(id) }
    fun updateStatus(id: String, status: String) = viewModelScope.launch { dao.getById(id)?.let { dao.update(it.copy(status = status)) } }
    fun updatePriority(id: String, priority: String) = viewModelScope.launch { dao.getById(id)?.let { dao.update(it.copy(priority = priority)) } }
    suspend fun seedIfEmpty() { if (!settings.seeded && dao.count() == 0) { dao.insertAll(DefaultData.SEED_TASKS); settings.seeded = true } }
    fun genId(): String { val max = allTasks.value?.mapNotNull { it.id.replace("TASK ","").trim().toIntOrNull() }?.maxOrNull() ?: 0; return "TASK " + (max+1).toString().padStart(3,'0') }
}
