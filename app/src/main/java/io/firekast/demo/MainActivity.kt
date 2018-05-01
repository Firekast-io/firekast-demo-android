package io.firekast.demo

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (navigation.selectedItemId == item.itemId && !supportFragmentManager.fragments.isEmpty()) {
            return@OnNavigationItemSelectedListener false
        }
        when (item.itemId) {
            R.id.navigation_streamer -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, StreamerFragment())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_player -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, Fragment())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_streamer
    }
}
