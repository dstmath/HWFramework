package com.android.mms.pdu;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.mms.pdu.EncodedStringValue;
import java.util.ArrayList;

public class HwCustPduPersisterImpl extends HwCustPduPersister {
    private static final String TAG = "HwCustPduPersisterImpl";
    private static boolean excludeShortCode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.mms.pdu.HwCustPduPersisterImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.mms.pdu.HwCustPduPersisterImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.pdu.HwCustPduPersisterImpl.<clinit>():void");
    }

    public boolean isShortCodeFeatureEnabled() {
        return excludeShortCode;
    }

    private boolean hasShortCode(EncodedStringValue[] enNumbers) {
        if (!excludeShortCode || enNumbers == null || enNumbers.length < 2) {
            return false;
        }
        String[] numbers = new String[enNumbers.length];
        for (int i = 0; i < enNumbers.length; i++) {
            if (enNumbers[i] != null) {
                numbers[i] = enNumbers[i].getString();
            }
        }
        boolean hasShortCode = false;
        for (String number : numbers) {
            String number2;
            if (TextUtils.isEmpty(number2) || !number2.contains("@") || !number2.contains(".")) {
                number2 = extractDigit(number2);
                if (!TextUtils.isEmpty(number2)) {
                    if (number2.startsWith("+") || number2.startsWith("011")) {
                        if (number2.length() == 12 && number2.startsWith("+") && number2.substring(1).startsWith("1") && (number2.substring(2).startsWith("0") || number2.substring(2).startsWith("1"))) {
                            hasShortCode = true;
                        }
                        if (number2.length() == 14 && number2.startsWith("011") && number2.substring(3).startsWith("1") && (number2.substring(4).startsWith("0") || number2.substring(4).startsWith("1"))) {
                            hasShortCode = true;
                        }
                    } else if (number2.length() < 10) {
                        hasShortCode = true;
                    } else if (number2.length() == 10 && (number2.startsWith("0") || number2.startsWith("1"))) {
                        hasShortCode = true;
                    } else if (number2.length() == 11 && number2.startsWith("1") && (number2.charAt(1) == '0' || number2.charAt(1) == '1')) {
                        hasShortCode = true;
                    }
                }
            }
        }
        return hasShortCode;
    }

    public boolean hasShortCode(boolean isMMSEnable, String[] list, Context context, String toastString) {
        if (!isMMSEnable || list == null) {
            return false;
        }
        boolean bShortCodeStatus = hasShortCode(list);
        if (bShortCodeStatus) {
            Log.i(TAG, "One of the recepients has short code");
            Toast.makeText(context, toastString, 0).show();
        }
        return bShortCodeStatus;
    }

    private boolean hasShortCode(String[] list) {
        return hasShortCode(EncodedStringValue.encodeStrings(list));
    }

    private String extractDigit(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < number.length(); i++) {
            if (i == 0 && number.charAt(0) == '+') {
                sb.append(number.charAt(0));
            } else if (Character.isDigit(number.charAt(i))) {
                sb.append(number.charAt(i));
            }
        }
        return sb.toString();
    }

    public boolean hasShortCode(EncodedStringValue[] toNumbers, EncodedStringValue[] ccNumbers) {
        return hasShortCode(concatEncodedStringValue(toNumbers, ccNumbers));
    }

    private EncodedStringValue[] concatEncodedStringValue(EncodedStringValue[] toNumbers, EncodedStringValue[] ccNumbers) {
        int length;
        int i = 0;
        ArrayList<EncodedStringValue> list = new ArrayList();
        if (toNumbers != null) {
            for (EncodedStringValue v : toNumbers) {
                EncodedStringValue v2;
                if (v2 != null) {
                    list.add(v2);
                }
            }
        }
        if (ccNumbers != null) {
            length = ccNumbers.length;
            while (i < length) {
                v2 = ccNumbers[i];
                if (v2 != null) {
                    list.add(v2);
                }
                i++;
            }
        }
        return (EncodedStringValue[]) list.toArray(new EncodedStringValue[list.size()]);
    }
}
