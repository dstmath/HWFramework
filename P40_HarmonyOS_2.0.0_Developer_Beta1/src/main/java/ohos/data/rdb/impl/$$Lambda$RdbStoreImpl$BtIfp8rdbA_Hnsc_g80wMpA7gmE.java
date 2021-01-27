package ohos.data.rdb.impl;

import java.util.function.Predicate;
import ohos.data.rdb.ValuesBucket;

/* renamed from: ohos.data.rdb.impl.-$$Lambda$RdbStoreImpl$BtIfp8rdbA_Hnsc_g80wMpA7gmE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RdbStoreImpl$BtIfp8rdbA_Hnsc_g80wMpA7gmE implements Predicate {
    public static final /* synthetic */ $$Lambda$RdbStoreImpl$BtIfp8rdbA_Hnsc_g80wMpA7gmE INSTANCE = new $$Lambda$RdbStoreImpl$BtIfp8rdbA_Hnsc_g80wMpA7gmE();

    private /* synthetic */ $$Lambda$RdbStoreImpl$BtIfp8rdbA_Hnsc_g80wMpA7gmE() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return RdbStoreImpl.lambda$batchInsertOrThrowException$0((ValuesBucket) obj);
    }
}
