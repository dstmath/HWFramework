package com.android.internal.util;

import android.os.Bundle;
import android.os.Parcelable;
import com.android.internal.os.IResultReceiver;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class SyncResultReceiver extends IResultReceiver.Stub {
    private static final String EXTRA = "EXTRA";
    private Bundle mBundle;
    private final CountDownLatch mLatch = new CountDownLatch(1);
    private int mResult;
    private final int mTimeoutMs;

    public SyncResultReceiver(int timeoutMs) {
        this.mTimeoutMs = timeoutMs;
    }

    private void waitResult() throws TimeoutException {
        try {
            if (!this.mLatch.await((long) this.mTimeoutMs, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("Not called in " + this.mTimeoutMs + "ms");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TimeoutException("Interrupted");
        }
    }

    public int getIntResult() throws TimeoutException {
        waitResult();
        return this.mResult;
    }

    public String getStringResult() throws TimeoutException {
        waitResult();
        Bundle bundle = this.mBundle;
        if (bundle == null) {
            return null;
        }
        return bundle.getString(EXTRA);
    }

    public String[] getStringArrayResult() throws TimeoutException {
        waitResult();
        Bundle bundle = this.mBundle;
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArray(EXTRA);
    }

    public <P extends Parcelable> P getParcelableResult() throws TimeoutException {
        waitResult();
        Bundle bundle = this.mBundle;
        if (bundle == null) {
            return null;
        }
        return (P) bundle.getParcelable(EXTRA);
    }

    public <P extends Parcelable> ArrayList<P> getParcelableListResult() throws TimeoutException {
        waitResult();
        Bundle bundle = this.mBundle;
        if (bundle == null) {
            return null;
        }
        return bundle.getParcelableArrayList(EXTRA);
    }

    public int getOptionalExtraIntResult(int defaultValue) throws TimeoutException {
        waitResult();
        Bundle bundle = this.mBundle;
        if (bundle == null || !bundle.containsKey(EXTRA)) {
            return defaultValue;
        }
        return this.mBundle.getInt(EXTRA);
    }

    @Override // com.android.internal.os.IResultReceiver
    public void send(int resultCode, Bundle resultData) {
        this.mResult = resultCode;
        this.mBundle = resultData;
        this.mLatch.countDown();
    }

    public static Bundle bundleFor(String value) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA, value);
        return bundle;
    }

    public static Bundle bundleFor(String[] value) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(EXTRA, value);
        return bundle;
    }

    public static Bundle bundleFor(Parcelable value) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA, value);
        return bundle;
    }

    public static Bundle bundleFor(ArrayList<? extends Parcelable> value) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA, value);
        return bundle;
    }

    public static Bundle bundleFor(int value) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA, value);
        return bundle;
    }

    public static final class TimeoutException extends RuntimeException {
        private TimeoutException(String msg) {
            super(msg);
        }
    }
}
