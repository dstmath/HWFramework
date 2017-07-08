package com.android.server.wifi.hotspot2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.anqp.eap.NonEAPInnerAuth;
import com.android.server.wifi.anqp.eap.NonEAPInnerAuth.NonEAPType;
import com.android.server.wifi.hotspot2.omadm.MOTree;
import com.android.server.wifi.hotspot2.omadm.OMAConstants;
import com.android.server.wifi.hotspot2.omadm.OMAConstructed;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class OMADMAdapter {
    private static final String DevDetail = "<MgmtTree><VerDTD>1.2</VerDTD><Node><NodeName>DevDetail</NodeName><RTProperties><Type><DDFName>urn:oma:mo:oma-dm-devdetail:1.0</DDFName></Type></RTProperties><Node><NodeName>Ext</NodeName><Node><NodeName>org.wi-fi</NodeName><RTProperties><Type><DDFName>urn:wfa:mo-ext:hotspot2dot0-devdetail-ext :1.0</DDFName></Type></RTProperties><Node><NodeName>Wi-Fi</NodeName><Node><NodeName>EAPMethodList</NodeName><Node><NodeName>Method01</NodeName><!-- EAP-TTLS/MS-CHAPv2 --><Node><NodeName>EAPType</NodeName><Value>21</Value></Node><Node><NodeName>InnerMethod</NodeName><Value>MS-CHAP-V2</Value></Node></Node><Node><NodeName>Method02</NodeName><!-- EAP-TLS --><Node><NodeName>EAPType</NodeName><Value>13</Value></Node></Node><Node><NodeName>Method03</NodeName><!-- EAP-SIM --><Node><NodeName>EAPType</NodeName><Value>18</Value></Node></Node><Node><NodeName>Method04</NodeName><!-- EAP-AKA --><Node><NodeName>EAPType</NodeName><Value>23</Value></Node></Node><Node><NodeName>Method05</NodeName><!-- EAP-AKA' --><Node><NodeName>EAPType</NodeName><Value>50</Value></Node></Node><Node><NodeName>Method06</NodeName><!-- Supported method (EAP-TTLS/PAP) not mandated by Hotspot2.0--><Node><NodeName>EAPType</NodeName><Value>21</Value></Node><Node><NodeName>InnerMethod</NodeName><Value>PAP</Value></Node></Node><Node><NodeName>Method07</NodeName><!-- Supported method (PEAP/EAP-GTC) not mandated by Hotspot 2.0--><Node><NodeName>EAPType</NodeName><Value>25</Value></Node><Node><NodeName>InnerEAPType</NodeName><Value>6</Value></Node></Node></Node><Node><NodeName>SPCertificate</NodeName><Node><NodeName>Cert01</NodeName><Node><NodeName>CertificateIssuerName</NodeName><Value>CN=RuckusCA</Value></Node></Node></Node><Node><NodeName>ManufacturingCertificate</NodeName><Value>FALSE</Value></Node><Node><NodeName>Wi-FiMACAddress</NodeName><Value>001d2e112233</Value></Node><Node><NodeName>ClientTriggerRedirectURI</NodeName><Value>http://127.0.0.1:12345/index.htm</Value></Node><Node><NodeName>Ops</NodeName><Node><NodeName>launchBrowserToURI</NodeName><Value></Value></Node><Node><NodeName>negotiateClientCertTLS</NodeName><Value></Value></Node><Node><NodeName>getCertificate</NodeName><Value></Value></Node></Node></Node><!-- End of Wi-Fi node --></Node><!-- End of org.wi-fi node --></Node><!-- End of Ext node --><Node><NodeName>URI</NodeName><Node><NodeName>MaxDepth</NodeName><Value>32</Value></Node><Node><NodeName>MaxTotLen</NodeName><Value>2048</Value></Node><Node><NodeName>MaxSegLen</NodeName><Value>64</Value></Node></Node><Node><NodeName>DevType</NodeName><Value>Smartphone</Value></Node><Node><NodeName>OEM</NodeName><Value>ACME</Value></Node><Node><NodeName>FwV</NodeName><Value>1.2.100.5</Value></Node><Node><NodeName>SwV</NodeName><Value>9.11.130</Value></Node><Node><NodeName>HwV</NodeName><Value>1.0</Value></Node><Node><NodeName>LrgObj</NodeName><Value>TRUE</Value></Node></Node></MgmtTree>";
    private static final String DevInfo = "<MgmtTree><VerDTD>1.2</VerDTD><Node><NodeName>DevInfo</NodeName><RTProperties><Type><DDFName>urn:oma:mo:oma-dm-devinfo:1.0</DDFName></Type></RTProperties></Node><Node><NodeName>DevID</NodeName><Path>DevInfo</Path><Value>urn:acme:00-11-22-33-44-55</Value></Node><Node><NodeName>Man</NodeName><Path>DevInfo</Path><Value>ACME</Value></Node><Node><NodeName>Mod</NodeName><Path>DevInfo</Path><Value>HS2.0-01</Value></Node><Node><NodeName>DmV</NodeName><Path>DevInfo</Path><Value>1.2</Value></Node><Node><NodeName>Lang</NodeName><Path>DevInfo</Path><Value>en-US</Value></Node></MgmtTree>";
    private static final String[] ExtWiFiPath = null;
    private static final int IMEI_Length = 14;
    private static final Map<String, String> RTProps = null;
    private static OMADMAdapter sInstance;
    private final Context mContext;
    private final List<PathAccessor> mDevDetail;
    private MOTree mDevDetailTree;
    private final String mDevID;
    private final List<PathAccessor> mDevInfo;
    private MOTree mDevInfoTree;
    private final String mImei;
    private final String mImsi;

    private static abstract class PathAccessor {
        private final int mHashCode;
        private final String[] mPath;

        protected abstract Object getValue();

        protected PathAccessor(Object... path) {
            int i;
            Object o;
            int length = 0;
            for (Object o2 : path) {
                if (o2.getClass() == String[].class) {
                    length += ((String[]) o2).length;
                } else {
                    length++;
                }
            }
            this.mPath = new String[length];
            int length2 = path.length;
            int i2 = 0;
            int n = 0;
            while (i2 < length2) {
                int n2;
                o2 = path[i2];
                if (o2.getClass() == String[].class) {
                    String[] strArr = (String[]) o2;
                    int length3 = strArr.length;
                    i = 0;
                    while (i < length3) {
                        n2 = n + 1;
                        this.mPath[n] = strArr[i];
                        i++;
                        n = n2;
                    }
                    n2 = n;
                } else if (o2.getClass() == Integer.class) {
                    n2 = n + 1;
                    this.mPath[n] = "x" + o2.toString();
                } else {
                    n2 = n + 1;
                    this.mPath[n] = o2.toString();
                }
                i2++;
                n = n2;
            }
            this.mHashCode = Arrays.hashCode(this.mPath);
        }

        public int hashCode() {
            return this.mHashCode;
        }

        public boolean equals(Object thatObject) {
            if (thatObject == this) {
                return true;
            }
            if (thatObject instanceof ConstPathAccessor) {
                return Arrays.equals(this.mPath, ((PathAccessor) thatObject).mPath);
            }
            return false;
        }

        private String[] getPath() {
            return this.mPath;
        }
    }

    /* renamed from: com.android.server.wifi.hotspot2.OMADMAdapter.1 */
    class AnonymousClass1 extends PathAccessor {
        AnonymousClass1(Object... $anonymous0) {
            super($anonymous0);
        }

        protected String getValue() {
            return OMADMAdapter.this.getMAC();
        }
    }

    private static class ConstPathAccessor<T> extends PathAccessor {
        private final T mValue;

        protected ConstPathAccessor(T value, Object... path) {
            super(path);
            this.mValue = value;
        }

        protected Object getValue() {
            return this.mValue;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.OMADMAdapter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.OMADMAdapter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.OMADMAdapter.<clinit>():void");
    }

    public static OMADMAdapter getInstance(Context context) {
        OMADMAdapter oMADMAdapter;
        synchronized (OMADMAdapter.class) {
            if (sInstance == null) {
                sInstance = new OMADMAdapter(context);
            }
            oMADMAdapter = sInstance;
        }
        return oMADMAdapter;
    }

    private OMADMAdapter(Context context) {
        String strDevId;
        this.mContext = context;
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        String simOperator = tm.getSimOperator();
        this.mImsi = tm.getSubscriberId();
        this.mImei = tm.getImei();
        if ("310120".equals(simOperator) || (this.mImsi != null && this.mImsi.startsWith("310120"))) {
            strDevId = tm.getDeviceId().toUpperCase(Locale.US);
            if (strDevId == null || strDevId.length() < IMEI_Length) {
                Log.w(Utils.hs2LogTag(getClass()), "MEID cannot be extracted from DeviceId " + strDevId);
            } else {
                strDevId = strDevId.substring(0, IMEI_Length);
            }
        } else {
            if (isPhoneTypeLTE()) {
                strDevId = this.mImei;
            } else {
                strDevId = tm.getDeviceId();
            }
            if (strDevId == null) {
                strDevId = "unknown";
            }
            strDevId = strDevId.toUpperCase(Locale.US);
            if (!isPhoneTypeLTE()) {
                strDevId = strDevId.substring(0, IMEI_Length);
            }
        }
        this.mDevID = strDevId;
        this.mDevInfo = new ArrayList();
        this.mDevInfo.add(new ConstPathAccessor(strDevId, "DevInfo", "DevID"));
        this.mDevInfo.add(new ConstPathAccessor(getProperty(context, "Man", "ro.product.manufacturer", "unknown"), "DevInfo", "Man"));
        this.mDevInfo.add(new ConstPathAccessor(getProperty(context, "Mod", "ro.product.model", "generic"), "DevInfo", "Mod"));
        this.mDevInfo.add(new ConstPathAccessor(getLocale(context), "DevInfo", "Lang"));
        this.mDevInfo.add(new ConstPathAccessor(OMAConstants.OMAVersion, "DevInfo", "DmV"));
        this.mDevDetail = new ArrayList();
        this.mDevDetail.add(new ConstPathAccessor(getDeviceType(), "DevDetail", "DevType"));
        this.mDevDetail.add(new ConstPathAccessor(SystemProperties.get("ro.product.brand"), "DevDetail", "OEM"));
        this.mDevDetail.add(new ConstPathAccessor(getVersion(context, false), "DevDetail", "FwV"));
        this.mDevDetail.add(new ConstPathAccessor(getVersion(context, true), "DevDetail", "SwV"));
        this.mDevDetail.add(new ConstPathAccessor(getHwV(), "DevDetail", "HwV"));
        this.mDevDetail.add(new ConstPathAccessor("TRUE", "DevDetail", "LrgObj"));
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(32), "DevDetail", PasspointManagementObjectManager.TAG_URI, "MaxDepth"));
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(2048), "DevDetail", PasspointManagementObjectManager.TAG_URI, "MaxTotLen"));
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(64), "DevDetail", PasspointManagementObjectManager.TAG_URI, "MaxSegLen"));
        AtomicInteger index = new AtomicInteger(1);
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(21), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_EAPType));
        this.mDevDetail.add(new ConstPathAccessor(NonEAPInnerAuth.mapInnerType(NonEAPType.MSCHAPv2), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_InnerMethod));
        index.incrementAndGet();
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(21), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_EAPType));
        this.mDevDetail.add(new ConstPathAccessor(NonEAPInnerAuth.mapInnerType(NonEAPType.PAP), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_InnerMethod));
        index.incrementAndGet();
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(21), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_EAPType));
        this.mDevDetail.add(new ConstPathAccessor(NonEAPInnerAuth.mapInnerType(NonEAPType.MSCHAP), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_InnerMethod));
        index.incrementAndGet();
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(13), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_EAPType));
        index.incrementAndGet();
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(23), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_EAPType));
        index.incrementAndGet();
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(50), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_EAPType));
        index.incrementAndGet();
        this.mDevDetail.add(new ConstPathAccessor(Integer.valueOf(18), ExtWiFiPath, "EAPMethodList", index, PasspointManagementObjectManager.TAG_EAPType));
        this.mDevDetail.add(new ConstPathAccessor("FALSE", ExtWiFiPath, "ManufacturingCertificate"));
        this.mDevDetail.add(new ConstPathAccessor(this.mImsi, ExtWiFiPath, PasspointManagementObjectManager.TAG_IMSI));
        this.mDevDetail.add(new ConstPathAccessor(this.mImei, ExtWiFiPath, "IMEI_MEID"));
        this.mDevDetail.add(new AnonymousClass1(ExtWiFiPath, "Wi-FiMACAddress"));
    }

    private static void buildNode(PathAccessor pathAccessor, int depth, OMAConstructed parent) throws IOException {
        String[] path = pathAccessor.getPath();
        String name = path[depth];
        if (depth < path.length - 1) {
            OMAConstructed node = (OMAConstructed) parent.getChild(name);
            if (node == null) {
                node = (OMAConstructed) parent.addChild(name, (String) RTProps.get(name), null, null);
            }
            buildNode(pathAccessor, depth + 1, node);
        } else if (pathAccessor.getValue() != null) {
            parent.addChild(name, null, pathAccessor.getValue().toString(), null);
        }
    }

    public String getMAC() {
        if (((WifiManager) this.mContext.getSystemService("wifi")) == null) {
            return null;
        }
        return String.format("%012x", new Object[]{Long.valueOf(Utils.parseMac(((WifiManager) this.mContext.getSystemService("wifi")).getConnectionInfo().getMacAddress()))});
    }

    public String getImei() {
        return this.mImei;
    }

    public byte[] getMeid() {
        return Arrays.copyOf(this.mImei.getBytes(StandardCharsets.ISO_8859_1), IMEI_Length);
    }

    public String getDevID() {
        return this.mDevID;
    }

    public MOTree getMO(String urn) {
        try {
            OMAConstructed root;
            if (urn.equals(OMAConstants.DevInfoURN)) {
                if (this.mDevInfoTree == null) {
                    root = new OMAConstructed(null, "DevInfo", urn, new String[0]);
                    for (PathAccessor pathAccessor : this.mDevInfo) {
                        buildNode(pathAccessor, 1, root);
                    }
                    this.mDevInfoTree = MOTree.buildMgmtTree(OMAConstants.DevInfoURN, OMAConstants.OMAVersion, root);
                }
                return this.mDevInfoTree;
            } else if (urn.equals(OMAConstants.DevDetailURN)) {
                if (this.mDevDetailTree == null) {
                    root = new OMAConstructed(null, "DevDetail", urn, new String[0]);
                    for (PathAccessor pathAccessor2 : this.mDevDetail) {
                        buildNode(pathAccessor2, 1, root);
                    }
                    this.mDevDetailTree = MOTree.buildMgmtTree(OMAConstants.DevDetailURN, OMAConstants.OMAVersion, root);
                }
                return this.mDevDetailTree;
            } else {
                throw new IllegalArgumentException(urn);
            }
        } catch (IOException ioe) {
            Log.e(Utils.hs2LogTag(getClass()), "Caught exception building OMA Tree: " + ioe, ioe);
            return null;
        }
    }

    private static boolean isPhoneTypeLTE() {
        return true;
    }

    private static String getHwV() {
        try {
            return SystemProperties.get("ro.hardware", "Unknown") + "." + SystemProperties.get("ro.revision", "Unknown");
        } catch (RuntimeException e) {
            return "Unknown";
        }
    }

    private static String getDeviceType() {
        String devicetype = SystemProperties.get("ro.build.characteristics");
        if (TextUtils.isEmpty(devicetype) || !devicetype.equals("tablet")) {
            return "phone";
        }
        return devicetype;
    }

    private static String getVersion(Context context, boolean swv) {
        try {
            if (!isSprint(context) && swv) {
                return "Android " + SystemProperties.get("ro.build.version.release");
            }
            String version = SystemProperties.get("ro.build.version.full");
            if (version == null || version.equals("")) {
                return SystemProperties.get("ro.build.id", null) + "~" + SystemProperties.get("ro.build.config.version", null) + "~" + SystemProperties.get("gsm.version.baseband", null) + "~" + SystemProperties.get("ro.gsm.flexversion", null);
            }
            return version;
        } catch (RuntimeException e) {
            return "Unknown";
        }
    }

    private static boolean isSprint(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        String simOperator = tm.getSimOperator();
        String imsi = tm.getSubscriberId();
        if ("310120".equals(simOperator) || (imsi != null && imsi.startsWith("310120"))) {
            return true;
        }
        return false;
    }

    private static String getLocale(Context context) {
        String strLang = readValueFromFile(context, "Lang");
        if (strLang == null) {
            return Locale.getDefault().toString();
        }
        return strLang;
    }

    private static String getProperty(Context context, String key, String propKey, String dflt) {
        String strMan = readValueFromFile(context, key);
        if (strMan == null) {
            return SystemProperties.get(propKey, dflt);
        }
        return strMan;
    }

    private static String readValueFromFile(Context context, String propName) {
        SharedPreferences prefs = context.getSharedPreferences("dmconfig", 0);
        if (!prefs.contains(propName)) {
            return null;
        }
        String ret = prefs.getString(propName, "");
        if (ret.length() == 0) {
            return null;
        }
        return ret;
    }
}
