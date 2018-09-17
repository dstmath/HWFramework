package com.android.internal.telephony;

import android.database.Cursor;
import android.net.Uri;
import android.provider.HwTelephony.NumMatchs;
import android.provider.HwTelephony.VirtualNets;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
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
    private static final Uri PREFERAPN_URI = null;
    private static final String PREFERRED_APN_URI = "content://telephony/carriers/preferapn";
    private static final int RULE_APN = 5;
    private static final String XML_ELEMENT_ITEM_NAME = "virtualNet";
    private static final String XML_ELEMENT_TAG_NAME = "virtualNets";
    private static final String XML_NAME = "VirtualNet_cust.xml";
    private static final String XML_PATH = "/data/cust/xml";
    private static List<VirtualNetOnsExtend> display_name_list;
    private static VirtualNetOnsExtend mVirtualNetOnsExtend;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.VirtualNetOnsExtend.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.VirtualNetOnsExtend.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.VirtualNetOnsExtend.<clinit>():void");
    }

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

    public static void loadVirtualNetCustFiles() {
        File confFile;
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        File file = new File(XML_PATH, XML_NAME);
        file = new File("/system/etc", XML_NAME);
        try {
            File cfg = HwCfgFilePolicy.getCfgFile("xml/VirtualNet_cust.xml", 0);
            if (cfg != null) {
                confFile = cfg;
                logd("loadVirtualNetCust from hwCfgPolicyPath folder");
            } else if (file.exists()) {
                logd("loadVirtualNetCust from cust folder");
            } else {
                confFile = file;
                logd("loadVirtualNetCust from etc folder");
            }
        } catch (NoClassDefFoundError e3) {
            Log.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
        }
        if (display_name_list == null && confFile.exists()) {
            display_name_list = new ArrayList();
            InputStream inputStream = null;
            XmlPullParser xmlPullParser = null;
            try {
                InputStream fileInputStream = new FileInputStream(confFile);
                try {
                    xmlPullParser = Xml.newPullParser();
                    xmlPullParser.setInput(fileInputStream, null);
                    for (int xmlEventType = xmlPullParser.getEventType(); xmlEventType != 1; xmlEventType = xmlPullParser.next()) {
                        if (xmlEventType != 2 || !XML_ELEMENT_ITEM_NAME.equals(xmlPullParser.getName())) {
                            if (xmlEventType == 3) {
                                if (XML_ELEMENT_TAG_NAME.equals(xmlPullParser.getName())) {
                                    break;
                                }
                            }
                        }
                        VirtualNetOnsExtend virtualnets = new VirtualNetOnsExtend(xmlPullParser.getAttributeValue(null, "rplmn"), xmlPullParser.getAttributeValue(null, "hplmn"), xmlPullParser.getAttributeValue(null, VirtualNets.VIRTUAL_NET_RULE), xmlPullParser.getAttributeValue(null, VirtualNets.IMSI_START), xmlPullParser.getAttributeValue(null, VirtualNets.GID1), xmlPullParser.getAttributeValue(null, VirtualNets.GID_MASK), xmlPullParser.getAttributeValue(null, VirtualNets.SPN), xmlPullParser.getAttributeValue(null, VirtualNets.MATCH_PATH), xmlPullParser.getAttributeValue(null, VirtualNets.MATCH_FILE), xmlPullParser.getAttributeValue(null, VirtualNets.MATCH_MASK), xmlPullParser.getAttributeValue(null, "apn"), xmlPullParser.getAttributeValue(null, VirtualNets.MATCH_VALUE), xmlPullParser.getAttributeValue(null, VirtualNets.ONS_NAME));
                        display_name_list.add(virtualnets);
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e4) {
                            loge("An error occurs attempting to close this stream!");
                        }
                    }
                    if (xmlPullParser != null) {
                        try {
                            xmlPullParser.setInput(null);
                        } catch (Exception e5) {
                            e5.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e6) {
                    inputStream = fileInputStream;
                } catch (XmlPullParserException e7) {
                    e = e7;
                    inputStream = fileInputStream;
                } catch (IOException e8) {
                    e2 = e8;
                    inputStream = fileInputStream;
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = fileInputStream;
                }
            } catch (FileNotFoundException e9) {
                loge("FileNotFoundException : could not find xml file.");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e10) {
                        loge("An error occurs attempting to close this stream!");
                    }
                }
                if (xmlPullParser != null) {
                    try {
                        xmlPullParser.setInput(null);
                    } catch (Exception e52) {
                        e52.printStackTrace();
                    }
                }
            } catch (XmlPullParserException e11) {
                e = e11;
                e.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e12) {
                        loge("An error occurs attempting to close this stream!");
                    }
                }
                if (xmlPullParser != null) {
                    try {
                        xmlPullParser.setInput(null);
                    } catch (Exception e522) {
                        e522.printStackTrace();
                    }
                }
            } catch (IOException e13) {
                e2 = e13;
                try {
                    e2.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e14) {
                            loge("An error occurs attempting to close this stream!");
                        }
                    }
                    if (xmlPullParser != null) {
                        try {
                            xmlPullParser.setInput(null);
                        } catch (Exception e5222) {
                            e5222.printStackTrace();
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e15) {
                            loge("An error occurs attempting to close this stream!");
                        }
                    }
                    if (xmlPullParser != null) {
                        try {
                            xmlPullParser.setInput(null);
                        } catch (Exception e52222) {
                            e52222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        }
    }

    public static void createVirtualNetByHplmn(String hplmn, IccRecords simRecords) {
        mVirtualNetOnsExtend = null;
        if (display_name_list != null && display_name_list.size() > 0) {
            for (int i = 0; i < display_name_list.size(); i++) {
                String simMccMnc = ((VirtualNetOnsExtend) display_name_list.get(i)).getHplmn();
                String VirtualNetRule = ((VirtualNetOnsExtend) display_name_list.get(i)).getRule();
                Uri preCarrierUri = PREFERAPN_URI;
                if (!TextUtils.isEmpty(simMccMnc) && simMccMnc.equals(hplmn)) {
                    int tmpVirtualNetRule = 0;
                    if (!TextUtils.isEmpty(VirtualNetRule)) {
                        tmpVirtualNetRule = Integer.parseInt(VirtualNetRule);
                    }
                    switch (tmpVirtualNetRule) {
                        case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                            if (!VirtualNet.isImsiVirtualNet(simRecords.getIMSI(), ((VirtualNetOnsExtend) display_name_list.get(i)).getImsistart())) {
                                break;
                            }
                            mVirtualNetOnsExtend = (VirtualNetOnsExtend) display_name_list.get(i);
                            break;
                        case HwVSimUtilsInner.STATE_EB /*2*/:
                            if (!VirtualNet.isGid1VirtualNet(simRecords.getGID1(), ((VirtualNetOnsExtend) display_name_list.get(i)).getGID1(), ((VirtualNetOnsExtend) display_name_list.get(i)).getGidmask())) {
                                break;
                            }
                            mVirtualNetOnsExtend = (VirtualNetOnsExtend) display_name_list.get(i);
                            break;
                        case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                            if (!VirtualNet.isSpnVirtualNet(simRecords.getServiceProviderName(), ((VirtualNetOnsExtend) display_name_list.get(i)).getSpn())) {
                                break;
                            }
                            mVirtualNetOnsExtend = (VirtualNetOnsExtend) display_name_list.get(i);
                            break;
                        case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                            String tmpMatchPath = ((VirtualNetOnsExtend) display_name_list.get(i)).getPath();
                            String tmpMatchFile = ((VirtualNetOnsExtend) display_name_list.get(i)).getFile();
                            String tmpMatchValue = ((VirtualNetOnsExtend) display_name_list.get(i)).getFilemask();
                            if (!VirtualNet.isSpecialFileVirtualNet(tmpMatchPath, tmpMatchFile, tmpMatchValue, ((VirtualNetOnsExtend) display_name_list.get(i)).getFilevalue(), simRecords.getSlotId())) {
                                break;
                            }
                            mVirtualNetOnsExtend = (VirtualNetOnsExtend) display_name_list.get(i);
                            break;
                        case RULE_APN /*5*/:
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

    /* JADX WARNING: inconsistent code. */
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
