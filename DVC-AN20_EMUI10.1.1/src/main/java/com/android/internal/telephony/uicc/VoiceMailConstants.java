package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.util.XmlUtils;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class VoiceMailConstants implements IVoiceMailConstantsInner {
    static final String LOG_TAG = "VoiceMailConstants";
    static final int NAME = 0;
    static final int NUMBER = 1;
    static final String PARTNER_VOICEMAIL_PATH = "etc/voicemail-conf.xml";
    static final int SIZE = 3;
    static final int TAG = 2;
    private HashMap<String, String[]> CarrierVmMap = new HashMap<>();
    IHwVoiceMailConstantsEx mHwVoiceMailConstantsEx;
    private boolean voiceMailLoaded = false;

    @UnsupportedAppUsage
    VoiceMailConstants(Context context, int slotId) {
        this.mHwVoiceMailConstantsEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).creatHwVoiceMailConstantsEx(this, context, slotId);
    }

    /* access modifiers changed from: package-private */
    public boolean containsCarrier(String carrier) {
        if (this.mHwVoiceMailConstantsEx.loadVoiceMailConfigFromCard("voicemail_carrier", carrier) != null) {
            return true;
        }
        loadVoiceMail();
        return this.CarrierVmMap.containsKey(carrier);
    }

    /* access modifiers changed from: package-private */
    public String getCarrierName(String carrier) {
        String carrierNameFromCard = this.mHwVoiceMailConstantsEx.loadVoiceMailConfigFromCard("voicemail_carrier", carrier);
        if (carrierNameFromCard != null) {
            return carrierNameFromCard;
        }
        loadVoiceMail();
        String[] data = this.CarrierVmMap.get(carrier);
        if (data != null) {
            return data[0];
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getVoiceMailNumber(String carrier) {
        String voiceMailNumberFromCard = this.mHwVoiceMailConstantsEx.loadVoiceMailConfigFromCard("voicemail_number", carrier);
        if (voiceMailNumberFromCard != null) {
            return voiceMailNumberFromCard;
        }
        loadVoiceMail();
        String[] data = this.CarrierVmMap.get(carrier);
        if (data != null) {
            return data[1];
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getVoiceMailTag(String carrier) {
        String voiceMailTagFromCard = this.mHwVoiceMailConstantsEx.loadVoiceMailConfigFromCard("voicemail_tag", carrier);
        if (voiceMailTagFromCard != null) {
            return voiceMailTagFromCard;
        }
        loadVoiceMail();
        String[] data = this.CarrierVmMap.get(carrier);
        if (data != null) {
            return data[2];
        }
        return null;
    }

    private void loadVoiceMail() {
        if (!this.voiceMailLoaded) {
            Rlog.w(LOG_TAG, "loadVoiceMail begin!");
            FileReader vmReader = HwTelephonyFactory.getHwUiccManager().getVoiceMailFileReader();
            if (vmReader == null) {
                Rlog.w(LOG_TAG, "loadVoiceMail failed!");
                return;
            }
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(vmReader);
                XmlUtils.beginDocument(parser, "voicemail");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"voicemail".equals(parser.getName())) {
                        try {
                            break;
                        } catch (IOException e) {
                        }
                    } else {
                        this.CarrierVmMap.put(parser.getAttributeValue(null, "numeric"), new String[]{parser.getAttributeValue(null, "carrier"), parser.getAttributeValue(null, "vmnumber"), parser.getAttributeValue(null, "vmtag")});
                    }
                }
                vmReader.close();
            } catch (XmlPullParserException e2) {
                Rlog.w(LOG_TAG, "Exception in Voicemail parser " + e2);
                vmReader.close();
            } catch (IOException e3) {
                Rlog.w(LOG_TAG, "Exception in Voicemail parser " + e3);
                vmReader.close();
            } catch (Throwable th) {
                try {
                    vmReader.close();
                } catch (IOException e4) {
                }
                throw th;
            }
            this.voiceMailLoaded = true;
        }
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public boolean containsCarrierInner(String carrier) {
        return containsCarrier(carrier);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public boolean containsCarrierHw(String carrier) {
        return this.mHwVoiceMailConstantsEx.containsCarrierHw(this, carrier);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public boolean containsCarrierHw(String carrier, int slotId) {
        return this.mHwVoiceMailConstantsEx.containsCarrierHw(this, carrier, slotId);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public String getVoiceMailNumberInner(String carrier) {
        return getVoiceMailNumber(carrier);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public String getVoiceMailNumberHw(String carrier) {
        return this.mHwVoiceMailConstantsEx.getVoiceMailNumberHw(this, carrier);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public String getVoiceMailNumberHw(String carrier, int slotId) {
        return this.mHwVoiceMailConstantsEx.getVoiceMailNumberHw(this, carrier, slotId);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public String getVoiceMailTagInner(String carrier) {
        return getVoiceMailTag(carrier);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public String getVoiceMailTagHw(String carrier) {
        return this.mHwVoiceMailConstantsEx.getVoiceMailTagHw(this, carrier);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public String getVoiceMailTagHw(String carrier, int slotId) {
        return this.mHwVoiceMailConstantsEx.getVoiceMailTagHw(this, carrier, slotId);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public boolean getVoiceMailFixed(String carrier) {
        return this.mHwVoiceMailConstantsEx.getVoiceMailFixed(carrier);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public boolean getVoiceMailFixed(String carrier, int slotId) {
        return this.mHwVoiceMailConstantsEx.getVoiceMailFixed(carrier, slotId);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public void setVoicemailOnSIM(String voicemailNumber, String voicemailTag) {
        this.mHwVoiceMailConstantsEx.setVoicemailOnSIM(voicemailNumber, voicemailTag);
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public void resetVoiceMailLoadFlag() {
        this.mHwVoiceMailConstantsEx.resetVoiceMailLoadFlag();
    }

    @Override // com.android.internal.telephony.uicc.IVoiceMailConstantsInner
    public void setVoicemailInClaro(int voicemailPriorityMode) {
        this.mHwVoiceMailConstantsEx.setVoicemailInClaro(voicemailPriorityMode);
    }
}
