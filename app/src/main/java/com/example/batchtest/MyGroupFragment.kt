package com.example.batchtest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentGroupCreationBinding
import com.example.batchtest.databinding.FragmentMyGroupBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyGroupFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentMyGroupBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
//
//            //This grabs the nav_bar and sets it visible upon this fragment's onCreateView
//            val navBar: BottomNavigationView? = activity?.findViewById(R.id.nav_bar)
//            navBar?.visibility = View.INVISIBLE

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMyGroupBinding.inflate(layoutInflater,container, false)

//      this button navigates from My Group view to Create a group view fragment
        binding.btnToGroupCreation.setOnClickListener{

//            hides the bottom nav when navigate to the group creation page
            val navBar: BottomNavigationView? = activity?.findViewById(R.id.nav_bar)
            navBar?.visibility = View.GONE
            findNavController().navigate(R.id.to_groupCreationFragment)
        }


        return binding.root

    }




//    free view from memory
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}