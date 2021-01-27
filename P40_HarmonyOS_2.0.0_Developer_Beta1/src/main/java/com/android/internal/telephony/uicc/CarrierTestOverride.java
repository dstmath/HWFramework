package com.android.internal.telephony.uicc;

import android.os.Environment;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class CarrierTestOverride {
    static final String CARRIER_TEST_XML_HEADER = "carrierTestOverrides";
    static final String CARRIER_TEST_XML_ITEM_KEY = "key";
    static final String CARRIER_TEST_XML_ITEM_KEY_STRING_GID1 = "gid1";
    static final String CARRIER_TEST_XML_ITEM_KEY_STRING_GID2 = "gid2";
    static final String CARRIER_TEST_XML_ITEM_KEY_STRING_ICCID = "iccid";
    static final String CARRIER_TEST_XML_ITEM_KEY_STRING_IMSI = "imsi";
    static final String CARRIER_TEST_XML_ITEM_KEY_STRING_ISINTESTMODE = "isInTestMode";
    static final String CARRIER_TEST_XML_ITEM_KEY_STRING_MCCMNC = "mccmnc";
    static final String CARRIER_TEST_XML_ITEM_KEY_STRING_PNN = "pnn";
    static final String CARRIER_TEST_XML_ITEM_KEY_STRING_SPN = "spn";
    static final String CARRIER_TEST_XML_ITEM_VALUE = "value";
    static final String CARRIER_TEST_XML_SUBHEADER = "carrierTestOverride";
    static final String DATA_CARRIER_TEST_OVERRIDE_PATH = "/user_de/0/com.android.phone/files/carrier_test_conf.xml";
    static final String LOG_TAG = "CarrierTestOverride";
    private HashMap<String, String> mCarrierTestParamMap = new HashMap<>();

    CarrierTestOverride() {
        loadCarrierTestOverrides();
    }

    /* access modifiers changed from: package-private */
    public boolean isInTestMode() {
        return this.mCarrierTestParamMap.containsKey(CARRIER_TEST_XML_ITEM_KEY_STRING_ISINTESTMODE) && this.mCarrierTestParamMap.get(CARRIER_TEST_XML_ITEM_KEY_STRING_ISINTESTMODE).equals("true");
    }

    /* access modifiers changed from: package-private */
    public String getFakeSpn() {
        try {
            String spn = this.mCarrierTestParamMap.get(CARRIER_TEST_XML_ITEM_KEY_STRING_SPN);
            Rlog.d(LOG_TAG, "reading spn from CarrierTestConfig file: " + spn);
            return spn;
        } catch (NullPointerException e) {
            Rlog.w(LOG_TAG, "No spn in CarrierTestConfig file ");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public String getFakeIMSI() {
        try {
            String imsi = this.mCarrierTestParamMap.get(CARRIER_TEST_XML_ITEM_KEY_STRING_IMSI);
            Rlog.d(LOG_TAG, "reading imsi from CarrierTestConfig file: " + imsi);
            return imsi;
        } catch (NullPointerException e) {
            Rlog.w(LOG_TAG, "No imsi in CarrierTestConfig file ");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public String getFakeGid1() {
        try {
            String gid1 = this.mCarrierTestParamMap.get(CARRIER_TEST_XML_ITEM_KEY_STRING_GID1);
            Rlog.d(LOG_TAG, "reading gid1 from CarrierTestConfig file: " + gid1);
            return gid1;
        } catch (NullPointerException e) {
            Rlog.w(LOG_TAG, "No gid1 in CarrierTestConfig file ");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public String getFakeGid2() {
        try {
            String gid2 = this.mCarrierTestParamMap.get(CARRIER_TEST_XML_ITEM_KEY_STRING_GID2);
            Rlog.d(LOG_TAG, "reading gid2 from CarrierTestConfig file: " + gid2);
            return gid2;
        } catch (NullPointerException e) {
            Rlog.w(LOG_TAG, "No gid2 in CarrierTestConfig file ");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public String getFakePnnHomeName() {
        try {
            String pnn = this.mCarrierTestParamMap.get(CARRIER_TEST_XML_ITEM_KEY_STRING_PNN);
            Rlog.d(LOG_TAG, "reading pnn from CarrierTestConfig file: " + pnn);
            return pnn;
        } catch (NullPointerException e) {
            Rlog.w(LOG_TAG, "No pnn in CarrierTestConfig file ");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public String getFakeIccid() {
        try {
            String iccid = this.mCarrierTestParamMap.get(CARRIER_TEST_XML_ITEM_KEY_STRING_ICCID);
            Rlog.d(LOG_TAG, "reading iccid from CarrierTestConfig file: " + iccid);
            return iccid;
        } catch (NullPointerException e) {
            Rlog.w(LOG_TAG, "No iccid in CarrierTestConfig file ");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public String getFakeMccMnc() {
        try {
            String mccmnc = this.mCarrierTestParamMap.get(CARRIER_TEST_XML_ITEM_KEY_STRING_MCCMNC);
            Rlog.d(LOG_TAG, "reading mccmnc from CarrierTestConfig file: " + mccmnc);
            return mccmnc;
        } catch (NullPointerException e) {
            Rlog.w(LOG_TAG, "No mccmnc in CarrierTestConfig file ");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void override(String mccmnc, String imsi, String iccid, String gid1, String gid2, String pnn, String spn) {
        this.mCarrierTestParamMap.put(CARRIER_TEST_XML_ITEM_KEY_STRING_ISINTESTMODE, "true");
        this.mCarrierTestParamMap.put(CARRIER_TEST_XML_ITEM_KEY_STRING_MCCMNC, mccmnc);
        this.mCarrierTestParamMap.put(CARRIER_TEST_XML_ITEM_KEY_STRING_IMSI, imsi);
        this.mCarrierTestParamMap.put(CARRIER_TEST_XML_ITEM_KEY_STRING_ICCID, iccid);
        this.mCarrierTestParamMap.put(CARRIER_TEST_XML_ITEM_KEY_STRING_GID1, gid1);
        this.mCarrierTestParamMap.put(CARRIER_TEST_XML_ITEM_KEY_STRING_GID2, gid2);
        this.mCarrierTestParamMap.put(CARRIER_TEST_XML_ITEM_KEY_STRING_PNN, pnn);
        this.mCarrierTestParamMap.put(CARRIER_TEST_XML_ITEM_KEY_STRING_SPN, spn);
    }

    private void loadCarrierTestOverrides() {
        File carrierTestConfigFile = new File(Environment.getDataDirectory(), DATA_CARRIER_TEST_OVERRIDE_PATH);
        try {
            FileReader carrierTestConfigReader = new FileReader(carrierTestConfigFile);
            Rlog.d(LOG_TAG, "CarrierTestConfig file Modified Timestamp: " + carrierTestConfigFile.lastModified());
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(carrierTestConfigReader);
                XmlUtils.beginDocument(parser, CARRIER_TEST_XML_HEADER);
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!CARRIER_TEST_XML_SUBHEADER.equals(parser.getName())) {
                        carrierTestConfigReader.close();
                        return;
                    }
                    String key = parser.getAttributeValue(null, CARRIER_TEST_XML_ITEM_KEY);
                    String value = parser.getAttributeValue(null, CARRIER_TEST_XML_ITEM_VALUE);
                    Rlog.d(LOG_TAG, "extracting key-values from CarrierTestConfig file: " + key + "|" + value);
                    this.mCarrierTestParamMap.put(key, value);
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG, "Exception in carrier_test_conf parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG, "Exception in carrier_test_conf parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG, "Can not open config file!");
        }
    }
}
