package  com.enroute.usermanager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.enroute.usermanager.api.AdminApiClient
import com.enroute.usermanager.theme.EnrouteTheme
import com.enroute.usermanager.ui.LoginScreen
import com.enroute.usermanager.ui.UserManagerScreen

// Change this to your backend's default address (or leave blank and type
// it in at login each time). This is only a starting value for the field
// on the login screen, not a hardcoded destination.
private const val DEFAULT_BASE_URL = "http://127.0.0.1:5000"

fun main() = application {
    val client = remember { AdminApiClient(DEFAULT_BASE_URL) }
    var loggedIn by remember { mutableStateOf(false) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Enroute User Manager",
        state = WindowState(size = DpSize(1000.dp, 700.dp))
    ) {
        EnrouteTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (loggedIn) {
                    UserManagerScreen(
                        client = client,
                        onLogOut = { loggedIn = false }
                    )
                } else {
                    LoginScreen(
                        client = client,
                        initialBaseUrl = DEFAULT_BASE_URL,
                        onLoggedIn = { loggedIn = true }
                    )
                }
            }
        }
    }
}
