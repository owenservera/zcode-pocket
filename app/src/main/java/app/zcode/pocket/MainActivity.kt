package app.zcode.pocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import app.zcode.pocket.ui.PocketApp
import app.zcode.pocket.ui.theme.ZpTheme
import app.zcode.pocket.vm.PocketViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZpTheme {
                val vm: PocketViewModel = viewModel()
                PocketApp(vm)
            }
        }
    }
}
