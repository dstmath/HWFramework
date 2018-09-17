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
    private HashMap<String, String[]> CarrierVmMap = new HashMap();
    private boolean voiceMailLoaded = false;

    VoiceMailConstants() {
    }

    boolean containsCarrier(String carrier) {
        loadVoiceMail();
        return this.CarrierVmMap.containsKey(carrier);
    }

    String getCarrierName(String carrier) {
        loadVoiceMail();
        String[] data = (String[]) this.CarrierVmMap.get(carrier);
        if (data != null) {
            return data[0];
        }
        return null;
    }

    String getVoiceMailNumber(String carrier) {
        loadVoiceMail();
        String[] data = (String[]) this.CarrierVmMap.get(carrier);
        if (data != null) {
            return data[1];
        }
        return null;
    }

    String getVoiceMailTag(String carrier) {
        loadVoiceMail();
        String[] data = (String[]) this.CarrierVmMap.get(carrier);
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
                    if ("voicemail".equals(parser.getName())) {
                        data = new String[3];
                        String numeric = parser.getAttributeValue(null, "numeric");
                        data[0] = parser.getAttributeValue(null, "carrier");
                        data[1] = parser.getAttributeValue(null, "vmnumber");
                        data[2] = parser.getAttributeValue(null, "vmtag");
                        this.CarrierVmMap.put(numeric, data);
                    } else if (vmReader != null) {
                        try {
                            vmReader.close();
                        } catch (IOException e) {
                        }
                    }
                }
            } catch (XmlPullParserException e2) {
                Rlog.w(LOG_TAG, "Exception in Voicemail parser " + e2);
                if (vmReader != null) {
                    try {
                        vmReader.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                Rlog.w(LOG_TAG, "Exception in Voicemail parser " + e4);
                if (vmReader != null) {
                    try {
                        vmReader.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Throwable th) {
                if (vmReader != null) {
                    try {
                        vmReader.close();
                    } catch (IOException e6) {
                    }
                }
            }
            this.voiceMailLoaded = true;
        }
    }
}
