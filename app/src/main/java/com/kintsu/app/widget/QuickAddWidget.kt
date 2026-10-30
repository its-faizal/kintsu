package com.kintsu.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class QuickAddWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val colors = KintsuWidgetTheme.getColors(context)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceProvider)
                    .cornerRadius(22.dp)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action 1: New Task
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevatedProvider)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_ADD_TASK))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+",
                        style = TextStyle(
                            color = colors.accentProvider,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "New Task",
                        style = TextStyle(
                            color = colors.textPrimaryProvider,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Action 2: New Habit
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevatedProvider)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_ADD_HABIT))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✦",
                        style = TextStyle(
                            color = colors.goldSeamProvider,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "New Habit",
                        style = TextStyle(
                            color = colors.textPrimaryProvider,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Action 3: Journal
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevatedProvider)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_OPEN_JOURNAL))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✎",
                        style = TextStyle(
                            color = colors.accentProvider,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "Evening Journal",
                        style = TextStyle(
                            color = colors.textPrimaryProvider,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Action 4: Start Focus
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevatedProvider)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_OPEN_FOCUS))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "▶",
                        style = TextStyle(
                            color = colors.goldSeamProvider,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "Start Focus",
                        style = TextStyle(
                            color = colors.textPrimaryProvider,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}
