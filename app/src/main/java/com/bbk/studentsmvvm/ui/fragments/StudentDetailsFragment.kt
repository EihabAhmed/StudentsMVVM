package com.bbk.studentsmvvm.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.bbk.studentsmvvm.R
import com.bbk.studentsmvvm.databinding.FragmentStudentDetailsBinding
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.util.NetworkListener
import com.bbk.studentsmvvm.util.UserData
import com.bbk.studentsmvvm.viewmodels.AllStudentsViewModel
import com.bbk.studentsmvvm.viewmodels.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jsoup.Jsoup

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class StudentDetailsFragment : Fragment() {

    private val args by navArgs<StudentDetailsFragmentArgs>()

    private var _binding: FragmentStudentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var allStudentsViewModel: AllStudentsViewModel
    private lateinit var dataStoreViewModel: DataStoreViewModel

    private lateinit var networkListener: NetworkListener

    private var student: Student? = null

    private var firstStart = true

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

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val action = StudentDetailsFragmentDirections.actionStudentDetailsFragmentToAllStudentsFragment(true)
                findNavController().navigate(action)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStudentDetailsBinding.inflate(inflater, container, false)

        if (args.origin == "delete") {
            val action = StudentDetailsFragmentDirections.actionStudentDetailsFragmentToAllStudentsFragment(true)
            findNavController().navigate(action)
        }

        if (UserData.isAdmin) {
            setHasOptionsMenu(true)
        }

        student = args.student

        binding.mainImageView.load(student!!.imageUrl) {
            error(R.drawable.ic_placeholder)
        }
        binding.nameTextView.text = student!!.firstName
        binding.ageTextView.text = "${student!!.age} years old"
        binding.gradeTextView.text = "Grade ${student!!.grade}"

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.student_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_edit) {
            editStudent()
        } else if (item.itemId == R.id.menu_delete) {
            deleteStudent()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun editStudent() {
        if (dataStoreViewModel.networkStatus) {
            try {
                val action = StudentDetailsFragmentDirections.actionStudentDetailsFragmentToAddStudentBottomSheet(student)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.d("editStudent", e.toString())
            }
        } else {
            dataStoreViewModel.showNetworkStatus()
        }
    }

    private fun deleteStudent() {
        if (dataStoreViewModel.networkStatus) {
            try {
                val action = StudentDetailsFragmentDirections.actionStudentDetailsFragmentToDeleteStudentBottomSheet(student!!)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.d("deleteStudent", e.toString())
            }
        } else {
            dataStoreViewModel.showNetworkStatus()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}