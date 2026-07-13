
package com.example.wallettemplate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wallettemplate.WalletApplication
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.wallet.DeterministicSeed
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun SettingsScreen(app: WalletApplication, nav: androidx.navigation.NavController) {
    val wallet = app.walletAppKit.wallet()
    val seed = wallet.activeKeyChain.seed
    var words by remember { mutableStateOf(seed?.mnemonicString ?: "") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var status by remember { mutableStateOf<String?>(null) }

    Scaffold { p ->
        Column(Modifier.padding(p).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)){
            Text("Settings", style=MaterialTheme.typography.headlineSmall)
            Text("These are your wallet words. Write them down along with the creation date, and you can get your money back even if you lose all your wallet backup files.")
            OutlinedTextField(words, {words=it}, label={Text("Mnemonic")}, modifier=Modifier.fillMaxWidth().height(120.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Text("Created on:")
                DatePickerDocked(date){ date=it }
            }
            status?.let{ Text(it, color=MaterialTheme.colorScheme.primary) }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(onClick={ nav.navigate("set_password") }){ Text(if(wallet.isEncrypted) "Remove password" else "Set password") }
                Button(onClick={
                    try {
                        if(!MnemonicCode.check(MnemonicCode.INSTANCE.toMnemonic(words))) throw Exception("Invalid checksum")
                        val creation = date.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
                        val seedNew = DeterministicSeed.ofMnemonic(words, creation)
                        // restore logic similar to original WalletSettingsController
                        app.walletAppKit.restoreWalletFromSeed(seedNew)
                        status="Restored! Restart app."
                    } catch(e:Exception){ status=e.message }
                }){ Text("Restore from words") }
                OutlinedButton(onClick={ nav.popBackStack() }){ Text("Close") }
            }
        }
    }
}

@Composable fun DatePickerDocked(current: LocalDate, onChange:(LocalDate)->Unit){ 
    var show by remember { mutableStateOf(false) }
    OutlinedButton(onClick={show=true}){ Text(current.toString()) }
}
