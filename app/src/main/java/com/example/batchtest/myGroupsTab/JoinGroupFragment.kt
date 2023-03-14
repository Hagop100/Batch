package com.example.batchtest.myGroupsTab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentGroupCreationBinding
import com.example.batchtest.databinding.FragmentJoinGroupBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [JoinGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JoinGroupFragment : Fragment() {
    private var _binding: FragmentJoinGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentJoinGroupBinding.inflate(layoutInflater, container, false)


        /**
         * search for an existing group
         */
//        binding.searchGroup.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                if (newText != null) {
//                    filter(newText)
//                }
//                return true
//            }
//
//        })
        /**
         * user exits back to myGroupFragment
         */
        binding.exitJoinGroup.setOnClickListener{
            findNavController().navigate(R.id.action_joinGroupFragment_to_myGroupFragment)
        }
        return binding.root
    }
//    private fun filter(query: String){
//        val filteredList = ArrayList<Group>()
//        for (group in availableGroupsList) {
//            if (group.name.toLowerCase().contains(query.toLowerCase())) {
//                filteredList.add(group)
//            }
//        }
//        JoinGroupAdapter.filterList(filteredList)
//
//
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment JoinGroupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JoinGroupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}