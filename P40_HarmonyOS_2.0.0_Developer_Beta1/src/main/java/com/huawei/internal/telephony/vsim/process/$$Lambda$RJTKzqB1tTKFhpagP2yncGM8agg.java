package com.huawei.internal.telephony.vsim.process;

import com.huawei.internal.telephony.vsim.HwVSimMtkController;
import java.util.function.Consumer;

/* renamed from: com.huawei.internal.telephony.vsim.process.-$$Lambda$RJTKzqB1tTKFhpagP2yncGM8agg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RJTKzqB1tTKFhpagP2yncGM8agg implements Consumer {
    public static final /* synthetic */ $$Lambda$RJTKzqB1tTKFhpagP2yncGM8agg INSTANCE = new $$Lambda$RJTKzqB1tTKFhpagP2yncGM8agg();

    private /* synthetic */ $$Lambda$RJTKzqB1tTKFhpagP2yncGM8agg() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((HwVSimMtkController) obj).cmdSemRelease();
    }
}
