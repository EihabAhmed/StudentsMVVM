package com.bbk.studentsmvvm.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bbk.studentsmvvm.data.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    application: Application,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    var networkStatus = false
    var backOnline = false
    var token = ""
    var userName = ""

    val readBackOnline = dataStoreRepository.readBackOnline.asLiveData()

    private fun saveBackOnline(backOnline: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveBackOnline(backOnline)
        }

    val readToken = dataStoreRepository.readToken.asLiveData()

    fun saveToken(token: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveToken(token)
        }

    val readUserName = dataStoreRepository.readUserName.asLiveData()

    fun saveUserName(userName: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveUserName(userName)
        }

    fun showNetworkStatus() {
        if (!networkStatus) {
            Toast.makeText(getApplication(), "No Internet Connection.", Toast.LENGTH_SHORT).show()
            saveBackOnline(true)
        } else if (networkStatus) {
            //Toast.makeText(getApplication(), "We're back online.", Toast.LENGTH_SHORT).show()
            saveBackOnline(false)
        }
    }
}