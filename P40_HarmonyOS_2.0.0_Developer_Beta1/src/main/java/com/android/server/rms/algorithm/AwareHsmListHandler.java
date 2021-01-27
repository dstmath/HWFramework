package com.android.server.rms.algorithm;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.util.Set;

public class AwareHsmListHandler {
    private static final int COL_PKG_NAME = 0;
    private static final int COL_PROTECTED = 1;
    private static final int COL_USER_SELECTED = 2;
    private static final String SMCS_AUTHORITY_URI = "content://";
    private static final String ST_PROTECTED_PKGS_TABLE = "smcs/st_protected_pkgs_table";
    private static final String TAG = "AwareHSMListHandler";
    private static final int UPDATE_APP_DELAY_TIME = 5000;
    private static final int UPDATE_PROTECT_INFO_FROM_DB = 1;
    private final Set<String> mAllProtectAppSet = new ArraySet();
    private final Set<String> mAllUnProtectAppSet = new ArraySet();
    private Context mContext = null;
    private int mCurUserId = 0;
    private HsmHandler mHandler = null;
    private final Object mLock = new Object();
    private final ContentObserver mProtectAppChangeObserver = new ContentObserver(null) {
        /* class com.android.server.rms.algorithm.AwareHsmListHandler.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            AwareHsmListHandler.this.updateProtectAppSet(AwareHsmListHandler.UPDATE_APP_DELAY_TIME);
        }
    };
    private final Set<String> mProtectAppSet = new ArraySet();
    private final Set<String> mUnProtectAppSet = new ArraySet();

    public AwareHsmListHandler(Context context) {
        this.mContext = context;
        initHandler();
    }

    /* access modifiers changed from: package-private */
    public void init() {
        startObserver();
        updateProtectAppSet(0);
    }

    /* access modifiers changed from: package-private */
    public void deInit() {
        stopObserver();
        synchronized (this.mLock) {
            this.mProtectAppSet.clear();
            this.mUnProtectAppSet.clear();
        }
        synchronized (this.mAllProtectAppSet) {
            this.mAllProtectAppSet.clear();
        }
        synchronized (this.mAllUnProtectAppSet) {
            this.mAllUnProtectAppSet.clear();
        }
    }

    public void setUserId(int userId) {
        synchronized (this.mLock) {
            this.mCurUserId = userId;
        }
        updateProtectAppSet(UPDATE_APP_DELAY_TIME);
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHandler = new HsmHandler(looper);
        } else {
            this.mHandler = new HsmHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public class HsmHandler extends Handler {
        public HsmHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                updateProtectInfoFromDb();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00ca, code lost:
            if (0 == 0) goto L_0x00cd;
         */
        private void updateProtectInfoFromDb() {
            ContentResolver resolver;
            AwareLog.d(AwareHsmListHandler.TAG, "Get Hsm list from DB.");
            if (AwareHsmListHandler.this.mContext != null && (resolver = AwareHsmListHandler.this.mContext.getContentResolver()) != null) {
                Set<String> allProtectAppSet = new ArraySet<>();
                Set<String> allUnProtectAppSet = new ArraySet<>();
                Cursor cursor = null;
                synchronized (AwareHsmListHandler.this.mLock) {
                    AwareHsmListHandler.this.mProtectAppSet.clear();
                    AwareHsmListHandler.this.mUnProtectAppSet.clear();
                    AwareLog.d(AwareHsmListHandler.TAG, "Updating Hsm data with userid " + AwareHsmListHandler.this.mCurUserId);
                    String[] projection = {"pkg_name", "is_checked", "userchanged"};
                    try {
                        cursor = resolver.query(Uri.parse(AwareHsmListHandler.SMCS_AUTHORITY_URI + AwareHsmListHandler.this.mCurUserId + "@" + AwareHsmListHandler.ST_PROTECTED_PKGS_TABLE), projection, null, null, null);
                        if (cursor == null) {
                            if (cursor != null) {
                                cursor.close();
                            }
                            return;
                        }
                        AwareHsmListHandler.this.addPkgNameToSet(allProtectAppSet, allUnProtectAppSet, cursor);
                        cursor.close();
                        AwareLog.d(AwareHsmListHandler.TAG, "get protect Set " + AwareHsmListHandler.this.mProtectAppSet + ", and unprotected Set " + AwareHsmListHandler.this.mUnProtectAppSet);
                        AwareHsmListHandler.this.updateAppSet(allProtectAppSet, allUnProtectAppSet);
                    } catch (IllegalArgumentException e) {
                        AwareLog.e(AwareHsmListHandler.TAG, "Exception when getProtectAppFromDB.");
                    } catch (IllegalStateException e2) {
                        AwareLog.e(AwareHsmListHandler.TAG, "IllegalStateException: load Hsm protectlist failed!");
                        if (0 != 0) {
                        }
                    } catch (SQLiteException e3) {
                        AwareLog.e(AwareHsmListHandler.TAG, "Error: load Hsm protectlist failed!");
                        if (0 != 0) {
                        }
                    } catch (Throwable th) {
                        if (0 != 0) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addPkgNameToSet(Set<String> allProtectAppSet, Set<String> allUnProtectAppSet, Cursor cursor) {
        while (cursor.moveToNext()) {
            String pkgName = cursor.getString(0);
            int isProtected = cursor.getInt(1);
            int isSelectByUser = cursor.getInt(2);
            if (pkgName != null) {
                if (isProtected == 1) {
                    allProtectAppSet.add(pkgName);
                }
                if (isProtected == 0) {
                    allUnProtectAppSet.add(pkgName);
                }
                if (isSelectByUser == 1) {
                    if (isProtected == 1) {
                        this.mProtectAppSet.add(pkgName);
                    }
                    if (isProtected == 0) {
                        this.mUnProtectAppSet.add(pkgName);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAppSet(Set<String> allProtectAppSet, Set<String> allUnProtectAppSet) {
        synchronized (this.mAllProtectAppSet) {
            this.mAllProtectAppSet.clear();
            this.mAllProtectAppSet.addAll(allProtectAppSet);
        }
        synchronized (this.mAllUnProtectAppSet) {
            this.mAllUnProtectAppSet.clear();
            this.mAllUnProtectAppSet.addAll(allUnProtectAppSet);
        }
    }

    private void startObserver() {
        AwareLog.i(TAG, "UserHabit Hsm db provider observer started.");
        Context context = this.mContext;
        if (context != null) {
            ContentResolverExt.registerContentObserver(context.getContentResolver(), Uri.parse("content://smcs/st_protected_pkgs_table"), true, this.mProtectAppChangeObserver, -1);
        }
    }

    private void stopObserver() {
        Context context = this.mContext;
        if (context != null) {
            context.getContentResolver().unregisterContentObserver(this.mProtectAppChangeObserver);
            AwareLog.i(TAG, "UserHabit Hsm db provider observer stopped.");
        }
    }

    /* access modifiers changed from: package-private */
    public Set<String> getProtectSet() {
        ArraySet<String> result = new ArraySet<>();
        synchronized (this.mLock) {
            result.addAll(this.mProtectAppSet);
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public Set<String> getAllProtectSet() {
        ArraySet<String> result = new ArraySet<>();
        synchronized (this.mAllProtectAppSet) {
            result.addAll(this.mAllProtectAppSet);
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public Set<String> getAllUnProtectSet() {
        ArraySet<String> result = new ArraySet<>();
        synchronized (this.mAllUnProtectAppSet) {
            result.addAll(this.mAllUnProtectAppSet);
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public Set<String> getUnProtectSet() {
        ArraySet<String> result = new ArraySet<>();
        synchronized (this.mLock) {
            result.addAll(this.mUnProtectAppSet);
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateProtectAppSet(int postTime) {
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
        this.mHandler.sendEmptyMessageDelayed(1, (long) postTime);
    }
}
