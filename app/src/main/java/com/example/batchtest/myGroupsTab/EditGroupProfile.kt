package com.example.batchtest.myGroupsTab

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.StateSet.TAG
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import com.example.batchtest.EditGroupProfile.EditGroupInfoFragment
import com.example.batchtest.EditGroupProfile.GroupProfileAdapter
import com.example.batchtest.EditGroupProfile.ViewGroupInfoFragment
import com.example.batchtest.databinding.FragmentEditGroupProfileBinding
import com.google.android.material.appbar.AppBarLayout.Behavior
import com.google.android.material.tabs.TabLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditGroupProfile.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditGroupProfile : Fragment() {
    private var _binding: FragmentEditGroupProfileBinding? = null
    private val binding get() = _binding!!



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        tabLayout = binding.groupEditTabs
//        viewPager = binding.groupProfileViewpager
//        viewPager.adapter = GroupProfileAdapter(this)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditGroupProfileBinding.inflate(layoutInflater, container, false)

        setUpTabs()


        return binding.root


    }
    private fun setUpTabs(){

        val adapter = GroupProfileAdapter(childFragmentManager)
        val viewPager = binding.groupProfileViewpager
        adapter.addFragment(EditGroupInfoFragment(), "Edit")
        adapter.addFragment(ViewGroupInfoFragment(), "Preview")
        viewPager.adapter = adapter
        binding.groupEditTabs.setupWithViewPager(viewPager)



    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditGroupProfile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditGroupProfile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}