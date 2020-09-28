package com.android.internal.telephony;

import android.text.TextUtils;
import android.util.Xml;
import com.huawei.android.internal.util.XmlUtilsEx;
import com.huawei.android.telephony.RlogEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwIccIdUtil {
    private static final List<String> AIS_ICCID_ARRAY = new ArrayList(10);
    private static final List<String> AIS_MCCMNC_ARRAY = new ArrayList(10);
    private static final List<String> CMCC_ICCID_ARRAY = new ArrayList();
    private static final List<String> CMCC_MCCMNC_ARRAY = new ArrayList();
    private static final List<String> CT_ICCID_ARRAY = new ArrayList();
    private static final List<String> CT_MCCMNC_ARRAY = new ArrayList();
    private static final List<String> CU_ICCID_ARRAY = new ArrayList();
    private static final List<String> CU_MCCMNC_ARRAY = new ArrayList();
    private static final int DEFULT_ARRAY_LEN = 10;
    private static final int ICCID_LENGTH = 20;
    public static final int ICCID_LEN_MINIMUM = 7;
    public static final int ICCID_LEN_SIX = 6;
    private static final String LOG_TAG = HwIccIdUtil.class.getSimpleName();
    public static final int MCCMNC_LEN_MINIMUM = 5;
    private static final String PREFIX_AIS_MCC = "520";
    private static final String PREFIX_IMSI_ICCID_CONFIG_FILE = "imsi_iccid_table.xml";
    private static final String PREFIX_LOCAL_ICCID = "8986";
    private static final String PREFIX_LOCAL_MCC = "460";
    private static final String TABLE_IMSI_ICCID_ATTRIBUTE_ICCID = "iccid";
    private static final String TABLE_IMSI_ICCID_ATTRIBUTE_IMSI = "imsi";
    private static final String TABLE_IMSI_ICCID_ATTRIBUTE_OPER = "operator";
    private static final String TABLE_IMSI_ICCID_ENTRY = "imsi_iccid_entry";
    private static final String TABLE_IMSI_ICCID_ROOT = "imsi_iccid_table";
    private static final String TABLE_ITEM_SEPERATOR = ",";
    private static final String TABLE_OPERATOR_NAME_AIS = "ais";
    private static final String TABLE_OPERATOR_NAME_CMCC = "cmcc";
    private static final String TABLE_OPERATOR_NAME_CT = "ct";
    private static final String TABLE_OPERATOR_NAME_CU = "cu";

    static {
        clearImsiIccidList();
        loadImsiIccidTable();
    }

    private HwIccIdUtil() {
    }

    private static void clearImsiIccidList() {
        CMCC_ICCID_ARRAY.clear();
        CMCC_MCCMNC_ARRAY.clear();
        CU_ICCID_ARRAY.clear();
        AIS_ICCID_ARRAY.clear();
        CU_MCCMNC_ARRAY.clear();
        AIS_MCCMNC_ARRAY.clear();
        CT_ICCID_ARRAY.clear();
        CT_MCCMNC_ARRAY.clear();
    }

    private static void loadImsiIccidLocal() {
        CMCC_ICCID_ARRAY.clear();
        CMCC_ICCID_ARRAY.add("898600");
        CMCC_ICCID_ARRAY.add("898602");
        CMCC_ICCID_ARRAY.add("898607");
        CMCC_ICCID_ARRAY.add("898212");
        CU_ICCID_ARRAY.clear();
        CU_ICCID_ARRAY.add("898601");
        CU_ICCID_ARRAY.add("898609");
        AIS_ICCID_ARRAY.clear();
        AIS_ICCID_ARRAY.add("896603");
        CT_ICCID_ARRAY.clear();
        CT_ICCID_ARRAY.add("898603");
        CT_ICCID_ARRAY.add("898611");
        CT_ICCID_ARRAY.add("898606");
        CT_ICCID_ARRAY.add("8985302");
        CT_ICCID_ARRAY.add("8985307");
        CMCC_MCCMNC_ARRAY.clear();
        CMCC_MCCMNC_ARRAY.add("46000");
        CMCC_MCCMNC_ARRAY.add("46002");
        CMCC_MCCMNC_ARRAY.add("46004");
        CMCC_MCCMNC_ARRAY.add("46007");
        CMCC_MCCMNC_ARRAY.add("46008");
        CU_MCCMNC_ARRAY.clear();
        CU_MCCMNC_ARRAY.add("46001");
        CU_MCCMNC_ARRAY.add("46006");
        CU_MCCMNC_ARRAY.add("46009");
        AIS_MCCMNC_ARRAY.clear();
        AIS_MCCMNC_ARRAY.add("52001");
        AIS_MCCMNC_ARRAY.add("52003");
    }

    private static File getImsiIccidFile() {
        try {
            File cfg = HwCfgFilePolicy.getCfgFile(String.format("/xml/%s", PREFIX_IMSI_ICCID_CONFIG_FILE), 0);
            if (cfg != null) {
                return cfg;
            }
            return null;
        } catch (NoClassDefFoundError e) {
            RlogEx.e(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
            return null;
        } catch (NoSuchMethodError e2) {
            RlogEx.e(LOG_TAG, "[ERROR:NoSuchMethodError] HwCfgFilePolicy.getImsiIccidFile.");
            return null;
        } catch (Exception e3) {
            RlogEx.e(LOG_TAG, "Exception get imsi_iccid_config file.");
            return null;
        }
    }

    private static void loadImsiIccidTable() {
        File imsiIccidFile = getImsiIccidFile();
        if (imsiIccidFile == null) {
            RlogEx.e(LOG_TAG, "load imsi iccid prefix file failed ! Load local data!");
            loadImsiIccidLocal();
            return;
        }
        RlogEx.i(LOG_TAG, "load imsi iccid from file.");
        FileInputStream fin = null;
        try {
            FileInputStream fin2 = new FileInputStream(imsiIccidFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fin2, "UTF-8");
            XmlUtilsEx.beginDocument(parser, TABLE_IMSI_ICCID_ROOT);
            while (true) {
                XmlUtilsEx.nextElement(parser);
                if (TABLE_IMSI_ICCID_ENTRY.equalsIgnoreCase(parser.getName())) {
                    String operator = parser.getAttributeValue(null, TABLE_IMSI_ICCID_ATTRIBUTE_OPER);
                    if (!TextUtils.isEmpty(operator)) {
                        String imsiString = parser.getAttributeValue(null, "imsi");
                        String iccidString = parser.getAttributeValue(null, TABLE_IMSI_ICCID_ATTRIBUTE_ICCID);
                        String[] imsiArray = null;
                        String[] iccidArray = null;
                        if (!TextUtils.isEmpty(imsiString)) {
                            imsiArray = imsiString.split(TABLE_ITEM_SEPERATOR);
                        }
                        if (!TextUtils.isEmpty(iccidString)) {
                            iccidArray = iccidString.split(TABLE_ITEM_SEPERATOR);
                        }
                        if (operator.equalsIgnoreCase("cmcc")) {
                            parseImsiIccidElement(imsiArray, iccidArray, CMCC_MCCMNC_ARRAY, CMCC_ICCID_ARRAY);
                        } else if (operator.equalsIgnoreCase(TABLE_OPERATOR_NAME_CU)) {
                            parseImsiIccidElement(imsiArray, iccidArray, CU_MCCMNC_ARRAY, CU_ICCID_ARRAY);
                        } else if (operator.equalsIgnoreCase("ct")) {
                            parseImsiIccidElement(imsiArray, iccidArray, CT_MCCMNC_ARRAY, CT_ICCID_ARRAY);
                        } else if (operator.equalsIgnoreCase(TABLE_OPERATOR_NAME_AIS)) {
                            parseImsiIccidElement(imsiArray, iccidArray, AIS_MCCMNC_ARRAY, AIS_ICCID_ARRAY);
                        }
                    }
                } else {
                    try {
                        fin2.close();
                        return;
                    } catch (IOException e) {
                        RlogEx.e(LOG_TAG, "IOException happened when close mcciccidtable parser.");
                        return;
                    }
                }
            }
        } catch (XmlPullParserException e2) {
            RlogEx.e(LOG_TAG, "XmlPullParserException in mcciccidtable parser.");
            if (0 != 0) {
                fin.close();
            }
        } catch (IOException e3) {
            RlogEx.e(LOG_TAG, "IOException in mcciccidtable parser.");
            if (0 != 0) {
                fin.close();
            }
        } catch (Exception e4) {
            RlogEx.e(LOG_TAG, "Exception in mcciccidtable parser.");
            if (0 != 0) {
                fin.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fin.close();
                } catch (IOException e5) {
                    RlogEx.e(LOG_TAG, "IOException happened when close mcciccidtable parser.");
                }
            }
            throw th;
        }
    }

    private static void parseImsiIccidElement(String[] imsiArray, String[] iccidArray, List<String> operatorImsiList, List<String> operatorIccidList) {
        if (!(operatorImsiList == null || operatorIccidList == null)) {
            if (imsiArray != null && imsiArray.length > 0) {
                for (String item : imsiArray) {
                    try {
                        operatorImsiList.add(String.valueOf(Integer.parseInt(item.trim())));
                    } catch (Exception e) {
                        RlogEx.e(LOG_TAG, "Invalid imsi in imsiIccidTable.");
                    }
                }
            }
            if (iccidArray != null && iccidArray.length > 0) {
                for (String item2 : iccidArray) {
                    try {
                        operatorIccidList.add(String.valueOf(Integer.parseInt(item2.trim())));
                    } catch (Exception e2) {
                        RlogEx.e(LOG_TAG, "Invalid iccid in imsiIccidTable.");
                    }
                }
            }
        }
    }

    public static boolean isCMCC(String inn) {
        if (inn != null && inn.length() >= 7) {
            inn = inn.substring(0, 6);
        }
        return CMCC_ICCID_ARRAY.contains(inn);
    }

    public static boolean isCT(String inn) {
        if (inn != null && inn.startsWith(PREFIX_LOCAL_ICCID) && inn.length() >= 7) {
            inn = inn.substring(0, 6);
        }
        return CT_ICCID_ARRAY.contains(inn);
    }

    public static boolean isAIS(String inn) {
        String tmpInn = inn;
        if (tmpInn != null && tmpInn.length() >= 7) {
            tmpInn = tmpInn.substring(0, 6);
        }
        return AIS_ICCID_ARRAY.contains(tmpInn);
    }

    public static boolean isCU(String inn) {
        if (inn != null && inn.length() >= 7) {
            inn = inn.substring(0, 6);
        }
        return CU_ICCID_ARRAY.contains(inn);
    }

    public static boolean isCMCCByMccMnc(String mccMnc) {
        if (mccMnc != null && mccMnc.length() > 5) {
            mccMnc = mccMnc.substring(0, 5);
        }
        return CMCC_MCCMNC_ARRAY.contains(mccMnc);
    }

    public static boolean isCUByMccMnc(String mccMnc) {
        if (mccMnc != null && mccMnc.length() > 5) {
            mccMnc = mccMnc.substring(0, 5);
        }
        return CU_MCCMNC_ARRAY.contains(mccMnc);
    }

    public static boolean isAISByMccMnc(String mccMnc) {
        String tmpMccMnc = mccMnc;
        if (tmpMccMnc != null && tmpMccMnc.length() > 5) {
            tmpMccMnc = tmpMccMnc.substring(0, 5);
        }
        return AIS_MCCMNC_ARRAY.contains(tmpMccMnc);
    }

    public static String padTrailingFs(String iccId) {
        if (TextUtils.isEmpty(iccId) || iccId.length() >= 20) {
            return iccId;
        }
        return iccId + new String(new char[(20 - iccId.length())]).replace((char) 0, 'F');
    }
}
