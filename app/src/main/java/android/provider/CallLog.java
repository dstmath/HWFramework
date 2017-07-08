package android.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.location.Country;
import android.location.CountryDetector;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract.CommonDataKinds.Callable;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.DataUsageFeedback;
import android.rms.iaware.AwareConstant.Database.HwUserData;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CallerInfo;
import java.util.List;

public class CallLog {
    public static final String AUTHORITY = "call_log";
    public static final Uri CONTENT_URI = null;
    private static final String LOG_TAG = "CallLog";
    public static final String SHADOW_AUTHORITY = "call_log_shadow";
    private static final boolean VERBOSE_LOG = false;

    public static class Calls implements BaseColumns {
        public static final String ADD_FOR_ALL_USERS = "add_for_all_users";
        public static final String ALLOW_VOICEMAILS_PARAM_KEY = "allow_voicemails";
        public static final int ANSWERED_EXTERNALLY_TYPE = 7;
        public static final int BLOCKED_TYPE = 6;
        public static final String CACHED_FORMATTED_NUMBER = "formatted_number";
        public static final String CACHED_LOOKUP_URI = "lookup_uri";
        public static final String CACHED_MATCHED_NUMBER = "matched_number";
        public static final String CACHED_NAME = "name";
        public static final String CACHED_NORMALIZED_NUMBER = "normalized_number";
        public static final String CACHED_NUMBER_LABEL = "numberlabel";
        public static final String CACHED_NUMBER_TYPE = "numbertype";
        public static final String CACHED_PHOTO_ID = "photo_id";
        public static final String CACHED_PHOTO_URI = "photo_uri";
        public static final Uri CONTENT_FILTER_URI = null;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/calls";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/calls";
        public static final Uri CONTENT_URI = null;
        public static final Uri CONTENT_URI_WITH_VOICEMAIL = null;
        public static final String COUNTRY_ISO = "countryiso";
        public static final String DATA_USAGE = "data_usage";
        public static final String DATE = "date";
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        public static final String DURATION = "duration";
        public static final String EXTRA_CALL_TYPE_FILTER = "android.provider.extra.CALL_TYPE_FILTER";
        public static final String FEATURES = "features";
        public static final int FEATURES_PULLED_EXTERNALLY = 2;
        public static final int FEATURES_VIDEO = 1;
        public static final String GEOCODED_LOCATION = "geocoded_location";
        public static final int INCOMING_TYPE = 1;
        public static final String IS_READ = "is_read";
        public static final String LAST_MODIFIED = "last_modified";
        public static final String LIMIT_PARAM_KEY = "limit";
        private static final int MIN_DURATION_FOR_NORMALIZED_NUMBER_UPDATE_MS = 10000;
        public static final int MISSED_TYPE = 3;
        public static final String NEW = "new";
        public static final String NUMBER = "number";
        public static final String NUMBER_PRESENTATION = "presentation";
        public static final String OFFSET_PARAM_KEY = "offset";
        public static final int OUTGOING_TYPE = 2;
        public static final String PHONE_ACCOUNT_ADDRESS = "phone_account_address";
        public static final String PHONE_ACCOUNT_COMPONENT_NAME = "subscription_component_name";
        public static final String PHONE_ACCOUNT_HIDDEN = "phone_account_hidden";
        public static final String PHONE_ACCOUNT_ID = "subscription_id";
        public static final String POST_DIAL_DIGITS = "post_dial_digits";
        public static final int PRESENTATION_ALLOWED = 1;
        public static final int PRESENTATION_PAYPHONE = 4;
        public static final int PRESENTATION_RESTRICTED = 2;
        public static final int PRESENTATION_UNKNOWN = 3;
        public static final int REJECTED_TYPE = 5;
        public static final Uri SHADOW_CONTENT_URI = null;
        public static final String SUB_ID = "sub_id";
        public static final String TRANSCRIPTION = "transcription";
        public static final String TYPE = "type";
        public static final String VIA_NUMBER = "via_number";
        public static final int VOICEMAIL_TYPE = 4;
        public static final String VOICEMAIL_URI = "voicemail_uri";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.CallLog.Calls.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.CallLog.Calls.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CallLog.Calls.<clinit>():void");
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage) {
            return addCall(ci, context, number, ProxyInfo.LOCAL_EXCL_LIST, ProxyInfo.LOCAL_EXCL_LIST, presentation, callType, features, accountHandle, start, duration, dataUsage, false, null, false);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, String postDialDigits, String viaNumber, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage, boolean addForAllUsers, UserHandle userToBeInsertedTo) {
            return addCall(ci, context, number, postDialDigits, viaNumber, presentation, callType, features, accountHandle, start, duration, dataUsage, addForAllUsers, userToBeInsertedTo, false);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, String postDialDigits, String viaNumber, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage, boolean addForAllUsers, UserHandle userToBeInsertedTo, boolean is_read) {
            ContentResolver resolver = context.getContentResolver();
            int numberPresentation = PRESENTATION_ALLOWED;
            TelecomManager telecomManager = null;
            try {
                telecomManager = TelecomManager.from(context);
            } catch (UnsupportedOperationException e) {
            }
            String accountAddress = null;
            if (!(telecomManager == null || accountHandle == null)) {
                PhoneAccount account = telecomManager.getPhoneAccount(accountHandle);
                if (account != null) {
                    Uri address = account.getSubscriptionAddress();
                    if (address != null) {
                        accountAddress = address.getSchemeSpecificPart();
                    }
                }
            }
            if (presentation == PRESENTATION_RESTRICTED) {
                numberPresentation = PRESENTATION_RESTRICTED;
            } else if (presentation == VOICEMAIL_TYPE) {
                numberPresentation = VOICEMAIL_TYPE;
            } else if (TextUtils.isEmpty(number) || presentation == PRESENTATION_UNKNOWN) {
                numberPresentation = PRESENTATION_UNKNOWN;
            }
            if (numberPresentation != PRESENTATION_ALLOWED) {
                number = ProxyInfo.LOCAL_EXCL_LIST;
                if (ci != null) {
                    ci.name = ProxyInfo.LOCAL_EXCL_LIST;
                }
            }
            String str = null;
            String accountId = null;
            if (accountHandle != null) {
                str = accountHandle.getComponentName().flattenToString();
                accountId = accountHandle.getId();
            }
            ContentValues contentValues = new ContentValues((int) BLOCKED_TYPE);
            contentValues.put(NUMBER, number);
            contentValues.put(POST_DIAL_DIGITS, postDialDigits);
            contentValues.put(VIA_NUMBER, viaNumber);
            contentValues.put(NUMBER_PRESENTATION, Integer.valueOf(numberPresentation));
            contentValues.put(TYPE, Integer.valueOf(callType));
            contentValues.put(FEATURES, Integer.valueOf(features));
            contentValues.put(DATE, Long.valueOf(start));
            contentValues.put(DURATION, Long.valueOf((long) duration));
            if (dataUsage != null) {
                contentValues.put(DATA_USAGE, dataUsage);
            }
            contentValues.put(PHONE_ACCOUNT_COMPONENT_NAME, str);
            contentValues.put(PHONE_ACCOUNT_ID, accountId);
            contentValues.put(PHONE_ACCOUNT_ADDRESS, accountAddress);
            contentValues.put(NEW, Integer.valueOf(PRESENTATION_ALLOWED));
            contentValues.put(ADD_FOR_ALL_USERS, Integer.valueOf(addForAllUsers ? PRESENTATION_ALLOWED : 0));
            if (callType == PRESENTATION_UNKNOWN) {
                contentValues.put(IS_READ, Integer.valueOf(is_read ? PRESENTATION_ALLOWED : 0));
            }
            if (ci != null && ci.contactIdOrZero > 0) {
                Cursor cursor;
                Uri uri;
                String[] strArr;
                String[] strArr2;
                if (ci.normalizedNumber != null) {
                    String normalizedPhoneNumber = ci.normalizedNumber;
                    uri = Phone.CONTENT_URI;
                    strArr = new String[PRESENTATION_ALLOWED];
                    strArr[0] = HwUserData._ID;
                    strArr2 = new String[PRESENTATION_RESTRICTED];
                    strArr2[0] = String.valueOf(ci.contactIdOrZero);
                    strArr2[PRESENTATION_ALLOWED] = normalizedPhoneNumber;
                    cursor = resolver.query(uri, strArr, "contact_id =? AND data4 =?", strArr2, null);
                } else {
                    uri = Uri.withAppendedPath(Callable.CONTENT_FILTER_URI, Uri.encode(ci.phoneNumber != null ? ci.phoneNumber : number));
                    strArr = new String[PRESENTATION_ALLOWED];
                    strArr[0] = HwUserData._ID;
                    strArr2 = new String[PRESENTATION_ALLOWED];
                    strArr2[0] = String.valueOf(ci.contactIdOrZero);
                    cursor = resolver.query(uri, strArr, "contact_id =?", strArr2, null);
                }
                if (cursor != null) {
                    try {
                        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                            String dataId = cursor.getString(0);
                            updateDataUsageStatForData(resolver, dataId);
                            if (duration >= MIN_DURATION_FOR_NORMALIZED_NUMBER_UPDATE_MS && callType == PRESENTATION_RESTRICTED && TextUtils.isEmpty(ci.normalizedNumber)) {
                                updateNormalizedNumber(context, resolver, dataId, number);
                            }
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
            }
            Uri uri2 = null;
            UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
            int currentUserId = userManager.getUserHandle();
            if (addForAllUsers) {
                Uri uriForSystem = addEntryAndRemoveExpiredEntries(context, userManager, UserHandle.SYSTEM, contentValues);
                if (uriForSystem == null || CallLog.SHADOW_AUTHORITY.equals(uriForSystem.getAuthority())) {
                    return null;
                }
                if (currentUserId == 0) {
                    uri2 = uriForSystem;
                }
                List<UserInfo> users = userManager.getUsers(true);
                if (users == null) {
                    return null;
                }
                int count = users.size();
                for (int i = 0; i < count; i += PRESENTATION_ALLOWED) {
                    UserHandle userHandle = ((UserInfo) users.get(i)).getUserHandle();
                    int userId = userHandle.getIdentifier();
                    if (!userHandle.isSystem() && shouldHaveSharedCallLogEntries(context, userManager, userId) && userManager.isUserRunning(userHandle) && userManager.isUserUnlocked(userHandle)) {
                        Uri uri3 = addEntryAndRemoveExpiredEntries(context, userManager, userHandle, contentValues);
                        if (userId == currentUserId) {
                            uri2 = uri3;
                        }
                    }
                }
            } else {
                UserHandle targetUserHandle;
                if (userToBeInsertedTo != null) {
                    targetUserHandle = userToBeInsertedTo;
                } else {
                    targetUserHandle = UserHandle.of(currentUserId);
                }
                uri2 = addEntryAndRemoveExpiredEntries(context, userManager, targetUserHandle, contentValues);
            }
            return uri2;
        }

        public static boolean shouldHaveSharedCallLogEntries(Context context, UserManager userManager, int userId) {
            boolean z = false;
            if (userManager.hasUserRestriction(UserManager.DISALLOW_OUTGOING_CALLS, UserHandle.of(userId))) {
                return false;
            }
            UserInfo userInfo = userManager.getUserInfo(userId);
            if (!(userInfo == null || userInfo.isManagedProfile())) {
                z = true;
            }
            return z;
        }

        public static String getLastOutgoingCall(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                Uri uri = CONTENT_URI;
                String[] strArr = new String[PRESENTATION_ALLOWED];
                strArr[0] = NUMBER;
                cursor = resolver.query(uri, strArr, "type = 2", null, "date DESC LIMIT 1");
                String str;
                if (cursor == null || !cursor.moveToFirst()) {
                    str = ProxyInfo.LOCAL_EXCL_LIST;
                    if (cursor != null) {
                        cursor.close();
                    }
                    return str;
                }
                str = cursor.getString(0);
                return str;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private static Uri addEntryAndRemoveExpiredEntries(Context context, UserManager userManager, UserHandle user, ContentValues values) {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = ContentProvider.maybeAddUserId(userManager.isUserUnlocked(user) ? CONTENT_URI : SHADOW_CONTENT_URI, user.getIdentifier());
            try {
                Uri result = resolver.insert(uri, values);
                resolver.delete(uri, "_id IN (SELECT _id FROM calls ORDER BY date DESC LIMIT -1 OFFSET 2000)", null);
                return result;
            } catch (IllegalArgumentException e) {
                Log.w(CallLog.LOG_TAG, "Failed to insert calllog", e);
                return null;
            }
        }

        private static void updateDataUsageStatForData(ContentResolver resolver, String dataId) {
            resolver.update(DataUsageFeedback.FEEDBACK_URI.buildUpon().appendPath(dataId).appendQueryParameter(TYPE, DataUsageFeedback.USAGE_TYPE_CALL).build(), new ContentValues(), null, null);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static void updateNormalizedNumber(Context context, ContentResolver resolver, String dataId, String number) {
            if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(dataId) && !TextUtils.isEmpty(getCurrentCountryIso(context))) {
                String normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, getCurrentCountryIso(context));
                if (!TextUtils.isEmpty(normalizedNumber)) {
                    ContentValues values = new ContentValues();
                    values.put(StructuredPostal.STREET, normalizedNumber);
                    String[] strArr = new String[PRESENTATION_ALLOWED];
                    strArr[0] = dataId;
                    resolver.update(Data.CONTENT_URI, values, "_id=?", strArr);
                }
            }
        }

        private static String getCurrentCountryIso(Context context) {
            CountryDetector detector = (CountryDetector) context.getSystemService(Context.COUNTRY_DETECTOR);
            if (detector == null) {
                return null;
            }
            Country country = detector.detectCountry();
            if (country != null) {
                return country.getCountryIso();
            }
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.CallLog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.CallLog.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.provider.CallLog.<clinit>():void");
    }

    public CallLog() {
    }
}
