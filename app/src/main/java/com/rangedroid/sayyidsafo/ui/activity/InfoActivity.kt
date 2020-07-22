package com.rangedroid.sayyidsafo.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.rangedroid.sayyidsafo.R
import com.rangedroid.sayyidsafo.ui.adapter.InfoPagerAdapter

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val infoPagerAdapter =
            InfoPagerAdapter(
                this,
                supportFragmentManager
            )

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = infoPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }
}