package com.bbk.studentsmvvm.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bbk.studentsmvvm.data.Repository
import com.bbk.studentsmvvm.models.Admin
import com.bbk.studentsmvvm.models.RegisterModel
import com.bbk.studentsmvvm.models.Token
import com.bbk.studentsmvvm.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    /** RETROFIT */
    var registerResponse: MutableLiveData<NetworkResult<String>> = MutableLiveData()
    var loginResponse: MutableLiveData<NetworkResult<Token>> = MutableLiveData()
    var isAdminResponse: MutableLiveData<NetworkResult<Admin>> = MutableLiveData()

    fun login(userName: String, password: String) = viewModelScope.launch {
        loginSafeCall(userName, password)
    }

    fun register(registerModel: RegisterModel) = viewModelScope.launch {
        registerSafeCall(registerModel)
    }

    fun checkAdmin(userName: String) = viewModelScope.launch {
        checkAdminSafeCall(userName)
    }

    private suspend fun loginSafeCall(userName: String, password: String) {
        loginResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.login(userName, password)
                loginResponse.value = handleLoginResponse(response)

//                val students = allStudentsResponse.value!!.data
//                if (students != null) {
//                    offlineCacheRecipes(foodRecipe)
//                }
            } catch (e: Exception) {
                loginResponse.value = NetworkResult.Error("Error Login")
            }
        } else {
            loginResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun registerSafeCall(registerModel: RegisterModel) {
        registerResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.register(registerModel)
                registerResponse.value = handleRegisterResponse(response)
            } catch (e: Exception) {
                registerResponse.value = NetworkResult.Error("Error Register")
            }
        } else {
            registerResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun checkAdminSafeCall(userName: String) {
        isAdminResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.isAdmin(userName)
                isAdminResponse.value = handleIsAdminResponse(response)
            } catch (e: Exception) {
                isAdminResponse.value = NetworkResult.Error("No Internet Connection.")
            }
        } else {
            isAdminResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private fun handleLoginResponse(response: Response<Token>): NetworkResult<Token> {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited.")
            }
            response.body()!!.token.isNullOrEmpty() -> {
                return NetworkResult.Error("Error Login")
            }
            response.isSuccessful -> {
                val token = response.body()
                return NetworkResult.Success(token!!)
            }
            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }

    private fun handleRegisterResponse(response: Response<Unit>): NetworkResult<String> {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited.")
            }
            response.isSuccessful -> {
                return NetworkResult.Success("OK")
            }
            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }

    private fun handleIsAdminResponse(response: Response<Admin>): NetworkResult<Admin> {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited.")
            }
            response.isSuccessful -> {
                val admin = response.body()
                return NetworkResult.Success(admin!!)
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