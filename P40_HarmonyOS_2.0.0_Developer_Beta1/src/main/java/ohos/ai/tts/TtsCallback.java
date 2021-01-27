package ohos.ai.tts;

import ohos.ai.engine.utils.HiAILog;
import ohos.ai.tts.TtsPluginListener;
import ohos.utils.PacMap;

public class TtsCallback extends TtsPluginListener.Stub {
    private static final String TAG = TtsCallback.class.getSimpleName();
    private TtsListener ttsListener;

    public TtsCallback() {
        super(TtsPluginListener.DESCRIPTOR);
    }

    @Override // ohos.ai.tts.TtsPluginListener
    public void onStart(String str) {
        HiAILog.info(TAG, "onStart");
        TtsListener ttsListener2 = this.ttsListener;
        if (ttsListener2 != null) {
            ttsListener2.onStart(str);
        }
    }

    @Override // ohos.ai.tts.TtsPluginListener
    public void onProgress(String str, byte[] bArr, int i) {
        HiAILog.info(TAG, "onProgress");
        TtsListener ttsListener2 = this.ttsListener;
        if (ttsListener2 != null) {
            ttsListener2.onProgress(str, bArr, i);
        }
    }

    @Override // ohos.ai.tts.TtsPluginListener
    public void onFinish(String str) {
        HiAILog.info(TAG, "onFinish");
        TtsListener ttsListener2 = this.ttsListener;
        if (ttsListener2 != null) {
            ttsListener2.onFinish(str);
        }
    }

    @Override // ohos.ai.tts.TtsPluginListener
    public void onError(String str, String str2) {
        HiAILog.info(TAG, "onError");
        TtsListener ttsListener2 = this.ttsListener;
        if (ttsListener2 != null) {
            ttsListener2.onError(str, str2);
        }
    }

    @Override // ohos.ai.tts.TtsPluginListener
    public void onEvent(int i, PacMap pacMap) {
        HiAILog.info(TAG, "onEvent");
        TtsListener ttsListener2 = this.ttsListener;
        if (ttsListener2 != null) {
            ttsListener2.onEvent(i, pacMap);
        }
    }

    @Override // ohos.ai.tts.TtsPluginListener
    public void onSpeechStart(String str) {
        HiAILog.info(TAG, "onSpeechStart");
        TtsListener ttsListener2 = this.ttsListener;
        if (ttsListener2 != null) {
            ttsListener2.onSpeechStart(str);
        }
    }

    @Override // ohos.ai.tts.TtsPluginListener
    public void onSpeechProgressChanged(String str, int i) {
        HiAILog.info(TAG, "onSpeechProgressChanged");
        TtsListener ttsListener2 = this.ttsListener;
        if (ttsListener2 != null) {
            ttsListener2.onSpeechProgressChanged(str, i);
        }
    }

    @Override // ohos.ai.tts.TtsPluginListener
    public void onSpeechFinish(String str) {
        HiAILog.info(TAG, "onSpeechFinish");
        TtsListener ttsListener2 = this.ttsListener;
        if (ttsListener2 != null) {
            ttsListener2.onSpeechFinish(str);
        }
    }

    public void setTtsListener(TtsListener ttsListener2) {
        HiAILog.info(TAG, "setTtsListener");
        if (this.ttsListener == null) {
            this.ttsListener = ttsListener2;
        } else {
            HiAILog.warn(TAG, "ttsListener is not null and cannot be reassigned");
        }
    }

    public void releaseTtsListener() {
        HiAILog.info(TAG, "releaseTtsListener");
        this.ttsListener = null;
    }
}
