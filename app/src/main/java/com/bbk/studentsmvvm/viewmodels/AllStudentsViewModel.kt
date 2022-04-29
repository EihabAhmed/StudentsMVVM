package com.bbk.studentsmvvm.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.*
import com.bbk.studentsmvvm.data.Repository
import com.bbk.studentsmvvm.data.database.entities.StudentEntity
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.models.Students
import com.bbk.studentsmvvm.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class AllStudentsViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    /** ROOM DATABASE */
    val readStudents: LiveData<List<StudentEntity>> = repository.local.readStudents().asLiveData()

    private fun insertStudent(studentEntity: StudentEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertStudent(studentEntity)
        }

    fun deleteStudent(studentEntity: StudentEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteStudent(studentEntity)
        }

    fun deleteAllStudents() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteAllStudents()
        }

    /** RETROFIT */
    var allStudentsResponse: MutableLiveData<NetworkResult<Students>> = MutableLiveData()

    fun getAllStudents() = viewModelScope.launch {
        getAllStudentsSafeCall()
    }

    private suspend fun getAllStudentsSafeCall() {
        allStudentsResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.getAllStudents()
                allStudentsResponse.value = handleAllStudentsResponse(response)

                val students = allStudentsResponse.value!!.data
                if (students != null) {
                    offlineCacheStudents(students)
                }
            } catch (e: Exception) {
                allStudentsResponse.value = NetworkResult.Error("No students found.")
            }
        } else {
            allStudentsResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private fun offlineCacheStudents(students: Students) {
        for (student in students.students) {
            val studentEntity = StudentEntity(student)
            insertStudent(studentEntity)
        }

    }

    private fun handleAllStudentsResponse(response: Response<Students>): NetworkResult<Students> {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited.")
            }
            response.body()!!.students.isNullOrEmpty() -> {
                return NetworkResult.Error("No students found.")
            }
            response.isSuccessful -> {
                val students = response.body()
                return NetworkResult.Success(students!!)
            }
            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}