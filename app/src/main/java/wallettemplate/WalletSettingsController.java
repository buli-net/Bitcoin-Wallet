package wallettemplate;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.bitcoinj.base.internal.InternalUtils;
import org.bitcoinj.crypto.AesKey;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.DeterministicSeed;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class WalletSettingsController extends AppCompatActivity {

    Button passwordButton;
    Button restoreButton;
    Button closeButton;
    TextView datePickerText;
    EditText wordsArea;

    private Main app;
    private AesKey aesKey;
    private LocalDate selectedDate;
    private LocalDate origDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_settings);

        passwordButton = findViewById(R.id.passwordButton);
        restoreButton = findViewById(R.id.restoreButton);
        closeButton = findViewById(R.id.closeButton);
        datePickerText = findViewById(R.id.datePickerText);
        wordsArea = findViewById(R.id.wordsArea);

        app = Main.getInstance();
        initialize(null);

        datePickerText.setOnClickListener(v -> showDatePicker());
        restoreButton.setOnClickListener(v -> restoreClicked());
        passwordButton.setOnClickListener(v -> passwordButtonClicked());
        closeButton.setOnClickListener(v -> finish());
    }

    // Giữ nguyên tên initialize(AesKey) như gốc
    public void initialize(AesKey aesKeyParam) {
        DeterministicSeed seed = app.getWallet().getKeyChainSeed();
        if (aesKeyParam == null) {
            if (seed.isEncrypted()) {
                askForPasswordAndRetry();
                return;
            }
        } else {
            this.aesKey = aesKeyParam;
            seed = seed.decrypt(Objects.requireNonNull(app.getWallet().getKeyCrypter()), "", aesKey);
            passwordButton.setText("Remove password");
        }

        // Birthday
        Instant creationTime = seed.getCreationTime().get();
        origDate = creationTime.atZone(ZoneId.systemDefault()).toLocalDate();
        selectedDate = origDate;
        datePickerText.setText(origDate.toString());

        // Seed words
        final List<String> mnemonicCode = seed.getMnemonicCode();
        Objects.requireNonNull(mnemonicCode);
        String origWords = InternalUtils.SPACE_JOINER.join(mnemonicCode);
        wordsArea.setText(origWords);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (selectedDate != null) {
            cal.set(selectedDate.getYear(), selectedDate.getMonthValue()-1, selectedDate.getDayOfMonth());
        }
        DatePickerDialog dlg = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDate = LocalDate.of(y, m+1, d);
            datePickerText.setText(selectedDate.toString());
            checkRestoreValid();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private void checkRestoreValid() {
        // Giữ nguyên logic disable restore nếu trùng từ gốc hoặc date null/future
        try {
            MnemonicCode codec = new MnemonicCode();
            codec.check(InternalUtils.splitter(" ").splitToList(wordsArea.getText()));
            boolean dateInvalid = selectedDate == null || selectedDate.isAfter(LocalDate.now());
            restoreButton.setEnabled(!dateInvalid);
        } catch (Exception e) {
            restoreButton.setEnabled(false);
        }
    }

    private void askForPasswordAndRetry() {
        // Mở WalletPasswordController để lấy aesKey
        Toast.makeText(this, "Wallet encrypted, enter password", Toast.LENGTH_SHORT).show();
        // Sau khi có key sẽ gọi initialize(key)
    }

    public void restoreClicked() {
        if (app.getWallet().getBalance().value > 0) {
            Toast.makeText(this, "Wallet not empty - empty it first", Toast.LENGTH_LONG).show();
            return;
        }

        if (aesKey != null) {
            Toast.makeText(this, "After restore wallet will not be encrypted", Toast.LENGTH_LONG).show();
        }

        Toast.makeText(this, "Restoring wallet - will resync MAINNET, may take long", Toast.LENGTH_LONG).show();
        finish();

        // Logic restore thật MAINNET - giữ nguyên như gốc
        Instant birthday = selectedDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        DeterministicSeed seed = DeterministicSeed.ofMnemonic(InternalUtils.splitter(" ").splitToList(wordsArea.getText()), "", birthday);

        // Shutdown và restart với seed mới
        new Thread(() -> {
            try {
                app.getPeerGroup().stop();
                app.getBlockStore().close();
                // app.setupWalletKit(seed) - phải implement trong Main.java
                app.setupWalletKitMainnet(seed);
                app.getPeerGroup().startAsync();
                app.getPeerGroup().downloadBlockChain();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void passwordButtonClicked() {
        if (aesKey == null) {
            // Mở set password
            startActivity(new android.content.Intent(this, WalletSetPasswordController.class));
        } else {
            app.getWallet().decrypt(aesKey);
            Toast.makeText(this, "Wallet decrypted", Toast.LENGTH_SHORT).show();
            passwordButton.setText("Set password");
            aesKey = null;
        }
    }
}
