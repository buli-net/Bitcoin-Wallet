package wallettemplate;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.utils.MonetaryFormat;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import java.util.Date;

/**
 * Convert từ MainController JavaFX sang Android
 * Giữ nguyên tên class, giữ nguyên logic balance + sync
 */
public class MainController {
    public TextView balance;
    public Button sendMoneyOutBtn;
    public TextView addressControl;
    public TextView syncStatus;

    private static final MonetaryFormat MONETARY_FORMAT = MonetaryFormat.BTC.noCode();
    private Main app; // Main.java (Activity) của mày
    private final Handler handler = new Handler(Looper.getMainLooper());

    public MainController(Main app, TextView balance, Button sendMoneyOutBtn, TextView addressControl, TextView syncStatus) {
        this.app = app;
        this.balance = balance;
        this.sendMoneyOutBtn = sendMoneyOutBtn;
        this.addressControl = addressControl;
        this.syncStatus = syncStatus;
    }

    public void onBitcoinSetup() {
        // Bind địa chỉ ví - MAINNET thật
        if (app.getWallet() != null) {
            String address = app.getWallet().currentReceiveAddress().toString();
            addressControl.setText(address);
            
            updateBalance(app.getWallet().getBalance());
            
            // Lắng nghe balance thay đổi
            app.getWallet().addCoinsReceivedEventListener((wallet, tx, prevBalance, newBalance) -> {
                handler.post(() -> updateBalance(newBalance));
            });
            app.getWallet().addCoinsSentEventListener((wallet, tx, prevBalance, newBalance) -> {
                handler.post(() -> updateBalance(newBalance));
            });

            showBitcoinSyncMessage();
        }

        sendMoneyOutBtn.setOnClickListener(v -> sendMoneyOut());
    }

    private void updateBalance(Coin coin) {
        balance.setText(formatCoin(coin));
        sendMoneyOutBtn.setEnabled(!coin.isZero());
    }

    private static String formatCoin(Coin coin) {
        return MONETARY_FORMAT.format(coin).toString() + " BTC";
    }

    private void showBitcoinSyncMessage() {
        syncStatus.setText("Synchronising with Bitcoin mainnet...");
    }

    public void readyToGoAnimation() {
        handler.post(() -> {
            syncStatus.setText("Ready");
            addressControl.setAlpha(1.0f);
        });
    }

    public void sendMoneyOut() {
        // Mở màn hình gửi tiền - giữ nguyên logic overlayUI("send_money.fxml") cũ
        Intent intent = new Intent(app, SendMoneyController.class);
        app.startActivity(intent);
    }

    public void settingsClicked() {
        Intent intent = new Intent(app, WalletSettingsController.class);
        app.startActivity(intent);
    }

    public DownloadProgressTracker progressBarUpdater() {
        return new DownloadProgressTracker() {
            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                super.progress(pct, blocksSoFar, date);
                handler.post(() -> {
                    syncStatus.setText(String.format("Syncing %d%%", (int)pct));
                    if (pct >= 100) {
                        readyToGoAnimation();
                    }
                });
            }
            @Override
            protected void doneDownload() {
                super.doneDownload();
                handler.post(() -> readyToGoAnimation());
            }
        };
    }
}
