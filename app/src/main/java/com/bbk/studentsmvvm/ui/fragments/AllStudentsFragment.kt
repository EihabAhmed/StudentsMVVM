package com.bbk.studentsmvvm.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbk.studentsmvvm.adapters.AllStudentsAdapter
import com.bbk.studentsmvvm.databinding.FragmentAllStudentsBinding
import com.bbk.studentsmvvm.util.NetworkListener
import com.bbk.studentsmvvm.util.NetworkResult
import com.bbk.studentsmvvm.viewmodels.DataStoreViewModel
import com.bbk.studentsmvvm.viewmodels.AllStudentsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AllStudentsFragment : Fragment() {

    private var _binding: FragmentAllStudentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var allStudentsViewModel: AllStudentsViewModel
    private lateinit var dataStoreViewModel: DataStoreViewModel
    private val mAdapter by lazy { AllStudentsAdapter() }

    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allStudentsViewModel = ViewModelProvider(requireActivity())[AllStudentsViewModel::class.java]
        dataStoreViewModel =
            ViewModelProvider(requireActivity())[DataStoreViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAllStudentsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.allStudentsViewModel = allStudentsViewModel

        setupRecyclerView()

        dataStoreViewModel.readBackOnline.observe(viewLifecycleOwner) {
            dataStoreViewModel.backOnline = it
        }

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    Log.d("NetworkListener", status.toString())
                    dataStoreViewModel.networkStatus = status
                    dataStoreViewModel.showNetworkStatus()

                    lifecycleScope.launch {
                        requestApiData()
                    }
                }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerview.adapter = mAdapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        showShimmerEffect()
    }

    private fun requestApiData() {
        Log.d("AllStudentsFragment", "requestApiData called!")
        allStudentsViewModel.getAllStudents()
        allStudentsViewModel.allStudentsResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    hideShimmerEffect()
                    response.data?.let { mAdapter.setData(it) }
                }
                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is NetworkResult.Loading -> {
                    showShimmerEffect()
                }
            }
        }
    }

    private fun showShimmerEffect() {
        binding.recyclerview.showShimmer()
    }

    private fun hideShimmerEffect() {
        binding.recyclerview.hideShimmer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}