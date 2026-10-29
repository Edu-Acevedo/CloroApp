package cl.duoc.clorocontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cl.duoc.clorocontrol.ui.navigation.CloroControlApp
import cl.duoc.clorocontrol.ui.theme.CloroControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CloroControlTheme {
                CloroControlApp()
            }
        }
    }
}
