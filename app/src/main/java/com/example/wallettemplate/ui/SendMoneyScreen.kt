
package com.example.wallettemplate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wallettemplate.WalletApplication
import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.Wallet.BalanceType
import kotlinx.coroutines.*

@Composable
fun SendMoneyScreen(app: WalletApplication, nav: androidx.navigation.NavController) {
    var amount by remember { mutableStateOf("") }
    var addressStr by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var pendingTx: Transaction? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    Scaffold { p ->
        Column(Modifier.padding(p).padding(20.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Text("Send", style=MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                OutlinedTextField(amount, {amount=it}, label={Text("Amount")}, modifier=Modifier.weight(1f))
                Text("BTC", modifier=Modifier.padding(top=16.dp))
            }
            OutlinedTextField(addressStr, {addressStr=it}, label={Text("to address")}, modifier=Modifier.fillMaxWidth())
            error?.let{ Text(it, color=MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp), modifier=Modifier.fillMaxWidth()){
                OutlinedButton(onClick={nav.popBackStack()}, modifier=Modifier.weight(1f)){ Text("Cancel") }
                Button(onClick={
                    try {
                        val c = Coin.parseCoin(amount)
                        val parser = app.walletAppKit.addressParser()
                        val addr = parser.parseAddress(addressStr)
                        val balances = app.walletAppKit.wallet().getBalance(BalanceType.AVAILABLE_SPENDABLE)
                        // validation like original sendMoneyDonation logic
                        if(c.isGreaterThan(balances)) { error="Insufficient"; return@Button }
                        val req = Wallet.SendRequest.to(addr, c)
                        val tx = app.walletAppKit.wallet().sendCoinsOffline(req)
                        pendingTx = tx.tx
                        if(app.walletAppKit.wallet().isEncrypted) showPassword=true else {
                            app.walletAppKit.peerGroup().broadcastTransaction(tx.tx).broadcast()
                            nav.popBackStack()
                        }
                    } catch(e:Exception){ error=e.message }
                }, modifier=Modifier.weight(1f)){ Text("Send") }
            }
        }
    }
    if(showPassword){
        PasswordDialog(app, onSuccess={ aesKey ->
            scope.launch(Dispatchers.IO){
                pendingTx?.let{ app.walletAppKit.peerGroup().broadcastTransaction(it).broadcast() }
                withContext(Dispatchers.Main){ nav.popBackStack() }
            }
        }, onCancel={showPassword=false})
    }
}
