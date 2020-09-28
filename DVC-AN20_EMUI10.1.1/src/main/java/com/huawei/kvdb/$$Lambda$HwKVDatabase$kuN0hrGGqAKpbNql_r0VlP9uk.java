package com.huawei.kvdb;

import com.huawei.kvdb.HwKVDatabase;
import java.util.Optional;

/* renamed from: com.huawei.kvdb.-$$Lambda$HwKVDatabase$ku-N0hrGGqA-KpbNql_r0VlP9uk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwKVDatabase$kuN0hrGGqAKpbNql_r0VlP9uk implements HwKVDatabase.ReadOperator {
    public static final /* synthetic */ $$Lambda$HwKVDatabase$kuN0hrGGqAKpbNql_r0VlP9uk INSTANCE = new $$Lambda$HwKVDatabase$kuN0hrGGqAKpbNql_r0VlP9uk();

    private /* synthetic */ $$Lambda$HwKVDatabase$kuN0hrGGqAKpbNql_r0VlP9uk() {
    }

    @Override // com.huawei.kvdb.HwKVDatabase.ReadOperator
    public final Optional operate(HwKVConnection hwKVConnection) {
        return Optional.ofNullable(hwKVConnection.getAllKeys());
    }
}
