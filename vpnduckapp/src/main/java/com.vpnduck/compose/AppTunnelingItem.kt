package com.vpnduck.compose

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.vpnduck.theme.backgroundSecondary
import com.vpnduck.theme.plusJakartaSans
import common.model.SplitTunnelingApp

@Composable
fun AppTunnelingItem(app: SplitTunnelingApp) {
    var toggleState by remember { mutableStateOf(false)}
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(70.dp)
            .background(backgroundSecondary, shape = RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = app.image.toBitmap().asImageBitmap(),
            contentDescription = "${app.appName} Icon",
            modifier = Modifier.padding(start = 18.dp).size(30.dp)
        )

        Text(
            modifier = Modifier.padding(start = 20.dp).fillMaxWidth(0.8f),
            text = app.appName,
            fontFamily = plusJakartaSans,
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(Modifier.width(5.dp))

        Switch(
            modifier = Modifier
                .width(35.dp)
                .height(22.dp),
            checked = toggleState,
            onCheckedChange = {
                toggleState = !toggleState
            }
        )
    }
    Spacer(
        modifier = Modifier.height(1.dp)
    )
}