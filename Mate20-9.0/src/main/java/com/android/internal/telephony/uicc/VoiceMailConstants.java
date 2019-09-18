package com.android.internal.telephony.uicc;

import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.util.XmlUtils;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class VoiceMailConstants extends AbstractVoiceMailConstants {
    static final String LOG_TAG = "VoiceMailConstants";
    static final int NAME = 0;
    static final int NUMBER = 1;
    static final String PARTNER_VOICEMAIL_PATH = "etc/voicemail-conf.xml";
    static final int SIZE = 3;
    static final int TAG = 2;
    private HashMap<String, String[]> CarrierVmMap = new HashMap<>();
    private boolean voiceMailLoaded = false;

    VoiceMailConstants() {
    }

    /* access modifiers changed from: package-private */
    public boolean containsCarrier(String carrier) {
        if (loadVoiceMailConfigFromCard("voicemail_carrier", carrier) != null) {
            return true;
        }
        loadVoiceMail();
        return this.CarrierVmMap.containsKey(carrier);
    }

    /* access modifiers changed from: package-private */
    public String getCarrierName(String carrier) {
        String carrierNameFromCard = loadVoiceMailConfigFromCard("voicemail_carrier", carrier);
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
        String voiceMailNumberFromCard = loadVoiceMailConfigFromCard("voicemail_number", carrier);
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
        String voiceMailTagFromCard = loadVoiceMailConfigFromCard("voicemail_tag", carrier);
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
                        break;
                    }
                    this.CarrierVmMap.put(parser.getAttributeValue(null, "numeric"), new String[]{parser.getAttributeValue(null, "carrier"), parser.getAttributeValue(null, "vmnumber"), parser.getAttributeValue(null, "vmtag")});
                }
                if (vmReader != null) {
                    try {
                        vmReader.close();
                    } catch (IOException e) {
                    }
                }
            } catch (XmlPullParserException e2) {
                Rlog.w(LOG_TAG, "Exception in Voicemail parser " + e2);
                if (vmReader != null) {
                    vmReader.close();
                }
            } catch (IOException e3) {
                Rlog.w(LOG_TAG, "Exception in Voicemail parser " + e3);
                if (vmReader != null) {
                    vmReader.close();
                }
            } catch (Throwable th) {
                if (vmReader != null) {
                    try {
                        vmReader.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
            this.voiceMailLoaded = true;
        }
    }
}
