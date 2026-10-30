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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.kintsu.app.data.KintsuDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class AestheticQuoteWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = KintsuDatabase.getDatabase(context)
        val colors = KintsuWidgetTheme.getColors(context)

        val todayEpochDay = LocalDate.now().toEpochDay()
        val todayIntention = database.dailyIntentionDao().getIntentionForDay(todayEpochDay).first()

        val reflectionText = todayIntention?.text?.ifBlank { null }
            ?: "Small steps every day. The vessel is mended with gold."

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceElevatedProvider)
                    .cornerRadius(22.dp)
                    .padding(14.dp)
                    .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_OPEN_INTENTION))),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold seam decorative glyph
                Text(
                    text = "— ✦ —",
                    style = TextStyle(
                        color = colors.goldSeamProvider,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                Text(
                    text = "“$reflectionText”",
                    style = TextStyle(
                        color = colors.textPrimaryProvider,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 4
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(colors.goldSeamProvider)
                        .cornerRadius(1.dp)
                ) {}
            }
        }
    }
}

class AestheticQuoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AestheticQuoteWidget()
}
