package com.android.server.wifi.hotspot2;

import android.util.Base64;
import android.util.Log;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.anqp.ANQPElement;
import com.android.server.wifi.anqp.ANQPFactory;
import com.android.server.wifi.anqp.CivicLocationElement;
import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.anqp.eap.AuthParam;
import com.android.server.wifi.anqp.eap.EAP.AuthInfoID;
import com.android.server.wifi.anqp.eap.EAP.EAPMethodID;
import com.android.server.wifi.anqp.eap.EAPMethod;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import com.android.server.wifi.hotspot2.pps.Credential;
import com.google.protobuf.nano.Extension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplicantBridge {
    private static final /* synthetic */ int[] -com-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues = null;
    private static final int IconChunkSize = 1400;
    private static final String[] TestStrings = null;
    private static final Map<Character, Integer> sMappings = null;
    private static final Map<String, ANQPElementType> sWpsNames = null;
    private final SupplicantBridgeCallbacks mCallbacks;
    private final Map<Long, ScanDetail> mRequestMap;
    private final WifiNative mSupplicantHook;

    public interface SupplicantBridgeCallbacks {
        void notifyANQPResponse(ScanDetail scanDetail, Map<ANQPElementType, ANQPElement> map);

        void notifyIconFailed(long j);
    }

    private static class CharIterator {
        private int mHex;
        private int mPosition;
        private final String mString;

        private CharIterator(String s) {
            this.mString = s;
        }

        private boolean hasNext() {
            return this.mPosition < this.mString.length();
        }

        private char next() {
            String str = this.mString;
            int i = this.mPosition;
            this.mPosition = i + 1;
            return str.charAt(i);
        }

        private boolean hasDoubleHex() {
            if (this.mString.length() - this.mPosition < 2) {
                return false;
            }
            int nh = Utils.fromHex(this.mString.charAt(this.mPosition), true);
            if (nh < 0) {
                return false;
            }
            int nl = Utils.fromHex(this.mString.charAt(this.mPosition + 1), true);
            if (nl < 0) {
                return false;
            }
            this.mPosition += 2;
            this.mHex = (nh << 4) | nl;
            return true;
        }

        private int nextDoubleHex() {
            return this.mHex;
        }
    }

    private static /* synthetic */ int[] -getcom-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues() {
        if (-com-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues != null) {
            return -com-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues;
        }
        int[] iArr = new int[AuthInfoID.values().length];
        try {
            iArr[AuthInfoID.CredentialType.ordinal()] = 8;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AuthInfoID.ExpandedEAPMethod.ordinal()] = 9;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AuthInfoID.ExpandedInnerEAPMethod.ordinal()] = 10;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AuthInfoID.InnerAuthEAPMethodType.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AuthInfoID.NonEAPInnerAuthType.ordinal()] = 2;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AuthInfoID.TunneledEAPMethodCredType.ordinal()] = 11;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[AuthInfoID.Undefined.ordinal()] = 12;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[AuthInfoID.VendorSpecific.ordinal()] = 13;
        } catch (NoSuchFieldError e8) {
        }
        -com-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues() {
        if (-com-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues != null) {
            return -com-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues;
        }
        int[] iArr = new int[EAPMethodID.values().length];
        try {
            iArr[EAPMethodID.EAP_3Com.ordinal()] = 8;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[EAPMethodID.EAP_AKA.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[EAPMethodID.EAP_AKAPrim.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[EAPMethodID.EAP_ActiontecWireless.ordinal()] = 9;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[EAPMethodID.EAP_EKE.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[EAPMethodID.EAP_FAST.ordinal()] = 11;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[EAPMethodID.EAP_GPSK.ordinal()] = 12;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[EAPMethodID.EAP_HTTPDigest.ordinal()] = 13;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[EAPMethodID.EAP_IKEv2.ordinal()] = 14;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[EAPMethodID.EAP_KEA.ordinal()] = 15;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[EAPMethodID.EAP_KEA_VALIDATE.ordinal()] = 16;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[EAPMethodID.EAP_LEAP.ordinal()] = 17;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[EAPMethodID.EAP_Link.ordinal()] = 18;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[EAPMethodID.EAP_MD5.ordinal()] = 19;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[EAPMethodID.EAP_MOBAC.ordinal()] = 20;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[EAPMethodID.EAP_MSCHAPv2.ordinal()] = 21;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[EAPMethodID.EAP_OTP.ordinal()] = 22;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[EAPMethodID.EAP_PAX.ordinal()] = 23;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[EAPMethodID.EAP_PEAP.ordinal()] = 24;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[EAPMethodID.EAP_POTP.ordinal()] = 25;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[EAPMethodID.EAP_PSK.ordinal()] = 26;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[EAPMethodID.EAP_PWD.ordinal()] = 27;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[EAPMethodID.EAP_RSA.ordinal()] = 28;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[EAPMethodID.EAP_SAKE.ordinal()] = 29;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[EAPMethodID.EAP_SIM.ordinal()] = 3;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[EAPMethodID.EAP_SPEKE.ordinal()] = 30;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[EAPMethodID.EAP_TEAP.ordinal()] = 31;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[EAPMethodID.EAP_TLS.ordinal()] = 4;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[EAPMethodID.EAP_TTLS.ordinal()] = 5;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[EAPMethodID.EAP_ZLXEAP.ordinal()] = 32;
        } catch (NoSuchFieldError e30) {
        }
        -com-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.SupplicantBridge.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.SupplicantBridge.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.SupplicantBridge.<clinit>():void");
    }

    public static boolean isAnqpAttribute(String line) {
        int split = line.indexOf(61);
        if (split >= 0) {
            return sWpsNames.containsKey(line.substring(0, split));
        }
        return false;
    }

    public SupplicantBridge(WifiNative supplicantHook, SupplicantBridgeCallbacks callbacks) {
        this.mRequestMap = new HashMap();
        this.mSupplicantHook = supplicantHook;
        this.mCallbacks = callbacks;
    }

    public static Map<ANQPElementType, ANQPElement> parseANQPLines(List<String> lines) {
        if (lines == null) {
            return null;
        }
        Map<ANQPElementType, ANQPElement> elements = new HashMap(lines.size());
        for (String line : lines) {
            try {
                ANQPElement element = buildElement(line);
                if (element != null) {
                    elements.put(element.getID(), element);
                }
            } catch (ProtocolException pe) {
                Log.e(Utils.hs2LogTag(SupplicantBridge.class), "Failed to parse ANQP: " + pe);
            }
        }
        return elements;
    }

    public boolean startANQP(ScanDetail scanDetail, List<ANQPElementType> elements) {
        String anqpGet = buildWPSQueryRequest(scanDetail.getNetworkDetail(), elements);
        if (anqpGet == null) {
            return false;
        }
        synchronized (this.mRequestMap) {
            this.mRequestMap.put(Long.valueOf(scanDetail.getNetworkDetail().getBSSID()), scanDetail);
        }
        String result = this.mSupplicantHook.doCustomSupplicantCommand(anqpGet);
        if (result == null || !result.startsWith("OK")) {
            Log.d(Utils.hs2LogTag(getClass()), "ANQP failed on " + scanDetail + ": " + result);
            return false;
        }
        Log.d(Utils.hs2LogTag(getClass()), "ANQP initiated on " + scanDetail + " (" + anqpGet + ")");
        return true;
    }

    public boolean doIconQuery(long bssid, String fileName) {
        String result = this.mSupplicantHook.doCustomSupplicantCommand("REQ_HS20_ICON " + Utils.macToString(bssid) + " " + fileName);
        return result != null ? result.startsWith("OK") : false;
    }

    public byte[] retrieveIcon(IconEvent iconEvent) throws IOException {
        String response;
        String result;
        byte[] iconData = new byte[iconEvent.getSize()];
        int offset = 0;
        while (offset < iconEvent.getSize()) {
            String command;
            try {
                int size = Math.min(iconEvent.getSize() - offset, IconChunkSize);
                command = String.format("GET_HS20_ICON %s %s %d %d", new Object[]{Utils.macToString(iconEvent.getBSSID()), iconEvent.getFileName(), Integer.valueOf(offset), Integer.valueOf(size)});
                Log.d(Utils.hs2LogTag(getClass()), "Issuing '" + command + "'");
                response = this.mSupplicantHook.doCustomSupplicantCommand(command);
                if (response == null) {
                    throw new IOException("No icon data returned");
                }
                byte[] fragment = Base64.decode(response, 0);
                if (fragment.length == 0) {
                    throw new IOException("Null data for '" + command + "': " + response);
                } else if (fragment.length + offset > iconData.length) {
                    throw new IOException("Icon chunk exceeds image size");
                } else {
                    System.arraycopy(fragment, 0, iconData, offset, fragment.length);
                    offset += fragment.length;
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("Failed to parse response to '" + command + "': " + response);
            } catch (Throwable th) {
                Log.d(Utils.hs2LogTag(getClass()), "Deleting icon for " + iconEvent);
                result = this.mSupplicantHook.doCustomSupplicantCommand("DEL_HS20_ICON " + Utils.macToString(iconEvent.getBSSID()) + " " + iconEvent.getFileName());
            }
        }
        if (offset != iconEvent.getSize()) {
            Log.w(Utils.hs2LogTag(getClass()), "Partial icon data: " + offset + ", expected " + iconEvent.getSize());
        }
        Log.d(Utils.hs2LogTag(getClass()), "Deleting icon for " + iconEvent);
        result = this.mSupplicantHook.doCustomSupplicantCommand("DEL_HS20_ICON " + Utils.macToString(iconEvent.getBSSID()) + " " + iconEvent.getFileName());
        return iconData;
    }

    public void notifyANQPDone(Long bssid, boolean success) {
        synchronized (this.mRequestMap) {
            ScanDetail scanDetail = (ScanDetail) this.mRequestMap.remove(bssid);
        }
        if (scanDetail == null) {
            if (!success) {
                this.mCallbacks.notifyIconFailed(bssid.longValue());
            }
            return;
        }
        String bssData = this.mSupplicantHook.scanResult(scanDetail.getBSSIDString());
        try {
            Map<ANQPElementType, ANQPElement> elements = parseWPSData(bssData);
            String hs2LogTag = Utils.hs2LogTag(getClass());
            String str = "%s ANQP response for %012x: %s";
            Object[] objArr = new Object[3];
            objArr[0] = success ? "successful" : "failed";
            objArr[1] = bssid;
            objArr[2] = elements;
            Log.d(hs2LogTag, String.format(str, objArr));
            SupplicantBridgeCallbacks supplicantBridgeCallbacks = this.mCallbacks;
            if (!success) {
                elements = null;
            }
            supplicantBridgeCallbacks.notifyANQPResponse(scanDetail, elements);
        } catch (IOException ioe) {
            Log.e(Utils.hs2LogTag(getClass()), "Failed to parse ANQP: " + ioe.toString() + ": " + bssData);
        } catch (RuntimeException rte) {
            Log.e(Utils.hs2LogTag(getClass()), "Failed to parse ANQP: " + rte.toString() + ": " + bssData, rte);
        }
        this.mCallbacks.notifyANQPResponse(scanDetail, null);
    }

    private static String escapeSSID(NetworkDetail networkDetail) {
        return escapeString(networkDetail.getSSID(), networkDetail.isSSID_UTF8());
    }

    private static String escapeString(String s, boolean utf8) {
        boolean asciiOnly = true;
        for (int n = 0; n < s.length(); n++) {
            if (s.charAt(n) > '\u007f') {
                asciiOnly = false;
                break;
            }
        }
        if (asciiOnly) {
            return '\"' + s + '\"';
        }
        byte[] octets = s.getBytes(utf8 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1);
        StringBuilder sb = new StringBuilder();
        int length = octets.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", new Object[]{Integer.valueOf(octets[i] & Constants.BYTE_MASK)}));
        }
        return sb.toString();
    }

    private static String buildWPSQueryRequest(NetworkDetail networkDetail, List<ANQPElementType> querySet) {
        boolean baseANQPElements = Constants.hasBaseANQPElements(querySet);
        StringBuilder sb = new StringBuilder();
        if (baseANQPElements) {
            sb.append("ANQP_GET ");
        } else {
            sb.append("HS20_ANQP_GET ");
        }
        sb.append(networkDetail.getBSSIDString()).append(' ');
        boolean first = true;
        for (ANQPElementType elementType : querySet) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            Integer id = Constants.getANQPElementID(elementType);
            if (id != null) {
                sb.append(id);
            } else {
                id = Constants.getHS20ElementID(elementType);
                if (baseANQPElements) {
                    sb.append("hs20:");
                }
                sb.append(id);
            }
        }
        return sb.toString();
    }

    private static List<String> getWPSNetCommands(String netID, NetworkDetail networkDetail, Credential credential) {
        List<String> commands = new ArrayList();
        EAPMethod eapMethod = credential.getEAPMethod();
        commands.add(String.format("SET_NETWORK %s key_mgmt WPA-EAP", new Object[]{netID}));
        commands.add(String.format("SET_NETWORK %s ssid %s", new Object[]{netID, escapeSSID(networkDetail)}));
        commands.add(String.format("SET_NETWORK %s bssid %s", new Object[]{netID, networkDetail.getBSSIDString()}));
        commands.add(String.format("SET_NETWORK %s eap %s", new Object[]{netID, mapEAPMethodName(eapMethod.getEAPMethodID())}));
        AuthParam authParam = credential.getEAPMethod().getAuthParam();
        if (authParam == null) {
            return null;
        }
        switch (-getcom-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues()[authParam.getAuthInfoID().ordinal()]) {
            case Extension.TYPE_DOUBLE /*1*/:
            case Extension.TYPE_FLOAT /*2*/:
                commands.add(String.format("SET_NETWORK %s identity %s", new Object[]{netID, escapeString(credential.getUserName(), true)}));
                commands.add(String.format("SET_NETWORK %s password %s", new Object[]{netID, escapeString(credential.getPassword(), true)}));
                commands.add(String.format("SET_NETWORK %s anonymous_identity \"anonymous\"", new Object[]{netID}));
                commands.add(String.format("SET_NETWORK %s priority 0", new Object[]{netID}));
                commands.add(String.format("ENABLE_NETWORK %s", new Object[]{netID}));
                commands.add(String.format("SAVE_CONFIG", new Object[0]));
                return commands;
            default:
                return null;
        }
    }

    private static Map<ANQPElementType, ANQPElement> parseWPSData(String bssInfo) throws IOException {
        Map<ANQPElementType, ANQPElement> elements = new HashMap();
        if (bssInfo == null) {
            return elements;
        }
        BufferedReader lineReader = new BufferedReader(new StringReader(bssInfo));
        while (true) {
            String line = lineReader.readLine();
            if (line == null) {
                return elements;
            }
            ANQPElement element = buildElement(line);
            if (element != null) {
                elements.put(element.getID(), element);
            }
        }
    }

    private static ANQPElement buildElement(String text) throws ProtocolException {
        int separator = text.indexOf(61);
        if (separator < 0) {
            return null;
        }
        ANQPElementType elementType = (ANQPElementType) sWpsNames.get(text.substring(0, separator));
        if (elementType == null) {
            return null;
        }
        try {
            ANQPElement buildElement;
            byte[] payload = Utils.hexToBytes(text.substring(separator + 1));
            if (Constants.getANQPElementID(elementType) != null) {
                buildElement = ANQPFactory.buildElement(ByteBuffer.wrap(payload), elementType, payload.length);
            } else {
                buildElement = ANQPFactory.buildHS20Element(elementType, ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN));
            }
            return buildElement;
        } catch (NumberFormatException e) {
            Log.e(Utils.hs2LogTag(SupplicantBridge.class), "Failed to parse hex string");
            return null;
        }
    }

    private static String mapEAPMethodName(EAPMethodID eapMethodID) {
        switch (-getcom-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues()[eapMethodID.ordinal()]) {
            case Extension.TYPE_DOUBLE /*1*/:
                return "AKA";
            case Extension.TYPE_FLOAT /*2*/:
                return "AKA'";
            case Extension.TYPE_INT64 /*3*/:
                return PasspointManagementObjectManager.TAG_SIM;
            case Extension.TYPE_UINT64 /*4*/:
                return "TLS";
            case Extension.TYPE_INT32 /*5*/:
                return "TTLS";
            default:
                throw new IllegalArgumentException("No mapping for " + eapMethodID);
        }
    }

    public static String unescapeSSID(String ssid) {
        CharIterator chars = new CharIterator(null);
        byte[] octets = new byte[ssid.length()];
        int bo = 0;
        while (chars.hasNext()) {
            char ch = chars.next();
            int bo2;
            if (ch == '\\' && chars.hasNext()) {
                char suffix = chars.next();
                Integer mapped = (Integer) sMappings.get(Character.valueOf(suffix));
                if (mapped != null) {
                    bo2 = bo + 1;
                    octets[bo] = mapped.byteValue();
                    bo = bo2;
                } else if (suffix == 'x' && chars.hasDoubleHex()) {
                    bo2 = bo + 1;
                    octets[bo] = (byte) chars.nextDoubleHex();
                    bo = bo2;
                } else {
                    bo2 = bo + 1;
                    octets[bo] = (byte) 92;
                    bo = bo2 + 1;
                    octets[bo2] = (byte) suffix;
                }
            } else {
                bo2 = bo + 1;
                octets[bo] = (byte) ch;
                bo = bo2;
            }
        }
        boolean asciiOnly = true;
        for (byte b : octets) {
            if ((b & CivicLocationElement.SCRIPT) != 0) {
                asciiOnly = false;
                break;
            }
        }
        if (asciiOnly) {
            return new String(octets, 0, bo, StandardCharsets.UTF_8);
        }
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(octets, 0, bo)).toString();
        } catch (CharacterCodingException e) {
            return new String(octets, 0, bo, StandardCharsets.ISO_8859_1);
        }
    }

    public static void main(String[] args) {
        for (String string : TestStrings) {
            System.out.println(unescapeSSID(string));
        }
    }
}
