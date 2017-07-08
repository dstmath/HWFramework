package android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.ProxyInfo;
import android.net.Uri;
import android.telecom.PhoneAccountHandle;
import android.telecom.Voicemail;
import java.util.List;

public class VoicemailContract {
    public static final String ACTION_FETCH_VOICEMAIL = "android.intent.action.FETCH_VOICEMAIL";
    public static final String ACTION_NEW_VOICEMAIL = "android.intent.action.NEW_VOICEMAIL";
    public static final String ACTION_SYNC_VOICEMAIL = "android.provider.action.SYNC_VOICEMAIL";
    public static final String AUTHORITY = "com.android.voicemail";
    public static final String EXTRA_SELF_CHANGE = "com.android.voicemail.extra.SELF_CHANGE";
    public static final String PARAM_KEY_SOURCE_PACKAGE = "source_package";
    public static final String SOURCE_PACKAGE_FIELD = "source_package";

    public static final class Status implements BaseColumns {
        public static final String CONFIGURATION_STATE = "configuration_state";
        public static final int CONFIGURATION_STATE_CAN_BE_CONFIGURED = 2;
        public static final int CONFIGURATION_STATE_IGNORE = -1;
        public static final int CONFIGURATION_STATE_NOT_CONFIGURED = 1;
        public static final int CONFIGURATION_STATE_OK = 0;
        public static final Uri CONTENT_URI = null;
        public static final String DATA_CHANNEL_STATE = "data_channel_state";
        public static final int DATA_CHANNEL_STATE_BAD_CONFIGURATION = 3;
        public static final int DATA_CHANNEL_STATE_COMMUNICATION_ERROR = 4;
        public static final int DATA_CHANNEL_STATE_IGNORE = -1;
        public static final int DATA_CHANNEL_STATE_NO_CONNECTION = 1;
        public static final int DATA_CHANNEL_STATE_NO_CONNECTION_CELLULAR_REQUIRED = 2;
        public static final int DATA_CHANNEL_STATE_OK = 0;
        public static final int DATA_CHANNEL_STATE_SERVER_CONNECTION_ERROR = 6;
        public static final int DATA_CHANNEL_STATE_SERVER_ERROR = 5;
        public static final String DIR_TYPE = "vnd.android.cursor.dir/voicemail.source.status";
        public static final String ITEM_TYPE = "vnd.android.cursor.item/voicemail.source.status";
        public static final String NOTIFICATION_CHANNEL_STATE = "notification_channel_state";
        public static final int NOTIFICATION_CHANNEL_STATE_IGNORE = -1;
        public static final int NOTIFICATION_CHANNEL_STATE_MESSAGE_WAITING = 2;
        public static final int NOTIFICATION_CHANNEL_STATE_NO_CONNECTION = 1;
        public static final int NOTIFICATION_CHANNEL_STATE_OK = 0;
        public static final String PHONE_ACCOUNT_COMPONENT_NAME = "phone_account_component_name";
        public static final String PHONE_ACCOUNT_ID = "phone_account_id";
        public static final String QUOTA_OCCUPIED = "quota_occupied";
        public static final String QUOTA_TOTAL = "quota_total";
        public static final int QUOTA_UNAVAILABLE = -1;
        public static final String SETTINGS_URI = "settings_uri";
        public static final String SOURCE_PACKAGE = "source_package";
        public static final String VOICEMAIL_ACCESS_URI = "voicemail_access_uri";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.VoicemailContract.Status.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.VoicemailContract.Status.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.VoicemailContract.Status.<clinit>():void");
        }

        private Status() {
        }

        public static Uri buildSourceUri(String packageName) {
            return CONTENT_URI.buildUpon().appendQueryParameter(SOURCE_PACKAGE, packageName).build();
        }

        public static void setStatus(Context context, PhoneAccountHandle accountHandle, int configurationState, int dataChannelState, int notificationChannelState) {
            ContentValues values = new ContentValues();
            values.put(PHONE_ACCOUNT_COMPONENT_NAME, accountHandle.getComponentName().flattenToString());
            values.put(PHONE_ACCOUNT_ID, accountHandle.getId());
            if (configurationState != QUOTA_UNAVAILABLE) {
                values.put(CONFIGURATION_STATE, Integer.valueOf(configurationState));
            }
            if (dataChannelState != QUOTA_UNAVAILABLE) {
                values.put(DATA_CHANNEL_STATE, Integer.valueOf(dataChannelState));
            }
            if (notificationChannelState != QUOTA_UNAVAILABLE) {
                values.put(NOTIFICATION_CHANNEL_STATE, Integer.valueOf(notificationChannelState));
            }
            context.getContentResolver().insert(buildSourceUri(context.getPackageName()), values);
        }

        public static void setQuota(Context context, PhoneAccountHandle accountHandle, int occupied, int total) {
            if (occupied != QUOTA_UNAVAILABLE || total != QUOTA_UNAVAILABLE) {
                ContentValues values = new ContentValues();
                values.put(PHONE_ACCOUNT_COMPONENT_NAME, accountHandle.getComponentName().flattenToString());
                values.put(PHONE_ACCOUNT_ID, accountHandle.getId());
                if (occupied != QUOTA_UNAVAILABLE) {
                    values.put(QUOTA_OCCUPIED, Integer.valueOf(occupied));
                }
                if (total != QUOTA_UNAVAILABLE) {
                    values.put(QUOTA_TOTAL, Integer.valueOf(total));
                }
                context.getContentResolver().insert(buildSourceUri(context.getPackageName()), values);
            }
        }
    }

    public static final class Voicemails implements BaseColumns, OpenableColumns {
        public static final Uri CONTENT_URI = null;
        public static final String DATE = "date";
        public static final String DELETED = "deleted";
        public static final String DIRTY = "dirty";
        public static final String DIR_TYPE = "vnd.android.cursor.dir/voicemails";
        public static final String DURATION = "duration";
        public static final String HAS_CONTENT = "has_content";
        public static final String IS_READ = "is_read";
        public static final String ITEM_TYPE = "vnd.android.cursor.item/voicemail";
        public static final String LAST_MODIFIED = "last_modified";
        public static final String MIME_TYPE = "mime_type";
        public static final String NUMBER = "number";
        public static final String PHONE_ACCOUNT_COMPONENT_NAME = "subscription_component_name";
        public static final String PHONE_ACCOUNT_ID = "subscription_id";
        public static final String SOURCE_DATA = "source_data";
        public static final String SOURCE_PACKAGE = "source_package";
        public static final String STATE = "state";
        public static int STATE_DELETED = 0;
        public static int STATE_INBOX = 0;
        public static int STATE_UNDELETED = 0;
        public static final String TRANSCRIPTION = "transcription";
        public static final String _DATA = "_data";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.VoicemailContract.Voicemails.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.VoicemailContract.Voicemails.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.VoicemailContract.Voicemails.<clinit>():void");
        }

        private Voicemails() {
        }

        public static Uri buildSourceUri(String packageName) {
            return CONTENT_URI.buildUpon().appendQueryParameter(SOURCE_PACKAGE, packageName).build();
        }

        public static Uri insert(Context context, Voicemail voicemail) {
            return context.getContentResolver().insert(buildSourceUri(context.getPackageName()), getContentValues(voicemail));
        }

        public static int insert(Context context, List<Voicemail> voicemails) {
            ContentResolver contentResolver = context.getContentResolver();
            int count = voicemails.size();
            for (int i = 0; i < count; i++) {
                contentResolver.insert(buildSourceUri(context.getPackageName()), getContentValues((Voicemail) voicemails.get(i)));
            }
            return count;
        }

        public static int deleteAll(Context context) {
            return context.getContentResolver().delete(buildSourceUri(context.getPackageName()), ProxyInfo.LOCAL_EXCL_LIST, new String[0]);
        }

        private static ContentValues getContentValues(Voicemail voicemail) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DATE, String.valueOf(voicemail.getTimestampMillis()));
            contentValues.put(NUMBER, voicemail.getNumber());
            contentValues.put(DURATION, String.valueOf(voicemail.getDuration()));
            contentValues.put(SOURCE_PACKAGE, voicemail.getSourcePackage());
            contentValues.put(SOURCE_DATA, voicemail.getSourceData());
            contentValues.put(IS_READ, Integer.valueOf(voicemail.isRead() ? 1 : 0));
            PhoneAccountHandle phoneAccount = voicemail.getPhoneAccount();
            if (phoneAccount != null) {
                contentValues.put(PHONE_ACCOUNT_COMPONENT_NAME, phoneAccount.getComponentName().flattenToString());
                contentValues.put(PHONE_ACCOUNT_ID, phoneAccount.getId());
            }
            if (voicemail.getTranscription() != null) {
                contentValues.put(TRANSCRIPTION, voicemail.getTranscription());
            }
            return contentValues;
        }
    }

    private VoicemailContract() {
    }
}
