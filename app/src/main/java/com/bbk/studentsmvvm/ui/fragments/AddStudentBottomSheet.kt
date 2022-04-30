package com.bbk.studentsmvvm.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bbk.studentsmvvm.databinding.AddStudentBottomSheetBinding
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.util.NetworkListener
import com.bbk.studentsmvvm.util.NetworkResult
import com.bbk.studentsmvvm.util.UserData
import com.bbk.studentsmvvm.viewmodels.AllStudentsViewModel
import com.bbk.studentsmvvm.viewmodels.DataStoreViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class AddStudentBottomSheet : BottomSheetDialogFragment() {

    private var _binding: AddStudentBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var allStudentsViewModel: AllStudentsViewModel
    private lateinit var dataStoreViewModel: DataStoreViewModel

    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allStudentsViewModel = ViewModelProvider(requireActivity())[AllStudentsViewModel::class.java]
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
        _binding = AddStudentBottomSheetBinding.inflate(inflater, container, false)

        binding.btnAdd.setOnClickListener {

            val name = binding.nameEt.text.toString()
            val age = binding.ageEt.text.toString()
            val grade = binding.gradeEt.text.toString()
            val imageUrl = binding.imgEt.text.toString()

            if (name.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter name",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (age.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter age",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (grade.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter grade",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val student: Student
            try {
                student = Student(0, name, age.toInt(), grade.toInt(), imageUrl)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (dataStoreViewModel.networkStatus) {
                showLoading()

                lifecycleScope.launch {
                    addStudent(student)
                }
            } else {
                hideLoading()

                Toast.makeText(
                    requireContext(),
                    "No Internet Connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    private fun addStudent(student: Student) {
        Log.d("AddStudentBottomSheet", "addStudent called!")
        allStudentsViewModel.addStudent(student)
        allStudentsViewModel.addStudentResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    val action = AddStudentBottomSheetDirections.actionAddStudentBottomSheetToAllStudentsFragment(true)
                    findNavController().navigate(action)
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
        binding.addProgress.visibility = View.VISIBLE
        binding.btnAdd.visibility = View.INVISIBLE
    }

    private fun hideLoading() {
        binding.addProgress.visibility = View.INVISIBLE
        binding.btnAdd.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}