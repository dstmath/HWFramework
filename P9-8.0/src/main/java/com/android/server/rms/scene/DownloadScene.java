package com.android.server.rms.scene;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.IScene;

public class DownloadScene implements IScene {
    private static final String TAG = "RMS.DownloadScene";
    private final Context mContext;

    public DownloadScene(Context context) {
        this.mContext = context;
    }

    public boolean identify(Bundle extras) {
        if (this.mContext == null) {
            return false;
        }
        DownloadManager dm = (DownloadManager) this.mContext.getSystemService("download");
        if (dm == null) {
            Log.w(TAG, "DownloadScene dm is null");
            return false;
        }
        Cursor cursor = null;
        try {
            Query query = new Query();
            query.setFilterByStatus(3);
            cursor = dm.query(query);
            if (cursor == null) {
                Log.w(TAG, "DownloadScene cursor is null");
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            boolean ret = cursor.getCount() > 0;
            if (Utils.DEBUG) {
                Log.d(TAG, "DownloadScene state is " + ret);
            }
            if (cursor != null) {
                cursor.close();
            }
            return ret;
        } catch (Exception e) {
            Log.e(TAG, "DownloadManager Query failed:", e);
            if (cursor != null) {
                cursor.close();
            }
            return false;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }
}
