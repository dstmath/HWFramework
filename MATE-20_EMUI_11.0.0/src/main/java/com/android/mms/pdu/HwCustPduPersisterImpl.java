package com.android.mms.pdu;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.mms.pdu.EncodedStringValue;
import java.util.ArrayList;

public class HwCustPduPersisterImpl extends HwCustPduPersister {
    private static final String LOCAL_NUMBER_FROM_DB = "localNumberFromDb";
    private static final String TAG = "HwCustPduPersisterImpl";
    private static boolean excludeShortCode = SystemProperties.getBoolean("ro.config.exclude_short_code", false);

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
            if (TextUtils.isEmpty(number) || !number.contains("@") || !number.contains(".")) {
                String number2 = extractDigit(number);
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
        ArrayList<EncodedStringValue> list = new ArrayList<>();
        if (toNumbers != null) {
            for (EncodedStringValue v : toNumbers) {
                if (v != null) {
                    list.add(v);
                }
            }
        }
        if (ccNumbers != null) {
            for (EncodedStringValue v2 : ccNumbers) {
                if (v2 != null) {
                    list.add(v2);
                }
            }
        }
        return (EncodedStringValue[]) list.toArray(new EncodedStringValue[list.size()]);
    }

    public String getCustLocalNumberFromDB(int subId, Context context, String number) {
        if (context == null || !SubscriptionManager.isUsableSubIdValue(subId)) {
            return number;
        }
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.Secure.getString(contentResolver, "localNumberFromDb_" + subId);
    }
}
