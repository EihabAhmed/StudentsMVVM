package com.bbk.studentsmvvm.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import com.bbk.studentsmvvm.R
import com.bbk.studentsmvvm.databinding.FragmentStudentDetailsBinding
import org.jsoup.Jsoup

class StudentDetailsFragment : Fragment() {

    private val args by navArgs<StudentDetailsFragmentArgs>()

    private var _binding: FragmentStudentDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStudentDetailsBinding.inflate(inflater, container, false)

        val student = args.student

        binding.mainImageView.load(student.imageUrl) {
            error(R.drawable.ic_placeholder)
        }
        binding.nameTextView.text = student.firstName
        binding.ageTextView.text = "${student.age} years old"
        binding.gradeTextView.text = "Grade ${student.grade}"

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}