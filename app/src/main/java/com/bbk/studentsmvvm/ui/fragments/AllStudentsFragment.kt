package com.bbk.studentsmvvm.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AllStudentsFragment : Fragment(), ActionMode.Callback {

    private val args by navArgs<AllStudentsFragmentArgs>()

    private var _binding: FragmentAllStudentsBinding? = null
    private val binding get() = _binding!!

    private val allStudentsViewModel: AllStudentsViewModel by viewModels()
    private lateinit var dataStoreViewModel: DataStoreViewModel

    private val mAdapter: AllStudentsAdapter by lazy { AllStudentsAdapter(this) }

    private lateinit var mActionMode: ActionMode

    private lateinit var networkListener: NetworkListener

    private var firstStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataStoreViewModel = ViewModelProvider(requireActivity())[DataStoreViewModel::class.java]

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

        // TODO: handle backOnline and show controls that are hidden

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

    private fun showSnackBar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).setAction("Okay") {}
            .show()
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
            showSnackBar("Only admins can delete students")
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
                        emptyArray(),
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
        clearContextualActionMode()
    }

    override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
        actionMode?.menuInflater?.inflate(R.menu.students_contextual_menu, menu)
        mActionMode = actionMode!!
        applyStatusBarColor(R.color.contextualStatusBarColor)
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {

        if (menu?.itemId == R.id.delete_students_menu) {

            deleteSelectedStudents()

//            mAdapter.selectedRecipes.forEach {
//                mainViewModel.deleteFavoriteRecipe(it)
//            }
//
//            showSnackBar("${mAdapter.selectedStudents.size} student(s) removed.")
//
//            mAdapter.multiSelection = false
//            mAdapter.selectedStudents.clear()
//            actionMode?.finish()

        }

        return true
    }

    private fun deleteSelectedStudents() {

        if (!UserData.isAdmin) {
            showSnackBar("Only admins can delete students")
        } else if (dataStoreViewModel.networkStatus) {
            try {
                val action =
                    AllStudentsFragmentDirections.actionAllStudentsFragmentToDeleteStudentBottomSheet(
                        mAdapter.selectedStudents.toTypedArray(),
                        "selected students"
                    )
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.d("deleteSelectedStudents", e.toString())
            }
        } else {
            dataStoreViewModel.showNetworkStatus()
        }
    }

    override fun onDestroyActionMode(actionMode: ActionMode?) {

        mAdapter.myViewHolders.forEach { holder ->
            mAdapter.changeStudentStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
        }

        mAdapter.multiSelection = false
        mAdapter.selectedStudents.clear()
        applyStatusBarColor(R.color.statusBarColor)
    }

    private fun applyStatusBarColor(color: Int) {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireActivity(), color)
    }

    fun applyActionModeTitle() {
        when (mAdapter.selectedStudents.size) {
            0 -> {
                mActionMode.finish()
                mAdapter.multiSelection = false
            }
            1 -> {
                mActionMode.title = "1 student selected"
            }
            else -> {
                mActionMode.title = "${mAdapter.selectedStudents.size} students selected"
            }
        }
    }

    private fun clearContextualActionMode() {
        if (this::mActionMode.isInitialized) {
            mActionMode.finish()
        }
    }
}