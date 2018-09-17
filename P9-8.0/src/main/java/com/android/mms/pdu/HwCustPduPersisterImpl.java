package com.android.mms.pdu;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.mms.pdu.EncodedStringValue;
import java.util.ArrayList;

public class HwCustPduPersisterImpl extends HwCustPduPersister {
    private static final String TAG = "HwCustPduPersisterImpl";
    private static boolean excludeShortCode = SystemProperties.getBoolean("ro.config.exclude_short_code", false);

    public boolean isShortCodeFeatureEnabled() {
        return excludeShortCode;
    }

    private boolean hasShortCode(EncodedStringValue[] enNumbers) {
        if (!excludeShortCode || enNumbers == null || enNumbers.length < 2) {
            return false;
        }
        int i;
        String[] numbers = new String[enNumbers.length];
        for (i = 0; i < enNumbers.length; i++) {
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
        int numLength = number.length();
        for (int i = 0; i < numLength; i++) {
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
        EncodedStringValue v;
        int i = 0;
        ArrayList<EncodedStringValue> list = new ArrayList();
        if (toNumbers != null) {
            for (EncodedStringValue v2 : toNumbers) {
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
