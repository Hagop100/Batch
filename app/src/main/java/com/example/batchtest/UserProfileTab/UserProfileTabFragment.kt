package com.example.batchtest.UserProfileTab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentAccountSettingBinding
import com.example.batchtest.databinding.FragmentUserProfileTabBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserProfileTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserProfileTabFragment : Fragment() {
    private var _binding: FragmentUserProfileTabBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserProfileTabBinding.inflate(layoutInflater, container, false)

        binding.editProfileBtn.setOnClickListener{

            //hides the bottom nav when navigate to the account setting page
            val navBar: BottomNavigationView? = activity?.findViewById(R.id.nav_bar)
            navBar?.visibility = View.GONE

            findNavController().navigate(R.id.action_userProfileTabFragment_to_editProfileFragment)
        }

        binding.settingBtn.setOnClickListener{

            //hides the bottom nav when navigate to the account setting page
            val navBar: BottomNavigationView? = activity?.findViewById(R.id.nav_bar)
            navBar?.visibility = View.GONE

            findNavController().navigate(R.id.action_userProfileTabFragment_to_accountSettingFragment)
        }

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