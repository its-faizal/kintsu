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
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kintsu.app.data.KintsuDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DailyQuoteWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KintsuDatabase.getDatabase(context)
        val colors = KintsuWidgetTheme.getColors(context)

        val todayEpochDay = LocalDate.now().toEpochDay()
        val todayIntention = database.dailyIntentionDao().getIntentionForDay(todayEpochDay).first()

        val quoteText = todayIntention?.text?.ifBlank { null }
            ?: "Move with gentle patience. Even broken vessels mend with gold."

        provideContent {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceProvider)
                    .cornerRadius(18.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_OPEN_INTENTION))),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold seam accent dot
                Text(
                    text = "✦",
                    style = TextStyle(
                        color = colors.goldSeamProvider,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Daily Intention",
                        style = TextStyle(
                            color = colors.accentProvider,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "“$quoteText”",
                        style = TextStyle(
                            color = colors.textPrimaryProvider,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

class DailyQuoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyQuoteWidget()
}
