package com.huawei.displayengine;

import huawei.cust.HwCfgFilePolicy;
import java.util.function.Supplier;

/* renamed from: com.huawei.displayengine.-$$Lambda$ImageProcessorAlgoImpl$ojMOCJyTk7r1_npxvyMBpAP2GHk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImageProcessorAlgoImpl$ojMOCJyTk7r1_npxvyMBpAP2GHk implements Supplier {
    public static final /* synthetic */ $$Lambda$ImageProcessorAlgoImpl$ojMOCJyTk7r1_npxvyMBpAP2GHk INSTANCE = new $$Lambda$ImageProcessorAlgoImpl$ojMOCJyTk7r1_npxvyMBpAP2GHk();

    private /* synthetic */ $$Lambda$ImageProcessorAlgoImpl$ojMOCJyTk7r1_npxvyMBpAP2GHk() {
    }

    @Override // java.util.function.Supplier
    public final Object get() {
        return HwCfgFilePolicy.getCfgFile("display/effect/algorithm/imageprocessor/ImageProcessAlgoParam.xml", 0);
    }
}
