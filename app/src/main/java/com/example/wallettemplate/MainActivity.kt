
package com.example.wallettemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.wallettemplate.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as WalletApplication
        setContent {
            MaterialTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination="main") {
                    composable("main") { MainScreen(app, nav) }
                    composable("send") { SendMoneyScreen(app, nav) }
                    composable("settings") { SettingsScreen(app, nav) }
                    composable("set_password") { SetPasswordScreen(app, nav) }
                }
            }
        }
    }
}

@Composable
fun MainScreen(app: WalletApplication, nav: androidx.navigation.NavController) {
    val wallet = app.walletAppKit.wallet()
    var balance by remember { mutableStateOf(wallet.balance.toString()) }
    LaunchedEffect(Unit) {
        wallet.addCoinsReceivedEventListener { _, _, _ -> balance = wallet.balance.toFriendlyString() }
    }
    Scaffold { p ->
        Column(Modifier.padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Balance", style=MaterialTheme.typography.headlineMedium)
            Row { Text(balance, style=MaterialTheme.typography.headlineSmall); Spacer(Modifier.width(8.dp)); Text("BTC") }
            // ClickableBitcoinAddress equivalent -> QR
            Text("Address: ${wallet.currentAddress(app.scriptType, app.network).toString()}")
            Button(onClick={ nav.navigate("settings") }){ Text("Settings") }
            Button(onClick={ nav.navigate("send") }, colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.primary)){ Text("Send money out") }
        }
    }
}
