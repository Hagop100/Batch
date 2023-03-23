package com.example.batchtest.myGroupsTab

import android.location.GnssAntennaInfo.Listener
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.batchtest.EditGroupProfile.GroupInfoViewModel
import com.example.batchtest.EditGroupProfile.GroupProfileAdapter
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentEditGroupProfileBinding
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
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val sharedViewModel: GroupInfoViewModel by activityViewModels()
    private lateinit var groupInfo: Group


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        tabLayout = binding.groupEditTabs
//        viewPager = binding.groupProfileViewpager
//        viewPager.adapter = GroupProfileAdapter(this)
//        sharedViewModel = ViewModelProvider(requireActivity())[GroupInfoViewModel::class.java]

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

//        val sharedViewModel = ViewModelProvider(this).get(GroupInfoViewModel::class.java)


        //save info to database
        binding.saveBtn.setOnClickListener{



        }

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

        viewPager = binding.groupProfileViewpager
        tabLayout = binding.groupEditTabs

        val adapter = GroupProfileAdapter(this)
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
        super.onDestroyView()
        viewPager.let {
            (viewPager.parent as? ViewGroup)?.removeView(viewPager)
            viewPager.adapter = null
//            viewPager = null
        }
        tabLayout.let {
            (tabLayout.parent as? ViewGroup)?.removeView(tabLayout)
            tabLayout.clearOnTabSelectedListeners()
//            tabLayout = null
        }
        _binding = null
    }
}

