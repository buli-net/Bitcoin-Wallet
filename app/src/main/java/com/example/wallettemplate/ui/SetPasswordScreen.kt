
package com.example.wallettemplate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wallettemplate.WalletApplication
import com.google.protobuf.ByteString
import org.bitcoinj.crypto.AesKey
import org.bitcoinj.crypto.KeyCrypterScrypt
import org.bitcoinj.protobuf.wallet.Protos
import kotlinx.coroutines.*

@Composable
fun SetPasswordScreen(app: WalletApplication, nav: androidx.navigation.NavController) {
    var p1 by remember { mutableStateOf("") }
    var p2 by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var msg by remember { mutableStateOf<String?>(null) }

    // same params as original
    val SCRYPT_PARAMS = Protos.ScryptParameters.newBuilder()
        .setP(6).setR(8).setN(32768)
        .setSalt(ByteString.copyFrom(KeyCrypterScrypt.randomSalt())).build()

    Scaffold { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)){
            Text("Set Password", style=MaterialTheme.typography.headlineSmall)
            Text("Setting a password on your wallet makes it safer against viruses and theft. You will need to enter your password whenever money is sent.")
            OutlinedTextField(p1, {p1=it}, label={Text("Enter password")}, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(p2, {p2=it}, label={Text("Repeat password")}, modifier=Modifier.fillMaxWidth())
            if(loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            msg?.let{ Text(it) }
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                OutlinedButton(onClick={nav.popBackStack()}){ Text("Close") }
                Button(onClick={
                    if(p1!=p2){ msg="Passwords do not match"; return@Button }
                    if(p1.length<4){ msg="Password too short"; return@Button }
                    loading=true
                    scope.launch(Dispatchers.Default){
                        val scrypt = KeyCrypterScrypt(SCRYPT_PARAMS)
                        val aesKey = scrypt.deriveKey(p1)
                        withContext(Dispatchers.Main){
                            app.walletAppKit.wallet().encrypt(scrypt, aesKey)
                            msg="Wallet encrypted"
                            loading=false
                            nav.popBackStack()
                        }
                    }
                }){ Text("Set password") }
            }
        }
    }
}
