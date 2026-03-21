package com.shiptrack

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).taskDao()
    val settings = SettingsStore(application)

    val allTasks: LiveData<List<Task>> = dao.getAllLive()

    // Filter state (observed by UI)
    val filterTypes = MutableLiveData<List<String>>(emptyList())
    val filterPrios = MutableLiveData<List<String>>(emptyList())
    val filterSearch = MutableLiveData<String>("")
    val showDone = MutableLiveData<Boolean>(true)
    val sortMode = MutableLiveData<String>("new") // new, old, prio, due

    /** Filtered + sorted task list derived from allTasks + filters */
    val filteredTasks: LiveData<List<Task>> = MediatorLiveData<List<Task>>().apply {
        value = emptyList()
        fun recompute() {
            val base = allTasks.value ?: emptyList()
            val types = filterTypes.value ?: emptyList()
            val prios = filterPrios.value ?: emptyList()
            val search = filterSearch.value ?: ""
            val done = showDone.value ?: true
            val sort = sortMode.value ?: "new"

            var result = base
            if (!done) result = result.filter { it.status != "Done" }
            if (types.isNotEmpty()) result = result.filter { it.type in types }
            if (prios.isNotEmpty()) result = result.filter { it.priority in prios }
            if (search.isNotBlank()) {
                val q = search.lowercase()
                result = result.filter {
                    it.title.lowercase().contains(q) ||
                    it.id.lowercase().contains(q) ||
                    it.type.lowercase().contains(q) ||
                    it.notes.lowercase().contains(q)
                }
            }
            result = when (sort) {
                "old"  -> result.sortedBy { it.created }
                "prio" -> result.sortedBy { listOf("Critical","High","Medium","Low").indexOf(it.priority) }
                "due"  -> result.sortedBy { it.due }
                else   -> result.sortedByDescending { it.created }
            }
            value = result
        }
        addSource(allTasks)    { recompute() }
        addSource(filterTypes) { recompute() }
        addSource(filterPrios) { recompute() }
        addSource(filterSearch){ recompute() }
        addSource(showDone)    { recompute() }
        addSource(sortMode)    { recompute() }
    }

    fun saveTask(task: Task) = viewModelScope.launch { dao.insert(task) }
    fun deleteTask(id: String) = viewModelScope.launch { dao.deleteById(id) }
    fun updateStatus(id: String, status: String) = viewModelScope.launch {
        dao.getById(id)?.let { dao.update(it.copy(status = status)) }
    }
    fun updatePriority(id: String, priority: String) = viewModelScope.launch {
        dao.getById(id)?.let { dao.update(it.copy(priority = priority)) }
    }

    suspend fun seedIfEmpty() {
        if (!settings.seeded && dao.count() == 0) {
            dao.insertAll(DefaultData.SEED_TASKS)
            settings.seeded = true
        }
    }

    fun genId(): String {
        val tasks = allTasks.value ?: emptyList()
        val max = tasks.mapNotNull { t ->
            t.id.replace("TASK ", "").trim().toIntOrNull()
        }.maxOrNull() ?: 0
        return "TASK " + (max + 1).toString().padStart(3, '0')
    }
}
