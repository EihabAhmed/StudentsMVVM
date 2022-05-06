package com.bbk.studentsmvvm.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bbk.studentsmvvm.R
import com.bbk.studentsmvvm.databinding.StudentRowLayoutBinding
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.models.Students
import com.bbk.studentsmvvm.ui.fragments.AllStudentsFragment
import com.bbk.studentsmvvm.ui.fragments.AllStudentsFragmentDirections
import com.bbk.studentsmvvm.util.StudentsDiffUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class AllStudentsAdapter(
    private val fragment: AllStudentsFragment
) : RecyclerView.Adapter<AllStudentsAdapter.MyViewHolder>() {

    private val requireActivity: FragmentActivity = fragment.requireActivity()

    var multiSelection = false

    private lateinit var rootView: View

    var selectedStudents = arrayListOf<Student>()
    var myViewHolders = arrayListOf<MyViewHolder>()
    private var students = emptyList<Student>()

    class MyViewHolder(val binding: StudentRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {
            binding.student = student
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = StudentRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        myViewHolders.add(holder)
        rootView = holder.itemView.rootView

        val currentStudent = students[position]
        holder.bind(currentStudent)

        /**
         * Single Click Listener
         */
        holder.binding.studentRowLayout.setOnClickListener {

            if (multiSelection) {
                applySelection(holder, currentStudent)
            } else {
                val action =
                    AllStudentsFragmentDirections.actionAllStudentsFragmentToStudentDetailsFragment(
                        currentStudent,
                        ""
                    )
                holder.itemView.findNavController().navigate(action)
            }
        }

        /**
         * Long Click Listener
         */
        holder.binding.studentRowLayout.setOnLongClickListener {

            if (!multiSelection) {
                multiSelection = true
                requireActivity.startActionMode(fragment)
                applySelection(holder, currentStudent)
                true
            } else {
                applySelection(holder, currentStudent)
                true
            }
        }
    }

    private fun applySelection(holder: MyViewHolder, currentStudent: Student) {
        if (selectedStudents.contains(currentStudent)) {
            selectedStudents.remove(currentStudent)
            changeStudentStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
        } else {
            selectedStudents.add(currentStudent)
            changeStudentStyle(holder, R.color.cardBackgroundLightColor, R.color.colorPrimary)
        }
        fragment.applyActionModeTitle()
    }

    fun changeStudentStyle(holder: MyViewHolder, backgroundColor: Int, strokeColor: Int) {
        holder.binding.studentRowLayout.setBackgroundColor(
            ContextCompat.getColor(requireActivity, backgroundColor)
        )
        holder.binding.rowCardView.strokeColor =
            ContextCompat.getColor(requireActivity, strokeColor)
    }

    override fun getItemCount(): Int {
        return students.size
    }

    fun setData(newData: Students) {
        val studentsDiffUtil = StudentsDiffUtil(students, newData.students)
        val diffUtilResult = DiffUtil.calculateDiff(studentsDiffUtil)
        students = newData.students
        diffUtilResult.dispatchUpdatesTo(this)
    }
}