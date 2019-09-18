package com.android.internal.widget;

import android.os.AsyncTask;
import android.util.Log;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LockPatternChecker {
    private static final String TAG = "LockPatternChecker";

    public interface OnCheckCallback {
        void onChecked(boolean z, int i);

        void onEarlyMatched() {
        }

        void onCancelled() {
        }
    }

    public interface OnVerifyCallback {
        void onVerified(byte[] bArr, int i);
    }

    public static AsyncTask<?, ?, ?> verifyPattern(LockPatternUtils utils, List<LockPatternView.Cell> pattern, long challenge, int userId, OnVerifyCallback callback) {
        final List<LockPatternView.Cell> list = pattern;
        final int i = userId;
        final LockPatternUtils lockPatternUtils = utils;
        final long j = challenge;
        final OnVerifyCallback onVerifyCallback = callback;
        AnonymousClass1 r0 = new AsyncTask<Void, Void, byte[]>() {
            private int mThrottleTimeout;
            private List<LockPatternView.Cell> patternCopy;

            /* access modifiers changed from: protected */
            public void onPreExecute() {
                this.patternCopy = new ArrayList(list);
            }

            /* access modifiers changed from: protected */
            public byte[] doInBackground(Void... args) {
                try {
                    Log.i(LockPatternChecker.TAG, "verifyPattern userId : " + i);
                    return lockPatternUtils.verifyPattern(this.patternCopy, j, i);
                } catch (LockPatternUtils.RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return null;
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(byte[] result) {
                onVerifyCallback.onVerified(result, this.mThrottleTimeout);
            }
        };
        r0.execute(new Void[0]);
        return r0;
    }

    public static AsyncTask<?, ?, ?> checkPattern(final LockPatternUtils utils, final List<LockPatternView.Cell> pattern, final int userId, final OnCheckCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;
            private List<LockPatternView.Cell> patternCopy;

            /* access modifiers changed from: protected */
            public void onPreExecute() {
                this.patternCopy = new ArrayList(pattern);
            }

            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... args) {
                try {
                    LockPatternUtils lockPatternUtils = utils;
                    List<LockPatternView.Cell> list = this.patternCopy;
                    int i = userId;
                    OnCheckCallback onCheckCallback = callback;
                    Objects.requireNonNull(onCheckCallback);
                    return Boolean.valueOf(lockPatternUtils.checkPattern(list, i, new LockPatternUtils.CheckCredentialProgressCallback() {
                        public final void onEarlyMatched() {
                            LockPatternChecker.OnCheckCallback.this.onEarlyMatched();
                        }
                    }));
                } catch (LockPatternUtils.RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return false;
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean result) {
                callback.onChecked(result.booleanValue(), this.mThrottleTimeout);
            }

            /* access modifiers changed from: protected */
            public void onCancelled() {
                callback.onCancelled();
            }
        };
        task.execute(new Void[0]);
        return task;
    }

    public static AsyncTask<?, ?, ?> verifyPassword(LockPatternUtils utils, String password, long challenge, int userId, OnVerifyCallback callback) {
        final int i = userId;
        final LockPatternUtils lockPatternUtils = utils;
        final String str = password;
        final long j = challenge;
        final OnVerifyCallback onVerifyCallback = callback;
        AnonymousClass3 r0 = new AsyncTask<Void, Void, byte[]>() {
            private int mThrottleTimeout;

            /* access modifiers changed from: protected */
            public byte[] doInBackground(Void... args) {
                try {
                    Log.i(LockPatternChecker.TAG, "verifyPassword userId : " + i);
                    return lockPatternUtils.verifyPassword(str, j, i);
                } catch (LockPatternUtils.RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return null;
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(byte[] result) {
                onVerifyCallback.onVerified(result, this.mThrottleTimeout);
            }
        };
        r0.execute(new Void[0]);
        return r0;
    }

    public static AsyncTask<?, ?, ?> verifyTiedProfileChallenge(LockPatternUtils utils, String password, boolean isPattern, long challenge, int userId, OnVerifyCallback callback) {
        final LockPatternUtils lockPatternUtils = utils;
        final String str = password;
        final boolean z = isPattern;
        final long j = challenge;
        final int i = userId;
        final OnVerifyCallback onVerifyCallback = callback;
        AnonymousClass4 r0 = new AsyncTask<Void, Void, byte[]>() {
            private int mThrottleTimeout;

            /* access modifiers changed from: protected */
            public byte[] doInBackground(Void... args) {
                try {
                    return LockPatternUtils.this.verifyTiedProfileChallenge(str, z, j, i);
                } catch (LockPatternUtils.RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return null;
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(byte[] result) {
                onVerifyCallback.onVerified(result, this.mThrottleTimeout);
            }
        };
        r0.execute(new Void[0]);
        return r0;
    }

    public static AsyncTask<?, ?, ?> checkPassword(final LockPatternUtils utils, final String password, final int userId, final OnCheckCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private int mThrottleTimeout;

            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... args) {
                try {
                    LockPatternUtils lockPatternUtils = LockPatternUtils.this;
                    String str = password;
                    int i = userId;
                    OnCheckCallback onCheckCallback = callback;
                    Objects.requireNonNull(onCheckCallback);
                    return Boolean.valueOf(lockPatternUtils.checkPassword(str, i, new LockPatternUtils.CheckCredentialProgressCallback() {
                        public final void onEarlyMatched() {
                            LockPatternChecker.OnCheckCallback.this.onEarlyMatched();
                        }
                    }));
                } catch (LockPatternUtils.RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return false;
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean result) {
                callback.onChecked(result.booleanValue(), this.mThrottleTimeout);
            }

            /* access modifiers changed from: protected */
            public void onCancelled() {
                callback.onCancelled();
            }
        };
        task.execute(new Void[0]);
        return task;
    }
}
