package defpackage;

import com.huawei.android.feature.tasks.OnFailureListener;
import com.huawei.android.feature.tasks.OnSuccessListener;
import java.util.concurrent.CountDownLatch;

/* renamed from: ap  reason: default package */
public final class ap implements OnFailureListener, OnSuccessListener<Object> {
    public final CountDownLatch S;

    private ap() {
        this.S = new CountDownLatch(1);
    }

    public /* synthetic */ ap(byte b) {
        this();
    }

    public final void onFailure(Exception exc) {
        this.S.countDown();
    }

    public final void onSuccess(Object obj) {
        this.S.countDown();
    }
}
