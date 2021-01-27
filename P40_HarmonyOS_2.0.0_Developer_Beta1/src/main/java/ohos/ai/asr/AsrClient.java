package ohos.ai.asr;

import java.util.Optional;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.Context;

public class AsrClient {
    private static final String TAG = AsrClient.class.getSimpleName();
    private static volatile AsrClient asrClient;
    private AsrRecognizer asrRecognizer;

    private AsrClient(Context context) {
        this.asrRecognizer = AsrRecognizer.createAsrRecognizer(context).orElse(null);
    }

    private static AsrClient getInstance(Context context) {
        if (asrClient == null) {
            synchronized (AsrClient.class) {
                if (asrClient == null) {
                    asrClient = new AsrClient(context);
                }
            }
        }
        return asrClient;
    }

    public static Optional<AsrClient> createAsrClient(Context context) {
        HiAILog.info(TAG, "Create asr client");
        if (context != null) {
            return Optional.of(getInstance(context));
        }
        HiAILog.error(TAG, "context is null");
        return Optional.empty();
    }

    public void init(AsrIntent asrIntent, AsrListener asrListener) {
        HiAILog.info(TAG, "init");
        AsrRecognizer asrRecognizer2 = this.asrRecognizer;
        if (asrRecognizer2 == null) {
            HiAILog.error(TAG, "[init] asrRecognizer is null");
        } else {
            asrRecognizer2.init(asrIntent, asrListener);
        }
    }

    public void startListening(AsrIntent asrIntent) {
        AsrRecognizer asrRecognizer2 = this.asrRecognizer;
        if (asrRecognizer2 == null) {
            HiAILog.error(TAG, "[startListening] asrRecognizer is null");
        } else {
            asrRecognizer2.startListening(asrIntent);
        }
    }

    public void stopListening() {
        AsrRecognizer asrRecognizer2 = this.asrRecognizer;
        if (asrRecognizer2 == null) {
            HiAILog.error(TAG, "[stopListening] asrRecognizer is null");
        } else {
            asrRecognizer2.stopListening();
        }
    }

    public void writePcm(byte[] bArr, int i) {
        AsrRecognizer asrRecognizer2 = this.asrRecognizer;
        if (asrRecognizer2 == null) {
            HiAILog.error(TAG, "[writePcm] asrRecognizer is null");
        } else {
            asrRecognizer2.writePcm(bArr, i);
        }
    }

    public void cancel() {
        AsrRecognizer asrRecognizer2 = this.asrRecognizer;
        if (asrRecognizer2 == null) {
            HiAILog.error(TAG, "[cancel] asrRecognizer is null");
        } else {
            asrRecognizer2.cancel();
        }
    }

    public void destroy() {
        AsrRecognizer asrRecognizer2 = this.asrRecognizer;
        if (asrRecognizer2 == null) {
            HiAILog.error(TAG, "[destroy] asrRecognizer is null");
            return;
        }
        asrRecognizer2.destroy();
        synchronized (AsrClient.class) {
            asrClient = null;
        }
    }
}
