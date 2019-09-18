package com.android.server.security.panpay;

import com.android.server.security.panpay.factoryreset.FactroyResetFlag;

/* renamed from: com.android.server.security.panpay.-$$Lambda$zs-vdS7cWTK70W24jx4p2lR189s  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$zsvdS7cWTK70W24jx4p2lR189s implements Runnable {
    public static final /* synthetic */ $$Lambda$zsvdS7cWTK70W24jx4p2lR189s INSTANCE = new $$Lambda$zsvdS7cWTK70W24jx4p2lR189s();

    private /* synthetic */ $$Lambda$zsvdS7cWTK70W24jx4p2lR189s() {
    }

    public final void run() {
        FactroyResetFlag.clear();
    }
}
