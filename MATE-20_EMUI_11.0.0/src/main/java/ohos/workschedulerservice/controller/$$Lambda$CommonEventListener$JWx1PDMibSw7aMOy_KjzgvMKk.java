package ohos.workschedulerservice.controller;

import java.util.function.Predicate;
import ohos.workschedulerservice.controller.CommonEventListener;

/* renamed from: ohos.workschedulerservice.controller.-$$Lambda$CommonEventListener$JWx1PDMibSw7aMOy_-Kjz-gvMKk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$CommonEventListener$JWx1PDMibSw7aMOy_KjzgvMKk implements Predicate {
    public static final /* synthetic */ $$Lambda$CommonEventListener$JWx1PDMibSw7aMOy_KjzgvMKk INSTANCE = new $$Lambda$CommonEventListener$JWx1PDMibSw7aMOy_KjzgvMKk();

    private /* synthetic */ $$Lambda$CommonEventListener$JWx1PDMibSw7aMOy_KjzgvMKk() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((CommonEventListener.SubscriberStatus) obj).isNeedRemove();
    }
}
