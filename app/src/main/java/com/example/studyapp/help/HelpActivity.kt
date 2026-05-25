package com.example.studyapp.help

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.core.view.setMargins
import androidx.viewpager2.widget.ViewPager2
import com.example.studyapp.R

class HelpActivity : ComponentActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout

    private val pages = listOf(
        HelpPage(
            title = "Lifestyle",
            description = "생활패턴을 먼저 입력해요",
            type = HelpPageType.Lifestyle
        ),
        HelpPage(
            title = "Setup",
            description = "과목과 고정 스케줄을 정해요",
            type = HelpPageType.ScheduleSetup
        ),
        HelpPage(
            title = "Timer",
            description = "카메라와 함께 집중 시간을 기록해요",
            type = HelpPageType.Timer
        ),
        HelpPage(
            title = "Stats",
            description = "공부 흐름을 확인하고 조정해요",
            type = HelpPageType.Stats
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        viewPager = findViewById(R.id.helpViewPager)
        indicatorLayout = findViewById(R.id.helpIndicator)

        viewPager.adapter = HelpAdapter(pages) {
            moveToNextPageOrFinish()
        }

        findViewById<View>(R.id.helpRoot).setOnClickListener {
            moveToNextPageOrFinish()
        }

        setupIndicator()
        updateIndicator(0)

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateIndicator(position)
                }
            }
        )
    }

    private fun moveToNextPageOrFinish() {
        val nextItem = viewPager.currentItem + 1
        if (nextItem < pages.size) {
            viewPager.setCurrentItem(nextItem, true)
        } else {
            finish()
        }
    }

    private fun setupIndicator() {
        indicatorLayout.removeAllViews()

        repeat(pages.size) {
            indicatorLayout.addView(createIndicatorDot(isSelected = false))
        }
    }

    private fun updateIndicator(selectedPosition: Int) {
        for (index in 0 until indicatorLayout.childCount) {
            indicatorLayout.getChildAt(index).background =
                createDotDrawable(isSelected = index == selectedPosition)
        }
    }

    private fun createIndicatorDot(isSelected: Boolean): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.help_indicator_dot_size),
                resources.getDimensionPixelSize(R.dimen.help_indicator_dot_size)
            ).apply {
                setMargins(resources.getDimensionPixelSize(R.dimen.help_indicator_dot_margin))
            }
            background = createDotDrawable(isSelected)
        }
    }

    private fun createDotDrawable(isSelected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(if (isSelected) Color.parseColor("#3D6BFF") else Color.parseColor("#C8D1E0"))
        }
    }
}
