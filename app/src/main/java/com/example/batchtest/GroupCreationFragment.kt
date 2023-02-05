package com.example.batchtest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentGroupCreationBinding
import com.example.batchtest.databinding.FragmentLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.w3c.dom.Text
import java.security.acl.Group
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupCreationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupCreationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentGroupCreationBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupCreation: GroupCreation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        initialize the values of  GroupCreation class
        groupCreation = GroupCreation(
            groupCode = UUID.randomUUID(),
            groupName = "",
//            tags = "",
            groupDescription = ""
        )

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        _binding = FragmentGroupCreationBinding.inflate(layoutInflater, container, false)

//        user clicks the X button to navigate back to my groups tab
        binding.exitGroupCreatn.setOnClickListener{
            findNavController().navigate(R.id.to_myGroupFragment)
        }

        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//    }

//    free view from memory
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}

}