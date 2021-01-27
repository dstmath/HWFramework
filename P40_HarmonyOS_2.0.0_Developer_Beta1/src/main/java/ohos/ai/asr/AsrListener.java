package ohos.ai.asr;

import ohos.utils.PacMap;

public interface AsrListener {
    void onAudioEnd();

    void onAudioStart();

    void onBeginningOfSpeech();

    void onBufferReceived(byte[] bArr);

    void onEnd();

    void onEndOfSpeech();

    void onError(int i);

    void onEvent(int i, PacMap pacMap);

    void onInit(PacMap pacMap);

    void onIntermediateResults(PacMap pacMap);

    void onResults(PacMap pacMap);

    void onRmsChanged(float f);
}
