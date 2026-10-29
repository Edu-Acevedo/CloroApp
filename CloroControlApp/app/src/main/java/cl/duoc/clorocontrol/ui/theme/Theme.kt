package cl.duoc.clorocontrol.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CloroColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = TealDark,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = WarningAmber,
    background = Canvas,
    surface = androidx.compose.ui.graphics.Color.White,
    error = CriticalRed
)

@Composable
fun CloroControlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CloroColors,
        typography = Typography(),
        content = content
    )
}
