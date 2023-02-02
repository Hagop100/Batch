package com.example.batchtest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Match Tab Fragment
 * displays potential groups the user can match with
 */
class MatchTabFragment : Fragment() {
    // checks if the view is visible
    private var _binding: FragmentMatchTabBinding? = null;
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        };
    // inflate and bind the match tab fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMatchTabBinding.inflate(inflater, container, false)

        // find the nav bar and set it visible upon this fragment's onCreateView
        val navBar: BottomNavigationView? = getActivity()?.findViewById(R.id.nav_bar)
        navBar?.visibility = View.VISIBLE

        return binding.root
    }
}