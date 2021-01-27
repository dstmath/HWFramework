package ohos.ace.plugin.vibrator;

import com.huawei.ace.plugin.vibrator.VibratorPluginBase;
import java.util.concurrent.CompletableFuture;
import ohos.vibrator.agent.VibratorAgent;

public class VibratorPlugin extends VibratorPluginBase {
    private static final int INVALID_ID = -1;
    private static final String LOG_TAG = "Ace_Vibrator";
    private VibratorAgent agent = new VibratorAgent();

    public VibratorPlugin() {
        nativeInit();
    }

    @Override // com.huawei.ace.plugin.vibrator.VibratorPluginBase
    public void vibrate(int i) {
        CompletableFuture.runAsync(new Runnable(i) {
            /* class ohos.ace.plugin.vibrator.$$Lambda$VibratorPlugin$YomtuOcA0fTEsK2ImUn5k0PeSK4 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                VibratorPlugin.this.lambda$vibrate$0$VibratorPlugin(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$vibrate$0$VibratorPlugin(int i) {
        this.agent.startOnce(i);
    }
}
