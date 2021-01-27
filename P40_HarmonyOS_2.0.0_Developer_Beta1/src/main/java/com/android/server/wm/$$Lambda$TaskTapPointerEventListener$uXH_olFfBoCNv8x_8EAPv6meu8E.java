package com.android.server.wm;

import com.huawei.android.app.HwActivityTaskManager;

/* renamed from: com.android.server.wm.-$$Lambda$TaskTapPointerEventListener$uXH_olFfBoCNv8x_8EAPv6meu8E  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskTapPointerEventListener$uXH_olFfBoCNv8x_8EAPv6meu8E implements Runnable {
    public static final /* synthetic */ $$Lambda$TaskTapPointerEventListener$uXH_olFfBoCNv8x_8EAPv6meu8E INSTANCE = new $$Lambda$TaskTapPointerEventListener$uXH_olFfBoCNv8x_8EAPv6meu8E();

    private /* synthetic */ $$Lambda$TaskTapPointerEventListener$uXH_olFfBoCNv8x_8EAPv6meu8E() {
    }

    @Override // java.lang.Runnable
    public final void run() {
        HwActivityTaskManager.updateFreeFormOutLine(1);
    }
}
