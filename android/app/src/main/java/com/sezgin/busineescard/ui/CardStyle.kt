package com.sezgin.busineescard.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sezgin.busineescard.R

object CardStyle {
    const val CANVAS_WIDTH = 1050f
    const val CANVAS_HEIGHT = 600f
    const val ASPECT_RATIO = CANVAS_WIDTH / CANVAS_HEIGHT

    val CARD_CORNER_RADIUS = 24.dp
    val CARD_ELEVATION = 10.dp

    // Standart koordinatlar
    const val DEFAULT_NAME_X = 60f
    const val DEFAULT_NAME_Y = 60f
    const val DEFAULT_TITLE_X = 60f
    const val DEFAULT_TITLE_Y = 150f
    const val DEFAULT_COMPANY_X = 60f
    const val DEFAULT_COMPANY_Y = 200f
    const val DEFAULT_DETAILS_X = 60f
    const val DEFAULT_DETAILS_Y = 440f
    const val DEFAULT_QR_X = 710f
    const val DEFAULT_QR_Y = 260f

    const val DETAILS_WIDTH = 610f
    const val DETAILS_LINE_GAP = 12f
    const val EMAIL_TO_PHONE_GAP = 16f
    const val PHONE_LINE_GAP = 12f

    const val QR_FRAME_SIZE_CANVAS = 300f
    const val QR_BIT_SIZE_CANVAS = 270f
    const val QR_CORNER_RADIUS_CANVAS = 38f

    const val CANVAS_NAME_SIZE = 65f
    const val CANVAS_TITLE_SIZE = 38f
    const val CANVAS_COMPANY_SIZE = 38f
    const val CANVAS_CONTACT_SIZE = 30f
    const val CANVAS_ADDRESS_SIZE = 28f
    const val CANVAS_ADDRESS_LINE_HEIGHT = 34f

    val DARK_TEXT = Color(0xFF2C3E50)
    val LIGHT_TEXT = Color.White
    val CANVAS_DARK_COLOR = android.graphics.Color.parseColor("#2C3E50")
    val CANVAS_LIGHT_COLOR = android.graphics.Color.WHITE

    fun textColor(templateId: Int): Color =
        if (templateId == 2 || templateId == 3) LIGHT_TEXT else DARK_TEXT

    fun canvasTextColor(templateId: Int): Int =
        if (templateId == 2 || templateId == 3) CANVAS_LIGHT_COLOR else CANVAS_DARK_COLOR

    fun getFontFamily(fontStyle: String?): FontFamily {
        return when (fontStyle) {
            "SansSerif" -> FontFamily.SansSerif
            "Serif" -> FontFamily.Serif
            "Monospace" -> FontFamily.Monospace
            "Cursive" -> FontFamily.Cursive
            "Premium-1" -> FontFamily.Serif
            "Premium-2" -> FontFamily.Cursive
            else -> FontFamily.Default
        }
    }
    
    val FONT_OPTIONS = listOf("Default", "SansSerif", "Serif", "Monospace", "Cursive", "Premium-1", "Premium-2")
}

@Composable
fun StandardBusinessCard(
    modifier: Modifier = Modifier,
    name: String,
    title: String,
    company: String,
    address: String,
    email: String,
    phone1: String,
    phone2: String? = null,
    templateId: Int = 1,
    fontStyle: String? = "Default",
    qrBitmap: Bitmap? = null
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CardStyle.ASPECT_RATIO)
            .shadow(CardStyle.CARD_ELEVATION, RoundedCornerShape(CardStyle.CARD_CORNER_RADIUS))
            .clip(RoundedCornerShape(CardStyle.CARD_CORNER_RADIUS))
    ) {
        val scale = maxWidth.value / CardStyle.CANVAS_WIDTH
        val textColor = CardStyle.textColor(templateId)
        val fontFamily = CardStyle.getFontFamily(fontStyle)
        val qrImageInset = ((CardStyle.QR_FRAME_SIZE_CANVAS - CardStyle.QR_BIT_SIZE_CANVAS) / 2f * scale).dp

        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(CardStyle.CARD_CORNER_RADIUS))) {
            Image(
                painter = painterResource(backgroundResId(templateId)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // İsim
            Text(
                text = name.uppercase(),
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = fontFamily,
                fontSize = (CardStyle.CANVAS_NAME_SIZE * scale).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(x = (CardStyle.DEFAULT_NAME_X * scale).dp, y = (CardStyle.DEFAULT_NAME_Y * scale).dp)
            )

            // Ünvan
            Text(
                text = title,
                color = textColor,
                fontFamily = fontFamily,
                fontSize = (CardStyle.CANVAS_TITLE_SIZE * scale).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(x = (CardStyle.DEFAULT_TITLE_X * scale).dp, y = (CardStyle.DEFAULT_TITLE_Y * scale).dp)
            )

            // Şirket
            Text(
                text = company,
                color = textColor,
                fontFamily = fontFamily,
                fontSize = (CardStyle.CANVAS_COMPANY_SIZE * scale).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(x = (CardStyle.DEFAULT_COMPANY_X * scale).dp, y = (CardStyle.DEFAULT_COMPANY_Y * scale).dp)
            )

            // Detaylar
            Column(
                modifier = Modifier
                    .offset(x = (CardStyle.DEFAULT_DETAILS_X * scale).dp, y = (CardStyle.DEFAULT_DETAILS_Y * scale).dp)
                    .fillMaxWidth(CardStyle.DETAILS_WIDTH / CardStyle.CANVAS_WIDTH)
            ) {
                if (address.isNotBlank()) {
                    Text(text = address, color = textColor, fontFamily = fontFamily, fontSize = (CardStyle.CANVAS_ADDRESS_SIZE * scale).sp, lineHeight = (CardStyle.CANVAS_ADDRESS_LINE_HEIGHT * scale).sp, maxLines = 3)
                    Spacer(modifier = Modifier.height((CardStyle.DETAILS_LINE_GAP * scale).dp))
                }
                if (email.isNotBlank()) {
                    Text(text = email, color = textColor, fontFamily = fontFamily, fontSize = (CardStyle.CANVAS_CONTACT_SIZE * scale).sp, maxLines = 1)
                    if (phone1.isNotBlank() || !phone2.isNullOrBlank()) Spacer(modifier = Modifier.height((CardStyle.EMAIL_TO_PHONE_GAP * scale).dp))
                }
                if (phone1.isNotBlank()) {
                    Text(text = phone1, color = textColor, fontFamily = fontFamily, fontSize = (CardStyle.CANVAS_CONTACT_SIZE * scale).sp, maxLines = 1)
                    if (!phone2.isNullOrBlank()) Spacer(modifier = Modifier.height((CardStyle.PHONE_LINE_GAP * scale).dp))
                }
                phone2?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, color = textColor, fontFamily = fontFamily, fontSize = (CardStyle.CANVAS_CONTACT_SIZE * scale).sp, maxLines = 1)
                }
            }

            if (qrBitmap != null) {
                Surface(
                    shape = RoundedCornerShape((CardStyle.QR_CORNER_RADIUS_CANVAS * scale).dp),
                    color = Color.White,
                    modifier = Modifier
                        .offset(x = (CardStyle.DEFAULT_QR_X * scale).dp, y = (CardStyle.DEFAULT_QR_Y * scale).dp)
                        .size((CardStyle.QR_FRAME_SIZE_CANVAS * scale).dp)
                        .shadow(10.dp, RoundedCornerShape((CardStyle.QR_CORNER_RADIUS_CANVAS * scale).dp))
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(qrImageInset),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

fun backgroundResId(templateId: Int): Int {
    return when (templateId) {
        1 -> R.drawable.card_bg_1
        2 -> R.drawable.card_bg_2
        3 -> R.drawable.card_bg_3
        4 -> R.drawable.card_bg_4
        5 -> R.drawable.card_bg_5
        6 -> R.drawable.card_bg_6
        else -> R.drawable.card_bg_1
    }
}
