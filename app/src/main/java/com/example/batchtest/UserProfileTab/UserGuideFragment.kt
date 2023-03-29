package com.example.batchtest.UserProfileTab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentAccountSettingBinding
import com.example.batchtest.databinding.FragmentUserGuideBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserGuideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserGuideFragment : Fragment() {
    private var _binding: FragmentUserGuideBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserGuideBinding.inflate(layoutInflater, container, false)


        return binding.root
    }

    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}