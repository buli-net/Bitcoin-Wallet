package wallettemplate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.Wallet;
import java.io.File;

public class Main extends AppCompatActivity {
    // ĐỔI SANG MAINNET THEO YÊU CẦU
    private static final BitcoinNetwork network = BitcoinNetwork.MAINNET;
    private static final ScriptType PREFERRED_OUTPUT_SCRIPT_TYPE = ScriptType.P2WPKH;
    private static final String APP_NAME = "WalletTemplate";

    private Wallet wallet;
    private SPVBlockStore blockStore;
    private BlockChain chain;
    private PeerGroup peerGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            File walletFile = new File(getFilesDir(), APP_NAME + ".wallet");
            File chainFile = new File(getFilesDir(), APP_NAME + ".spvchain");

            blockStore = new SPVBlockStore(network, chainFile);
            if (walletFile.exists()) {
                wallet = Wallet.loadFromFile(walletFile);
            } else {
                wallet = Wallet.createDeterministic(network, PREFERRED_OUTPUT_SCRIPT_TYPE);
            }
            
            chain = new BlockChain(network, wallet, blockStore);
            peerGroup = new PeerGroup(network, chain);
            peerGroup.addWallet(wallet);
            peerGroup.startAsync();
            peerGroup.downloadBlockChain(); // sync MAINNET thật
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (peerGroup != null) peerGroup.stop();
            if (blockStore != null) blockStore.close();
            if (wallet != null) wallet.saveToFile(new File(getFilesDir(), APP_NAME + ".wallet"));
        } catch (Exception e) {}
        super.onDestroy();
    }
}
