
package com.example.wallettemplate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wallettemplate.WalletApplication
import org.bitcoinj.crypto.KeyCrypterScrypt
import org.bitcoinj.crypto.AesKey
import kotlinx.coroutines.*

@Composable
fun PasswordDialog(app: WalletApplication, onSuccess:(AesKey)->Unit, onCancel:()->Unit) {
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest=onCancel, title={Text("Password")}, text={
        Column{
            Text("Please enter your wallet password now:")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(pass, {pass=it}, label={Text("Password")})
            if(loading) LinearProgressIndicator(progress={progress}, modifier=Modifier.fillMaxWidth().padding(top=12.dp))
        }
    }, confirmButton={
        Button(onClick={
            loading=true
            scope.launch(Dispatchers.Default){
                try {
                    val wallet = app.walletAppKit.wallet()
                    val scrypt = wallet.keyCrypter as KeyCrypterScrypt
                    // estimate key derivation like original WalletPasswordController
                    val key = scrypt.deriveKey(pass)
                    if(!wallet.checkAESKey(key)){ withContext(Dispatchers.Main){ loading=false }; return@launch }
                    withContext(Dispatchers.Main){ onSuccess(key) }
                } catch(e:Exception){ withContext(Dispatchers.Main){ loading=false } }
            }
        }){ Text("Confirm") }
    }, dismissButton={ OutlinedButton(onClick=onCancel){ Text("Cancel") } })
}
