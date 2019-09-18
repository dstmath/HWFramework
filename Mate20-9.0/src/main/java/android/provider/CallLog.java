package android.provider;

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
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CallerInfo;

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
        public static final String CACHED_FORMATTED_NUMBER = "formatted_number";
        public static final String CACHED_LOOKUP_URI = "lookup_uri";
        public static final String CACHED_MATCHED_NUMBER = "matched_number";
        public static final String CACHED_NAME = "name";
        public static final String CACHED_NORMALIZED_NUMBER = "normalized_number";
        public static final String CACHED_NUMBER_LABEL = "numberlabel";
        public static final String CACHED_NUMBER_TYPE = "numbertype";
        public static final String CACHED_PHOTO_ID = "photo_id";
        public static final String CACHED_PHOTO_URI = "photo_uri";
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
            return addCall(ci, context, number, "", "", presentation, callType, features, accountHandle, start, duration, dataUsage, false, null, false);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, String postDialDigits, String viaNumber, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage, boolean addForAllUsers, UserHandle userToBeInsertedTo) {
            return addCall(ci, context, number, postDialDigits, viaNumber, presentation, callType, features, accountHandle, start, duration, dataUsage, addForAllUsers, userToBeInsertedTo, false);
        }

        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
            */
        public static android.net.Uri addCall(com.android.internal.telephony.CallerInfo r26, android.content.Context r27, java.lang.String r28, java.lang.String r29, java.lang.String r30, int r31, int r32, int r33, android.telecom.PhoneAccountHandle r34, long r35, int r37, java.lang.Long r38, boolean r39, android.os.UserHandle r40, boolean r41) {
            /*
                r1 = r26
                r2 = r27
                r3 = r31
                r4 = r32
                r5 = r34
                r6 = r37
                r7 = r38
                android.content.ContentResolver r14 = r27.getContentResolver()
                r8 = 1
                r15 = 0
                r9 = r15
                android.telecom.TelecomManager r0 = android.telecom.TelecomManager.from(r27)     // Catch:{ UnsupportedOperationException -> 0x001d }
                r9 = r0
            L_0x001b:
                r13 = r9
                goto L_0x001f
            L_0x001d:
                r0 = move-exception
                goto L_0x001b
            L_0x001f:
                r0 = 0
                if (r13 == 0) goto L_0x0034
                if (r5 == 0) goto L_0x0034
                android.telecom.PhoneAccount r9 = r13.getPhoneAccount(r5)
                if (r9 == 0) goto L_0x0034
                android.net.Uri r10 = r9.getSubscriptionAddress()
                if (r10 == 0) goto L_0x0034
                java.lang.String r0 = r10.getSchemeSpecificPart()
            L_0x0034:
                r12 = r0
                r0 = 3
                r11 = 2
                if (r3 != r11) goto L_0x003c
                r8 = 2
            L_0x003a:
                r10 = r8
                goto L_0x004b
            L_0x003c:
                r9 = 4
                if (r3 != r9) goto L_0x0041
                r8 = 4
                goto L_0x003a
            L_0x0041:
                boolean r9 = android.text.TextUtils.isEmpty(r28)
                if (r9 != 0) goto L_0x0049
                if (r3 != r0) goto L_0x003a
            L_0x0049:
                r8 = 3
                goto L_0x003a
            L_0x004b:
                r9 = 1
                if (r10 == r9) goto L_0x0058
                java.lang.String r8 = ""
                if (r1 == 0) goto L_0x0056
                java.lang.String r15 = ""
                r1.name = r15
            L_0x0056:
                r15 = r8
                goto L_0x005a
            L_0x0058:
                r15 = r28
            L_0x005a:
                r8 = 0
                r17 = 0
                if (r5 == 0) goto L_0x006b
                android.content.ComponentName r11 = r34.getComponentName()
                java.lang.String r8 = r11.flattenToString()
                java.lang.String r17 = r34.getId()
            L_0x006b:
                r11 = r8
                r8 = r17
                android.content.ContentValues r0 = new android.content.ContentValues
                r9 = 6
                r0.<init>(r9)
                r9 = r0
                java.lang.String r0 = "number"
                r9.put(r0, r15)
                java.lang.String r0 = "post_dial_digits"
                r20 = r13
                r13 = r29
                r9.put(r0, r13)
                java.lang.String r0 = "via_number"
                r13 = r30
                r9.put(r0, r13)
                java.lang.String r0 = "presentation"
                java.lang.Integer r3 = java.lang.Integer.valueOf(r10)
                r9.put(r0, r3)
                java.lang.String r0 = "type"
                java.lang.Integer r3 = java.lang.Integer.valueOf(r32)
                r9.put(r0, r3)
                java.lang.String r0 = "features"
                java.lang.Integer r3 = java.lang.Integer.valueOf(r33)
                r9.put(r0, r3)
                java.lang.String r0 = "date"
                java.lang.Long r3 = java.lang.Long.valueOf(r35)
                r9.put(r0, r3)
                java.lang.String r0 = "duration"
                long r2 = (long) r6
                java.lang.Long r2 = java.lang.Long.valueOf(r2)
                r9.put(r0, r2)
                if (r7 == 0) goto L_0x00c4
                java.lang.String r0 = "data_usage"
                r9.put(r0, r7)
            L_0x00c4:
                java.lang.String r0 = "subscription_component_name"
                r9.put(r0, r11)
                java.lang.String r0 = "subscription_id"
                r9.put(r0, r8)
                java.lang.String r0 = "phone_account_address"
                r9.put(r0, r12)
                java.lang.String r0 = "new"
                r2 = 1
                java.lang.Integer r3 = java.lang.Integer.valueOf(r2)
                r9.put(r0, r3)
                java.lang.String r0 = "add_for_all_users"
                java.lang.Integer r2 = java.lang.Integer.valueOf(r39)
                r9.put(r0, r2)
                r0 = 3
                if (r4 != r0) goto L_0x00f7
                java.lang.String r0 = "is_read"
                java.lang.Integer r2 = java.lang.Integer.valueOf(r41)
                r9.put(r0, r2)
            L_0x00f7:
                if (r1 == 0) goto L_0x01c1
                long r2 = r1.contactIdOrZero
                r21 = 0
                int r2 = (r2 > r21 ? 1 : (r2 == r21 ? 0 : -1))
                if (r2 <= 0) goto L_0x01c1
                java.lang.String r2 = r1.normalizedNumber
                if (r2 == 0) goto L_0x013b
                java.lang.String r2 = r1.normalizedNumber
                android.net.Uri r3 = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                java.lang.String r0 = "_id"
                java.lang.String[] r0 = new java.lang.String[]{r0}
                java.lang.String r17 = "contact_id =? AND data4 =?"
                r5 = 2
                java.lang.String[] r7 = new java.lang.String[r5]
                long r5 = r1.contactIdOrZero
                java.lang.String r5 = java.lang.String.valueOf(r5)
                r6 = 0
                r7[r6] = r5
                r5 = 1
                r7[r5] = r2
                r6 = 0
                r18 = r8
                r8 = r14
                r5 = r9
                r9 = r3
                r3 = r10
                r10 = r0
                r19 = r11
                r0 = 2
                r11 = r17
                r17 = r12
                r12 = r7
                r7 = r20
                r13 = r6
                android.database.Cursor r2 = r8.query(r9, r10, r11, r12, r13)
                r23 = r7
                goto L_0x0174
            L_0x013b:
                r18 = r8
                r5 = r9
                r3 = r10
                r19 = r11
                r17 = r12
                r7 = r20
                r0 = 2
                java.lang.String r2 = r1.phoneNumber
                if (r2 == 0) goto L_0x014d
                java.lang.String r2 = r1.phoneNumber
                goto L_0x014e
            L_0x014d:
                r2 = r15
            L_0x014e:
                android.net.Uri r6 = android.provider.ContactsContract.CommonDataKinds.Callable.CONTENT_FILTER_URI
                java.lang.String r8 = android.net.Uri.encode(r2)
                android.net.Uri r9 = android.net.Uri.withAppendedPath(r6, r8)
                java.lang.String r6 = "_id"
                java.lang.String[] r10 = new java.lang.String[]{r6}
                java.lang.String r11 = "contact_id =?"
                r6 = 1
                java.lang.String[] r12 = new java.lang.String[r6]
                r23 = r7
                long r6 = r1.contactIdOrZero
                java.lang.String r6 = java.lang.String.valueOf(r6)
                r7 = 0
                r12[r7] = r6
                r13 = 0
                r8 = r14
                android.database.Cursor r2 = r8.query(r9, r10, r11, r12, r13)
            L_0x0174:
                if (r2 == 0) goto L_0x01bb
                int r6 = r2.getCount()     // Catch:{ all -> 0x01b2 }
                if (r6 <= 0) goto L_0x01a9
                boolean r6 = r2.moveToFirst()     // Catch:{ all -> 0x01b2 }
                if (r6 == 0) goto L_0x01a9
                r6 = 0
                java.lang.String r7 = r2.getString(r6)     // Catch:{ all -> 0x01b2 }
                updateDataUsageStatForData(r14, r7)     // Catch:{ all -> 0x01b2 }
                r8 = 10000(0x2710, float:1.4013E-41)
                r9 = r37
                if (r9 < r8) goto L_0x01a6
                if (r4 != r0) goto L_0x01a6
                java.lang.String r0 = r1.normalizedNumber     // Catch:{ all -> 0x01a2 }
                boolean r0 = android.text.TextUtils.isEmpty(r0)     // Catch:{ all -> 0x01a2 }
                if (r0 == 0) goto L_0x01a6
                r8 = r27
                updateNormalizedNumber(r8, r14, r7, r15)     // Catch:{ all -> 0x01a0 }
                goto L_0x01ae
            L_0x01a0:
                r0 = move-exception
                goto L_0x01b7
            L_0x01a2:
                r0 = move-exception
                r8 = r27
                goto L_0x01b7
            L_0x01a6:
                r8 = r27
                goto L_0x01ae
            L_0x01a9:
                r6 = 0
                r8 = r27
                r9 = r37
            L_0x01ae:
                r2.close()
                goto L_0x01cf
            L_0x01b2:
                r0 = move-exception
                r8 = r27
                r9 = r37
            L_0x01b7:
                r2.close()
                throw r0
            L_0x01bb:
                r6 = 0
                r8 = r27
                r9 = r37
                goto L_0x01cf
            L_0x01c1:
                r18 = r8
                r5 = r9
                r3 = r10
                r19 = r11
                r17 = r12
                r23 = r20
                r8 = r27
                r9 = r6
                r6 = 0
            L_0x01cf:
                r0 = 0
                java.lang.Class<android.os.UserManager> r2 = android.os.UserManager.class
                java.lang.Object r2 = r8.getSystemService(r2)
                android.os.UserManager r2 = (android.os.UserManager) r2
                int r7 = r2.getUserHandle()
                if (r39 == 0) goto L_0x0249
                android.os.UserHandle r11 = android.os.UserHandle.SYSTEM
                android.net.Uri r11 = addEntryAndRemoveExpiredEntries(r8, r2, r11, r5)
                if (r11 == 0) goto L_0x0247
                java.lang.String r12 = "call_log_shadow"
                java.lang.String r13 = r11.getAuthority()
                boolean r12 = r12.equals(r13)
                if (r12 == 0) goto L_0x01f3
                goto L_0x0247
            L_0x01f3:
                if (r7 != 0) goto L_0x01f6
                r0 = r11
            L_0x01f6:
                r12 = 1
                java.util.List r12 = r2.getUsers(r12)
                if (r12 != 0) goto L_0x01ff
                r6 = 0
                return r6
            L_0x01ff:
                int r13 = r12.size()
            L_0x0204:
                if (r6 >= r13) goto L_0x0244
                java.lang.Object r16 = r12.get(r6)
                r24 = r0
                r0 = r16
                android.content.pm.UserInfo r0 = (android.content.pm.UserInfo) r0
                android.os.UserHandle r1 = r0.getUserHandle()
                r25 = r0
                int r0 = r1.getIdentifier()
                boolean r16 = r1.isSystem()
                if (r16 == 0) goto L_0x0221
                goto L_0x023d
            L_0x0221:
                boolean r16 = shouldHaveSharedCallLogEntries(r8, r2, r0)
                if (r16 != 0) goto L_0x0228
                goto L_0x023d
            L_0x0228:
                boolean r16 = r2.isUserRunning((android.os.UserHandle) r1)
                if (r16 == 0) goto L_0x023d
                boolean r16 = r2.isUserUnlocked((android.os.UserHandle) r1)
                if (r16 == 0) goto L_0x023d
                android.net.Uri r16 = addEntryAndRemoveExpiredEntries(r8, r2, r1, r5)
                if (r0 != r7) goto L_0x023d
                r0 = r16
                goto L_0x023f
            L_0x023d:
                r0 = r24
            L_0x023f:
                int r6 = r6 + 1
                r1 = r26
                goto L_0x0204
            L_0x0244:
                r24 = r0
                goto L_0x0257
            L_0x0247:
                r1 = 0
                return r1
            L_0x0249:
                if (r40 == 0) goto L_0x024f
                r6 = r40
                goto L_0x0253
            L_0x024f:
                android.os.UserHandle r6 = android.os.UserHandle.of(r7)
            L_0x0253:
                android.net.Uri r0 = addEntryAndRemoveExpiredEntries(r8, r2, r6, r5)
            L_0x0257:
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CallLog.Calls.addCall(com.android.internal.telephony.CallerInfo, android.content.Context, java.lang.String, java.lang.String, java.lang.String, int, int, int, android.telecom.PhoneAccountHandle, long, int, java.lang.Long, boolean, android.os.UserHandle, boolean):android.net.Uri");
        }

        public static boolean shouldHaveSharedCallLogEntries(Context context, UserManager userManager, int userId) {
            boolean z = false;
            if (userManager.hasUserRestriction(UserManager.DISALLOW_OUTGOING_CALLS, UserHandle.of(userId))) {
                return false;
            }
            UserInfo userInfo = userManager.getUserInfo(userId);
            if (userInfo != null && !userInfo.isManagedProfile()) {
                z = true;
            }
            return z;
        }

        public static String getLastOutgoingCall(Context context) {
            Cursor c = null;
            try {
                c = context.getContentResolver().query(CONTENT_URI, new String[]{"number"}, "type = 2", null, "date DESC LIMIT 1");
                if (c != null) {
                    if (c.moveToFirst()) {
                        String string = c.getString(0);
                        if (c != null) {
                            c.close();
                        }
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
                    resolver.delete(uri, "_id IN (SELECT _id FROM calls ORDER BY date DESC LIMIT -1 OFFSET 2000)", null);
                } else {
                    resolver.delete(uri, "_id IN (SELECT _id FROM calls WHERE subscription_component_name = ? AND subscription_id = ? ORDER BY date DESC LIMIT -1 OFFSET 2000)", new String[]{values.getAsString("subscription_component_name"), values.getAsString("subscription_id")});
                }
                return result;
            } catch (IllegalArgumentException e) {
                Log.w(CallLog.LOG_TAG, "Failed to insert calllog", e);
                return null;
            }
        }

        private static void updateDataUsageStatForData(ContentResolver resolver, String dataId) {
            resolver.update(ContactsContract.DataUsageFeedback.FEEDBACK_URI.buildUpon().appendPath(dataId).appendQueryParameter("type", ContactsContract.DataUsageFeedback.USAGE_TYPE_CALL).build(), new ContentValues(), null, null);
        }

        private static void updateNormalizedNumber(Context context, ContentResolver resolver, String dataId, String number) {
            if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(dataId) && !TextUtils.isEmpty(getCurrentCountryIso(context))) {
                String normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, getCurrentCountryIso(context));
                if (!TextUtils.isEmpty(normalizedNumber)) {
                    ContentValues values = new ContentValues();
                    values.put("data4", normalizedNumber);
                    resolver.update(ContactsContract.Data.CONTENT_URI, values, "_id=?", new String[]{dataId});
                }
            }
        }

        private static String getCurrentCountryIso(Context context) {
            CountryDetector detector = (CountryDetector) context.getSystemService("country_detector");
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
}
