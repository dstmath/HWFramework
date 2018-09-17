package android.support.v4.provider;

import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

@RequiresApi(19)
class DocumentsContractApi19 {
    private static final int FLAG_VIRTUAL_DOCUMENT = 512;
    private static final String TAG = "DocumentFile";

    DocumentsContractApi19() {
    }

    public static boolean isDocumentUri(Context context, Uri self) {
        return DocumentsContract.isDocumentUri(context, self);
    }

    public static boolean isVirtual(Context context, Uri self) {
        boolean z = false;
        if (!isDocumentUri(context, self)) {
            return false;
        }
        if ((getFlags(context, self) & 512) != 0) {
            z = true;
        }
        return z;
    }

    public static String getName(Context context, Uri self) {
        return queryForString(context, self, "_display_name", null);
    }

    private static String getRawType(Context context, Uri self) {
        return queryForString(context, self, "mime_type", null);
    }

    public static String getType(Context context, Uri self) {
        String rawType = getRawType(context, self);
        if ("vnd.android.document/directory".equals(rawType)) {
            return null;
        }
        return rawType;
    }

    public static long getFlags(Context context, Uri self) {
        return queryForLong(context, self, "flags", 0);
    }

    public static boolean isDirectory(Context context, Uri self) {
        return "vnd.android.document/directory".equals(getRawType(context, self));
    }

    public static boolean isFile(Context context, Uri self) {
        String type = getRawType(context, self);
        if ("vnd.android.document/directory".equals(type) || TextUtils.isEmpty(type)) {
            return false;
        }
        return true;
    }

    public static long lastModified(Context context, Uri self) {
        return queryForLong(context, self, "last_modified", 0);
    }

    public static long length(Context context, Uri self) {
        return queryForLong(context, self, "_size", 0);
    }

    public static boolean canRead(Context context, Uri self) {
        return context.checkCallingOrSelfUriPermission(self, 1) == 0 && !TextUtils.isEmpty(getRawType(context, self));
    }

    public static boolean canWrite(Context context, Uri self) {
        if (context.checkCallingOrSelfUriPermission(self, 2) != 0) {
            return false;
        }
        String type = getRawType(context, self);
        int flags = queryForInt(context, self, "flags", 0);
        if (TextUtils.isEmpty(type)) {
            return false;
        }
        if ((flags & 4) != 0) {
            return true;
        }
        if (!"vnd.android.document/directory".equals(type) || (flags & 8) == 0) {
            return (TextUtils.isEmpty(type) || (flags & 2) == 0) ? false : true;
        } else {
            return true;
        }
    }

    public static boolean delete(Context context, Uri self) {
        try {
            return DocumentsContract.deleteDocument(context.getContentResolver(), self);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean exists(Context context, Uri self) {
        AutoCloseable c = null;
        try {
            c = context.getContentResolver().query(self, new String[]{"document_id"}, null, null, null);
            boolean z = c.getCount() > 0;
            closeQuietly(c);
            return z;
        } catch (Exception e) {
            Log.w(TAG, "Failed query: " + e);
            closeQuietly(c);
            return false;
        } catch (Throwable th) {
            closeQuietly(c);
            throw th;
        }
    }

    private static String queryForString(Context context, Uri self, String column, String defaultValue) {
        AutoCloseable autoCloseable = null;
        String string;
        try {
            autoCloseable = context.getContentResolver().query(self, new String[]{column}, null, null, null);
            if (!autoCloseable.moveToFirst() || (autoCloseable.isNull(0) ^ 1) == 0) {
                closeQuietly(autoCloseable);
                return defaultValue;
            }
            string = autoCloseable.getString(0);
            return string;
        } catch (Exception e) {
            string = TAG;
            Log.w(string, "Failed query: " + e);
            return defaultValue;
        } finally {
            closeQuietly(autoCloseable);
        }
    }

    private static int queryForInt(Context context, Uri self, String column, int defaultValue) {
        return (int) queryForLong(context, self, column, (long) defaultValue);
    }

    private static long queryForLong(Context context, Uri self, String column, long defaultValue) {
        AutoCloseable autoCloseable = null;
        long j;
        try {
            autoCloseable = context.getContentResolver().query(self, new String[]{column}, null, null, null);
            if (!autoCloseable.moveToFirst() || (autoCloseable.isNull(0) ^ 1) == 0) {
                closeQuietly(autoCloseable);
                return defaultValue;
            }
            j = autoCloseable.getLong(0);
            return j;
        } catch (Exception e) {
            String str = TAG;
            j = "Failed query: " + e;
            Log.w(str, j);
            return defaultValue;
        } finally {
            closeQuietly(autoCloseable);
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }
}
