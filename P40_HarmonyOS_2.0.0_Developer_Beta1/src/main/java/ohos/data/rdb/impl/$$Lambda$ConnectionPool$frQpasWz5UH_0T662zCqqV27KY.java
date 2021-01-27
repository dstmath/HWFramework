package ohos.data.rdb.impl;

import java.util.Comparator;
import ohos.data.rdb.impl.ConnectionPool;

/* renamed from: ohos.data.rdb.impl.-$$Lambda$ConnectionPool$frQpasWz5UH_0T662zCqq-V27KY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ConnectionPool$frQpasWz5UH_0T662zCqqV27KY implements Comparator {
    public static final /* synthetic */ $$Lambda$ConnectionPool$frQpasWz5UH_0T662zCqqV27KY INSTANCE = new $$Lambda$ConnectionPool$frQpasWz5UH_0T662zCqqV27KY();

    private /* synthetic */ $$Lambda$ConnectionPool$frQpasWz5UH_0T662zCqqV27KY() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return ConnectionPool.lambda$static$0((ConnectionPool.Waiter) obj, (ConnectionPool.Waiter) obj2);
    }
}
