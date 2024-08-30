package common.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import common.R
import common.compose.theme.background2


@Composable
fun SpeedBar() {
    Row(
        modifier = Modifier.background(shape = RoundedCornerShape(12.dp), color = background2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpeedBarComponent(
            Modifier.weight(1f),
            painterResource(R.drawable.ic_arrow_up),
            "Download",
            "60 mb/s"
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(50.dp)
                .background(Color.Gray)
        )
        SpeedBarComponent(
            Modifier.weight(1f),
            painterResource(R.drawable.arrow_down),
            "Upload",
            "5 mb/s"
        )

    }
}


@Composable
@Preview(showBackground = true)
fun SpeedBarPreview() {
    SpeedBar()
}