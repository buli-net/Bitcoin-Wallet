package com.wallettemplate;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
public class SendMoneyActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_send_money);
        EditText addr = findViewById(R.id.sendAddress);
        EditText amt = findViewById(R.id.sendAmount);
        findViewById(R.id.doSendBtn).setOnClickListener(v->{
            Toast.makeText(this, "Gửi " + amt.getText() + " tới " + addr.getText() + " - cần nối với WalletAppKit trong MainActivity", Toast.LENGTH_LONG).show();
        });
    }
}
