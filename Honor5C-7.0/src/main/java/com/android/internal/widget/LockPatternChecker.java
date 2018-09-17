package com.android.internal.widget;

import android.os.AsyncTask;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.LockPatternView.Cell;
import java.util.List;

public final class LockPatternChecker {

    /* renamed from: com.android.internal.widget.LockPatternChecker.1 */
    static class AnonymousClass1 extends AsyncTask<Void, Void, byte[]> {
        private int mThrottleTimeout;
        final /* synthetic */ OnVerifyCallback val$callback;
        final /* synthetic */ long val$challenge;
        final /* synthetic */ List val$pattern;
        final /* synthetic */ int val$userId;
        final /* synthetic */ LockPatternUtils val$utils;

        AnonymousClass1(LockPatternUtils val$utils, List val$pattern, long val$challenge, int val$userId, OnVerifyCallback val$callback) {
            this.val$utils = val$utils;
            this.val$pattern = val$pattern;
            this.val$challenge = val$challenge;
            this.val$userId = val$userId;
            this.val$callback = val$callback;
        }

        protected byte[] doInBackground(Void... args) {
            try {
                return this.val$utils.verifyPattern(this.val$pattern, this.val$challenge, this.val$userId);
            } catch (RequestThrottledException ex) {
                this.mThrottleTimeout = ex.getTimeoutMs();
                return null;
            }
        }

        protected void onPostExecute(byte[] result) {
            this.val$callback.onVerified(result, this.mThrottleTimeout);
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternChecker.2 */
    static class AnonymousClass2 extends AsyncTask<Void, Void, Boolean> {
        private int mThrottleTimeout;
        final /* synthetic */ OnCheckCallback val$callback;
        final /* synthetic */ List val$pattern;
        final /* synthetic */ int val$userId;
        final /* synthetic */ LockPatternUtils val$utils;

        AnonymousClass2(LockPatternUtils val$utils, List val$pattern, int val$userId, OnCheckCallback val$callback) {
            this.val$utils = val$utils;
            this.val$pattern = val$pattern;
            this.val$userId = val$userId;
            this.val$callback = val$callback;
        }

        protected Boolean doInBackground(Void... args) {
            try {
                return Boolean.valueOf(this.val$utils.checkPattern(this.val$pattern, this.val$userId));
            } catch (RequestThrottledException ex) {
                this.mThrottleTimeout = ex.getTimeoutMs();
                return Boolean.valueOf(false);
            }
        }

        protected void onPostExecute(Boolean result) {
            this.val$callback.onChecked(result.booleanValue(), this.mThrottleTimeout);
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternChecker.3 */
    static class AnonymousClass3 extends AsyncTask<Void, Void, byte[]> {
        private int mThrottleTimeout;
        final /* synthetic */ OnVerifyCallback val$callback;
        final /* synthetic */ long val$challenge;
        final /* synthetic */ String val$password;
        final /* synthetic */ int val$userId;
        final /* synthetic */ LockPatternUtils val$utils;

        AnonymousClass3(LockPatternUtils val$utils, String val$password, long val$challenge, int val$userId, OnVerifyCallback val$callback) {
            this.val$utils = val$utils;
            this.val$password = val$password;
            this.val$challenge = val$challenge;
            this.val$userId = val$userId;
            this.val$callback = val$callback;
        }

        protected byte[] doInBackground(Void... args) {
            try {
                return this.val$utils.verifyPassword(this.val$password, this.val$challenge, this.val$userId);
            } catch (RequestThrottledException ex) {
                this.mThrottleTimeout = ex.getTimeoutMs();
                return null;
            }
        }

        protected void onPostExecute(byte[] result) {
            this.val$callback.onVerified(result, this.mThrottleTimeout);
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternChecker.4 */
    static class AnonymousClass4 extends AsyncTask<Void, Void, byte[]> {
        private int mThrottleTimeout;
        final /* synthetic */ OnVerifyCallback val$callback;
        final /* synthetic */ long val$challenge;
        final /* synthetic */ boolean val$isPattern;
        final /* synthetic */ String val$password;
        final /* synthetic */ int val$userId;
        final /* synthetic */ LockPatternUtils val$utils;

        AnonymousClass4(LockPatternUtils val$utils, String val$password, boolean val$isPattern, long val$challenge, int val$userId, OnVerifyCallback val$callback) {
            this.val$utils = val$utils;
            this.val$password = val$password;
            this.val$isPattern = val$isPattern;
            this.val$challenge = val$challenge;
            this.val$userId = val$userId;
            this.val$callback = val$callback;
        }

        protected byte[] doInBackground(Void... args) {
            try {
                return this.val$utils.verifyTiedProfileChallenge(this.val$password, this.val$isPattern, this.val$challenge, this.val$userId);
            } catch (RequestThrottledException ex) {
                this.mThrottleTimeout = ex.getTimeoutMs();
                return null;
            }
        }

        protected void onPostExecute(byte[] result) {
            this.val$callback.onVerified(result, this.mThrottleTimeout);
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternChecker.5 */
    static class AnonymousClass5 extends AsyncTask<Void, Void, Boolean> {
        private int mThrottleTimeout;
        final /* synthetic */ OnCheckCallback val$callback;
        final /* synthetic */ String val$password;
        final /* synthetic */ int val$userId;
        final /* synthetic */ LockPatternUtils val$utils;

        AnonymousClass5(LockPatternUtils val$utils, String val$password, int val$userId, OnCheckCallback val$callback) {
            this.val$utils = val$utils;
            this.val$password = val$password;
            this.val$userId = val$userId;
            this.val$callback = val$callback;
        }

        protected Boolean doInBackground(Void... args) {
            try {
                return Boolean.valueOf(this.val$utils.checkPassword(this.val$password, this.val$userId));
            } catch (RequestThrottledException ex) {
                this.mThrottleTimeout = ex.getTimeoutMs();
                return Boolean.valueOf(false);
            }
        }

        protected void onPostExecute(Boolean result) {
            this.val$callback.onChecked(result.booleanValue(), this.mThrottleTimeout);
        }
    }

    public interface OnCheckCallback {
        void onChecked(boolean z, int i);
    }

    public interface OnVerifyCallback {
        void onVerified(byte[] bArr, int i);
    }

    public static AsyncTask<?, ?, ?> verifyPattern(LockPatternUtils utils, List<Cell> pattern, long challenge, int userId, OnVerifyCallback callback) {
        AsyncTask<Void, Void, byte[]> task = new AnonymousClass1(utils, pattern, challenge, userId, callback);
        task.execute(new Void[0]);
        return task;
    }

    public static AsyncTask<?, ?, ?> checkPattern(LockPatternUtils utils, List<Cell> pattern, int userId, OnCheckCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AnonymousClass2(utils, pattern, userId, callback);
        task.execute(new Void[0]);
        return task;
    }

    public static AsyncTask<?, ?, ?> verifyPassword(LockPatternUtils utils, String password, long challenge, int userId, OnVerifyCallback callback) {
        AsyncTask<Void, Void, byte[]> task = new AnonymousClass3(utils, password, challenge, userId, callback);
        task.execute(new Void[0]);
        return task;
    }

    public static AsyncTask<?, ?, ?> verifyTiedProfileChallenge(LockPatternUtils utils, String password, boolean isPattern, long challenge, int userId, OnVerifyCallback callback) {
        AsyncTask<Void, Void, byte[]> task = new AnonymousClass4(utils, password, isPattern, challenge, userId, callback);
        task.execute(new Void[0]);
        return task;
    }

    public static AsyncTask<?, ?, ?> checkPassword(LockPatternUtils utils, String password, int userId, OnCheckCallback callback) {
        AsyncTask<Void, Void, Boolean> task = new AnonymousClass5(utils, password, userId, callback);
        task.execute(new Void[0]);
        return task;
    }
}
