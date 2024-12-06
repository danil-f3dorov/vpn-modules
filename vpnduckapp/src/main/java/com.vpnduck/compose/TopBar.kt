package com.vpnduck.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnduck.R
import com.vpnduck.theme.plusJakartaSans
import com.vpnduck.theme.textColor1

@Composable
fun TopBar(
    title: String,
    onBackPressed: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = 32.dp)
            .padding(start = 24.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Green)
                .clickable { onBackPressed() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                contentDescription = "back",
                tint = textColor1
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontFamily = plusJakartaSans,
            color = textColor1
        )
    }
}