package com.bbk.studentsmvvm.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.*
import com.bbk.studentsmvvm.data.Repository
import com.bbk.studentsmvvm.data.database.entities.StudentEntity
import com.bbk.studentsmvvm.models.DeleteStudentsModel
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

    private fun insertStudentIntoDatabase(studentEntity: StudentEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertStudent(studentEntity)
        }

//    fun deleteStudent(studentEntity: StudentEntity) =
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.local.deleteStudent(studentEntity)
//        }

    fun deleteAllStudentsFromDatabase() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteAllStudents()
        }

    /** RETROFIT */
    var allStudentsResponse: MutableLiveData<NetworkResult<Students>> = MutableLiveData()
    var addStudentResponse: MutableLiveData<NetworkResult<Student>> = MutableLiveData()
    var updateStudentResponse: MutableLiveData<NetworkResult<Student>> = MutableLiveData()
    var deleteStudentsResponse: MutableLiveData<NetworkResult<String>> = MutableLiveData()

    fun getAllStudents() = viewModelScope.launch {
        getAllStudentsSafeCall()
    }

    fun addStudent(student: Student) = viewModelScope.launch {
        addStudentSafeCall(student)
    }

    fun updateStudent(student: Student) = viewModelScope.launch {
        updateStudentSafeCall(student)
    }

    fun deleteStudent(id: Int) = viewModelScope.launch {
        deleteStudentSafeCall(id)
    }

    fun deleteAllStudents() = viewModelScope.launch {
        deleteAllStudentsSafeCall()
    }

    fun deleteSelectedStudents(deleteStudentsModel: DeleteStudentsModel) = viewModelScope.launch {
        deleteSelectedStudentsSafeCall(deleteStudentsModel)
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

    private suspend fun addStudentSafeCall(student: Student) {
        addStudentResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.addStudent(student)
                addStudentResponse.value = handleAddStudentResponse(response)
            } catch (e: Exception) {
                addStudentResponse.value = NetworkResult.Error("Error adding student")
            }
        } else {
            addStudentResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun updateStudentSafeCall(student: Student) {
        updateStudentResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.updateStudent(student)
                updateStudentResponse.value = handleUpdateStudentResponse(response)
            } catch (e: Exception) {
                updateStudentResponse.value = NetworkResult.Error("Error updating student")
            }
        } else {
            updateStudentResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun deleteStudentSafeCall(id: Int) {
        deleteStudentsResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.deleteStudent(id)
                deleteStudentsResponse.value = handleDeleteStudentResponse(response)
            } catch (e: Exception) {
                deleteStudentsResponse.value = NetworkResult.Error("Error deleting student")
            }
        } else {
            deleteStudentsResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun deleteAllStudentsSafeCall() {
        deleteStudentsResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.deleteAllStudents()
                deleteStudentsResponse.value = handleDeleteStudentResponse(response)
            } catch (e: Exception) {
                deleteStudentsResponse.value = NetworkResult.Error("Error deleting students")
            }
        } else {
            deleteStudentsResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun deleteSelectedStudentsSafeCall(deleteStudentsModel: DeleteStudentsModel) {
        deleteStudentsResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.deleteSelectedStudents(deleteStudentsModel)
                deleteStudentsResponse.value = handleDeleteStudentResponse(response)
            } catch (e: Exception) {
                deleteStudentsResponse.value = NetworkResult.Error("Error deleting students")
            }
        } else {
            deleteStudentsResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private fun offlineCacheStudents(students: Students) {
        for (student in students.students) {
            val studentEntity = StudentEntity(student)
            insertStudentIntoDatabase(studentEntity)
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

    private fun handleAddStudentResponse(response: Response<Student>): NetworkResult<Student> {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited.")
            }
            response.body()!!.firstName.isNullOrBlank() -> {
                return NetworkResult.Error("Error adding student")
            }
            response.isSuccessful -> {
                val student = response.body()
                return NetworkResult.Success(student!!)
            }
            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }

    private fun handleUpdateStudentResponse(response: Response<Student>): NetworkResult<Student> {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited.")
            }
            response.body()!!.firstName.isNullOrBlank() -> {
                return NetworkResult.Error("Error updating student")
            }
            response.isSuccessful -> {
                val student = response.body()
                return NetworkResult.Success(student!!)
            }
            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }

    private fun handleDeleteStudentResponse(response: Response<Unit>): NetworkResult<String> {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited.")
            }
            response.isSuccessful -> {
                return NetworkResult.Success("")
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