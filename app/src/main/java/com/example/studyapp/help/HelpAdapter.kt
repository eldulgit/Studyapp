package com.example.studyapp.help

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studyapp.R

class HelpAdapter(
    private val pages: List<HelpPage>,
    private val onPageClick: () -> Unit
) : RecyclerView.Adapter<HelpAdapter.HelpViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help_page, parent, false)
        return HelpViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        holder.bind(pages[position], onPageClick)
    }

    override fun getItemCount(): Int = pages.size

    class HelpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.helpPageTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.helpPageDescription)
        private val mockContainer: FrameLayout = itemView.findViewById(R.id.helpMockContainer)

        fun bind(page: HelpPage, onPageClick: () -> Unit) {
            titleText.text = page.title
            descriptionText.text = page.description
            mockContainer.removeAllViews()
            mockContainer.addView(createPhoneMock(page.type))
            itemView.setOnClickListener { onPageClick() }
        }

        private fun createPhoneMock(type: HelpPageType): View {
            val phone = LinearLayout(itemView.context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), dp(18), dp(18), dp(14))
                background = rounded(Color.WHITE, dp(28), strokeColor = Color.parseColor("#D8E1F0"))
                elevation = dp(6).toFloat()
            }

            phone.addView(statusBar())
            phone.addView(
                when (type) {
                    HelpPageType.Lifestyle -> lifestyleMock()
                    HelpPageType.ScheduleSetup -> scheduleSetupMock()
                    HelpPageType.Timer -> timerMock()
                    HelpPageType.Stats -> statsMock()
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )

            return phone.apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    leftMargin = dp(8)
                    rightMargin = dp(8)
                }
            }
        }

        private fun statusBar(): View {
            return LinearLayout(itemView.context).apply {
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
                addView(dot("#172033", 6))
                addView(spacer(6, 1))
                addView(dot("#172033", 6))
                addView(weightSpace())
                addView(pill(width = 42, height = 8, color = "#D7DFEF", radius = 8))
            }.withHeight(18)
        }

        private fun lifestyleMock(): View {
            return vertical {
                addView(sectionTitle("Lifestyle"))
                addView(inputRow("Wake", "07:30"))
                addView(inputRow("Sleep", "24:00"))
                addView(twoInputs("Lunch", "12:00", "13:00"))
                addView(twoInputs("Dinner", "18:00", "19:00"))
                addView(weightSpace())
                addView(button("Save"))
            }
        }

        private fun scheduleSetupMock(): View {
            return vertical {
                addView(sectionTitle("Setup"))
                addView(goalCard("Math", "#FFE2E6", "#D94862"))
                addView(goalCard("English", "#E2F4EC", "#28966F"))
                addView(timetablePreview())
                addView(bottomTabs(selected = 0))
            }
        }

        private fun timerMock(): View {
            return vertical {
                addView(sectionTitle("Timer"))
                addView(timerCircle())
                addView(timerTask("Math", "01:20:00", "#F7C8D4"))
                addView(timerTask("English", "00:40:00", "#CDEADB"))
                addView(timerTask("Science", "00:30:00", "#C8D8F4"))
                addView(bottomTabs(selected = 1))
            }
        }

        private fun statsMock(): View {
            return vertical {
                addView(sectionTitle("Stats"))
                addView(statBars())
                addView(goalCard("Today 3h 20m", "#EAF4FF", "#2E5AAC"))
                addView(goalCard("Focus score 82%", "#E9F7F1", "#2F8B57"))
                addView(weightSpace())
                addView(bottomTabs(selected = 3))
            }
        }

        private fun sectionTitle(text: String): View {
            return TextView(itemView.context).apply {
                this.text = text
                textSize = 20f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.parseColor("#182235"))
                gravity = Gravity.CENTER_VERTICAL
            }.withMargins(bottom = 14)
        }

        private fun inputRow(label: String, value: String): View {
            return horizontal {
                addView(labelText(label), LinearLayout.LayoutParams(0, dp(44), 1f))
                addView(
                    chip(value, "#F2F6FC", "#172033"),
                    LinearLayout.LayoutParams(dp(116), dp(44))
                )
            }.withMargins(bottom = 10)
        }

        private fun twoInputs(label: String, start: String, end: String): View {
            return vertical {
                addView(labelText(label))
                addView(horizontal {
                    addView(chip(start, "#F2F6FC", "#172033"), LinearLayout.LayoutParams(0, dp(42), 1f))
                    addView(spacer(8, 1))
                    addView(chip(end, "#F2F6FC", "#172033"), LinearLayout.LayoutParams(0, dp(42), 1f))
                })
            }.withMargins(bottom = 10)
        }

        private fun timetablePreview(): View {
            return LinearLayout(itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(8), dp(8), dp(8), dp(8))
                background = rounded(Color.parseColor("#F5F8FE"), dp(16))

                listOf("#FFE3D2", "#E2F4EC", "#DCE8FF", "#F2E2FF").forEachIndexed { index, color ->
                    addView(
                        View(context).apply {
                            background = rounded(Color.parseColor(color), dp(10))
                        },
                        LinearLayout.LayoutParams(0, dp(if (index % 2 == 0) 132 else 92), 1f).apply {
                            leftMargin = dp(4)
                            rightMargin = dp(4)
                            topMargin = dp(if (index % 2 == 0) 8 else 36)
                        }
                    )
                }
            }.withMargins(top = 6, bottom = 12)
        }

        private fun timerCircle(): View {
            return FrameLayout(itemView.context).apply {
                val outer = View(context).apply {
                    background = rounded(Color.parseColor("#DCE8FF"), dp(999))
                }
                val inner = TextView(context).apply {
                    text = "01:20"
                    textSize = 25f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(Color.parseColor("#25314A"))
                    gravity = Gravity.CENTER
                    background = rounded(Color.WHITE, dp(999))
                }
                addView(outer, FrameLayout.LayoutParams(dp(172), dp(172), Gravity.CENTER))
                addView(inner, FrameLayout.LayoutParams(dp(118), dp(118), Gravity.CENTER))
            }.withHeight(190).withMargins(bottom = 12)
        }

        private fun timerTask(title: String, time: String, color: String): View {
            return horizontal {
                gravity = Gravity.CENTER_VERTICAL
                addView(dot(color, 16))
                addView(spacer(10, 1))
                addView(labelText(title), LinearLayout.LayoutParams(0, dp(40), 1f))
                addView(chip(time, "#F2F6FC", "#172033"), LinearLayout.LayoutParams(dp(96), dp(36)))
            }.withMargins(bottom = 8)
        }

        private fun statBars(): View {
            return LinearLayout(itemView.context).apply {
                gravity = Gravity.BOTTOM
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(10), dp(10), dp(10), dp(10))
                background = rounded(Color.parseColor("#F5F8FE"), dp(16))

                listOf(72, 112, 86, 142, 118).forEach { height ->
                    addView(
                        View(context).apply {
                            background = rounded(Color.parseColor("#8FB2FF"), dp(8))
                        },
                        LinearLayout.LayoutParams(0, dp(height), 1f).apply {
                            leftMargin = dp(6)
                            rightMargin = dp(6)
                        }
                    )
                }
            }.withHeight(176).withMargins(bottom = 12)
        }

        private fun goalCard(text: String, background: String, foreground: String): View {
            return horizontal {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), 0, dp(14), 0)
                this.background = rounded(Color.parseColor(background), dp(14))
                addView(dot(foreground, 12))
                addView(spacer(10, 1))
                addView(labelText(text), LinearLayout.LayoutParams(0, dp(46), 1f))
            }.withMargins(bottom = 10)
        }

        private fun bottomTabs(selected: Int): View {
            val labels = listOf("Setup", "Timer", "Schedule", "Stats", "Settings")
            return LinearLayout(itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                labels.forEachIndexed { index, label ->
                    addView(
                        TextView(context).apply {
                            text = label.take(5)
                            textSize = 9f
                            gravity = Gravity.CENTER
                            setTextColor(
                                Color.parseColor(
                                    if (index == selected) "#3D6BFF" else "#7C8798"
                                )
                            )
                        },
                        LinearLayout.LayoutParams(0, dp(34), 1f)
                    )
                }
            }.withMargins(top = 6)
        }

        private fun button(text: String): View {
            return TextView(itemView.context).apply {
                this.text = text
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                background = rounded(Color.parseColor("#3D6BFF"), dp(16))
            }.withHeight(52)
        }

        private fun chip(text: String, backgroundColor: String, textColor: String): TextView {
            return TextView(itemView.context).apply {
                this.text = text
                textSize = 15f
                setTextColor(Color.parseColor(textColor))
                gravity = Gravity.CENTER
                background = rounded(Color.parseColor(backgroundColor), dp(12))
            }
        }

        private fun labelText(text: String): TextView {
            return TextView(itemView.context).apply {
                this.text = text
                textSize = 15f
                setTextColor(Color.parseColor("#25314A"))
                gravity = Gravity.CENTER_VERTICAL
            }
        }

        private fun dot(color: String, size: Int): View {
            return View(itemView.context).apply {
                background = rounded(Color.parseColor(color), dp(999))
                layoutParams = LinearLayout.LayoutParams(dp(size), dp(size))
            }
        }

        private fun pill(width: Int, height: Int, color: String, radius: Int): View {
            return View(itemView.context).apply {
                background = rounded(Color.parseColor(color), dp(radius))
                layoutParams = LinearLayout.LayoutParams(dp(width), dp(height))
            }
        }

        private fun vertical(block: LinearLayout.() -> Unit): LinearLayout {
            return LinearLayout(itemView.context).apply {
                orientation = LinearLayout.VERTICAL
                block()
            }
        }

        private fun horizontal(block: LinearLayout.() -> Unit): LinearLayout {
            return LinearLayout(itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                block()
            }
        }

        private fun spacer(width: Int, height: Int): View {
            return View(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(width), dp(height))
            }
        }

        private fun weightSpace(): View {
            return View(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
        }

        private fun rounded(
            color: Int,
            radius: Int,
            strokeColor: Int? = null
        ): GradientDrawable {
            return GradientDrawable().apply {
                cornerRadius = radius.toFloat()
                setColor(color)
                strokeColor?.let { setStroke(dp(1), it) }
            }
        }

        private fun View.withHeight(height: Int): View {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(height)
            )
            return this
        }

        private fun View.withMargins(
            top: Int = 0,
            bottom: Int = 0
        ): View {
            val current = layoutParams as? LinearLayout.LayoutParams
                ?: LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            current.topMargin = dp(top)
            current.bottomMargin = dp(bottom)
            layoutParams = current
            return this
        }

        private fun dp(value: Int): Int {
            return (value * itemView.resources.displayMetrics.density).toInt()
        }
    }
}
