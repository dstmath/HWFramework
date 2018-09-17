package android.telephony;

import android.content.Context;

public class HwCarrierConfigManager {
    public static final int HD_ICON_MASK_CALL_LOG = 12;
    public static final int HD_ICON_MASK_DIALER = 192;
    public static final int HD_ICON_MASK_INCALL_UI = 3;
    public static final int HD_ICON_MASK_STATUS_BAR = 48;
    public static final int HD_ICON_SHOW_RULE_IMS_REGISTRATION = 0;
    public static final int HD_ICON_SHOW_RULE_SPEECH_CODEC = 1;
    private static HwCarrierConfigManager sInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.HwCarrierConfigManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.HwCarrierConfigManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.HwCarrierConfigManager.<clinit>():void");
    }

    public static HwCarrierConfigManager getDefault() {
        return sInstance;
    }

    public int getCarrierVolteHDIconFlag(Context context, int subId) {
        return HwCarrierConfigManagerInner.getDefault().getCarrierVolteHDIconFlag(context, subId);
    }

    public int getShowHDIconRule(Context context, int subId) {
        return HwCarrierConfigManagerInner.getDefault().getShowHDIconRule(context, subId);
    }

    public boolean needShowHDIcon(int flag, int mask) {
        return HwCarrierConfigManagerInner.getDefault().needShowHDIcon(flag, mask);
    }

    public int getVolteIconRule(Context context, int subId, int mask) {
        return HwCarrierConfigManagerInner.getDefault().getVolteIconRule(context, subId, mask);
    }
}
