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
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.example.batchtest.EditGroupProfile.EditGroupInfoFragment
import com.example.batchtest.EditGroupProfile.GroupProfileAdapter
import com.example.batchtest.EditGroupProfile.ViewGroupInfoFragment
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentEditGroupProfileBinding
import com.google.android.material.appbar.AppBarLayout.Behavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditGroupProfile.newInstance] factory method to
 * create an instance of this fragment.
 */

val tabArray = arrayOf(
    "Edit",
    "Preview"

)
class EditGroupProfile : Fragment() {
    private var _binding: FragmentEditGroupProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager: ViewPager



//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        tabLayout = binding.groupEditTabs
////        viewPager = binding.groupProfileViewpager
////        viewPager.adapter = GroupProfileAdapter(this)
//
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentEditGroupProfileBinding.inflate(layoutInflater, container, false)

        //set up the 2 tab layout: edit and preview
        setUpTabs()



        //cancel to navigate back to my group page
        binding.cancelGroupEdit.setOnClickListener{
            findNavController().navigate(R.id.action_editGroupProfile_to_myGroupFragment)
        }


        return binding.root


    }

    /**
     * display the 2 edit and preview tabs
     */
    private fun setUpTabs() {

        val viewPager = binding.groupProfileViewpager
        val tabLayout = binding.groupEditTabs

        val adapter = GroupProfileAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            tab.text = tabArray[position]

        }.attach()


    }


    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

