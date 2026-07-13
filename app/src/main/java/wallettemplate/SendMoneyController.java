package wallettemplate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.crypto.AesKey;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

public class SendMoneyController extends AppCompatActivity {
    public Button sendBtn;
    public Button cancelBtn;
    public EditText address;
    public TextView titleLabel;
    public EditText amountEdit;

    private Main app; // lấy ví từ Main.java
    private Wallet.SendResult sendResult;
    private AesKey aesKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_money); // layout mày tự tạo với id trùng tên
        
        sendBtn = findViewById(R.id.sendBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        address = findViewById(R.id.address);
        titleLabel = findViewById(R.id.titleLabel);
        amountEdit = findViewById(R.id.amountEdit);

        app = Main.getInstance(); // tạo singleton trong Main.java để lấy wallet
        initialize();
        
        sendBtn.setOnClickListener(v -> send());
        cancelBtn.setOnClickListener(v -> cancel());
    }

    public void initialize() {
        Coin balance = app.getWallet().getBalance();
        if (balance.isZero()) {
            Toast.makeText(this, "Wallet empty", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Gợi ý: hiện balance
        amountEdit.setText(balance.toPlainString());
        amountEdit.setHint("Max: " + balance.toPlainString() + " BTC");
    }

    public void cancel() {
        finish();
    }

    public void send() {
        try {
            Coin amount = Coin.parseCoin(amountEdit.getText().toString().trim());
            Address destination = app.getWallet().parseAddress(address.getText().toString().trim());
            
            SendRequest req;
            // Logic gốc của mày giữ nguyên
            if (amount.equals(app.getWallet().getBalance())) {
                req = SendRequest.emptyWallet(destination);
            } else {
                req = SendRequest.to(destination, amount);
            }
            req.aesKey = aesKey;
            req.allowUnconfirmed();

            // GỬI THẬT - MAINNET
            sendResult = app.getWallet().sendCoins(app.getPeerGroup(), req);
            
            sendBtn.setEnabled(false);
            address.setEnabled(false);
            amountEdit.setEnabled(false);
            titleLabel.setText("Broadcasting...");

            // Lắng nghe broadcast thật
            sendResult.transaction().getConfidence().addEventListener((tx, reason) -> {
                if (reason == TransactionConfidence.Listener.ChangeReason.SEEN_PEERS) {
                    runOnUiThread(() -> updateTitleForBroadcast());
                }
            });

            sendResult.awaitRelayed().whenComplete((result, t) -> {
                runOnUiThread(() -> {
                    if (t == null) {
                        Toast.makeText(this, "Sent! Tx: " + result.transaction().getTxId(), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        sendBtn.setEnabled(true);
                    }
                });
            });

        } catch (InsufficientMoneyException e) {
            Toast.makeText(this, "Not enough funds: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("encrypted")) {
                askForPasswordAndRetry();
            } else {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void askForPasswordAndRetry() {
        // Mở màn hình nhập password - giữ nguyên logic gốc
        // Sau khi có aesKey thì gọi lại send()
        Toast.makeText(this, "Wallet encrypted, need password", Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, WalletPasswordController.class);
        // startActivityForResult(intent, 100);
    }

    private void updateTitleForBroadcast() {
        final int peers = sendResult.transaction().getConfidence().numBroadcastPeers();
        titleLabel.setText(String.format("Broadcasting ... seen by %d peers", peers));
    }
}
