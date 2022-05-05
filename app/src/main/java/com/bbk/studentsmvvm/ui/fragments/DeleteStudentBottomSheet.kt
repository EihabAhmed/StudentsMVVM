package com.bbk.studentsmvvm.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bbk.studentsmvvm.databinding.DeleteStudentBottomSheetBinding
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.util.NetworkListener
import com.bbk.studentsmvvm.util.NetworkResult
import com.bbk.studentsmvvm.viewmodels.AllStudentsViewModel
import com.bbk.studentsmvvm.viewmodels.DataStoreViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class DeleteStudentBottomSheet : BottomSheetDialogFragment() {

    private val args by navArgs<DeleteStudentBottomSheetArgs>()

    private var _binding: DeleteStudentBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var allStudentsViewModel: AllStudentsViewModel
    private lateinit var dataStoreViewModel: DataStoreViewModel

    private lateinit var networkListener: NetworkListener

    private var student: Student? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allStudentsViewModel =
            ViewModelProvider(requireActivity())[AllStudentsViewModel::class.java]
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
        _binding = DeleteStudentBottomSheetBinding.inflate(inflater, container, false)

        student = args.student

        if (args.origin == "student details") {
            binding.deleteMessageTv.text =
                "Are you sure you want to delete student ${student!!.firstName}?"
        } else if (args.origin == "all students") {
            binding.deleteMessageTv.text = "Are you sure you want to delete selected students?"
        }

        binding.btnYesDelete.setOnClickListener {

            if (dataStoreViewModel.networkStatus) {
                showLoading()

                if (args.origin == "student details") {
                    deleteStudent()
                } else if (args.origin == "all students") {
                    deleteAllStudents()
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

        binding.btnNoDelete.setOnClickListener {

            var action: NavDirections? = null

            if (args.origin == "student details") {
                action =
                    DeleteStudentBottomSheetDirections.actionDeleteStudentBottomSheetToStudentDetailsFragment(
                        student,
                        ""
                    )
            } else if (args.origin == "all students") {
                action =
                    DeleteStudentBottomSheetDirections.actionDeleteStudentBottomSheetToAllStudentsFragment()
            }

            findNavController().navigate(action!!)
        }

        return binding.root
    }

    private fun deleteStudent() {
        Log.d("DeleteStudentBottomSheet", "deleteStudent called!")
        allStudentsViewModel.deleteStudent(student!!.id)
        allStudentsViewModel.deleteStudentsResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    allStudentsViewModel.deleteStudentsResponse.removeObservers(viewLifecycleOwner)

                    Toast.makeText(
                        requireContext(),
                        "Deleted student ${student!!.firstName} successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val action =
                        DeleteStudentBottomSheetDirections.actionDeleteStudentBottomSheetToStudentDetailsFragment(
                            student,
                            "delete"
                        )
                    findNavController().navigate(action)
                }
                is NetworkResult.Error -> {
                    allStudentsViewModel.deleteStudentsResponse.removeObservers(viewLifecycleOwner)

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

    private fun deleteAllStudents() {
        Log.d("DeleteStudentBottomSheet", "deleteAllStudents called!")
        allStudentsViewModel.deleteAllStudents()
        allStudentsViewModel.deleteStudentsResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    allStudentsViewModel.deleteStudentsResponse.removeObservers(viewLifecycleOwner)

                    Snackbar.make(
                        binding.root,
                        "Deleted students successfully",
                        Snackbar.LENGTH_SHORT
                    ).setAction("Okay") {}
                        .show()

                    val action =
                        DeleteStudentBottomSheetDirections.actionDeleteStudentBottomSheetToAllStudentsFragment(
                            true
                        )
                    findNavController().navigate(action)
                }
                is NetworkResult.Error -> {
                    allStudentsViewModel.deleteStudentsResponse.removeObservers(viewLifecycleOwner)

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
//        binding.addProgress.visibility = View.VISIBLE
//        binding.btnAdd.visibility = View.INVISIBLE
    }

    private fun hideLoading() {
//        binding.addProgress.visibility = View.INVISIBLE
//        binding.btnAdd.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}