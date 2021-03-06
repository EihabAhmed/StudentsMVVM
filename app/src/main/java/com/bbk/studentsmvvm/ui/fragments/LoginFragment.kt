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
                //showLoading()

                login(userName, password)

            } else {
                Toast.makeText(
                    requireContext(),
                    "No Internet Connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.loginCreateoneTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return binding.root
    }

    private fun login(userName: String, password: String) {
        Log.d("LoginFragment", "login called!")
        loginViewModel.login(userName, password)
        loginViewModel.loginResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    loginViewModel.loginResponse.removeObservers(viewLifecycleOwner)

                    //hideLoading()
                    UserData.userName = userName
                    UserData.token = response.data?.token!!
                    dataStoreViewModel.saveToken(UserData.token)
                    dataStoreViewModel.saveUserName(UserData.userName)

                    requestIsAdmin()

                }
                is NetworkResult.Error -> {
                    loginViewModel.loginResponse.removeObservers(viewLifecycleOwner)

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

    private fun requestIsAdmin() {
        Log.d("LoginFragment", "requestIsAdmin called!")
        loginViewModel.checkAdmin(UserData.userName)
        loginViewModel.isAdminResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    loginViewModel.isAdminResponse.removeObservers(viewLifecycleOwner)

                    UserData.isAdmin = response.data?.admin ?: false
                    findNavController().navigate(R.id.action_loginFragment_to_allStudentsFragment)
                }
                is NetworkResult.Error -> {
                    loginViewModel.isAdminResponse.removeObservers(viewLifecycleOwner)

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
        binding.loginEmailEditText.isEnabled = false
        binding.loginPasswordEditText.isEnabled = false
        binding.loginButton.isEnabled = false
        binding.loginCreateoneTextView.isEnabled = false
        binding.loginLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loginEmailEditText.isEnabled = true
        binding.loginPasswordEditText.isEnabled = true
        binding.loginButton.isEnabled = true
        binding.loginCreateoneTextView.isEnabled = true
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