package com.android.internal.telephony;

import android.database.Cursor;
import android.net.Uri;
import android.provider.HwTelephony.NumMatchs;
import android.provider.HwTelephony.VirtualNets;
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

    public VirtualNetOnsExtend(String rplmn, String hplmn, String virtual_net_rule, String imsi_start, String gid1, String gid_mask, String spn, String match_path, String match_file, String match_mask, String apn, String match_value, String ons_name) {
        this.rplmn = rplmn;
        this.hplmn = hplmn;
        this.virtual_net_rule = virtual_net_rule;
        this.imsi_start = imsi_start;
        this.gid1 = gid1;
        this.gid_mask = gid_mask;
        this.spn = spn;
        this.match_path = match_path;
        this.match_file = match_file;
        this.match_mask = match_mask;
        this.apn = apn;
        this.match_value = match_value;
        this.ons_name = ons_name;
    }

    public static boolean isVirtualNetOnsExtend() {
        loadVirtualNetCustFiles();
        if (display_name_list == null || display_name_list.size() <= 0) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:77:0x01c8 A:{SYNTHETIC, Splitter: B:77:0x01c8} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01cd  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x01a9 A:{SYNTHETIC, Splitter: B:65:0x01a9} */
    /* JADX WARNING: Removed duplicated region for block: B:108:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x01ae  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0184 A:{SYNTHETIC, Splitter: B:51:0x0184} */
    /* JADX WARNING: Removed duplicated region for block: B:107:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0189  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0162 A:{SYNTHETIC, Splitter: B:37:0x0162} */
    /* JADX WARNING: Removed duplicated region for block: B:106:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0167  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void loadVirtualNetCustFiles() {
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        File confFile = initFile();
        if (display_name_list == null && confFile.exists()) {
            display_name_list = new ArrayList();
            InputStream operatorFile = null;
            try {
                InputStream fileInputStream = new FileInputStream(confFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fileInputStream, null);
                    for (int xmlEventType = parser.getEventType(); xmlEventType != 1; xmlEventType = parser.next()) {
                        if (xmlEventType != 2 || !XML_ELEMENT_ITEM_NAME.equals(parser.getName())) {
                            if (xmlEventType == 3 && XML_ELEMENT_TAG_NAME.equals(parser.getName())) {
                                break;
                            }
                        } else {
                            display_name_list.add(new VirtualNetOnsExtend(parser.getAttributeValue(null, "rplmn"), parser.getAttributeValue(null, "hplmn"), parser.getAttributeValue(null, VirtualNets.VIRTUAL_NET_RULE), parser.getAttributeValue(null, VirtualNets.IMSI_START), parser.getAttributeValue(null, VirtualNets.GID1), parser.getAttributeValue(null, VirtualNets.GID_MASK), parser.getAttributeValue(null, VirtualNets.SPN), parser.getAttributeValue(null, VirtualNets.MATCH_PATH), parser.getAttributeValue(null, VirtualNets.MATCH_FILE), parser.getAttributeValue(null, VirtualNets.MATCH_MASK), parser.getAttributeValue(null, "apn"), parser.getAttributeValue(null, VirtualNets.MATCH_VALUE), parser.getAttributeValue(null, VirtualNets.ONS_NAME)));
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3) {
                            loge("An error occurs attempting to close this stream!");
                        }
                    }
                    if (parser != null) {
                        try {
                            parser.setInput(null);
                        } catch (Exception e4) {
                            e4.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e5) {
                    operatorFile = fileInputStream;
                    loge("FileNotFoundException : could not find xml file.");
                    if (operatorFile != null) {
                        try {
                            operatorFile.close();
                        } catch (IOException e6) {
                            loge("An error occurs attempting to close this stream!");
                        }
                    }
                    if (null == null) {
                        try {
                            null.setInput(null);
                        } catch (Exception e42) {
                            e42.printStackTrace();
                        }
                    }
                } catch (XmlPullParserException e7) {
                    e = e7;
                    operatorFile = fileInputStream;
                    e.printStackTrace();
                    if (operatorFile != null) {
                        try {
                            operatorFile.close();
                        } catch (IOException e8) {
                            loge("An error occurs attempting to close this stream!");
                        }
                    }
                    if (null == null) {
                        try {
                            null.setInput(null);
                        } catch (Exception e422) {
                            e422.printStackTrace();
                        }
                    }
                } catch (IOException e9) {
                    e2 = e9;
                    operatorFile = fileInputStream;
                    try {
                        e2.printStackTrace();
                        if (operatorFile != null) {
                            try {
                                operatorFile.close();
                            } catch (IOException e10) {
                                loge("An error occurs attempting to close this stream!");
                            }
                        }
                        if (null == null) {
                            try {
                                null.setInput(null);
                            } catch (Exception e4222) {
                                e4222.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (operatorFile != null) {
                            try {
                                operatorFile.close();
                            } catch (IOException e11) {
                                loge("An error occurs attempting to close this stream!");
                            }
                        }
                        if (null != null) {
                            try {
                                null.setInput(null);
                            } catch (Exception e42222) {
                                e42222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    operatorFile = fileInputStream;
                    if (operatorFile != null) {
                    }
                    if (null != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e12) {
                loge("FileNotFoundException : could not find xml file.");
                if (operatorFile != null) {
                }
                if (null == null) {
                }
            } catch (XmlPullParserException e13) {
                e = e13;
                e.printStackTrace();
                if (operatorFile != null) {
                }
                if (null == null) {
                }
            } catch (IOException e14) {
                e2 = e14;
                e2.printStackTrace();
                if (operatorFile != null) {
                }
                if (null == null) {
                }
            }
        }
    }

    private static File initFile() {
        File confFile = new File(XML_PATH, XML_NAME);
        File vnSystemFile = new File("/system/etc", XML_NAME);
        try {
            File cfg = HwCfgFilePolicy.getCfgFile("xml/VirtualNet_cust.xml", 0);
            if (cfg != null) {
                confFile = cfg;
                logd("loadVirtualNetCust from hwCfgPolicyPath folder");
                return confFile;
            } else if (confFile.exists()) {
                logd("loadVirtualNetCust from cust folder");
                return confFile;
            } else {
                confFile = vnSystemFile;
                logd("loadVirtualNetCust from etc folder");
                return confFile;
            }
        } catch (NoClassDefFoundError e) {
            Log.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
            return confFile;
        }
    }

    public static void createVirtualNetByHplmn(String hplmn, IccRecords simRecords) {
        mVirtualNetOnsExtend = null;
        if (display_name_list != null && display_name_list.size() > 0) {
            int displayNameListSize = display_name_list.size();
            for (int i = 0; i < displayNameListSize; i++) {
                String simMccMnc = ((VirtualNetOnsExtend) display_name_list.get(i)).getHplmn();
                String VirtualNetRule = ((VirtualNetOnsExtend) display_name_list.get(i)).getRule();
                Uri preCarrierUri = PREFERAPN_URI;
                if (!TextUtils.isEmpty(simMccMnc) && simMccMnc.equals(hplmn)) {
                    int tmpVirtualNetRule = 0;
                    if (!TextUtils.isEmpty(VirtualNetRule)) {
                        tmpVirtualNetRule = Integer.parseInt(VirtualNetRule);
                    }
                    switch (tmpVirtualNetRule) {
                        case 1:
                            if (!VirtualNet.isImsiVirtualNet(simRecords.getIMSI(), ((VirtualNetOnsExtend) display_name_list.get(i)).getImsistart())) {
                                break;
                            }
                            mVirtualNetOnsExtend = (VirtualNetOnsExtend) display_name_list.get(i);
                            break;
                        case 2:
                            if (!VirtualNet.isGid1VirtualNet(simRecords.getGID1(), ((VirtualNetOnsExtend) display_name_list.get(i)).getGID1(), ((VirtualNetOnsExtend) display_name_list.get(i)).getGidmask())) {
                                break;
                            }
                            mVirtualNetOnsExtend = (VirtualNetOnsExtend) display_name_list.get(i);
                            break;
                        case 3:
                            if (!VirtualNet.isSpnVirtualNet(simRecords.getServiceProviderName(), ((VirtualNetOnsExtend) display_name_list.get(i)).getSpn())) {
                                break;
                            }
                            mVirtualNetOnsExtend = (VirtualNetOnsExtend) display_name_list.get(i);
                            break;
                        case 4:
                            String tmpMatchPath = ((VirtualNetOnsExtend) display_name_list.get(i)).getPath();
                            String tmpMatchFile = ((VirtualNetOnsExtend) display_name_list.get(i)).getFile();
                            String tmpMatchValue = ((VirtualNetOnsExtend) display_name_list.get(i)).getFilemask();
                            if (!VirtualNet.isSpecialFileVirtualNet(tmpMatchPath, tmpMatchFile, tmpMatchValue, ((VirtualNetOnsExtend) display_name_list.get(i)).getFilevalue(), simRecords.getSlotId())) {
                                break;
                            }
                            mVirtualNetOnsExtend = (VirtualNetOnsExtend) display_name_list.get(i);
                            break;
                        case 5:
                            if (!isApnVirtualNet(((VirtualNetOnsExtend) display_name_list.get(i)).getApn(), getPreApnName(preCarrierUri))) {
                                break;
                            }
                            mVirtualNetOnsExtend = (VirtualNetOnsExtend) display_name_list.get(i);
                            break;
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

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isApnVirtualNet(String apn, String preApn) {
        if (apn == null || preApn == null || !apn.equals(preApn)) {
            return false;
        }
        return true;
    }

    private static String getPreApnName(Uri uri) {
        Uri uri2 = uri;
        Cursor cursor = PhoneFactory.getDefaultPhone().getContext().getContentResolver().query(uri2, new String[]{"_id", NumMatchs.NAME, "apn"}, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        String apn = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apn = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
        }
        if (cursor != null) {
            cursor.close();
        }
        return apn;
    }

    private static void logd(String text) {
        Log.d(LOG_TAG, "[VirtualNet] " + text);
    }

    private static void loge(String text) {
        Log.e(LOG_TAG, "[VirtualNet] " + text);
    }
}
