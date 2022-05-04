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
import com.bbk.studentsmvvm.databinding.FragmentRegisterBinding
import com.bbk.studentsmvvm.models.RegisterModel
import com.bbk.studentsmvvm.util.NetworkListener
import com.bbk.studentsmvvm.util.NetworkResult
import com.bbk.studentsmvvm.util.UserData
import com.bbk.studentsmvvm.viewmodels.DataStoreViewModel
import com.bbk.studentsmvvm.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.registerButton.setOnClickListener {

            val userName = binding.registerEmailEditText.text.toString()
            val password = binding.registerPasswordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

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

            if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(
                    requireContext(),
                    "Password and Confirm Password must be identical",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (dataStoreViewModel.networkStatus) {
                //showLoading()

                register(RegisterModel(userName, password, confirmPassword))
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

    private fun register(registerModel: RegisterModel) {
        Log.d("RegisterFragment", "register called!")
        loginViewModel.register(registerModel)
        loginViewModel.registerResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {

                    loginViewModel.registerResponse.removeObservers(viewLifecycleOwner)

                    login(registerModel.email, registerModel.password)
                }
                is NetworkResult.Error -> {
                    loginViewModel.registerResponse.removeObservers(viewLifecycleOwner)

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
        Log.d("RegisterFragment", "login called!")
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
        Log.d("RegisterFragment", "requestIsAdmin called!")
        loginViewModel.isAdminResponse.value = NetworkResult.Loading()
        loginViewModel.checkAdmin(UserData.userName)
        loginViewModel.isAdminResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    loginViewModel.isAdminResponse.removeObservers(viewLifecycleOwner)

                    UserData.isAdmin = response.data?.admin ?: false
                    findNavController().navigate(R.id.action_registerFragment_to_allStudentsFragment)
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
        binding.registerEmailEditText.isEnabled = false
        binding.registerPasswordEditText.isEnabled = false
        binding.confirmPasswordEditText.isEnabled = false
        binding.registerButton.isEnabled = false
        binding.registerLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.registerEmailEditText.isEnabled = true
        binding.registerPasswordEditText.isEnabled = true
        binding.confirmPasswordEditText.isEnabled = true
        binding.registerButton.isEnabled = true
        binding.registerLoading.visibility = View.INVISIBLE
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