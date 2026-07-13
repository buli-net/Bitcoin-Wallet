package com.example.wallettemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wallettemplate.ui.theme.WalletTemplateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletTemplateTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Bitcoin Wallet") })
                    }
                ) { padding ->
                    var address by remember { mutableStateOf("Chưa có ví") }
                    var balance by remember { mutableStateOf("0.0 BTC") }
                    
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Địa chỉ ví:", style = MaterialTheme.typography.titleMedium)
                        Text(address)
                        Text("Số dư: $balance", style = MaterialTheme.typography.titleLarge)
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Button(onClick = { 
                            // test tạo ví
                            address = "bc1q...test123"
                            balance = "0.001 BTC"
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Tạo ví mới / Làm mới")
                        }
                        
                        Button(onClick = { 
                            // quét QR
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Quét QR")
                        }
                    }
                }
            }
        }
    }
}