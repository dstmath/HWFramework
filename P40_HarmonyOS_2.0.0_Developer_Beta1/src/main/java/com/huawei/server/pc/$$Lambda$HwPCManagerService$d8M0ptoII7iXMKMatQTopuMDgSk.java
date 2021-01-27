package com.huawei.server.pc;

import android.widget.CompoundButton;

/* renamed from: com.huawei.server.pc.-$$Lambda$HwPCManagerService$d8M0ptoII7iXMKMatQTopuMDgSk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwPCManagerService$d8M0ptoII7iXMKMatQTopuMDgSk implements CompoundButton.OnCheckedChangeListener {
    public static final /* synthetic */ $$Lambda$HwPCManagerService$d8M0ptoII7iXMKMatQTopuMDgSk INSTANCE = new $$Lambda$HwPCManagerService$d8M0ptoII7iXMKMatQTopuMDgSk();

    private /* synthetic */ $$Lambda$HwPCManagerService$d8M0ptoII7iXMKMatQTopuMDgSk() {
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        HwPCManagerService.lambda$showExitDesktopAlertDialog$4(compoundButton, z);
    }
}
