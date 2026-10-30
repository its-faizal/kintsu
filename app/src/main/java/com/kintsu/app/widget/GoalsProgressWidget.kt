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
import com.kintsu.app.data.KintsuDatabase
import kotlinx.coroutines.flow.first

class GoalsProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KintsuDatabase.getDatabase(context)
        val colors = KintsuWidgetTheme.getColors(context)

        val goals = database.goalDao().getAllGoals().first()
        val activeGoal = goals.firstOrNull()

        provideContent {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceProvider)
                    .cornerRadius(18.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_TAB_GOALS))),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeGoal == null) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Goals Progress",
                            style = TextStyle(
                                color = colors.textPrimaryProvider,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Set a meaningful goal · Tap to start",
                            style = TextStyle(
                                color = colors.textSecondaryProvider,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .background(colors.accentSoftProvider)
                            .cornerRadius(10.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+ Goal",
                            style = TextStyle(
                                color = colors.accentProvider,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Goal",
                                style = TextStyle(
                                    color = colors.goldSeamProvider,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = activeGoal.title,
                                style = TextStyle(
                                    color = colors.textPrimaryProvider,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        // Custom Progress bar
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .height(4.dp)
                                    .background(colors.surfaceElevatedProvider)
                                    .cornerRadius(2.dp)
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(colors.goldSeamProvider)
                                        .cornerRadius(2.dp)
                                ) {}
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    Text(
                        text = "${activeGoal.manualProgressPercent}%",
                        style = TextStyle(
                            color = colors.goldSeamProvider,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

class GoalsProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GoalsProgressWidget()
}
