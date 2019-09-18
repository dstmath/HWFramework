package com.android.internal.telephony;

import android.database.Cursor;
import android.net.Uri;
import android.provider.HwTelephony;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.telephony.uicc.IccRecords;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class VirtualNetOnsExtend extends VirtualNet {
    private static final String LOG_TAG = "GSM";
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);
    private static final String PREFERRED_APN_URI = "content://telephony/carriers/preferapn";
    private static final int RULE_APN = 5;
    private static final String XML_ELEMENT_ITEM_NAME = "virtualNet";
    private static final String XML_ELEMENT_TAG_NAME = "virtualNets";
    private static final String XML_NAME = "VirtualNet_cust.xml";
    private static final String XML_PATH = "/data/cust/xml";
    private static List<VirtualNetOnsExtend> display_name_list = null;
    private static VirtualNetOnsExtend mVirtualNetOnsExtend = null;
    private String apn;
    private String gid1;
    private String gid_mask;
    private String hplmn;
    private String imsi_start;
    private String match_file;
    private String match_mask;
    private String match_path;
    private String match_value;
    private String ons_name;
    private String rplmn;
    private String spn;
    private String virtual_net_rule;

    public static VirtualNetOnsExtend getCurrentVirtualNet() {
        return mVirtualNetOnsExtend;
    }

    public VirtualNetOnsExtend(String rplmn2, String hplmn2, String virtual_net_rule2, String imsi_start2, String gid12, String gid_mask2, String spn2, String match_path2, String match_file2, String match_mask2, String apn2, String match_value2, String ons_name2) {
        this.rplmn = rplmn2;
        this.hplmn = hplmn2;
        this.virtual_net_rule = virtual_net_rule2;
        this.imsi_start = imsi_start2;
        this.gid1 = gid12;
        this.gid_mask = gid_mask2;
        this.spn = spn2;
        this.match_path = match_path2;
        this.match_file = match_file2;
        this.match_mask = match_mask2;
        this.apn = apn2;
        this.match_value = match_value2;
        this.ons_name = ons_name2;
    }

    public static boolean isVirtualNetOnsExtend() {
        loadVirtualNetCustFiles();
        return display_name_list != null && display_name_list.size() > 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00b2, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00b3, code lost:
        r5 = r0;
        loge("An error occurs attempting to close this stream!");
     */
    public static void loadVirtualNetCustFiles() {
        XmlPullParser parser;
        File confFile = initFile();
        if (display_name_list == null && confFile.exists()) {
            display_name_list = new ArrayList();
            InputStream operatorFile = null;
            XmlPullParser parser2 = null;
            try {
                InputStream operatorFile2 = new FileInputStream(confFile);
                parser = Xml.newPullParser();
                parser.setInput(operatorFile2, null);
                int xmlEventType = parser.getEventType();
                while (true) {
                    if (xmlEventType != 1) {
                        if (xmlEventType != 2 || !XML_ELEMENT_ITEM_NAME.equals(parser.getName())) {
                            if (xmlEventType == 3 && XML_ELEMENT_TAG_NAME.equals(parser.getName())) {
                                break;
                            }
                        } else {
                            VirtualNetOnsExtend virtualNetOnsExtend = new VirtualNetOnsExtend(parser.getAttributeValue(null, "rplmn"), parser.getAttributeValue(null, "hplmn"), parser.getAttributeValue(null, HwTelephony.VirtualNets.VIRTUAL_NET_RULE), parser.getAttributeValue(null, HwTelephony.VirtualNets.IMSI_START), parser.getAttributeValue(null, HwTelephony.VirtualNets.GID1), parser.getAttributeValue(null, HwTelephony.VirtualNets.GID_MASK), parser.getAttributeValue(null, "spn"), parser.getAttributeValue(null, HwTelephony.VirtualNets.MATCH_PATH), parser.getAttributeValue(null, HwTelephony.VirtualNets.MATCH_FILE), parser.getAttributeValue(null, HwTelephony.VirtualNets.MATCH_MASK), parser.getAttributeValue(null, "apn"), parser.getAttributeValue(null, HwTelephony.VirtualNets.MATCH_VALUE), parser.getAttributeValue(null, HwTelephony.VirtualNets.ONS_NAME));
                            display_name_list.add(virtualNetOnsExtend);
                        }
                        xmlEventType = parser.next();
                    }
                }
            } catch (FileNotFoundException e) {
                loge("FileNotFoundException : could not find xml file.");
                if (operatorFile != null) {
                    try {
                        operatorFile.close();
                    } catch (IOException e2) {
                        IOException iOException = e2;
                        loge("An error occurs attempting to close this stream!");
                    }
                }
                if (parser2 != null) {
                    try {
                        parser2.setInput(null);
                    } catch (Exception e3) {
                        e = e3;
                    }
                } else {
                    return;
                }
            } catch (XmlPullParserException e4) {
                e4.printStackTrace();
                if (operatorFile != null) {
                    try {
                        operatorFile.close();
                    } catch (IOException e5) {
                        IOException iOException2 = e5;
                        loge("An error occurs attempting to close this stream!");
                    }
                }
                if (parser2 != null) {
                    try {
                        parser2.setInput(null);
                    } catch (Exception e6) {
                        e = e6;
                    }
                } else {
                    return;
                }
            } catch (IOException e7) {
                e7.printStackTrace();
                if (operatorFile != null) {
                    try {
                        operatorFile.close();
                    } catch (IOException e8) {
                        IOException iOException3 = e8;
                        loge("An error occurs attempting to close this stream!");
                    }
                }
                if (parser2 != null) {
                    try {
                        parser2.setInput(null);
                    } catch (Exception e9) {
                        e = e9;
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                XmlPullParser parser3 = parser2;
                InputStream operatorFile3 = operatorFile;
                Throwable th2 = th;
                if (operatorFile3 != null) {
                    try {
                        operatorFile3.close();
                    } catch (IOException e10) {
                        IOException iOException4 = e10;
                        loge("An error occurs attempting to close this stream!");
                    }
                }
                if (parser3 != null) {
                    try {
                        parser3.setInput(null);
                    } catch (Exception e11) {
                        Exception exc = e11;
                        e11.printStackTrace();
                    }
                }
                throw th2;
            }
        }
        return;
        if (parser != null) {
            try {
                parser.setInput(null);
            } catch (Exception e12) {
                e = e12;
            }
        }
        return;
        Exception exc2 = e;
        e.printStackTrace();
    }

    private static File initFile() {
        File confFile = new File(XML_PATH, XML_NAME);
        File vnSystemFile = new File("/system/etc", XML_NAME);
        try {
            File cfg = HwCfgFilePolicy.getCfgFile("xml/VirtualNet_cust.xml", 0);
            if (cfg != null) {
                File confFile2 = cfg;
                logd("loadVirtualNetCust from hwCfgPolicyPath folder");
                return confFile2;
            } else if (confFile.exists()) {
                logd("loadVirtualNetCust from cust folder");
                return confFile;
            } else {
                File confFile3 = vnSystemFile;
                logd("loadVirtualNetCust from etc folder");
                return confFile3;
            }
        } catch (NoClassDefFoundError e) {
            Log.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
            return confFile;
        }
    }

    public static void createVirtualNetByHplmn(String hplmn2, IccRecords simRecords) {
        mVirtualNetOnsExtend = null;
        if (display_name_list != null && display_name_list.size() > 0) {
            int displayNameListSize = display_name_list.size();
            for (int i = 0; i < displayNameListSize; i++) {
                String simMccMnc = display_name_list.get(i).getHplmn();
                String VirtualNetRule = display_name_list.get(i).getRule();
                Uri preCarrierUri = PREFERAPN_URI;
                if (!TextUtils.isEmpty(simMccMnc) && simMccMnc.equals(hplmn2)) {
                    int tmpVirtualNetRule = 0;
                    if (!TextUtils.isEmpty(VirtualNetRule)) {
                        tmpVirtualNetRule = Integer.parseInt(VirtualNetRule);
                    }
                    switch (tmpVirtualNetRule) {
                        case 1:
                            if (!isImsiVirtualNet(simRecords.getIMSI(), display_name_list.get(i).getImsistart())) {
                                break;
                            } else {
                                mVirtualNetOnsExtend = display_name_list.get(i);
                                break;
                            }
                        case 2:
                            if (!isGid1VirtualNet(simRecords.getGID1(), display_name_list.get(i).getGID1(), display_name_list.get(i).getGidmask())) {
                                break;
                            } else {
                                mVirtualNetOnsExtend = display_name_list.get(i);
                                break;
                            }
                        case 3:
                            if (!isSpnVirtualNet(simRecords.getServiceProviderName(), display_name_list.get(i).getSpn())) {
                                break;
                            } else {
                                mVirtualNetOnsExtend = display_name_list.get(i);
                                break;
                            }
                        case 4:
                            if (!isSpecialFileVirtualNet(display_name_list.get(i).getPath(), display_name_list.get(i).getFile(), display_name_list.get(i).getFilemask(), display_name_list.get(i).getFilevalue(), simRecords.getSlotId())) {
                                break;
                            } else {
                                mVirtualNetOnsExtend = display_name_list.get(i);
                                break;
                            }
                        case 5:
                            if (!isApnVirtualNet(display_name_list.get(i).getApn(), getPreApnName(preCarrierUri))) {
                                break;
                            } else {
                                mVirtualNetOnsExtend = display_name_list.get(i);
                                break;
                            }
                        default:
                            logd("unhandled case: " + tmpVirtualNetRule);
                            break;
                    }
                }
            }
        }
    }

    public String getRplmn() {
        return this.rplmn;
    }

    public String getApn() {
        return this.apn;
    }

    public String getHplmn() {
        return this.hplmn;
    }

    public String getImsistart() {
        return this.imsi_start;
    }

    public String getRule() {
        return this.virtual_net_rule;
    }

    public String getGID1() {
        return this.gid1;
    }

    public String getGidmask() {
        return this.gid_mask;
    }

    public String getSpn() {
        return this.spn;
    }

    public String getPath() {
        return this.match_path;
    }

    public String getFile() {
        return this.match_file;
    }

    public String getFilemask() {
        return this.match_mask;
    }

    public String getOperatorName() {
        return this.ons_name;
    }

    public String getFilevalue() {
        return this.match_value;
    }

    private static boolean isApnVirtualNet(String apn2, String preApn) {
        if (apn2 == null || preApn == null || !apn2.equals(preApn)) {
            return false;
        }
        return true;
    }

    private static String getPreApnName(Uri uri) {
        Cursor cursor = PhoneFactory.getDefaultPhone().getContext().getContentResolver().query(uri, new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        String apn2 = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apn2 = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
        }
        if (cursor != null) {
            cursor.close();
        }
        return apn2;
    }

    private static void logd(String text) {
        Log.d(LOG_TAG, "[VirtualNet] " + text);
    }

    private static void loge(String text) {
        Log.e(LOG_TAG, "[VirtualNet] " + text);
    }
}
