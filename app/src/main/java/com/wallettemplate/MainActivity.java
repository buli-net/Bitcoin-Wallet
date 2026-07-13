package com.wallettemplate;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.Wallet;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private TextView balanceView, addressView, syncStatus;
    private ImageView qrCodeView;
    private ProgressBar syncProgress;
    private Button sendMoneyBtn, settingsBtn;

    private WalletAppKit kit;
    private NetworkParameters params = TestNet3Params.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        balanceView = findViewById(R.id.balance);
        addressView = findViewById(R.id.address);
        qrCodeView = findViewById(R.id.qrCode);
        syncStatus = findViewById(R.id.syncStatus);
        syncProgress = findViewById(R.id.syncProgress);
        sendMoneyBtn = findViewById(R.id.sendMoneyBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        sendMoneyBtn.setEnabled(false);

        File walletDir = getFilesDir();
        kit = new WalletAppKit(params, walletDir, "wallettemplate") {
            @Override
            protected void onSetupCompleted() {
                runOnUiThread(() -> {
                    wallet().addCoinsReceivedEventListener((w, tx, prev, newBal) -> updateBalance());
                    wallet().addCoinsSentEventListener((w, tx, prev, newBal) -> updateBalance());
                    updateBalance();
                    updateAddress();
                    readyToGoAnimation();
                });
            }
        };
        kit.setDownloadListener(new org.bitcoinj.core.listeners.DownloadProgressTracker() {
            @Override
            protected void progress(double pct, int blocksSoFar, java.util.Date date) {
                super.progress(pct, blocksSoFar, date);
                runOnUiThread(() -> {
                    syncProgress.setProgress((int) pct);
                    if (pct < 100) syncStatus.setText("Synchronising: " + (int)pct + "%");
                    else {
                        syncStatus.setText("Synchronised");
                        syncProgress.setVisibility(ProgressBar.GONE);
                    }
                });
            }
            @Override
            protected void doneDownload() {
                super.doneDownload();
                runOnUiThread(() -> {
                    syncStatus.setText("Ready");
                    syncProgress.setVisibility(ProgressBar.GONE);
                });
            }
        });

        kit.setBlockingStartup(false);
        kit.startAsync();

        findViewById(R.id.addressCard).setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("btc", addressView.getText()));
            Toast.makeText(this, "Copied address", Toast.LENGTH_SHORT).show();
        });

        sendMoneyBtn.setOnClickListener(v -> startActivity(new Intent(this, SendMoneyActivity.class)));
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, WalletSettingsActivity.class)));
    }

    private void updateBalance() {
        Coin bal = kit.wallet().getBalance();
        runOnUiThread(() -> {
            balanceView.setText(bal.toFriendlyString());
            sendMoneyBtn.setEnabled(!bal.isZero());
        });
    }

    private void updateAddress() {
        Address addr = kit.wallet().currentReceiveAddress();
        runOnUiThread(() -> {
            addressView.setText(addr.toString());
            try {
                BarcodeEncoder enc = new BarcodeEncoder();
                Bitmap bmp = enc.encodeBitmap(addr.toString(), BarcodeFormat.QR_CODE, 600, 600);
                qrCodeView.setImageBitmap(bmp);
            } catch (WriterException e) { e.printStackTrace(); }
        });
    }

    private void readyToGoAnimation() {
        // Tương đương readyToGoAnimation() trong JavaFX: fade + slide
        findViewById(R.id.controlsBox).animate().translationY(0).setDuration(800).start();
        qrCodeView.animate().alpha(1f).setDuration(800).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kit != null) kit.stopAsync();
    }
}
