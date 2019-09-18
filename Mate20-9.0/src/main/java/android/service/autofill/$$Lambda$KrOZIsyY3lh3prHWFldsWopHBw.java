package android.service.autofill;

import com.android.internal.util.function.TriConsumer;

/* renamed from: android.service.autofill.-$$Lambda$KrOZIsyY-3lh3prHWFldsWopHBw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$KrOZIsyY3lh3prHWFldsWopHBw implements TriConsumer {
    public static final /* synthetic */ $$Lambda$KrOZIsyY3lh3prHWFldsWopHBw INSTANCE = new $$Lambda$KrOZIsyY3lh3prHWFldsWopHBw();

    private /* synthetic */ $$Lambda$KrOZIsyY3lh3prHWFldsWopHBw() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AutofillService) obj).onSaveRequest((SaveRequest) obj2, (SaveCallback) obj3);
    }
}
