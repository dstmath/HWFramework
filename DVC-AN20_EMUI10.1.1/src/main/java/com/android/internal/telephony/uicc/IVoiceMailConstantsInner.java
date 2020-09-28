package com.android.internal.telephony.uicc;

public interface IVoiceMailConstantsInner {
    boolean containsCarrierHw(String str);

    boolean containsCarrierHw(String str, int i);

    boolean containsCarrierInner(String str);

    boolean getVoiceMailFixed(String str);

    boolean getVoiceMailFixed(String str, int i);

    String getVoiceMailNumberHw(String str);

    String getVoiceMailNumberHw(String str, int i);

    String getVoiceMailNumberInner(String str);

    String getVoiceMailTagHw(String str);

    String getVoiceMailTagHw(String str, int i);

    String getVoiceMailTagInner(String str);

    void resetVoiceMailLoadFlag();

    void setVoicemailInClaro(int i);

    void setVoicemailOnSIM(String str, String str2);
}
