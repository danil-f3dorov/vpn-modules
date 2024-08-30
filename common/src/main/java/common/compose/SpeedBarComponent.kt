package common.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import common.R


@Composable
fun SpeedBarComponent(
    modifier: Modifier = Modifier,
    arrow: Painter, text: String, speed: String
) {
    Column(
        modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            Image(painter = arrow, contentDescription = text)
            Spacer(Modifier.width(6.dp))
            Text(text, color = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        Row {
            Text(speed, color = Color.White)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SpeedBarComponentPreview() {
    SpeedBarComponent(
        arrow = painterResource(R.drawable.ic_arrow_up),
        text = "Download",
        speed = "620 mb/s"
    )
}