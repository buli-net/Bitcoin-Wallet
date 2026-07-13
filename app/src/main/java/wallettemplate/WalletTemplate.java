package wallettemplate;

import android.content.Context;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import java.io.File;

/**
 * Template implementation - Android MAINNET version
 * Giữ nguyên tên WalletTemplate như gốc, bỏ JavaFX
 */
public class WalletTemplate {

    private final String applicationName;
    private final BitcoinNetwork network;
    private final ScriptType preferredOutputScriptType;

    private Wallet wallet;
    private SPVBlockStore blockStore;
    private BlockChain chain;
    private PeerGroup peerGroup;
    private File walletFile;
    private File chainFile;
    private Context context;

    public WalletTemplate(Context context, String applicationName, BitcoinNetwork network, ScriptType preferredOutputScriptType) {
        this.context = context;
        this.applicationName = applicationName;
        // ÉP MAINNET THEO YÊU CẦU MÀY
        this.network = BitcoinNetwork.MAINNET;
        this.preferredOutputScriptType = preferredOutputScriptType;
    }

    // Constructor gốc vẫn giữ để không vỡ code cũ
    public WalletTemplate(String applicationName, BitcoinNetwork network, ScriptType preferredOutputScriptType) {
        this.applicationName = applicationName;
        this.network = BitcoinNetwork.MAINNET; // ép MAINNET luôn
        this.preferredOutputScriptType = preferredOutputScriptType;
    }

    public void setup(Context ctx) throws Exception {
        this.context = ctx;
        walletFile = new File(ctx.getFilesDir(), applicationName + ".wallet");
        chainFile = new File(ctx.getFilesDir(), applicationName + ".spvchain");

        blockStore = new SPVBlockStore(network, chainFile);
        
        if (walletFile.exists()) {
            wallet = Wallet.loadFromFile(walletFile);
        } else {
            wallet = Wallet.createDeterministic(network, preferredOutputScriptType);
        }

        chain = new BlockChain(network, wallet, blockStore);
        peerGroup = new PeerGroup(network, chain);
        peerGroup.addWallet(wallet);
    }

    public void setupWithSeed(Context ctx, DeterministicSeed seed) throws Exception {
        this.context = ctx;
        walletFile = new File(ctx.getFilesDir(), applicationName + ".wallet");
        chainFile = new File(ctx.getFilesDir(), applicationName + ".spvchain");
        
        if (chainFile.exists()) chainFile.delete();
        if (walletFile.exists()) walletFile.delete();

        blockStore = new SPVBlockStore(network, chainFile);
        wallet = Wallet.fromSeed(network, seed, preferredOutputScriptType);

        chain = new BlockChain(network, wallet, blockStore);
        peerGroup = new PeerGroup(network, chain);
        peerGroup.addWallet(wallet);
    }

    public void startAsync() {
        if (peerGroup != null) {
            peerGroup.startAsync();
            peerGroup.downloadBlockChain();
        }
    }

    public void stopAsync() throws Exception {
        if (peerGroup != null) peerGroup.stop();
        if (blockStore != null) blockStore.close();
        if (wallet != null && walletFile != null) wallet.saveToFile(walletFile);
    }

    // Thay cho loadController() FXML cũ - giờ trả về MainController Android
    protected MainController loadController(Main mainActivity) {
        // MainController Android cần TextView, Button từ activity
        return new MainController(
            mainActivity,
            mainActivity.findViewById(R.id.balance),
            mainActivity.findViewById(R.id.sendMoneyOutBtn),
            mainActivity.findViewById(R.id.addressControl),
            mainActivity.findViewById(R.id.syncStatus)
        );
    }

    public Wallet wallet() { return wallet; }
    public PeerGroup peerGroup() { return peerGroup; }
    public BlockChain chain() { return chain; }
    public SPVBlockStore blockStore() { return blockStore; }
    public BitcoinNetwork network() { return network; }
    public ScriptType preferredOutputScriptType() { return preferredOutputScriptType; }
    public String applicationName() { return applicationName; }
}
