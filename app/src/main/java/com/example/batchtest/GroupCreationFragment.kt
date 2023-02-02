package com.example.batchtest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.batchtest.databinding.FragmentGroupCreationBinding
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
    private lateinit var binding: FragmentGroupCreationBinding
    private lateinit var groupCreation: GroupCreation

    private val _binding
        get() = checkNotNull(binding) {
            "Cannot access binding because it is null. Is the view visible?"
        };

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

//        TODO: this code does not seem to work here!!!FIXME
        //This grabs the nav_bar and sets it visible upon this fragment's onCreateView
        val navBar: BottomNavigationView? = activity?.findViewById(R.id.nav_bar)
        navBar?.visibility = View.INVISIBLE

        // Inflate the layout for this fragment
        binding = FragmentGroupCreationBinding.inflate(layoutInflater, container, false)


        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupCreationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupCreationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}