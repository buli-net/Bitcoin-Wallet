package wallettemplate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.bitcoinj.crypto.AesKey;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.walletfx.utils.KeyDerivationTasks;

public class WalletPasswordController extends AppCompatActivity {

    public EditText pass1;
    public ProgressBar progressMeter;
    public TextView explanationLabel;
    public Button confirmBtn;
    public Button cancelBtn;

    private Main app;
    private AesKey aesKeyResult;

    public static final String TAG = "WalletPasswordController.target-time";
    public static long targetTimeMillis = 200; // default 200ms scrypt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_password);

        pass1 = findViewById(R.id.pass1);
        progressMeter = findViewById(R.id.progressMeter);
        explanationLabel = findViewById(R.id.explanationLabel);
        confirmBtn = findViewById(R.id.confirmBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        app = Main.getInstance();
        progressMeter.setAlpha(0);

        confirmBtn.setOnClickListener(v -> confirmClicked());
        cancelBtn.setOnClickListener(v -> cancelClicked());
    }

    void confirmClicked() {
        String password = pass1.getText().toString();
        if (password.isEmpty() || password.length() < 4) {
            Toast.makeText(this, "Bad password - empty or too short", Toast.LENGTH_SHORT).show();
            return;
        }

        final KeyCrypterScrypt keyCrypter = (KeyCrypterScrypt) app.getWallet().getKeyCrypter();
        if (keyCrypter == null) {
            Toast.makeText(this, "Wallet not encrypted", Toast.LENGTH_SHORT).show();
            return;
        }

        KeyDerivationTasks tasks = new KeyDerivationTasks(keyCrypter, password, java.time.Duration.ofMillis(targetTimeMillis)) {
            @Override
            protected final void onFinish(AesKey aesKey, int timeTakenMsec) {
                runOnUiThread(() -> {
                    if (app.getWallet().checkAESKey(aesKey)) {
                        aesKeyResult = aesKey;
                        // Trả về cho SendMoneyController
                        SendMoneyController.setAesKey(aesKey);
                        finish();
                    } else {
                        progressMeter.setAlpha(0);
                        Toast.makeText(WalletPasswordController.this, "Wrong password", Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
        
        progressMeter.setProgress(0);
        progressMeter.setAlpha(1);
        progressMeter.setVisibility(ProgressBar.VISIBLE);
        // bind progress
        tasks.progressProperty().addListener((obs, old, cur) -> {
            runOnUiThread(() -> progressMeter.setProgress((int)(cur.doubleValue()*100)));
        });
        tasks.start();
    }

    void cancelClicked() {
        finish();
    }

    public AesKey getAesKey() {
        return aesKeyResult;
    }

    // Giữ nguyên logic tag time như gốc
    public static void setTargetTime(java.time.Duration targetTime) {
        targetTimeMillis = targetTime.toMillis();
        // Lưu vào wallet tag nếu cần
        // app.getWallet().setTag(TAG, ByteString.copyFrom(longToByteArray(...)))
    }

    public static java.time.Duration getTargetTime() {
        return java.time.Duration.ofMillis(targetTimeMillis);
    }
}
