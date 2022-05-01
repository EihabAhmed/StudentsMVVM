package com.bbk.studentsmvvm.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bbk.studentsmvvm.R
import com.bbk.studentsmvvm.databinding.FragmentLoginBinding
import com.bbk.studentsmvvm.util.NetworkListener
import com.bbk.studentsmvvm.util.NetworkResult
import com.bbk.studentsmvvm.util.UserData
import com.bbk.studentsmvvm.viewmodels.DataStoreViewModel
import com.bbk.studentsmvvm.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var dataStoreViewModel: DataStoreViewModel

    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginViewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        dataStoreViewModel = ViewModelProvider(requireActivity())[DataStoreViewModel::class.java]

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    Log.d("NetworkListener", status.toString())
                    dataStoreViewModel.networkStatus = status
                    dataStoreViewModel.showNetworkStatus()
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.loginViewModel = loginViewModel

        binding.loginButton.setOnClickListener {

            val userName = binding.loginEmailEditText.text.toString()
            val password = binding.loginPasswordEditText.text.toString()

            if (userName.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter your email",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter your password",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (dataStoreViewModel.networkStatus) {
                showLoading()
                lifecycleScope.launch {
                    login(userName, password)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "No Internet Connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    private fun requestIsAdmin() {
        Log.d("LoginFragment", "requestIsAdmin called!")
        loginViewModel.isAdminResponse.value = NetworkResult.Loading()
        loginViewModel.checkAdmin(UserData.userName)
        loginViewModel.isAdminResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    UserData.isAdmin = response.data?.admin ?: false
                    findNavController().navigate(R.id.action_loginFragment_to_allStudentsFragment)
                }
                is NetworkResult.Error -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is NetworkResult.Loading -> {
                    showLoading()
                }
            }
        }
    }

    private fun login(userName: String, password: String) {
        Log.d("LoginFragment", "login called!")
        loginViewModel.login(userName, password)
        loginViewModel.loginResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    //hideLoading()
                    UserData.userName = userName
                    UserData.token = response.data?.token!!
                    dataStoreViewModel.saveToken(UserData.token)
                    dataStoreViewModel.saveUserName(UserData.userName)

                    lifecycleScope.launch {
                        requestIsAdmin()
                    }

                }
                is NetworkResult.Error -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is NetworkResult.Loading -> {
                    showLoading()
                }
            }
        }
    }

    private fun showLoading() {
        binding.loginScreenLayout.visibility = View.VISIBLE
        binding.loginLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loginScreenLayout.visibility = View.INVISIBLE
        binding.loginLoading.visibility = View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}