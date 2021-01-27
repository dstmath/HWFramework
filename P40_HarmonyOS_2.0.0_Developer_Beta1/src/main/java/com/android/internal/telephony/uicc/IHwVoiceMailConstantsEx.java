package com.android.internal.telephony.uicc;

public interface IHwVoiceMailConstantsEx {
    default void setVoicemailOnSIM(String voicemailNumber, String voicemailTag) {
    }

    default void resetVoiceMailLoadFlag() {
    }

    default boolean getVoiceMailFixed(String carrier, int slotId) {
        return true;
    }

    default boolean getVoiceMailFixed(String carrier) {
        return true;
    }

    default String getVoiceMailTagHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier) {
        return voiceMailConstantsInner.getVoiceMailTagInner(carrier);
    }

    default String getVoiceMailTagHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier, int slotId) {
        return voiceMailConstantsInner.getVoiceMailTagInner(carrier);
    }

    default void setVoicemailInClaro(int voicemailPriorityMode) {
    }

    default String loadVoiceMailConfigFromCard(String configName, String carrier) {
        return null;
    }

    default boolean containsCarrierHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier) {
        return voiceMailConstantsInner.containsCarrierInner(carrier);
    }

    default boolean containsCarrierHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier, int slotId) {
        return voiceMailConstantsInner.containsCarrierInner(carrier);
    }

    default String getVoiceMailNumberHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier, int slotId) {
        return voiceMailConstantsInner.getVoiceMailNumberInner(carrier);
    }

    default String getVoiceMailNumberHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier) {
        return voiceMailConstantsInner.getVoiceMailNumberInner(carrier);
    }
}
