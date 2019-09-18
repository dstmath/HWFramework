package com.android.server.location;

import android.hardware.location.ContextHubTransaction;
import android.hardware.location.NanoAppState;
import java.util.List;
import java.util.concurrent.TimeUnit;

abstract class ContextHubServiceTransaction {
    private boolean mIsComplete = false;
    private final int mTransactionId;
    private final int mTransactionType;

    /* access modifiers changed from: package-private */
    public abstract int onTransact();

    ContextHubServiceTransaction(int id, int type) {
        this.mTransactionId = id;
        this.mTransactionType = type;
    }

    /* access modifiers changed from: package-private */
    public void onTransactionComplete(int result) {
    }

    /* access modifiers changed from: package-private */
    public void onQueryResponse(int result, List<NanoAppState> list) {
    }

    /* access modifiers changed from: package-private */
    public int getTransactionId() {
        return this.mTransactionId;
    }

    /* access modifiers changed from: package-private */
    public int getTransactionType() {
        return this.mTransactionType;
    }

    /* access modifiers changed from: package-private */
    public long getTimeout(TimeUnit unit) {
        return this.mTransactionType != 0 ? unit.convert(5, TimeUnit.SECONDS) : unit.convert(30, TimeUnit.SECONDS);
    }

    /* access modifiers changed from: package-private */
    public void setComplete() {
        this.mIsComplete = true;
    }

    /* access modifiers changed from: package-private */
    public boolean isComplete() {
        return this.mIsComplete;
    }

    public String toString() {
        return ContextHubTransaction.typeToString(this.mTransactionType, true) + " transaction (ID = " + this.mTransactionId + ")";
    }
}
