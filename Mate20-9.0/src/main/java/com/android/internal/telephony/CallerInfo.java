package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Country;
import android.location.CountryDetector;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import java.util.Locale;

public class CallerInfo {
    private static final String TAG = "CallerInfo";
    public static final long USER_TYPE_CURRENT = 0;
    public static final long USER_TYPE_WORK = 1;
    private static final boolean VDBG = Rlog.isLoggable(TAG, 2);
    public Drawable cachedPhoto;
    public Bitmap cachedPhotoIcon;
    public String cnapName;
    public Uri contactDisplayPhotoUri;
    public boolean contactExists;
    public long contactIdOrZero;
    public Uri contactRefUri;
    public Uri contactRingtoneUri;
    public String geoDescription;
    public boolean isCachedPhotoCurrent;
    public String lookupKey;
    private boolean mIsEmergency = false;
    private boolean mIsVoiceMail = false;
    public int mVoipDeviceType;
    public String name;
    public int namePresentation;
    public boolean needUpdate;
    public String normalizedNumber;
    public String numberLabel;
    public int numberPresentation;
    public int numberType;
    public String phoneLabel;
    public String phoneNumber;
    public int photoResource;
    public boolean shouldSendToVoicemail;
    public long userType = 0;

    public static CallerInfo getCallerInfo(Context context, Uri contactRef, Cursor cursor) {
        CallerInfo info = new CallerInfo();
        info.photoResource = 0;
        info.phoneLabel = null;
        info.numberType = 0;
        info.numberLabel = null;
        info.cachedPhoto = null;
        info.isCachedPhotoCurrent = false;
        info.contactExists = false;
        info.userType = 0;
        if (VDBG) {
            Rlog.v(TAG, "getCallerInfo() based on cursor...");
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("display_name");
                if (columnIndex != -1) {
                    info.name = cursor.getString(columnIndex);
                }
                int columnIndex2 = cursor.getColumnIndex("number");
                if (columnIndex2 != -1) {
                    info.phoneNumber = cursor.getString(columnIndex2);
                }
                int columnIndex3 = cursor.getColumnIndex("normalized_number");
                if (columnIndex3 != -1) {
                    info.normalizedNumber = cursor.getString(columnIndex3);
                }
                int columnIndex4 = cursor.getColumnIndex("label");
                if (columnIndex4 != -1) {
                    int typeColumnIndex = cursor.getColumnIndex("type");
                    if (typeColumnIndex != -1) {
                        info.numberType = cursor.getInt(typeColumnIndex);
                        info.numberLabel = cursor.getString(columnIndex4);
                        info.phoneLabel = ContactsContract.CommonDataKinds.Phone.getDisplayLabel(context, info.numberType, info.numberLabel).toString();
                    }
                }
                int columnIndex5 = getColumnIndexForPersonId(contactRef, cursor);
                if (columnIndex5 != -1) {
                    long contactId = cursor.getLong(columnIndex5);
                    if (contactId != 0 && !ContactsContract.Contacts.isEnterpriseContactId(contactId)) {
                        info.contactIdOrZero = contactId;
                        if (VDBG) {
                            Rlog.v(TAG, "==> got info.contactIdOrZero: " + info.contactIdOrZero);
                        }
                    }
                    if (ContactsContract.Contacts.isEnterpriseContactId(contactId)) {
                        info.userType = 1;
                    }
                } else {
                    Rlog.w(TAG, "Couldn't find contact_id column for " + contactRef);
                }
                int columnIndex6 = cursor.getColumnIndex("lookup");
                if (columnIndex6 != -1) {
                    info.lookupKey = cursor.getString(columnIndex6);
                }
                int columnIndex7 = cursor.getColumnIndex("photo_uri");
                if (columnIndex7 == -1 || cursor.getString(columnIndex7) == null) {
                    info.contactDisplayPhotoUri = null;
                } else {
                    info.contactDisplayPhotoUri = Uri.parse(cursor.getString(columnIndex7));
                }
                int columnIndex8 = cursor.getColumnIndex("custom_ringtone");
                if (columnIndex8 == -1 || cursor.getString(columnIndex8) == null) {
                    info.contactRingtoneUri = null;
                } else if (TextUtils.isEmpty(cursor.getString(columnIndex8))) {
                    info.contactRingtoneUri = Uri.EMPTY;
                } else {
                    info.contactRingtoneUri = Uri.parse(cursor.getString(columnIndex8));
                }
                int columnIndex9 = cursor.getColumnIndex("send_to_voicemail");
                info.shouldSendToVoicemail = columnIndex9 != -1 && cursor.getInt(columnIndex9) == 1;
                info.contactExists = true;
                int columnIndex10 = cursor.getColumnIndex("data7");
                if (columnIndex10 != -1) {
                    info.mVoipDeviceType = cursor.getInt(columnIndex10);
                }
            }
            cursor.close();
        }
        info.needUpdate = false;
        info.name = normalize(info.name);
        info.contactRefUri = contactRef;
        return info;
    }

    public static CallerInfo getCallerInfo(Context context, Uri contactRef) {
        ContentResolver cr = CallerInfoAsyncQuery.getCurrentProfileContentResolver(context);
        if (cr == null) {
            return null;
        }
        try {
            return getCallerInfo(context, contactRef, cr.query(contactRef, null, null, null, null));
        } catch (RuntimeException re) {
            Rlog.e(TAG, "Error getting caller info.", re);
            return null;
        }
    }

    public static CallerInfo getCallerInfo(Context context, String number) {
        if (VDBG) {
            Rlog.v(TAG, "getCallerInfo() based on number...");
        }
        return getCallerInfo(context, number, SubscriptionManager.getDefaultSubscriptionId());
    }

    public static CallerInfo getCallerInfo(Context context, String number, int subId) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        if (PhoneNumberUtils.isLocalEmergencyNumber(context, number)) {
            return new CallerInfo().markAsEmergency(context);
        }
        if (PhoneNumberUtils.isVoiceMailNumber(subId, number)) {
            return new CallerInfo().markAsVoiceMail();
        }
        CallerInfo info = doSecondaryLookupIfNecessary(context, number, getCallerInfo(context, Uri.withAppendedPath(ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(number))));
        if (TextUtils.isEmpty(info.phoneNumber)) {
            info.phoneNumber = number;
        }
        return info;
    }

    static CallerInfo doSecondaryLookupIfNecessary(Context context, String number, CallerInfo previousResult) {
        if (previousResult.contactExists || !PhoneNumberUtils.isUriNumber(number)) {
            return previousResult;
        }
        String username = PhoneNumberUtils.getUsernameFromUriNumber(number);
        if (PhoneNumberUtils.isGlobalPhoneNumber(username)) {
            return getCallerInfo(context, Uri.withAppendedPath(ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(username)));
        }
        return previousResult;
    }

    public boolean isEmergencyNumber() {
        return this.mIsEmergency;
    }

    public boolean isVoiceMailNumber() {
        return this.mIsVoiceMail;
    }

    /* access modifiers changed from: package-private */
    public CallerInfo markAsEmergency(Context context) {
        this.phoneNumber = context.getString(17039986);
        this.photoResource = 17303082;
        this.mIsEmergency = true;
        return this;
    }

    /* access modifiers changed from: package-private */
    public CallerInfo markAsVoiceMail() {
        return markAsVoiceMail(SubscriptionManager.getDefaultSubscriptionId());
    }

    /* access modifiers changed from: package-private */
    public CallerInfo markAsVoiceMail(int subId) {
        this.mIsVoiceMail = true;
        try {
            this.phoneNumber = TelephonyManager.getDefault().getVoiceMailAlphaTag(subId);
        } catch (SecurityException se) {
            Rlog.e(TAG, "Cannot access VoiceMail.", se);
        }
        return this;
    }

    private static String normalize(String s) {
        if (s == null || s.length() > 0) {
            return s;
        }
        return null;
    }

    private static int getColumnIndexForPersonId(Uri contactRef, Cursor cursor) {
        if (VDBG) {
            Rlog.v(TAG, "- getColumnIndexForPersonId: contactRef URI = '" + contactRef + "'...");
        }
        String url = contactRef.toString();
        String columnName = null;
        if (url.startsWith("content://com.android.contacts/data/phones")) {
            if (VDBG) {
                Rlog.v(TAG, "'data/phones' URI; using RawContacts.CONTACT_ID");
            }
            columnName = "contact_id";
        } else if (url.startsWith("content://com.android.contacts/data")) {
            if (VDBG) {
                Rlog.v(TAG, "'data' URI; using Data.CONTACT_ID");
            }
            columnName = "contact_id";
        } else if (url.startsWith("content://com.android.contacts/phone_lookup")) {
            if (VDBG) {
                Rlog.v(TAG, "'phone_lookup' URI; using PhoneLookup._ID");
            }
            columnName = "_id";
        } else {
            Rlog.w(TAG, "Unexpected prefix for contactRef '" + url + "'");
        }
        int columnIndex = columnName != null ? cursor.getColumnIndex(columnName) : -1;
        if (VDBG) {
            Rlog.v(TAG, "==> Using column '" + columnName + "' (columnIndex = " + columnIndex + ") for person_id lookup...");
        }
        return columnIndex;
    }

    public void updateGeoDescription(Context context, String fallbackNumber) {
        this.geoDescription = getGeoDescription(context, TextUtils.isEmpty(this.phoneNumber) ? fallbackNumber : this.phoneNumber);
    }

    public static String getGeoDescription(Context context, String number) {
        String description;
        if (VDBG) {
            Rlog.v(TAG, "getGeoDescription('XXXXXX')...");
        }
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
        Locale locale = context.getResources().getConfiguration().locale;
        String countryIso = getCurrentCountryIso(context, locale);
        Phonenumber.PhoneNumber pn = null;
        try {
            if (VDBG) {
                Rlog.v(TAG, "parsing 'XXXXXX' for countryIso '" + countryIso + "'...");
            }
            pn = util.parse(number, countryIso);
            if (VDBG) {
                Rlog.v(TAG, "- parsed number: XXXXXX");
            }
        } catch (NumberParseException e) {
            if (VDBG) {
                Rlog.w(TAG, "getGeoDescription: NumberParseException for incoming number 'XXXXXX'");
            }
        } catch (RuntimeException e2) {
            Rlog.e(TAG, "parsed number RuntimeException.");
            return "";
        }
        if (pn == null) {
            return null;
        }
        try {
            description = geocoder.getDescriptionForNumber(pn, locale);
        } catch (NullPointerException e3) {
            if (VDBG) {
                Rlog.w(TAG, "getDescriptionForNumber NullPointerException");
            }
            description = "";
        } catch (RuntimeException e4) {
            if (VDBG) {
                Rlog.w(TAG, "getDescriptionForNumber RuntimeException");
            }
            description = "";
        }
        if (VDBG) {
            Rlog.v(TAG, "- got description: 'XXXXXX'");
        }
        return description;
    }

    private static String getCurrentCountryIso(Context context, Locale locale) {
        String countryIso = null;
        CountryDetector detector = (CountryDetector) context.getSystemService("country_detector");
        if (detector != null) {
            Country country = detector.detectCountry();
            if (country != null) {
                countryIso = country.getCountryIso();
            } else {
                Rlog.e(TAG, "CountryDetector.detectCountry() returned null.");
            }
        }
        if (countryIso != null) {
            return countryIso;
        }
        String countryIso2 = locale.getCountry();
        Rlog.w(TAG, "No CountryDetector; falling back to countryIso based on locale: " + countryIso2);
        return countryIso2;
    }

    protected static String getCurrentCountryIso(Context context) {
        return getCurrentCountryIso(context, Locale.getDefault());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append(super.toString() + " { ");
        StringBuilder sb2 = new StringBuilder();
        sb2.append("name ");
        sb2.append(this.name == null ? "null" : "non-null");
        sb.append(sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append(", phoneNumber ");
        sb3.append(this.phoneNumber == null ? "null" : "non-null");
        sb.append(sb3.toString());
        sb.append(" }");
        return sb.toString();
    }
}
