package com.bbk.studentsmvvm.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bbk.studentsmvvm.databinding.StudentRowLayoutBinding
import com.bbk.studentsmvvm.models.Student
import com.bbk.studentsmvvm.models.Students
import com.bbk.studentsmvvm.util.StudentsDiffUtil

class AllStudentsAdapter : RecyclerView.Adapter<AllStudentsAdapter.MyViewHolder>() {

    private var students = emptyList<Student>()

    class MyViewHolder(private val binding: StudentRowLayoutBinding) :
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
        val currentStudent = students[position]
        holder.bind(currentStudent)
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