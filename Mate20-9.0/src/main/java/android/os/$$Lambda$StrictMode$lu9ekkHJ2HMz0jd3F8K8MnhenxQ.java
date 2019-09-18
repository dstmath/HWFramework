package android.os;

import android.os.strictmode.NonSdkApiUsedViolation;
import java.util.function.Consumer;

/* renamed from: android.os.-$$Lambda$StrictMode$lu9ekkHJ2HMz0jd3F8K8MnhenxQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StrictMode$lu9ekkHJ2HMz0jd3F8K8MnhenxQ implements Consumer {
    public static final /* synthetic */ $$Lambda$StrictMode$lu9ekkHJ2HMz0jd3F8K8MnhenxQ INSTANCE = new $$Lambda$StrictMode$lu9ekkHJ2HMz0jd3F8K8MnhenxQ();

    private /* synthetic */ $$Lambda$StrictMode$lu9ekkHJ2HMz0jd3F8K8MnhenxQ() {
    }

    public final void accept(Object obj) {
        StrictMode.onVmPolicyViolation(new NonSdkApiUsedViolation((String) obj));
    }
}
