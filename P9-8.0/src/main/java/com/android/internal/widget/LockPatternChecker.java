package com.android.internal.widget;

import android.os.AsyncTask;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.LockPatternView.Cell;
import java.util.ArrayList;
import java.util.List;

public final class LockPatternChecker {

    public interface OnCheckCallback {
        void onChecked(boolean z, int i);

        /* renamed from: onEarlyMatched */
        void -com_android_internal_widget_LockPatternChecker$5-mthref-0() {
        }

        void onCancelled() {
        }
    }

    public interface OnVerifyCallback {
        void onVerified(byte[] bArr, int i);
    }

    public static AsyncTask<?, ?, ?> verifyPattern(LockPatternUtils utils, List<Cell> pattern, long challenge, int userId, OnVerifyCallback callback) {
        final List<Cell> list = pattern;
        final LockPatternUtils lockPatternUtils = utils;
        final long j = challenge;
        final int i = userId;
        final OnVerifyCallback onVerifyCallback = callback;
        AsyncTask<Void, Void, byte[]> task = new AsyncTask<Void, Void, byte[]>() {
            private int mThrottleTimeout;
            private List<Cell> patternCopy;

            protected void onPreExecute() {
                this.patternCopy = new ArrayList(list);
            }

            protected byte[] doInBackground(Void... args) {
                try {
                    return lockPatternUtils.verifyPattern(this.patternCopy, j, i);
                } catch (RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return null;
                }
            }

            protected void onPostExecute(byte[] result) {
                onVerifyCallback.onVerified(result, this.mThrottleTimeout);
            }
        };
        task.execute(new Void[0]);
        return task;
    }

    public static AsyncTask<?, ?, ?> checkPattern(final LockPatternUtils utils, final List<Cell> pattern, final int userId, final OnCheckCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;
            private List<Cell> patternCopy;

            protected void onPreExecute() {
                this.patternCopy = new ArrayList(pattern);
            }

            protected Boolean doInBackground(Void... args) {
                try {
                    LockPatternUtils lockPatternUtils = utils;
                    List list = this.patternCopy;
                    int i = userId;
                    OnCheckCallback onCheckCallback = callback;
                    onCheckCallback.getClass();
                    return Boolean.valueOf(lockPatternUtils.checkPattern(list, i, new -$Lambda$E2sSlgjiM2w1MdavtCJi6YeQRgk(onCheckCallback)));
                } catch (RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return Boolean.valueOf(false);
                }
            }

            protected void onPostExecute(Boolean result) {
                callback.onChecked(result.booleanValue(), this.mThrottleTimeout);
            }

            protected void onCancelled() {
                callback.onCancelled();
            }
        };
        task.execute(new Void[0]);
        return task;
    }

    public static AsyncTask<?, ?, ?> verifyPassword(LockPatternUtils utils, String password, long challenge, int userId, OnVerifyCallback callback) {
        final LockPatternUtils lockPatternUtils = utils;
        final String str = password;
        final long j = challenge;
        final int i = userId;
        final OnVerifyCallback onVerifyCallback = callback;
        AsyncTask<Void, Void, byte[]> task = new AsyncTask<Void, Void, byte[]>() {
            private int mThrottleTimeout;

            protected byte[] doInBackground(Void... args) {
                try {
                    return lockPatternUtils.verifyPassword(str, j, i);
                } catch (RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return null;
                }
            }

            protected void onPostExecute(byte[] result) {
                onVerifyCallback.onVerified(result, this.mThrottleTimeout);
            }
        };
        task.execute(new Void[0]);
        return task;
    }

    public static AsyncTask<?, ?, ?> verifyTiedProfileChallenge(LockPatternUtils utils, String password, boolean isPattern, long challenge, int userId, OnVerifyCallback callback) {
        final LockPatternUtils lockPatternUtils = utils;
        final String str = password;
        final boolean z = isPattern;
        final long j = challenge;
        final int i = userId;
        final OnVerifyCallback onVerifyCallback = callback;
        AsyncTask<Void, Void, byte[]> task = new AsyncTask<Void, Void, byte[]>() {
            private int mThrottleTimeout;

            protected byte[] doInBackground(Void... args) {
                try {
                    return lockPatternUtils.verifyTiedProfileChallenge(str, z, j, i);
                } catch (RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return null;
                }
            }

            protected void onPostExecute(byte[] result) {
                onVerifyCallback.onVerified(result, this.mThrottleTimeout);
            }
        };
        task.execute(new Void[0]);
        return task;
    }

    public static AsyncTask<?, ?, ?> checkPassword(final LockPatternUtils utils, final String password, final int userId, final OnCheckCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;

            protected Boolean doInBackground(Void... args) {
                try {
                    LockPatternUtils lockPatternUtils = utils;
                    String str = password;
                    int i = userId;
                    OnCheckCallback onCheckCallback = callback;
                    onCheckCallback.getClass();
                    return Boolean.valueOf(lockPatternUtils.checkPassword(str, i, new com.android.internal.widget.-$Lambda$E2sSlgjiM2w1MdavtCJi6YeQRgk.AnonymousClass1(onCheckCallback)));
                } catch (RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return Boolean.valueOf(false);
                }
            }

            protected void onPostExecute(Boolean result) {
                callback.onChecked(result.booleanValue(), this.mThrottleTimeout);
            }

            protected void onCancelled() {
                callback.onCancelled();
            }
        };
        task.execute(new Void[0]);
        return task;
    }
}
