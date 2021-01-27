package com.huawei.anim.dynamicanimation;

import android.util.Log;
import android.util.SparseArray;
import android.view.Choreographer;
import com.huawei.anim.dynamicanimation.DynamicAnimation;
import com.huawei.anim.dynamicanimation.PhysicalChain;
import com.huawei.anim.dynamicanimation.util.Pools;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PhysicalChain<K extends PhysicalChain, T extends DynamicAnimation> implements DynamicAnimation.OnAnimationStartListener, DynamicAnimation.OnAnimationUpdateListener {
    private static final String a = "PhysicalChain";
    private static final int b = 16;
    private static final int c = 2;
    private static final int d = -1;
    private boolean e = true;
    private long f = 0;
    private Pools.SimplePool<PhysicalChain<K, T>.a> g;
    private Pools.SimplePool<T> h;
    protected int mControlModelIndex = Integer.MIN_VALUE;
    protected AtomicBoolean mIsDirty = new AtomicBoolean(true);
    protected int mMaxChainSize;
    protected SparseArray<T> mModelList = new SparseArray<>();

    /* access modifiers changed from: package-private */
    public abstract T createAnimationObj();

    /* access modifiers changed from: protected */
    public abstract void onChainTransfer(T t, float f2, float f3, int i);

    /* access modifiers changed from: package-private */
    public abstract void reConfig(T t, int i);

    /* access modifiers changed from: package-private */
    public abstract T reUseAnimationObj(T t);

    /* access modifiers changed from: package-private */
    public abstract T resetAnimationObj(T t);

    public PhysicalChain(int i) {
        if (this.mMaxChainSize < 0) {
            this.mMaxChainSize = 16;
        }
        this.mMaxChainSize = i;
        this.g = new Pools.SimplePool<>(i * 2);
        this.h = new Pools.SimplePool<>(i);
    }

    public K addObject(ChainListener chainListener) {
        Log.i(a, "addObject: listener=" + chainListener);
        return addObject(-1, chainListener);
    }

    public K addObject(int i, ChainListener chainListener) {
        if (this.mModelList.size() > this.mMaxChainSize - 1) {
            Log.i(a, "addObject: remove first");
            T valueAt = this.mModelList.valueAt(0);
            this.mModelList.removeAt(0);
            resetAnimationObj(valueAt);
            this.h.release(valueAt);
        }
        T acquire = this.h.acquire();
        if (acquire == null) {
            acquire = createAnimationObj();
        } else {
            reUseAnimationObj(acquire);
        }
        acquire.addUpdateListener(chainListener).addUpdateListener(this);
        if (i < 0) {
            i = this.mModelList.size();
        }
        this.mModelList.append(i, acquire);
        reConfig(acquire, Math.abs(this.mModelList.indexOfKey(i) - this.mModelList.indexOfKey(this.mControlModelIndex)));
        return this;
    }

    public PhysicalChain removeObject(int i) {
        if (!a(i)) {
            return this;
        }
        int indexOfKey = this.mModelList.indexOfKey(i);
        this.mModelList.removeAt(indexOfKey);
        this.h.release(this.mModelList.valueAt(indexOfKey));
        return this;
    }

    public void reParamsTransfer() {
        reConfig(this.mModelList.get(this.mControlModelIndex), 0);
        int indexOfKey = this.mModelList.indexOfKey(this.mControlModelIndex);
        int size = this.mModelList.size();
        int i = indexOfKey;
        while (true) {
            i++;
            if (i >= size) {
                break;
            }
            reConfig(this.mModelList.valueAt(i), i - indexOfKey);
        }
        int i2 = indexOfKey;
        while (true) {
            i2--;
            if (i2 >= 0) {
                reConfig(this.mModelList.valueAt(i2), indexOfKey - i2);
            } else {
                return;
            }
        }
    }

    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation.OnAnimationStartListener
    public void onAnimationStart(DynamicAnimation dynamicAnimation, float f2, float f3) {
        if (this.mModelList.size() > 0 && this.mIsDirty.compareAndSet(true, false)) {
            reParamsTransfer();
        }
    }

    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation.OnAnimationUpdateListener
    public void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f2, float f3) {
        int i;
        int i2;
        int indexOfValue = this.mModelList.indexOfValue(dynamicAnimation);
        int indexOfKey = this.mModelList.indexOfKey(this.mControlModelIndex);
        if (indexOfValue == indexOfKey) {
            i2 = indexOfValue - 1;
            i = indexOfValue + 1;
        } else if (indexOfValue < indexOfKey) {
            i2 = indexOfValue - 1;
            i = -1;
        } else {
            i = indexOfValue + 1;
            i2 = -1;
        }
        if (i > -1 && i < this.mModelList.size()) {
            a(this.mModelList.valueAt(i), f2, f3, i);
        }
        if (i2 > -1 && i2 < this.mModelList.size()) {
            a(this.mModelList.valueAt(i2), f2, f3, i2);
        }
    }

    public void cancel() {
        if (a(getControlModelIndex())) {
            getModelList().valueAt(getControlModelIndex()).cancel();
        }
    }

    /* access modifiers changed from: protected */
    public void reConfig() {
    }

    private void a(T t, float f2, float f3, int i) {
        if (!this.e) {
            onChainTransfer(t, f2, f3, i);
            return;
        }
        PhysicalChain<K, T>.a acquire = this.g.acquire();
        if (acquire == null) {
            acquire = new a();
        }
        if (this.f <= 0) {
            Choreographer.getInstance().postFrameCallback(acquire.a((PhysicalChain<K, T>.a) t).a(f2).c(f3).a(i));
        } else {
            Choreographer.getInstance().postFrameCallbackDelayed(acquire.a((PhysicalChain<K, T>.a) t).a(f2).c(f3).a(i), this.f);
        }
    }

    /* access modifiers changed from: package-private */
    public class a implements Choreographer.FrameCallback {
        private T b;
        private float c;
        private float d;
        private int e;

        a() {
        }

        /* JADX DEBUG: Multi-variable search result rejected for r4v1, resolved type: com.huawei.anim.dynamicanimation.PhysicalChain */
        /* JADX WARN: Multi-variable type inference failed */
        public void a(long j) {
            PhysicalChain.this.onChainTransfer(this.b, this.c, this.d, this.e);
            PhysicalChain.this.g.release(this);
        }

        public PhysicalChain<K, T>.a a(T t) {
            this.b = t;
            return this;
        }

        public PhysicalChain<K, T>.a a(float f) {
            this.c = f;
            return this;
        }

        public PhysicalChain<K, T>.a b(float f) {
            return this;
        }

        public PhysicalChain<K, T>.a c(float f) {
            this.d = f;
            return this;
        }

        public int a() {
            return this.e;
        }

        public PhysicalChain<K, T>.a a(int i) {
            this.e = i;
            return this;
        }
    }

    public SparseArray<T> getModelList() {
        return this.mModelList;
    }

    public int getControlModelIndex() {
        return this.mControlModelIndex;
    }

    /* access modifiers changed from: protected */
    public ParamTransfer diffMember(ParamTransfer paramTransfer, ParamTransfer paramTransfer2) {
        if (paramTransfer == paramTransfer2) {
            return paramTransfer;
        }
        if (paramTransfer != null && paramTransfer.equals(paramTransfer2)) {
            return paramTransfer;
        }
        this.mIsDirty.compareAndSet(false, true);
        return paramTransfer2;
    }

    /* access modifiers changed from: protected */
    public float diffMember(float f2, float f3) {
        if (Float.compare(f2, f3) == 0) {
            return f2;
        }
        this.mIsDirty.compareAndSet(false, true);
        return f3;
    }

    public K setControlModelIndex(int i) {
        int i2;
        if (!a(i) || (i2 = this.mControlModelIndex) == i) {
            return this;
        }
        if (i2 != Integer.MIN_VALUE) {
            this.mModelList.get(i2).removeStartListener(this);
        }
        this.mControlModelIndex = i;
        this.mModelList.get(this.mControlModelIndex).addStartListener(this);
        this.mIsDirty.set(true);
        return this;
    }

    private boolean a(int i) {
        return this.mModelList.indexOfKey(i) >= 0;
    }

    public int getChainSize() {
        return this.mMaxChainSize;
    }

    public K setChainSize(int i) {
        this.mMaxChainSize = i;
        return this;
    }

    public boolean isDelayed() {
        return this.e;
    }

    public K setDelayed(boolean z) {
        this.e = z;
        return this;
    }

    public long getDelay() {
        return this.f;
    }

    public K setDelay(long j) {
        this.f = j;
        return this;
    }
}
