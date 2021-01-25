package ohos.ai.tts;

import ohos.utils.PacMap;

public interface TtsListener {
    void onError(String str, String str2);

    void onEvent(int i, PacMap pacMap);

    void onFinish(String str);

    void onProgress(String str, byte[] bArr, int i);

    void onSpeechFinish(String str);

    void onSpeechProgressChanged(String str, int i);

    void onSpeechStart(String str);

    void onStart(String str);
}
