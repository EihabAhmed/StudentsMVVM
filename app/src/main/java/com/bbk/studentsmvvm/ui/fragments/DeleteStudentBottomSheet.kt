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
import com.bbk.studentsmvvm.models.DeleteStudentsModel
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.util.NetworkListener
import com.bbk.studentsmvvm.util.NetworkResult
import com.bbk.studentsmvvm.viewmodels.AllStudentsViewModel
import com.bbk.studentsmvvm.viewmodels.DataStoreViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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

    private var students = emptyArray<Student>()

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

        students = args.students

        if (args.origin == "student details") {
            binding.deleteMessageTv.text =
                "Are you sure you want to delete student ${students[0].firstName}?"
        } else if (args.origin == "all students") {
            binding.deleteMessageTv.text = "Are you sure you want to delete all students?"
        } else if (args.origin == "selected students") {
            binding.deleteMessageTv.text = "Are you sure you want to delete selected students?"
        }

        binding.btnYesDelete.setOnClickListener {

            if (dataStoreViewModel.networkStatus) {
                showLoading()

                if (args.origin == "student details") {
                    deleteStudent()
                } else if (args.origin == "all students") {
                    deleteAllStudents()
                } else if (args.origin == "selected students") {
                    deleteSelectedStudents()
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

            val action: NavDirections?

            if (args.origin == "student details") {
                action =
                    DeleteStudentBottomSheetDirections.actionDeleteStudentBottomSheetToStudentDetailsFragment(
                        students[0],
                        ""
                    )
                findNavController().navigate(action)
            } else if (args.origin == "all students") {
                requireActivity().onBackPressed()
            } else if (args.origin == "selected students") {
                requireActivity().onBackPressed()
            }
        }

        return binding.root
    }

    private fun deleteStudent() {
        Log.d("DeleteStudentBottomSheet", "deleteStudent called!")
        allStudentsViewModel.deleteStudent(students[0].id)
        allStudentsViewModel.deleteStudentsResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    allStudentsViewModel.deleteStudentsResponse.removeObservers(viewLifecycleOwner)

                    Toast.makeText(
                        requireContext(),
                        "Deleted student ${students[0].firstName} successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val action =
                        DeleteStudentBottomSheetDirections.actionDeleteStudentBottomSheetToStudentDetailsFragment(
                            students[0],
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

                    Toast.makeText(
                        requireContext(),
                        "Deleted students successfully",
                        Toast.LENGTH_SHORT
                    ).show()

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

    private fun deleteSelectedStudents() {
        val deleteStudentsModel = DeleteStudentsModel()

        students.forEach {
            deleteStudentsModel.studentIds.add(it.id)
        }

        Log.d("DeleteStudentBottomSheet", "deleteSelectedStudents called!")
        allStudentsViewModel.deleteSelectedStudents(deleteStudentsModel)
        allStudentsViewModel.deleteStudentsResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    allStudentsViewModel.deleteStudentsResponse.removeObservers(viewLifecycleOwner)

                    Toast.makeText(
                        requireContext(),
                        "Deleted students successfully",
                        Toast.LENGTH_SHORT
                    ).show()

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