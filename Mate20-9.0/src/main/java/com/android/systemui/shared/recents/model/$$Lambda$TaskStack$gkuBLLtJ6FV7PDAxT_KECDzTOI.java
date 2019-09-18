package com.android.systemui.shared.recents.model;

import android.util.SparseArray;

/* renamed from: com.android.systemui.shared.recents.model.-$$Lambda$TaskStack$gkuBLLtJ6FV7PDAxT-_KECDzTOI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskStack$gkuBLLtJ6FV7PDAxT_KECDzTOI implements TaskFilter {
    public static final /* synthetic */ $$Lambda$TaskStack$gkuBLLtJ6FV7PDAxT_KECDzTOI INSTANCE = new $$Lambda$TaskStack$gkuBLLtJ6FV7PDAxT_KECDzTOI();

    private /* synthetic */ $$Lambda$TaskStack$gkuBLLtJ6FV7PDAxT_KECDzTOI() {
    }

    public final boolean acceptTask(SparseArray sparseArray, Task task, int i) {
        return task.isStackTask;
    }
}
