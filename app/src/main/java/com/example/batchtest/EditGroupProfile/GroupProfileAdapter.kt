package com.example.batchtest.EditGroupProfile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.batchtest.myGroupsTab.EditGroupProfile
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class GroupProfileAdapter(
    fragment: Fragment)
    : FragmentStateAdapter(fragment) {



    override fun getItemCount(): Int {
       return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> EditGroupInfoFragment()
            1 -> PreviewGroupInfoFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }

    }


}
