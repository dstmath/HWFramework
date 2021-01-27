package com.huawei.kvdb;

import com.huawei.kvdb.HwKVDatabase;
import java.util.Optional;

/* renamed from: com.huawei.kvdb.-$$Lambda$HwKVDatabase$J7EW2KRUPghV7qiH6-bV5JGkYDs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwKVDatabase$J7EW2KRUPghV7qiH6bV5JGkYDs implements HwKVDatabase.ReadOperator {
    public static final /* synthetic */ $$Lambda$HwKVDatabase$J7EW2KRUPghV7qiH6bV5JGkYDs INSTANCE = new $$Lambda$HwKVDatabase$J7EW2KRUPghV7qiH6bV5JGkYDs();

    private /* synthetic */ $$Lambda$HwKVDatabase$J7EW2KRUPghV7qiH6bV5JGkYDs() {
    }

    @Override // com.huawei.kvdb.HwKVDatabase.ReadOperator
    public final Optional operate(HwKVConnection hwKVConnection) {
        return Optional.of(Integer.valueOf(hwKVConnection.getKeyNum()));
    }
}
