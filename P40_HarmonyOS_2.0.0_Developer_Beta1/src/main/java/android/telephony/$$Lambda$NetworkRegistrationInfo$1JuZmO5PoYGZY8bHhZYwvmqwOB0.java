package android.telephony;

import java.util.function.Function;

/* renamed from: android.telephony.-$$Lambda$NetworkRegistrationInfo$1JuZmO5PoYGZY8bHhZYwvmqwOB0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NetworkRegistrationInfo$1JuZmO5PoYGZY8bHhZYwvmqwOB0 implements Function {
    public static final /* synthetic */ $$Lambda$NetworkRegistrationInfo$1JuZmO5PoYGZY8bHhZYwvmqwOB0 INSTANCE = new $$Lambda$NetworkRegistrationInfo$1JuZmO5PoYGZY8bHhZYwvmqwOB0();

    private /* synthetic */ $$Lambda$NetworkRegistrationInfo$1JuZmO5PoYGZY8bHhZYwvmqwOB0() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return NetworkRegistrationInfo.serviceTypeToString(((Integer) obj).intValue());
    }
}
