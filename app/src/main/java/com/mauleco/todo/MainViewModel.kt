package com.mauleco.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauleco.todo.models.Todo
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.log

class MainViewModel: ViewModel() {

    private val realm = MyApp.realm

    val todos = realm
        .query<Todo>()
        .asFlow()
        .map { results ->
            results.list.toList()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )

    init {
//        createSampleData()
    }

    fun clearAllTodos() {
        realm.writeBlocking {
            deleteAll()
        }
    }

    fun clearTodo(todo: Todo) {
        realm.writeBlocking {
            val latestTodo = findLatest(todo)
            if (latestTodo != null) {
                this.delete(latestTodo)
            }
        }
    }

    fun updateTodo(todo: Todo, uNote: String, uStatus: Boolean? ) {
        realm.writeBlocking {
            val latestTodo = findLatest(todo)
            if (latestTodo != null) {
                if (uNote.isNotBlank()){
                    latestTodo.note = uNote
                }
                if (uStatus != latestTodo.status) {
                    latestTodo.status = uStatus
                }
                copyToRealm(latestTodo, updatePolicy = UpdatePolicy.ALL)
            }
        }
    }

    private fun getSize() : Int{
        return todos.value.size
    }

    fun addTask(pNote : String) {
        viewModelScope.launch {
            realm.write {
                val todo = Todo().apply {
                    itemNumber = getSize() + 1
                    note = pNote
                    status = null
                    completionTime = null
                }
                copyToRealm(todo, updatePolicy = UpdatePolicy.ALL)
            }
        }
    }

    private fun createSampleData() {
        viewModelScope.launch {
            realm.write {
                val todo1 = Todo().apply {
                    itemNumber = 0
                    note = "Visit the library"
                    status = null
                    completionTime = null
                }
                val todo2 = Todo().apply {
                    itemNumber = 1
                    note = "Consume food"
                    status = true
                    completionTime = null
                }
                val todo3 = Todo().apply {
                    itemNumber = 2
                    note = "Drink water"
                    status = false
                    completionTime = null
                }

                copyToRealm(todo1, updatePolicy = UpdatePolicy.ALL)
                copyToRealm(todo2, updatePolicy = UpdatePolicy.ALL)
                copyToRealm(todo3, updatePolicy = UpdatePolicy.ALL)

            }
        }
    }

}