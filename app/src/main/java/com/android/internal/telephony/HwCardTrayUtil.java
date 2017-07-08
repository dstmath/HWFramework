package com.android.internal.telephony;

import android.telephony.Rlog;
import java.io.FileInputStream;
import java.io.IOException;

public class HwCardTrayUtil {
    private static final int CARDTRAY_OUT_SLOT = 0;
    private static final String CARDTRAY_STATE_FILE = "/sys/kernel/sim/sim_hotplug_state";
    public static final boolean IS_SINGLE_CARD_TRAY = false;
    private static final String TAG = "HwCardTrayUtil";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwCardTrayUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwCardTrayUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwCardTrayUtil.<clinit>():void");
    }

    private static String getStateFile() {
        return CARDTRAY_STATE_FILE;
    }

    public static boolean isCardTrayOut(int SlotId) {
        Throwable th;
        boolean z = true;
        byte[] cardTrayState = new byte[4];
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(getStateFile());
            try {
                int length = fis2.read(cardTrayState, CARDTRAY_OUT_SLOT, 4);
                fis2.close();
                if (length < 4) {
                    loge("isCardTrayOut read byte fail.");
                    if (fis2 != null) {
                        try {
                            fis2.close();
                        } catch (IOException e) {
                            return IS_SINGLE_CARD_TRAY;
                        }
                    }
                    return IS_SINGLE_CARD_TRAY;
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e2) {
                        return IS_SINGLE_CARD_TRAY;
                    }
                }
                if (SlotId < 0 || SlotId > 1) {
                    return IS_SINGLE_CARD_TRAY;
                }
                if (cardTrayState[(SlotId * 2) + 1] != null) {
                    z = IS_SINGLE_CARD_TRAY;
                }
                return z;
            } catch (IOException e3) {
                fis = fis2;
                try {
                    loge("isCardTrayOut Exception");
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e4) {
                            return IS_SINGLE_CARD_TRAY;
                        }
                    }
                    return IS_SINGLE_CARD_TRAY;
                } catch (Throwable th2) {
                    th = th2;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e5) {
                            return IS_SINGLE_CARD_TRAY;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                if (fis != null) {
                    fis.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            loge("isCardTrayOut Exception");
            if (fis != null) {
                fis.close();
            }
            return IS_SINGLE_CARD_TRAY;
        }
    }

    private static void loge(String message) {
        Rlog.e(TAG, message);
    }
}
