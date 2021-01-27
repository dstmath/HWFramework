package android.provider;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.location.Country;
import android.location.CountryDetector;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract;
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
    public static final Uri CONTENT_URI = Uri.parse("content://call_log");
    private static final String LOG_TAG = "CallLog";
    public static final String SHADOW_AUTHORITY = "call_log_shadow";
    private static final boolean VERBOSE_LOG = false;

    public static class Calls implements BaseColumns {
        public static final String ADD_FOR_ALL_USERS = "add_for_all_users";
        public static final String ALLOW_VOICEMAILS_PARAM_KEY = "allow_voicemails";
        public static final int ANSWERED_EXTERNALLY_TYPE = 7;
        public static final int BLOCKED_TYPE = 6;
        public static final String BLOCK_REASON = "block_reason";
        public static final int BLOCK_REASON_BLOCKED_NUMBER = 3;
        public static final int BLOCK_REASON_CALL_SCREENING_SERVICE = 1;
        public static final int BLOCK_REASON_DIRECT_TO_VOICEMAIL = 2;
        public static final int BLOCK_REASON_NOT_BLOCKED = 0;
        public static final int BLOCK_REASON_NOT_IN_CONTACTS = 7;
        public static final int BLOCK_REASON_PAY_PHONE = 6;
        public static final int BLOCK_REASON_RESTRICTED_NUMBER = 5;
        public static final int BLOCK_REASON_UNKNOWN_NUMBER = 4;
        public static final String CACHED_FORMATTED_NUMBER = "formatted_number";
        public static final String CACHED_LOOKUP_URI = "lookup_uri";
        public static final String CACHED_MATCHED_NUMBER = "matched_number";
        public static final String CACHED_NAME = "name";
        public static final String CACHED_NORMALIZED_NUMBER = "normalized_number";
        public static final String CACHED_NUMBER_LABEL = "numberlabel";
        public static final String CACHED_NUMBER_TYPE = "numbertype";
        public static final String CACHED_PHOTO_ID = "photo_id";
        public static final String CACHED_PHOTO_URI = "photo_uri";
        public static final String CALL_SCREENING_APP_NAME = "call_screening_app_name";
        public static final String CALL_SCREENING_COMPONENT_NAME = "call_screening_component_name";
        public static final Uri CONTENT_FILTER_URI = Uri.parse("content://call_log/calls/filter");
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/calls";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/calls";
        public static final Uri CONTENT_URI = Uri.parse("content://call_log/calls");
        public static final Uri CONTENT_URI_WITH_VOICEMAIL = CONTENT_URI.buildUpon().appendQueryParameter(ALLOW_VOICEMAILS_PARAM_KEY, "true").build();
        public static final String COUNTRY_ISO = "countryiso";
        public static final String DATA_USAGE = "data_usage";
        public static final String DATE = "date";
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        public static final String DURATION = "duration";
        public static final String EXTRA_CALL_TYPE_FILTER = "android.provider.extra.CALL_TYPE_FILTER";
        public static final String FEATURES = "features";
        public static final int FEATURES_ASSISTED_DIALING_USED = 16;
        public static final int FEATURES_HD_CALL = 4;
        public static final int FEATURES_PULLED_EXTERNALLY = 2;
        public static final int FEATURES_RTT = 32;
        public static final int FEATURES_VIDEO = 1;
        public static final int FEATURES_WIFI = 8;
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
        public static final Uri SHADOW_CONTENT_URI = Uri.parse("content://call_log_shadow/calls");
        public static final String SUB_ID = "sub_id";
        public static final String TRANSCRIPTION = "transcription";
        public static final String TRANSCRIPTION_STATE = "transcription_state";
        public static final String TYPE = "type";
        public static final String VIA_NUMBER = "via_number";
        public static final int VOICEMAIL_TYPE = 4;
        public static final String VOICEMAIL_URI = "voicemail_uri";

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage) {
            return addCall(ci, context, number, "", "", presentation, callType, features, accountHandle, start, duration, dataUsage, false, null, false, 0, null, null);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, String postDialDigits, String viaNumber, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage, boolean addForAllUsers, UserHandle userToBeInsertedTo) {
            return addCall(ci, context, number, postDialDigits, viaNumber, presentation, callType, features, accountHandle, start, duration, dataUsage, addForAllUsers, userToBeInsertedTo, false, 0, null, null);
        }

        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public static Uri addCall(CallerInfo ci, Context context, String number, String postDialDigits, String viaNumber, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage, boolean addForAllUsers, UserHandle userToBeInsertedTo, boolean isRead, int callBlockReason, CharSequence callScreeningAppName, String callScreeningComponentName) {
            String number2;
            String accountComponentString;
            String accountId;
            ContentValues values;
            boolean z;
            UserHandle targetUserHandle;
            int i;
            Cursor cursor;
            ContentResolver resolver = context.getContentResolver();
            String accountAddress = getLogAccountAddress(context, accountHandle);
            int numberPresentation = getLogNumberPresentation(number, presentation);
            if (numberPresentation != 1) {
                if (ci != null) {
                    ci.name = "";
                }
                number2 = "";
            } else {
                number2 = number;
            }
            if (accountHandle != null) {
                accountComponentString = accountHandle.getComponentName().flattenToString();
                accountId = accountHandle.getId();
            } else {
                accountComponentString = null;
                accountId = null;
            }
            ContentValues values2 = new ContentValues(6);
            values2.put("number", number2);
            values2.put(POST_DIAL_DIGITS, postDialDigits);
            values2.put(VIA_NUMBER, viaNumber);
            values2.put(NUMBER_PRESENTATION, Integer.valueOf(numberPresentation));
            values2.put("type", Integer.valueOf(callType));
            values2.put(FEATURES, Integer.valueOf(features));
            values2.put("date", Long.valueOf(start));
            values2.put("duration", Long.valueOf((long) duration));
            if (dataUsage != null) {
                values2.put(DATA_USAGE, dataUsage);
            }
            values2.put("subscription_component_name", accountComponentString);
            values2.put("subscription_id", accountId);
            values2.put(PHONE_ACCOUNT_ADDRESS, accountAddress);
            values2.put("new", (Integer) 1);
            if (!(ci == null || ci.name == null)) {
                values2.put("name", ci.name);
            }
            values2.put(ADD_FOR_ALL_USERS, Integer.valueOf(addForAllUsers ? 1 : 0));
            if (callType == 3) {
                values2.put("is_read", Integer.valueOf(isRead ? 1 : 0));
            }
            values2.put(BLOCK_REASON, Integer.valueOf(callBlockReason));
            values2.put(CALL_SCREENING_APP_NAME, charSequenceToString(callScreeningAppName));
            values2.put(CALL_SCREENING_COMPONENT_NAME, callScreeningComponentName);
            if (ci == null) {
                values = values2;
                z = true;
            } else if (ci.contactIdOrZero > 0) {
                if (ci.normalizedNumber != null) {
                    values = values2;
                    z = true;
                    cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{"_id"}, "contact_id =? AND data4 =?", new String[]{String.valueOf(ci.contactIdOrZero), ci.normalizedNumber}, null);
                    i = 0;
                } else {
                    values = values2;
                    z = true;
                    i = 0;
                    cursor = resolver.query(Uri.withAppendedPath(ContactsContract.CommonDataKinds.Callable.CONTENT_FILTER_URI, Uri.encode(ci.phoneNumber != null ? ci.phoneNumber : number2)), new String[]{"_id"}, "contact_id =?", new String[]{String.valueOf(ci.contactIdOrZero)}, null);
                }
                if (cursor != null) {
                    try {
                        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                            String dataId = cursor.getString(i);
                            updateDataUsageStatForData(resolver, dataId);
                            if (duration >= 10000 && callType == 2 && TextUtils.isEmpty(ci.normalizedNumber)) {
                                updateNormalizedNumber(context, resolver, dataId, number2);
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }
            } else {
                values = values2;
                z = true;
            }
            Uri result = null;
            UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
            int currentUserId = userManager.getUserHandle();
            if (addForAllUsers) {
                Uri uriForSystem = addEntryAndRemoveExpiredEntries(context, userManager, UserHandle.SYSTEM, values);
                if (uriForSystem == null || CallLog.SHADOW_AUTHORITY.equals(uriForSystem.getAuthority())) {
                    return null;
                }
                if (currentUserId == 0) {
                    result = uriForSystem;
                }
                List<UserInfo> users = userManager.getUsers(z);
                int count = users.size();
                int i2 = 0;
                while (i2 < count) {
                    UserHandle userHandle = users.get(i2).getUserHandle();
                    int userId = userHandle.getIdentifier();
                    if (!userHandle.isSystem() && shouldHaveSharedCallLogEntries(context, userManager, userId) && userManager.isUserRunning(userHandle) && userManager.isUserUnlocked(userHandle)) {
                        Uri uri = addEntryAndRemoveExpiredEntries(context, userManager, userHandle, values);
                        if (userId == currentUserId) {
                            result = uri;
                        }
                    }
                    i2++;
                    users = users;
                }
                return result;
            }
            if (userToBeInsertedTo != null) {
                targetUserHandle = userToBeInsertedTo;
            } else {
                targetUserHandle = UserHandle.of(currentUserId);
            }
            return addEntryAndRemoveExpiredEntries(context, userManager, targetUserHandle, values);
        }

        private static String charSequenceToString(CharSequence sequence) {
            if (sequence == null) {
                return null;
            }
            return sequence.toString();
        }

        public static boolean shouldHaveSharedCallLogEntries(Context context, UserManager userManager, int userId) {
            UserInfo userInfo;
            if (!userManager.hasUserRestriction(UserManager.DISALLOW_OUTGOING_CALLS, UserHandle.of(userId)) && (userInfo = userManager.getUserInfo(userId)) != null && !userInfo.isManagedProfile()) {
                return true;
            }
            return false;
        }

        public static String getLastOutgoingCall(Context context) {
            Cursor c = null;
            try {
                c = context.getContentResolver().query(CONTENT_URI, new String[]{"number"}, "type = 2", null, "date DESC LIMIT 1");
                if (c != null) {
                    if (c.moveToFirst()) {
                        String string = c.getString(0);
                        c.close();
                        return string;
                    }
                }
                return "";
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        private static Uri addEntryAndRemoveExpiredEntries(Context context, UserManager userManager, UserHandle user, ContentValues values) {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = ContentProvider.maybeAddUserId(userManager.isUserUnlocked(user) ? CONTENT_URI : SHADOW_CONTENT_URI, user.getIdentifier());
            try {
                Uri result = resolver.insert(uri, values);
                if (!values.containsKey("subscription_id") || TextUtils.isEmpty(values.getAsString("subscription_id")) || !values.containsKey("subscription_component_name") || TextUtils.isEmpty(values.getAsString("subscription_component_name"))) {
                    resolver.delete(uri, "_id IN (SELECT _id FROM calls ORDER BY date DESC LIMIT -1 OFFSET 500)", null);
                } else {
                    resolver.delete(uri, "_id IN (SELECT _id FROM calls WHERE subscription_component_name = ? AND subscription_id = ? ORDER BY date DESC LIMIT -1 OFFSET 500)", new String[]{values.getAsString("subscription_component_name"), values.getAsString("subscription_id")});
                }
                return result;
            } catch (IllegalArgumentException e) {
                Log.w(CallLog.LOG_TAG, "Failed to insert calllog", e);
                return null;
            }
        }

        private static void updateDataUsageStatForData(ContentResolver resolver, String dataId) {
            resolver.update(ContactsContract.DataUsageFeedback.FEEDBACK_URI.buildUpon().appendPath(dataId).appendQueryParameter("type", "call").build(), new ContentValues(), null, null);
        }

        private static void updateNormalizedNumber(Context context, ContentResolver resolver, String dataId, String number) {
            if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(dataId)) {
                String countryIso = getCurrentCountryIso(context);
                if (!TextUtils.isEmpty(countryIso)) {
                    String normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, countryIso);
                    if (!TextUtils.isEmpty(normalizedNumber)) {
                        ContentValues values = new ContentValues();
                        values.put("data4", normalizedNumber);
                        resolver.update(ContactsContract.Data.CONTENT_URI, values, "_id=?", new String[]{dataId});
                    }
                }
            }
        }

        private static int getLogNumberPresentation(String number, int presentation) {
            if (presentation == 2 || presentation == 4) {
                return presentation;
            }
            if (TextUtils.isEmpty(number) || presentation == 3) {
                return 3;
            }
            return 1;
        }

        private static String getLogAccountAddress(Context context, PhoneAccountHandle accountHandle) {
            PhoneAccount account;
            Uri address;
            TelecomManager tm = null;
            try {
                tm = TelecomManager.from(context);
            } catch (UnsupportedOperationException e) {
            }
            if (tm == null || accountHandle == null || (account = tm.getPhoneAccount(accountHandle)) == null || (address = account.getSubscriptionAddress()) == null) {
                return null;
            }
            return address.getSchemeSpecificPart();
        }

        private static String getCurrentCountryIso(Context context) {
            Country country;
            CountryDetector detector = (CountryDetector) context.getSystemService(Context.COUNTRY_DETECTOR);
            if (detector == null || (country = detector.detectCountry()) == null) {
                return null;
            }
            return country.getCountryIso();
        }
    }
}
