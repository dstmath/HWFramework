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
import com.android.internal.os.BackgroundThread;
import java.util.Set;

public class AwareHSMListHandler {
    private static final int COL_PKGNAME = 0;
    private static final int COL_PROTECTED = 1;
    private static final int COL_USER_SELECTED = 2;
    private static final String SMCS_AUTHORITY_URI = "content://";
    private static final String ST_PROTECTED_PKGS_TABLE = "smcs/st_protected_pkgs_table";
    private static final String TAG = "AwareHSMListHandler";
    private static final int UPDATE_APP_DELAYTIME = 5000;
    private static final int UPDATE_PROTECT_INFO_FROM_DB = 1;
    private final Set<String> mAllProtectAppSet;
    private final Set<String> mAllUnProtectAppSet;
    private Context mContext;
    private int mCurUserId;
    private final HSMHandler mHandler;
    private final ContentObserver mProtectAppChangeObserver;
    private final Set<String> mProtectAppSet;
    private final Set<String> mUnProtectAppSet;

    /* renamed from: com.android.server.rms.algorithm.AwareHSMListHandler.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            AwareHSMListHandler.this.updateProtectAppSet(AwareHSMListHandler.UPDATE_APP_DELAYTIME);
        }
    }

    private class HSMHandler extends Handler {
        public HSMHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AwareHSMListHandler.UPDATE_PROTECT_INFO_FROM_DB /*1*/:
                    updateProtectInfoFromDB();
                default:
            }
        }

        private void updateProtectInfoFromDB() {
            AwareLog.d(AwareHSMListHandler.TAG, "Get HSM list from DB.");
            if (AwareHSMListHandler.this.mContext != null) {
                Cursor cursor = null;
                ContentResolver resolver = AwareHSMListHandler.this.mContext.getContentResolver();
                if (resolver != null) {
                    Set<String> allProtectAppSet = new ArraySet();
                    Set<String> allUnProtectAppSet = new ArraySet();
                    synchronized (AwareHSMListHandler.this) {
                        AwareHSMListHandler.this.mProtectAppSet.clear();
                        AwareHSMListHandler.this.mUnProtectAppSet.clear();
                        AwareLog.d(AwareHSMListHandler.TAG, "Updating HSM data with userid " + AwareHSMListHandler.this.mCurUserId);
                        try {
                            cursor = resolver.query(Uri.parse(AwareHSMListHandler.SMCS_AUTHORITY_URI + AwareHSMListHandler.this.mCurUserId + "@" + AwareHSMListHandler.ST_PROTECTED_PKGS_TABLE), null, null, null, null);
                            if (cursor == null) {
                                if (cursor != null) {
                                    cursor.close();
                                }
                                return;
                            }
                            while (cursor.moveToNext()) {
                                String pkgName = cursor.getString(AwareHSMListHandler.COL_PKGNAME);
                                int isProtected = cursor.getInt(AwareHSMListHandler.UPDATE_PROTECT_INFO_FROM_DB);
                                int isSelectByUser = cursor.getInt(AwareHSMListHandler.COL_USER_SELECTED);
                                if (pkgName != null) {
                                    if (AwareHSMListHandler.UPDATE_PROTECT_INFO_FROM_DB == isProtected) {
                                        allProtectAppSet.add(pkgName);
                                    } else if (isProtected == 0) {
                                        allUnProtectAppSet.add(pkgName);
                                    }
                                    if (AwareHSMListHandler.UPDATE_PROTECT_INFO_FROM_DB != isSelectByUser) {
                                        continue;
                                    } else if (AwareHSMListHandler.UPDATE_PROTECT_INFO_FROM_DB == isProtected) {
                                        AwareHSMListHandler.this.mProtectAppSet.add(pkgName);
                                    } else if (isProtected == 0) {
                                        AwareHSMListHandler.this.mUnProtectAppSet.add(pkgName);
                                    } else {
                                        continue;
                                    }
                                }
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                            AwareLog.d(AwareHSMListHandler.TAG, "get protect Set " + AwareHSMListHandler.this.mProtectAppSet + ", and unprotected Set " + AwareHSMListHandler.this.mUnProtectAppSet);
                            synchronized (AwareHSMListHandler.this.mAllProtectAppSet) {
                                AwareHSMListHandler.this.mAllProtectAppSet.clear();
                                AwareHSMListHandler.this.mAllProtectAppSet.addAll(allProtectAppSet);
                            }
                            synchronized (AwareHSMListHandler.this.mAllUnProtectAppSet) {
                                AwareHSMListHandler.this.mAllUnProtectAppSet.clear();
                                AwareHSMListHandler.this.mAllUnProtectAppSet.addAll(allUnProtectAppSet);
                            }
                        } catch (IllegalArgumentException e) {
                            AwareLog.e(AwareHSMListHandler.TAG, "Exception when getProtectAppFromDB.");
                            if (cursor != null) {
                                cursor.close();
                            }
                        } catch (SQLiteException e2) {
                            AwareLog.e(AwareHSMListHandler.TAG, "Error: load HSM protectlist failed!");
                            if (cursor != null) {
                                cursor.close();
                            }
                        } catch (Throwable th) {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                }
            }
        }
    }

    public AwareHSMListHandler(Context context) {
        this.mAllProtectAppSet = new ArraySet();
        this.mAllUnProtectAppSet = new ArraySet();
        this.mProtectAppSet = new ArraySet();
        this.mUnProtectAppSet = new ArraySet();
        this.mHandler = new HSMHandler(BackgroundThread.get().getLooper());
        this.mCurUserId = COL_PKGNAME;
        this.mProtectAppChangeObserver = new AnonymousClass1(null);
        this.mContext = null;
        this.mContext = context;
    }

    void init() {
        startObserver();
        updateProtectAppSet(COL_PKGNAME);
    }

    void deinit() {
        stopObserver();
        synchronized (this) {
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
        synchronized (this) {
            this.mCurUserId = userId;
        }
        updateProtectAppSet(UPDATE_APP_DELAYTIME);
    }

    private void startObserver() {
        AwareLog.i(TAG, "UserHabit HSM db provider observer started.");
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://smcs/st_protected_pkgs_table"), true, this.mProtectAppChangeObserver, -1);
        }
    }

    private void stopObserver() {
        if (this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mProtectAppChangeObserver);
            AwareLog.i(TAG, "UserHabit HSM db provider observer stopped.");
        }
    }

    Set<String> getProtectSet() {
        ArraySet<String> result = new ArraySet();
        synchronized (this) {
            result.addAll(this.mProtectAppSet);
        }
        return result;
    }

    Set<String> getAllProtectSet() {
        ArraySet<String> result = new ArraySet();
        synchronized (this.mAllProtectAppSet) {
            result.addAll(this.mAllProtectAppSet);
        }
        return result;
    }

    Set<String> getAllUnProtectSet() {
        ArraySet<String> result = new ArraySet();
        synchronized (this.mAllUnProtectAppSet) {
            result.addAll(this.mAllUnProtectAppSet);
        }
        return result;
    }

    Set<String> getUnProtectSet() {
        ArraySet<String> result = new ArraySet();
        synchronized (this) {
            result.addAll(this.mUnProtectAppSet);
        }
        return result;
    }

    private void updateProtectAppSet(int postTime) {
        if (this.mHandler.hasMessages(UPDATE_PROTECT_INFO_FROM_DB)) {
            this.mHandler.removeMessages(UPDATE_PROTECT_INFO_FROM_DB);
        }
        this.mHandler.sendEmptyMessageDelayed(UPDATE_PROTECT_INFO_FROM_DB, (long) postTime);
    }
}
