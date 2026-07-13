package wallettemplate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import java.io.File;

public class Main extends AppCompatActivity {
    // ĐỔI SANG MAINNET THEO YÊU CẦU
    private static final BitcoinNetwork network = BitcoinNetwork.MAINNET;
    private static final ScriptType PREFERRED_OUTPUT_SCRIPT_TYPE = ScriptType.P2WPKH;
    private static final String APP_NAME = "WalletTemplate";

    // ===== CHỖ 1 FIX: INSTANCE =====
    private static Main instance;
    public static Main getInstance() { return instance; }
    // =================================

    private Wallet wallet;
    private SPVBlockStore blockStore;
    private BlockChain chain;
    private PeerGroup peerGroup;
    private File walletFile;
    private File chainFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this; // FIX INSTANCE
        setContentView(R.layout.activity_main); // thêm layout main mày mới tạo

        try {
            walletFile = new File(getFilesDir(), APP_NAME + ".wallet");
            chainFile = new File(getFilesDir(), APP_NAME + ".spvchain");

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

    // ===== CHỖ 2 FIX: SETUP LẠI KHI RESTORE + GETTER =====
    public void setupWalletKitMainnet(DeterministicSeed seed) {
        try {
            if (walletFile.exists()) walletFile.delete();
            if (chainFile.exists()) chainFile.delete();

            blockStore = new SPVBlockStore(network, chainFile);
            wallet = Wallet.fromSeed(network, seed, PREFERRED_OUTPUT_SCRIPT_TYPE);
            chain = new BlockChain(network, wallet, blockStore);
            peerGroup = new PeerGroup(network, chain);
            peerGroup.addWallet(wallet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Wallet getWallet() { return wallet; }
    public PeerGroup getPeerGroup() { return peerGroup; }
    public BlockChain getChain() { return chain; }
    public SPVBlockStore getBlockStore() { return blockStore; }
    // =====================================================

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