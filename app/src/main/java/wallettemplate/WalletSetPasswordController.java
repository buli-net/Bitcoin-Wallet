package wallettemplate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.protobuf.ByteString;
import org.bitcoinj.crypto.AesKey;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.protobuf.wallet.Protos;
import org.bitcoinj.walletfx.utils.KeyDerivationTasks;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class WalletSetPasswordController extends AppCompatActivity {
    public EditText pass1, pass2;
    public ProgressBar progressMeter;
    public Button closeButton;
    public TextView explanationLabel;
    public Button setPasswordBtn;

    private Main app;

    public static final Protos.ScryptParameters SCRYPT_PARAMETERS = Protos.ScryptParameters.newBuilder()
            .setP(6)
            .setR(8)
            .setN(32768)
            .setSalt(ByteString.copyFrom(KeyCrypterScrypt.randomSalt()))
            .build();

    private static Duration estimatedKeyDerivationTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_set_password);

        pass1 = findViewById(R.id.pass1);
        pass2 = findViewById(R.id.pass2);
        progressMeter = findViewById(R.id.progressMeter);
        closeButton = findViewById(R.id.closeButton);
        explanationLabel = findViewById(R.id.explanationLabel);
        setPasswordBtn = findViewById(R.id.setPasswordBtn);

        app = Main.getInstance();
        progressMeter.setAlpha(0);

        initEstimatedKeyDerivationTime();

        setPasswordBtn.setOnClickListener(v -> setPasswordClicked());
        closeButton.setOnClickListener(v -> closeClicked());
    }

    public static void initEstimatedKeyDerivationTime() {
        if (estimatedKeyDerivationTime == null) {
            CompletableFuture
                .supplyAsync(WalletSetPasswordController::estimateKeyDerivationTime)
                .thenAccept(duration -> estimatedKeyDerivationTime = duration);
        }
    }
    
    private static Duration estimateKeyDerivationTime() {
        KeyCrypterScrypt scrypt = new KeyCrypterScrypt(SCRYPT_PARAMETERS);
        long start = System.currentTimeMillis();
        scrypt.deriveKey("test password");
        return Duration.ofMillis(System.currentTimeMillis() - start);
    }

    public void setPasswordClicked() {
        if (!pass1.getText().toString().equals(pass2.getText().toString())) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = pass1.getText().toString();
        if (password.length() < 4) {
            Toast.makeText(this, "Password too short, at least 5 chars", Toast.LENGTH_SHORT).show();
            return;
        }

        progressMeter.setAlpha(1);
        progressMeter.setVisibility(ProgressBar.VISIBLE);

        KeyCrypterScrypt scrypt = new KeyCrypterScrypt(SCRYPT_PARAMETERS);

        KeyDerivationTasks tasks = new KeyDerivationTasks(scrypt, password, estimatedKeyDerivationTime != null ? estimatedKeyDerivationTime : Duration.ofMillis(500)) {
            @Override
            protected final void onFinish(AesKey aesKey, int timeTakenMsec) {
                runOnUiThread(() -> {
                    WalletPasswordController.setTargetTime(Duration.ofMillis(timeTakenMsec));
                    // MÃ HOÁ THẬT - MAINNET
                    app.getWallet().encrypt(scrypt, aesKey);
                    Toast.makeText(WalletSetPasswordController.this, "Wallet encrypted", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        };
        
        tasks.progressProperty().addListener((obs, old, cur) -> {
            runOnUiThread(() -> progressMeter.setProgress((int)(cur.doubleValue()*100)));
        });
        tasks.start();
    }

    public void closeClicked() {
        finish();
    }
}
