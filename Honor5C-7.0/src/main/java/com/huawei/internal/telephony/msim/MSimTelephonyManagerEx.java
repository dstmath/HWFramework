package com.huawei.internal.telephony.msim;

import android.content.ContentResolver;
import android.provider.Settings.SettingNotFoundException;
import com.huawei.android.util.NoExtAPIException;

public class MSimTelephonyManagerEx {
    private static MSimTelephonyManagerEx sInstance;

    public enum MultiSimVariants {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.internal.telephony.msim.MSimTelephonyManagerEx.MultiSimVariants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.internal.telephony.msim.MSimTelephonyManagerEx.MultiSimVariants.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.internal.telephony.msim.MSimTelephonyManagerEx.MultiSimVariants.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.internal.telephony.msim.MSimTelephonyManagerEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.internal.telephony.msim.MSimTelephonyManagerEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.internal.telephony.msim.MSimTelephonyManagerEx.<clinit>():void");
    }

    private MSimTelephonyManagerEx() {
    }

    public static MSimTelephonyManagerEx getDefault() {
        return sInstance;
    }

    public MultiSimVariants getMultiSimConfiguration() {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getIntAtIndex(ContentResolver cr, String name, int index) throws SettingNotFoundException {
        return -1;
    }

    public static boolean putIntAtIndex(ContentResolver cr, String name, int index, int value) {
        return false;
    }

    public boolean isMultiSimEnabled() {
        throw new NoExtAPIException("method not supported.");
    }

    public int getMmsAutoSetDataSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public String getPesn(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getPreferredVoiceSubscription(ContentResolver contentResolver) {
        throw new NoExtAPIException("method not supported.");
    }

    public String getIccCardType(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }
}
