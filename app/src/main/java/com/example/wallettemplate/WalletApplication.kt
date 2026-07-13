
package com.example.wallettemplate

import android.app.Application
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import org.bitcoinj.kits.WalletAppKit
import java.io.File

class WalletApplication : Application() {
    lateinit var walletAppKit: WalletAppKit
    val network = BitcoinNetwork.REGTEST
    val scriptType = ScriptType.P2WPKH
    override fun onCreate() {
        super.onCreate()
        val dir = File(filesDir, "wallet")
        dir.mkdirs()
        walletAppKit = object : WalletAppKit(network, scriptType, dir, "wallettemplate") {
            override fun onSetupCompleted() {
                wallet().allowSpendingUnconfirmedTransactions()
            }
        }
        walletAppKit.setAutoSave(true)
        walletAppKit.startAsync()
        walletAppKit.awaitRunning()
    }
}
