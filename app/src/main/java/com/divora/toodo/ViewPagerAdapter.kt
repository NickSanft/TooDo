package com.divora.toodo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TaskListFragment.newInstance(isCompleted = false)
            1 -> TaskListFragment.newInstance(isCompleted = true)
            else -> PrizesFragment()
        }
    }
}
