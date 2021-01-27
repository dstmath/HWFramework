package com.huawei.kvdb;

import com.huawei.kvdb.HwKVDatabase;
import java.util.Optional;

/* renamed from: com.huawei.kvdb.-$$Lambda$HwKVDatabase$FfiNH65RptV7OtUNdS6EecnQFqA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwKVDatabase$FfiNH65RptV7OtUNdS6EecnQFqA implements HwKVDatabase.ReadOperator {
    public static final /* synthetic */ $$Lambda$HwKVDatabase$FfiNH65RptV7OtUNdS6EecnQFqA INSTANCE = new $$Lambda$HwKVDatabase$FfiNH65RptV7OtUNdS6EecnQFqA();

    private /* synthetic */ $$Lambda$HwKVDatabase$FfiNH65RptV7OtUNdS6EecnQFqA() {
    }

    @Override // com.huawei.kvdb.HwKVDatabase.ReadOperator
    public final Optional operate(HwKVConnection hwKVConnection) {
        return Optional.ofNullable(hwKVConnection.getAllKeysGeneral());
    }
}
