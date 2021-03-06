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
import com.bbk.studentsmvvm.databinding.FragmentSplashBinding
import com.bbk.studentsmvvm.util.NetworkListener
import com.bbk.studentsmvvm.util.NetworkResult
import com.bbk.studentsmvvm.util.UserData
import com.bbk.studentsmvvm.viewmodels.DataStoreViewModel
import com.bbk.studentsmvvm.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var dataStoreViewModel: DataStoreViewModel

    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("eeee", "onCreate")

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
        Log.d("eeee", "onCreateView")

        // Inflate the layout for this fragment
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        lifecycleScope.launchWhenStarted {

            delay(1000L)

            dataStoreViewModel.readBackOnline.observe(viewLifecycleOwner) {
                dataStoreViewModel.backOnline = it
            }

            dataStoreViewModel.readToken.observe(viewLifecycleOwner) {
                dataStoreViewModel.token = it
                UserData.token = it

                if (UserData.token.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "Token empty",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Token found",
                        Toast.LENGTH_SHORT
                    ).show()

                    dataStoreViewModel.readUserName.observe(viewLifecycleOwner) { userName ->
                        dataStoreViewModel.userName = userName
                        UserData.userName = userName

                        requestIsAdmin()
                    }
                }
            }

        }

        return binding.root
    }

    private fun requestIsAdmin() {
        Log.d("SplashFragment", "requestIsAdmin called!")

        if (dataStoreViewModel.networkStatus) {
            loginViewModel.checkAdmin(UserData.userName)
            loginViewModel.isAdminResponse.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is NetworkResult.Success -> {
                        loginViewModel.isAdminResponse.removeObservers(viewLifecycleOwner)

                        UserData.isAdmin = response.data?.admin ?: false
                        findNavController().navigate(R.id.action_splashFragment_to_allStudentsFragment)
                    }
                    is NetworkResult.Error -> {
                        loginViewModel.isAdminResponse.removeObservers(viewLifecycleOwner)

                        Toast.makeText(
                            requireContext(),
                            response.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

                        UserData.isAdmin = false
                        findNavController().navigate(R.id.action_splashFragment_to_allStudentsFragment)
                    }
                    is NetworkResult.Loading -> {
                        showLoading()
                    }
                }
            }
        } else {
            UserData.isAdmin = false
            findNavController().navigate(R.id.action_splashFragment_to_allStudentsFragment)
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.INVISIBLE
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