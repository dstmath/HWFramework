package android.speech.tts;

abstract class AbstractSynthesisCallback implements SynthesisCallback {
    protected final boolean mClientIsUsingV2;

    /* access modifiers changed from: package-private */
    public abstract void stop();

    AbstractSynthesisCallback(boolean clientIsUsingV2) {
        this.mClientIsUsingV2 = clientIsUsingV2;
    }

    /* access modifiers changed from: package-private */
    public int errorCodeOnStop() {
        return this.mClientIsUsingV2 ? -2 : -1;
    }
}
