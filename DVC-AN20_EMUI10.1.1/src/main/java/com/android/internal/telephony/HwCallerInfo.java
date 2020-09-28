package com.android.internal.telephony;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.ContactsContractEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CallerInfoExt;
import huawei.android.telephony.TelephonyInterfacesHW;

public class HwCallerInfo {
    private static final String TAG = "CallerInfo";
    private static final boolean VDBG = RlogEx.isLoggable(TAG, 2);

    public static CallerInfoExt getCallerInfo(Context context, Uri contactRef, Cursor cursor, String compNum) {
        if (SystemPropertiesEx.getInt("ro.config.hwft_MatchNum", 0) < 7 && SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 0) == 0 && !isCaaSVoipNumber(compNum)) {
            return CallerInfoExt.getCallerInfo(context, contactRef, cursor);
        }
        CallerInfoExt info = new CallerInfoExt();
        info.setPhotoResource(0);
        info.setPhoneLabel((String) null);
        info.setNumberType(0);
        info.setNumberLabel((String) null);
        info.setCachedPhoto((Drawable) null);
        info.setCachedPhotoCurrent(false);
        info.setNeedUpdate(false);
        info.setContactRefUri(contactRef);
        info.setUserType(0);
        Log.v(TAG, "construct callerInfo from cursor");
        if (cursor == null) {
            return info;
        }
        try {
            try {
                int fixedIndex = ((TelephonyInterfacesHW) Class.forName("huawei.android.telephony.CallerInfoHW").newInstance()).getCallerIndex(cursor, compNum);
                if (-1 == fixedIndex) {
                    cursor.close();
                    return info;
                }
                if (cursor.moveToPosition(fixedIndex)) {
                    int columnIndex = cursor.getColumnIndex("display_name");
                    if (columnIndex != -1) {
                        info.setName(cursor.getString(columnIndex));
                        logd("info.name: " + info.getName());
                    }
                    int columnIndex2 = cursor.getColumnIndex("number");
                    if (columnIndex2 != -1) {
                        info.setPhoneNumber(cursor.getString(columnIndex2));
                        logd("info.phoneNumber: " + info.getPhoneNumber());
                    }
                    int columnIndex3 = cursor.getColumnIndex("normalized_number");
                    if (columnIndex3 != -1) {
                        info.setNormalizedNumber(cursor.getString(columnIndex3));
                    }
                    int columnIndex4 = cursor.getColumnIndex("label");
                    if (columnIndex4 != -1) {
                        int typeColumnIndex = cursor.getColumnIndex("type");
                        if (typeColumnIndex != -1) {
                            info.setNumberType(cursor.getInt(typeColumnIndex));
                            info.setNumberLabel(cursor.getString(columnIndex4));
                            info.setPhoneLabel(ContactsContractEx.CommonDataKindsEx.PhoneEx.getDisplayLabel(context, info.getNumberType(), info.getNumberLabel()).toString());
                        }
                    }
                    int columnIndex5 = -1;
                    String url = contactRef.toString();
                    if (url.startsWith("content://com.android.contacts/data/phones")) {
                        if (VDBG) {
                            Log.v(TAG, "URL path starts with 'data/phones' using RawContacts.CONTACT_ID");
                        }
                        columnIndex5 = cursor.getColumnIndex("contact_id");
                    } else if (url.startsWith("content://com.android.contacts/phone_lookup")) {
                        if (VDBG) {
                            Log.v(TAG, "URL path starts with 'phone_lookup' using PhoneLookup._ID");
                        }
                        columnIndex5 = cursor.getColumnIndex("_id");
                    } else {
                        Log.e(TAG, "Bad contact URL 'XXXXXX'");
                    }
                    if (columnIndex5 != -1) {
                        long contactId = cursor.getLong(columnIndex5);
                        info.setContactIdOrZero(contactId);
                        if (ContactsContract.Contacts.isEnterpriseContactId(contactId)) {
                            info.setUserType(1);
                        }
                    } else {
                        Log.e(TAG, "person_id column missing for " + contactRef);
                    }
                    int columnIndex6 = cursor.getColumnIndex("custom_ringtone");
                    if (columnIndex6 == -1 || cursor.getString(columnIndex6) == null) {
                        info.setContactRingtoneUri((Uri) null);
                    } else {
                        info.setContactRingtoneUri(Uri.parse(cursor.getString(columnIndex6)));
                    }
                    int columnIndex7 = cursor.getColumnIndex("send_to_voicemail");
                    info.setShouldSendToVoicemail(columnIndex7 != -1 && cursor.getInt(columnIndex7) == 1);
                    info.setContactExists(true);
                    int columnIndex8 = cursor.getColumnIndex("data7");
                    if (columnIndex8 != -1) {
                        info.setVoipDeviceType(cursor.getInt(columnIndex8));
                    }
                }
                cursor.close();
                info.setNeedUpdate(false);
                info.setName(normalize(info.getName()));
                info.setContactRefUri(contactRef);
                return info;
            } catch (InstantiationException e) {
                Log.e(TAG, "getCallerInfo InstantiationException");
                return CallerInfoExt.getCallerInfo(context, contactRef, cursor);
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "getCallerInfo IllegalAccessException");
                return CallerInfoExt.getCallerInfo(context, contactRef, cursor);
            }
        } catch (ClassNotFoundException e3) {
            Log.e(TAG, "getCallerInfo ClassNotFoundException");
            return CallerInfoExt.getCallerInfo(context, contactRef, cursor);
        }
    }

    private static String normalize(String s) {
        if (s == null || s.length() > 0) {
            return s;
        }
        return null;
    }

    private static void logd(String msg) {
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, msg);
        }
    }

    private static boolean isCaaSVoipNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        int[] voipNumberLength = {14, 15};
        boolean isRightHead = number.indexOf("+887") == 0 || number.indexOf("887") == 0;
        int length = number.length();
        boolean isRightLength = voipNumberLength[0] == length || voipNumberLength[1] == length;
        if (!isRightHead || !isRightLength) {
            return false;
        }
        return true;
    }
}
