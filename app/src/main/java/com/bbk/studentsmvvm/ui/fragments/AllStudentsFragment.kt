package com.bbk.studentsmvvm.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bbk.studentsmvvm.R
import com.bbk.studentsmvvm.adapters.AllStudentsAdapter
import com.bbk.studentsmvvm.databinding.FragmentAllStudentsBinding
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.models.Students
import com.bbk.studentsmvvm.util.NetworkListener
import com.bbk.studentsmvvm.util.NetworkResult
import com.bbk.studentsmvvm.util.UserData
import com.bbk.studentsmvvm.util.observeOnce
import com.bbk.studentsmvvm.viewmodels.AllStudentsViewModel
import com.bbk.studentsmvvm.viewmodels.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AllStudentsFragment : Fragment() {

    private val args by navArgs<AllStudentsFragmentArgs>()

    private var _binding: FragmentAllStudentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var allStudentsViewModel: AllStudentsViewModel
    private lateinit var dataStoreViewModel: DataStoreViewModel
    private val mAdapter by lazy { AllStudentsAdapter() }

    private lateinit var networkListener: NetworkListener

    private var firstStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allStudentsViewModel =
            ViewModelProvider(requireActivity())[AllStudentsViewModel::class.java]
        dataStoreViewModel =
            ViewModelProvider(requireActivity())[DataStoreViewModel::class.java]

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    Log.d("NetworkListener", status.toString())
                    dataStoreViewModel.networkStatus = status
                    dataStoreViewModel.showNetworkStatus()

                    readDatabase()
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAllStudentsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.allStudentsViewModel = allStudentsViewModel

        setHasOptionsMenu(true)

        binding.userNameTextView.text = "Welcome ${UserData.userName}"
        setupRecyclerView()

        dataStoreViewModel.readBackOnline.observe(viewLifecycleOwner) {
            dataStoreViewModel.backOnline = it
        }

        if (firstStart) {
            firstStart = false
        } else {
            lifecycleScope.launchWhenStarted {
                networkListener = NetworkListener()
                networkListener.checkNetworkAvailability(requireContext())
                    .collect { status ->
                        Log.d("NetworkListener", status.toString())
                        dataStoreViewModel.networkStatus = status
                        dataStoreViewModel.showNetworkStatus()

                        readDatabase()
                    }
            }
        }

        if (UserData.isAdmin) {
            binding.addStudentFab.visibility = View.VISIBLE
        } else {
            binding.addStudentFab.visibility = View.INVISIBLE
        }

        binding.addStudentFab.setOnClickListener {
            if (dataStoreViewModel.networkStatus) {
                val action =
                    AllStudentsFragmentDirections.actionAllStudentsFragmentToAddStudentBottomSheet(
                        null
                    )
                findNavController().navigate(action)
            } else {
                dataStoreViewModel.showNetworkStatus()
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerview.adapter = mAdapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        showShimmerEffect()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.all_students_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_reload) {
            requestApiData()
        } else if (item.itemId == R.id.menu_logout) {
            logout()
        } else if (item.itemId == R.id.menu_delete_all) {
            deleteAllStudents()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllStudents() {

        if (!UserData.isAdmin) {
            Toast.makeText(
                requireContext(),
                "Only admins can delete students",
                Toast.LENGTH_SHORT
            ).show()
        } else if (binding.recyclerview.adapter!!.itemCount == 0) {
            Toast.makeText(
                requireContext(),
                "No students found",
                Toast.LENGTH_SHORT
            ).show()
        } else if (dataStoreViewModel.networkStatus) {
            try {
                val action =
                    AllStudentsFragmentDirections.actionAllStudentsFragmentToDeleteStudentBottomSheet(
                        null,
                        "all students"
                    )
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.d("deleteAllStudents", e.toString())
            }
        } else {
            dataStoreViewModel.showNetworkStatus()
        }
    }

    private fun logout() {
        UserData.userName = ""
        UserData.token = ""
        UserData.isAdmin = false

        dataStoreViewModel.saveUserName(UserData.userName)
        dataStoreViewModel.saveToken(UserData.token)

        findNavController().navigate(R.id.action_allStudentsFragment_to_loginFragment)
    }

    private fun readDatabase() {
        lifecycleScope.launch {
            allStudentsViewModel.readStudents.observeOnce(viewLifecycleOwner) { database ->
                if (database.isNotEmpty() && !args.invalidate) {
                    Log.d("AllStudentsFragment", "readDatabase called!")

                    val studentList = mutableListOf<Student>()
                    for (studentEntity in database) {
                        studentList.add(studentEntity.student)
                    }

                    val students = Students(studentList)

                    mAdapter.setData(students)
                    hideShimmerEffect()
                } else {
                    requestApiData()
                }
            }
        }
    }

    private fun requestApiData() {
        Log.d("AllStudentsFragment", "requestApiData called!")
        if (dataStoreViewModel.networkStatus) {
            allStudentsViewModel.deleteAllStudentsFromDatabase()
            allStudentsViewModel.getAllStudents()
            allStudentsViewModel.allStudentsResponse.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is NetworkResult.Success -> {
                        allStudentsViewModel.allStudentsResponse.removeObservers(viewLifecycleOwner)

                        hideShimmerEffect()
                        response.data?.let { mAdapter.setData(it) }
                    }
                    is NetworkResult.Error -> {
                        allStudentsViewModel.allStudentsResponse.removeObservers(viewLifecycleOwner)

                        hideShimmerEffect()
                        loadDataFromCache()
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
        } else {
            Toast.makeText(
                requireContext(),
                "No Internet Connection.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadDataFromCache() {
        lifecycleScope.launch {
            allStudentsViewModel.readStudents.observe(viewLifecycleOwner) { database ->
                if (database.isNotEmpty()) {
                    val studentList = mutableListOf<Student>()
                    for (studentEntity in database) {
                        studentList.add(studentEntity.student)
                    }

                    val students = Students(studentList)

                    mAdapter.setData(students)
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