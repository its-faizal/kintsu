package com.kintsu.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
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
import com.kintsu.app.MainActivity
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.domain.StreakCalculator
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class StreakGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KintsuDatabase.getDatabase(context)
        val habits = database.habitDao().getActiveHabits().first()
        val logs = database.habitDao().getAllLogs().first()
        val todayEpochDay = LocalDate.now().toEpochDay()

        val streakResult = StreakCalculator.calculateStreaks(
            habits = habits,
            logs = logs,
            todayEpochDay = todayEpochDay,
            goldRepairEnabled = true
        )

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(KintsuGlanceTheme.backgroundProvider)
                    .cornerRadius(20.dp)
                    .padding(14.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${streakResult.currentStreak}",
                    style = TextStyle(
                        color = KintsuGlanceTheme.textPrimaryProvider,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Day Streak",
                    style = TextStyle(
                        color = KintsuGlanceTheme.textSecondaryProvider,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                Row(
                    modifier = GlanceModifier
                        .background(KintsuGlanceTheme.goldAccentSoftProvider)
                        .cornerRadius(12.dp)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✦ ${streakResult.totalRepairs} Mended",
                        style = TextStyle(
                            color = KintsuGlanceTheme.goldAccentProvider,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

class StreakGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakGlanceWidget()
}
