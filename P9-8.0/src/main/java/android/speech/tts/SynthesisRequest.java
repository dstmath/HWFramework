package android.speech.tts;

import android.os.Bundle;

public final class SynthesisRequest {
    private int mCallerUid;
    private String mCountry;
    private String mLanguage;
    private final Bundle mParams;
    private int mPitch;
    private int mSpeechRate;
    private final CharSequence mText;
    private String mVariant;
    private String mVoiceName;

    public SynthesisRequest(String text, Bundle params) {
        this.mText = text;
        this.mParams = new Bundle(params);
    }

    public SynthesisRequest(CharSequence text, Bundle params) {
        this.mText = text;
        this.mParams = new Bundle(params);
    }

    @Deprecated
    public String getText() {
        return this.mText.toString();
    }

    public CharSequence getCharSequenceText() {
        return this.mText;
    }

    public String getVoiceName() {
        return this.mVoiceName;
    }

    public String getLanguage() {
        return this.mLanguage;
    }

    public String getCountry() {
        return this.mCountry;
    }

    public String getVariant() {
        return this.mVariant;
    }

    public int getSpeechRate() {
        return this.mSpeechRate;
    }

    public int getPitch() {
        return this.mPitch;
    }

    public Bundle getParams() {
        return this.mParams;
    }

    public int getCallerUid() {
        return this.mCallerUid;
    }

    void setLanguage(String language, String country, String variant) {
        this.mLanguage = language;
        this.mCountry = country;
        this.mVariant = variant;
    }

    void setVoiceName(String voiceName) {
        this.mVoiceName = voiceName;
    }

    void setSpeechRate(int speechRate) {
        this.mSpeechRate = speechRate;
    }

    void setPitch(int pitch) {
        this.mPitch = pitch;
    }

    void setCallerUid(int uid) {
        this.mCallerUid = uid;
    }
}
