package android.provider;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;

public final class Downloads {
    public static final String CALL_CREATE_EXTERNAL_PUBLIC_DIR = "create_external_public_dir";
    public static final String CALL_MEDIASTORE_DOWNLOADS_DELETED = "mediastore_downloads_deleted";
    public static final String CALL_REVOKE_MEDIASTORE_URI_PERMS = "revoke_mediastore_uri_perms";
    public static final String DIR_TYPE = "dir_type";
    public static final String EXTRA_IDS = "ids";
    public static final String EXTRA_MIME_TYPES = "mime_types";
    private static final String QUERY_WHERE_CLAUSE = "notificationpackage=? AND notificationclass=?";

    private Downloads() {
    }

    public static final class Impl implements BaseColumns {
        public static final String ACTION_DOWNLOAD_COMPLETED = "android.intent.action.DOWNLOAD_COMPLETED";
        public static final String ACTION_NOTIFICATION_CLICKED = "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
        @UnsupportedAppUsage
        public static final Uri ALL_DOWNLOADS_CONTENT_URI = Uri.parse("content://downloads/all_downloads");
        public static final String AUTHORITY = "downloads";
        @UnsupportedAppUsage
        public static final String COLUMN_ALLOWED_NETWORK_TYPES = "allowed_network_types";
        public static final String COLUMN_ALLOW_METERED = "allow_metered";
        @UnsupportedAppUsage
        public static final String COLUMN_ALLOW_ROAMING = "allow_roaming";
        public static final String COLUMN_ALLOW_WRITE = "allow_write";
        public static final String COLUMN_APP_DATA = "entity";
        public static final String COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT = "bypass_recommended_size_limit";
        public static final String COLUMN_CONTROL = "control";
        @UnsupportedAppUsage
        public static final String COLUMN_COOKIE_DATA = "cookiedata";
        public static final String COLUMN_CURRENT_BYTES = "current_bytes";
        @UnsupportedAppUsage
        public static final String COLUMN_DELETED = "deleted";
        @UnsupportedAppUsage
        public static final String COLUMN_DESCRIPTION = "description";
        @UnsupportedAppUsage
        public static final String COLUMN_DESTINATION = "destination";
        public static final String COLUMN_ERROR_MSG = "errorMsg";
        public static final String COLUMN_FAILED_CONNECTIONS = "numfailed";
        @UnsupportedAppUsage
        public static final String COLUMN_FILE_NAME_HINT = "hint";
        public static final String COLUMN_FLAGS = "flags";
        @UnsupportedAppUsage
        public static final String COLUMN_IS_PUBLIC_API = "is_public_api";
        @UnsupportedAppUsage
        public static final String COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI = "is_visible_in_downloads_ui";
        public static final String COLUMN_LAST_MODIFICATION = "lastmod";
        public static final String COLUMN_LAST_UPDATESRC = "lastUpdateSrc";
        public static final String COLUMN_MEDIAPROVIDER_URI = "mediaprovider_uri";
        public static final String COLUMN_MEDIASTORE_URI = "mediastore_uri";
        @UnsupportedAppUsage
        public static final String COLUMN_MEDIA_SCANNED = "scanned";
        @UnsupportedAppUsage
        public static final String COLUMN_MIME_TYPE = "mimetype";
        @UnsupportedAppUsage
        public static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";
        @UnsupportedAppUsage
        public static final String COLUMN_NOTIFICATION_EXTRAS = "notificationextras";
        @UnsupportedAppUsage
        public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
        public static final String COLUMN_NO_INTEGRITY = "no_integrity";
        public static final String COLUMN_OTHER_UID = "otheruid";
        @UnsupportedAppUsage
        public static final String COLUMN_REFERER = "referer";
        public static final String COLUMN_STATUS = "status";
        @UnsupportedAppUsage
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TOTAL_BYTES = "total_bytes";
        @UnsupportedAppUsage
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_USER_AGENT = "useragent";
        @UnsupportedAppUsage
        public static final String COLUMN_VISIBILITY = "visibility";
        @UnsupportedAppUsage
        public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
        public static final int CONTROL_PAUSED = 1;
        public static final int CONTROL_RUN = 0;
        public static final int DESTINATION_CACHE_PARTITION = 1;
        public static final int DESTINATION_CACHE_PARTITION_NOROAMING = 3;
        @UnsupportedAppUsage
        public static final int DESTINATION_CACHE_PARTITION_PURGEABLE = 2;
        public static final int DESTINATION_EXTERNAL = 0;
        @UnsupportedAppUsage
        public static final int DESTINATION_FILE_URI = 4;
        public static final int DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD = 6;
        @Deprecated
        public static final int DESTINATION_SYSTEMCACHE_PARTITION = 5;
        public static final int FLAG_REQUIRES_CHARGING = 1;
        public static final int FLAG_REQUIRES_DEVICE_IDLE = 2;
        public static final int LAST_UPDATESRC_DONT_NOTIFY_DOWNLOADSVC = 1;
        public static final int LAST_UPDATESRC_NOT_RELEVANT = 0;
        public static final int MEDIA_NOT_SCANNABLE = 2;
        public static final int MEDIA_NOT_SCANNED = 0;
        public static final int MEDIA_SCANNED = 1;
        public static final int MIN_ARTIFICIAL_ERROR_STATUS = 488;
        public static final String PERMISSION_ACCESS = "android.permission.ACCESS_DOWNLOAD_MANAGER";
        public static final String PERMISSION_ACCESS_ADVANCED = "android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED";
        public static final String PERMISSION_ACCESS_ALL = "android.permission.ACCESS_ALL_DOWNLOADS";
        public static final String PERMISSION_CACHE = "android.permission.ACCESS_CACHE_FILESYSTEM";
        public static final String PERMISSION_CACHE_NON_PURGEABLE = "android.permission.DOWNLOAD_CACHE_NON_PURGEABLE";
        public static final String PERMISSION_NO_NOTIFICATION = "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION";
        public static final String PERMISSION_SEND_INTENTS = "android.permission.SEND_DOWNLOAD_COMPLETED_INTENTS";
        @UnsupportedAppUsage
        public static final Uri PUBLICLY_ACCESSIBLE_DOWNLOADS_URI = Uri.parse("content://downloads/public_downloads");
        public static final String PUBLICLY_ACCESSIBLE_DOWNLOADS_URI_SEGMENT = "public_downloads";
        public static final int STATUS_BAD_REQUEST = 400;
        @Deprecated
        public static final int STATUS_BLOCKED = 498;
        public static final int STATUS_CANCELED = 490;
        public static final int STATUS_CANNOT_RESUME = 489;
        public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 199;
        public static final int STATUS_FILE_ALREADY_EXISTS_ERROR = 488;
        public static final int STATUS_FILE_ERROR = 492;
        public static final int STATUS_HTTP_DATA_ERROR = 495;
        public static final int STATUS_HTTP_EXCEPTION = 496;
        public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 198;
        public static final int STATUS_LENGTH_REQUIRED = 411;
        public static final int STATUS_NOT_ACCEPTABLE = 406;
        public static final int STATUS_PAUSED_BY_APP = 193;
        public static final int STATUS_PENDING = 190;
        public static final int STATUS_PRECONDITION_FAILED = 412;
        public static final int STATUS_QUEUED_FOR_WIFI = 196;
        public static final int STATUS_RUNNING = 192;
        public static final int STATUS_SUCCESS = 200;
        public static final int STATUS_TOO_MANY_REDIRECTS = 497;
        public static final int STATUS_UNHANDLED_HTTP_CODE = 494;
        public static final int STATUS_UNHANDLED_REDIRECT = 493;
        public static final int STATUS_UNKNOWN_ERROR = 491;
        public static final int STATUS_WAITING_FOR_NETWORK = 195;
        public static final int STATUS_WAITING_TO_RETRY = 194;
        public static final int VISIBILITY_HIDDEN = 2;
        public static final int VISIBILITY_VISIBLE = 0;
        public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
        public static final String _DATA = "_data";

        public static class RequestHeaders {
            public static final String COLUMN_DOWNLOAD_ID = "download_id";
            public static final String COLUMN_HEADER = "header";
            public static final String COLUMN_VALUE = "value";
            public static final String HEADERS_DB_TABLE = "request_headers";
            @UnsupportedAppUsage
            public static final String INSERT_KEY_PREFIX = "http_header_";
            public static final String URI_SEGMENT = "headers";
        }

        private Impl() {
        }

        public static boolean isStatusInformational(int status) {
            return status >= 100 && status < 200;
        }

        @UnsupportedAppUsage
        public static boolean isStatusSuccess(int status) {
            return status >= 200 && status < 300;
        }

        @UnsupportedAppUsage
        public static boolean isStatusError(int status) {
            return status >= 400 && status < 600;
        }

        public static boolean isStatusClientError(int status) {
            return status >= 400 && status < 500;
        }

        public static boolean isStatusServerError(int status) {
            return status >= 500 && status < 600;
        }

        @UnsupportedAppUsage
        public static boolean isNotificationToBeDisplayed(int visibility) {
            return visibility == 1 || visibility == 3;
        }

        @UnsupportedAppUsage
        public static boolean isStatusCompleted(int status) {
            return (status >= 200 && status < 300) || (status >= 400 && status < 600);
        }

        public static String statusToString(int status) {
            if (status == 190) {
                return "PENDING";
            }
            if (status == 400) {
                return "BAD_REQUEST";
            }
            if (status == 406) {
                return "NOT_ACCEPTABLE";
            }
            if (status == 411) {
                return "LENGTH_REQUIRED";
            }
            if (status == 412) {
                return "PRECONDITION_FAILED";
            }
            switch (status) {
                case 192:
                    return "RUNNING";
                case 193:
                    return "PAUSED_BY_APP";
                case 194:
                    return "WAITING_TO_RETRY";
                case 195:
                    return "WAITING_FOR_NETWORK";
                case 196:
                    return "QUEUED_FOR_WIFI";
                default:
                    switch (status) {
                        case 198:
                            return "INSUFFICIENT_SPACE_ERROR";
                        case 199:
                            return "DEVICE_NOT_FOUND_ERROR";
                        case 200:
                            return WifiManager.PPPOE_RESULT_SUCCESS;
                        default:
                            switch (status) {
                                case 488:
                                    return "FILE_ALREADY_EXISTS_ERROR";
                                case 489:
                                    return "CANNOT_RESUME";
                                case 490:
                                    return "CANCELED";
                                case 491:
                                    return "UNKNOWN_ERROR";
                                case 492:
                                    return "FILE_ERROR";
                                case 493:
                                    return "UNHANDLED_REDIRECT";
                                case 494:
                                    return "UNHANDLED_HTTP_CODE";
                                case 495:
                                    return "HTTP_DATA_ERROR";
                                case 496:
                                    return "HTTP_EXCEPTION";
                                case STATUS_TOO_MANY_REDIRECTS /*{ENCODED_INT: 497}*/:
                                    return "TOO_MANY_REDIRECTS";
                                case 498:
                                    return "BLOCKED";
                                default:
                                    return Integer.toString(status);
                            }
                    }
            }
        }
    }

    public static final void removeAllDownloadsByPackage(Context context, String notification_package, String notification_class) {
        context.getContentResolver().delete(Impl.CONTENT_URI, QUERY_WHERE_CLAUSE, new String[]{notification_package, notification_class});
    }
}
