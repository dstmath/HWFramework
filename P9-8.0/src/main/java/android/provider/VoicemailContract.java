package android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.telecom.PhoneAccountHandle;
import android.telecom.Voicemail;
import android.util.LogException;
import java.util.List;

public class VoicemailContract {
    public static final String ACTION_FETCH_VOICEMAIL = "android.intent.action.FETCH_VOICEMAIL";
    public static final String ACTION_NEW_VOICEMAIL = "android.intent.action.NEW_VOICEMAIL";
    public static final String ACTION_SYNC_VOICEMAIL = "android.provider.action.SYNC_VOICEMAIL";
    public static final String ACTION_VOICEMAIL_SMS_RECEIVED = "com.android.internal.provider.action.VOICEMAIL_SMS_RECEIVED";
    public static final String AUTHORITY = "com.android.voicemail";
    public static final String EXTRA_PHONE_ACCOUNT_HANDLE = "android.provider.extra.PHONE_ACCOUNT_HANDLE";
    public static final String EXTRA_SELF_CHANGE = "com.android.voicemail.extra.SELF_CHANGE";
    public static final String EXTRA_VOICEMAIL_SMS = "android.provider.extra.VOICEMAIL_SMS";
    public static final String PARAM_KEY_SOURCE_PACKAGE = "source_package";
    public static final String SOURCE_PACKAGE_FIELD = "source_package";

    public static final class Status implements BaseColumns {
        public static final String CONFIGURATION_STATE = "configuration_state";
        public static final int CONFIGURATION_STATE_CAN_BE_CONFIGURED = 2;
        public static final int CONFIGURATION_STATE_CONFIGURING = 3;
        public static final int CONFIGURATION_STATE_DISABLED = 5;
        public static final int CONFIGURATION_STATE_FAILED = 4;
        public static final int CONFIGURATION_STATE_NOT_CONFIGURED = 1;
        public static final int CONFIGURATION_STATE_OK = 0;
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.voicemail/status");
        public static final String DATA_CHANNEL_STATE = "data_channel_state";
        public static final int DATA_CHANNEL_STATE_BAD_CONFIGURATION = 3;
        public static final int DATA_CHANNEL_STATE_COMMUNICATION_ERROR = 4;
        public static final int DATA_CHANNEL_STATE_NO_CONNECTION = 1;
        public static final int DATA_CHANNEL_STATE_NO_CONNECTION_CELLULAR_REQUIRED = 2;
        public static final int DATA_CHANNEL_STATE_OK = 0;
        public static final int DATA_CHANNEL_STATE_SERVER_CONNECTION_ERROR = 6;
        public static final int DATA_CHANNEL_STATE_SERVER_ERROR = 5;
        public static final String DIR_TYPE = "vnd.android.cursor.dir/voicemail.source.status";
        public static final String ITEM_TYPE = "vnd.android.cursor.item/voicemail.source.status";
        public static final String NOTIFICATION_CHANNEL_STATE = "notification_channel_state";
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
        public static final String SOURCE_TYPE = "source_type";
        public static final String VOICEMAIL_ACCESS_URI = "voicemail_access_uri";

        private Status() {
        }

        public static Uri buildSourceUri(String packageName) {
            return CONTENT_URI.buildUpon().appendQueryParameter("source_package", packageName).build();
        }
    }

    public static final class Voicemails implements BaseColumns, OpenableColumns {
        public static final String ARCHIVED = "archived";
        public static final String BACKED_UP = "backed_up";
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.voicemail/voicemail");
        public static final String DATE = "date";
        public static final String DELETED = "deleted";
        public static final String DIRTY = "dirty";
        public static final String DIR_TYPE = "vnd.android.cursor.dir/voicemails";
        public static final String DURATION = "duration";
        public static final String HAS_CONTENT = "has_content";
        public static final String IS_OMTP_VOICEMAIL = "is_omtp_voicemail";
        public static final String IS_READ = "is_read";
        public static final String ITEM_TYPE = "vnd.android.cursor.item/voicemail";
        public static final String LAST_MODIFIED = "last_modified";
        public static final String MIME_TYPE = "mime_type";
        public static final String NUMBER = "number";
        public static final String PHONE_ACCOUNT_COMPONENT_NAME = "subscription_component_name";
        public static final String PHONE_ACCOUNT_ID = "subscription_id";
        public static final String RESTORED = "restored";
        public static final String SOURCE_DATA = "source_data";
        public static final String SOURCE_PACKAGE = "source_package";
        public static final String STATE = "state";
        public static int STATE_DELETED = 1;
        public static int STATE_INBOX = 0;
        public static int STATE_UNDELETED = 2;
        public static final String TRANSCRIPTION = "transcription";
        public static final int TRANSCRIPTION_AVAILABLE = 3;
        public static final int TRANSCRIPTION_FAILED = 2;
        public static final int TRANSCRIPTION_IN_PROGRESS = 1;
        public static final int TRANSCRIPTION_NOT_STARTED = 0;
        public static final String TRANSCRIPTION_STATE = "transcription_state";
        public static final String _DATA = "_data";

        private Voicemails() {
        }

        public static Uri buildSourceUri(String packageName) {
            return CONTENT_URI.buildUpon().appendQueryParameter("source_package", packageName).build();
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
            return context.getContentResolver().delete(buildSourceUri(context.getPackageName()), LogException.NO_VALUE, new String[0]);
        }

        private static ContentValues getContentValues(Voicemail voicemail) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("date", String.valueOf(voicemail.getTimestampMillis()));
            contentValues.put("number", voicemail.getNumber());
            contentValues.put("duration", String.valueOf(voicemail.getDuration()));
            contentValues.put("source_package", voicemail.getSourcePackage());
            contentValues.put(SOURCE_DATA, voicemail.getSourceData());
            contentValues.put("is_read", Integer.valueOf(voicemail.isRead() ? 1 : 0));
            PhoneAccountHandle phoneAccount = voicemail.getPhoneAccount();
            if (phoneAccount != null) {
                contentValues.put("subscription_component_name", phoneAccount.getComponentName().flattenToString());
                contentValues.put("subscription_id", phoneAccount.getId());
            }
            if (voicemail.getTranscription() != null) {
                contentValues.put("transcription", voicemail.getTranscription());
            }
            return contentValues;
        }
    }

    private VoicemailContract() {
    }
}
