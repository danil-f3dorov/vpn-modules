package common.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import common.R
import common.compose.theme.background
import common.compose.theme.dim

@Composable
fun CountryBar(
    countryFlag: Painter,
    countryName: String,
    countryIp: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(12.dp), color = background),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(18.dp))
        Image(painter = countryFlag, contentDescription = "countryFlag")
        Spacer(Modifier.width(24.dp))

        Column(
            modifier = Modifier
                .height(75.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = countryName,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = countryIp,
                fontSize = 14.sp,
                color = dim
            )

        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .padding(end = 20.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Image(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = "go to choose vpn server",
        )
    }

}


@Composable
@Preview(showBackground = true)
fun CountryBarPreview() {
    CountryBar(
        painterResource(R.drawable.ic_poland),
        "Poland",
        "224.23.423.651"
    )
}






































