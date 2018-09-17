package com.android.server.wifi.anqp.eap;

import java.util.Map;

public abstract class EAP {
    public static final int CredentialType = 5;
    public static final int EAP_3Com = 24;
    public static final int EAP_AKA = 23;
    public static final int EAP_AKAPrim = 50;
    public static final int EAP_ActiontecWireless = 35;
    public static final int EAP_EKE = 53;
    public static final int EAP_FAST = 43;
    public static final int EAP_GPSK = 51;
    public static final int EAP_HTTPDigest = 38;
    public static final int EAP_IKEv2 = 49;
    public static final int EAP_KEA = 11;
    public static final int EAP_KEA_VALIDATE = 12;
    public static final int EAP_LEAP = 17;
    public static final int EAP_Link = 45;
    public static final int EAP_MD5 = 4;
    public static final int EAP_MOBAC = 42;
    public static final int EAP_MSCHAPv2 = 26;
    public static final int EAP_OTP = 5;
    public static final int EAP_PAX = 46;
    public static final int EAP_PEAP = 29;
    public static final int EAP_POTP = 32;
    public static final int EAP_PSK = 47;
    public static final int EAP_PWD = 52;
    public static final int EAP_RSA = 9;
    public static final int EAP_SAKE = 48;
    public static final int EAP_SIM = 18;
    public static final int EAP_SPEKE = 41;
    public static final int EAP_TEAP = 55;
    public static final int EAP_TLS = 13;
    public static final int EAP_TTLS = 21;
    public static final int EAP_ZLXEAP = 44;
    public static final int ExpandedEAPMethod = 1;
    public static final int ExpandedInnerEAPMethod = 4;
    public static final int InnerAuthEAPMethodType = 3;
    public static final int NonEAPInnerAuthType = 2;
    public static final int TunneledEAPMethodCredType = 6;
    public static final int VendorSpecific = 221;
    private static final Map<Integer, AuthInfoID> sAuthIds = null;
    private static final Map<Integer, EAPMethodID> sEapIds = null;
    private static final Map<EAPMethodID, Integer> sRevEapIds = null;

    public enum AuthInfoID {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.eap.EAP.AuthInfoID.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.eap.EAP.AuthInfoID.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.eap.EAP.AuthInfoID.<clinit>():void");
        }
    }

    public enum EAPMethodID {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.eap.EAP.EAPMethodID.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.eap.EAP.EAPMethodID.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.eap.EAP.EAPMethodID.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.eap.EAP.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.eap.EAP.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.eap.EAP.<clinit>():void");
    }

    public EAP() {
    }

    public static EAPMethodID mapEAPMethod(int methodID) {
        return (EAPMethodID) sEapIds.get(Integer.valueOf(methodID));
    }

    public static Integer mapEAPMethod(EAPMethodID methodID) {
        return (Integer) sRevEapIds.get(methodID);
    }

    public static AuthInfoID mapAuthMethod(int methodID) {
        return (AuthInfoID) sAuthIds.get(Integer.valueOf(methodID));
    }
}
