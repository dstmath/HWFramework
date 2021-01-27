package ohos.bundlemgr.webability;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class WebPackagesResolver {
    private static final String APP_DATA_PATH_COLUMN_NAME = "app_load_cache_path";
    private static final String APP_ICON_COLUMN_NAME = "app_icon";
    private static final String APP_NAME_COLUMN_NAME = "app_name";
    private static final String APP_TYPE_COLUMN_NAME = "app_type";
    private static final String PACKAGE_NAME_COLUMN_NAME = "app_package_name";
    private static final String PACKAGE_SELECTION_CLAUSE = "app_package_name=?";
    private static final String[] PROJECTION = {PACKAGE_NAME_COLUMN_NAME, APP_NAME_COLUMN_NAME, APP_ICON_COLUMN_NAME, "app_type", APP_DATA_PATH_COLUMN_NAME};
    private static final String TAG = "WebPackagesResolver";
    private static final String WEB_ABILITY_CONTENT_URI = "content://com.huawei.fastapp.provider/installed_app_info/";
    private static final HiLogLabel WEB_RESOLVER_LABEL = new HiLogLabel(3, 218108160, TAG);
    private ContentResolver contentResolver = null;

    public WebPackagesResolver(Context context) {
        if (context != null) {
            this.contentResolver = context.getContentResolver();
        } else {
            AppLog.e(WEB_RESOLVER_LABEL, "Context is null", new Object[0]);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0093, code lost:
        if (r3 != null) goto L_0x00aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00a8, code lost:
        if (0 == 0) goto L_0x00ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00aa, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00ad, code lost:
        return r0;
     */
    public List<WebPackageInfo> getPackages(String str) {
        String[] strArr;
        String str2;
        ArrayList arrayList = new ArrayList();
        if (this.contentResolver == null) {
            AppLog.d(WEB_RESOLVER_LABEL, "contentResolver is null.", new Object[0]);
            return arrayList;
        }
        Cursor cursor = null;
        if (str != null) {
            strArr = new String[]{str};
            str2 = PACKAGE_SELECTION_CLAUSE;
        } else {
            str2 = null;
            strArr = null;
        }
        try {
            cursor = this.contentResolver.query(Uri.parse(WEB_ABILITY_CONTENT_URI), PROJECTION, str2, strArr, null);
            if (cursor != null) {
                int columnIndexOrThrow = cursor.getColumnIndexOrThrow(PACKAGE_NAME_COLUMN_NAME);
                int columnIndexOrThrow2 = cursor.getColumnIndexOrThrow(APP_NAME_COLUMN_NAME);
                int columnIndexOrThrow3 = cursor.getColumnIndexOrThrow(APP_ICON_COLUMN_NAME);
                int columnIndexOrThrow4 = cursor.getColumnIndexOrThrow("app_type");
                int columnIndexOrThrow5 = cursor.getColumnIndexOrThrow(APP_DATA_PATH_COLUMN_NAME);
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    WebPackageInfo webPackageInfo = new WebPackageInfo();
                    webPackageInfo.setPackageName(cursor.getString(columnIndexOrThrow));
                    webPackageInfo.setAppName(cursor.getString(columnIndexOrThrow2));
                    webPackageInfo.setIcon(cursor.getString(columnIndexOrThrow3));
                    webPackageInfo.setAppType(cursor.getString(columnIndexOrThrow4));
                    webPackageInfo.setDataPath(cursor.getString(columnIndexOrThrow5));
                    arrayList.add(webPackageInfo);
                }
            } else {
                AppLog.w(WEB_RESOLVER_LABEL, "cursor is null while query package %{public}s", str);
            }
        } catch (SecurityException e) {
            AppLog.w(WEB_RESOLVER_LABEL, "Query content got a SecurityException: %{public}s", e.getMessage());
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }
}
