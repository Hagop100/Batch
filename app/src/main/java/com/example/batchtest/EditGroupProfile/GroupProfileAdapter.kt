package com.example.batchtest.EditGroupProfile

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.batchtest.myGroupsTab.EditGroupProfile

class GroupProfileAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle) :
FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
       return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> EditGroupInfoFragment()
            1 -> PreviewGroupInfoFragment()
            else -> EditGroupInfoFragment()
        }

    }


}
