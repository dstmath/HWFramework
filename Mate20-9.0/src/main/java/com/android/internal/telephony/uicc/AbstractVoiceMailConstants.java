package com.android.internal.telephony.uicc;

public abstract class AbstractVoiceMailConstants {
    public boolean getVoiceMailFixed(String carrier) {
        return false;
    }

    public void setVoicemailOnSIM(String voicemailNumber, String voicemailTag) {
    }

    public void resetVoiceMailLoadFlag() {
    }

    public boolean getVoiceMailFixed(String carrier, int slotId) {
        return false;
    }

    public boolean containsCarrier(String carrier, int slotId) {
        return false;
    }

    public String getVoiceMailNumber(String carrier, int slotId) {
        return null;
    }

    public String getVoiceMailTag(String carrier, int slotId) {
        return null;
    }

    public void clearVoicemailLoadedFlag() {
    }

    public void setVoicemailInClaro(int voicemailPriorityMode) {
    }

    public String loadVoiceMailConfigFromCard(String configName, String carrier) {
        return null;
    }
}
