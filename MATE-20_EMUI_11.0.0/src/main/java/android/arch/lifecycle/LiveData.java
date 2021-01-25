package android.arch.lifecycle;

import android.arch.core.executor.ArchTaskExecutor;
import android.arch.core.internal.SafeIterableMap;
import android.arch.lifecycle.Lifecycle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

public abstract class LiveData<T> {
    private static final Object NOT_SET = new Object();
    static final int START_VERSION = -1;
    private int mActiveCount = 0;
    private volatile Object mData = NOT_SET;
    private final Object mDataLock = new Object();
    private boolean mDispatchInvalidated;
    private boolean mDispatchingValue;
    private SafeIterableMap<Observer<? super T>, LiveData<T>.ObserverWrapper> mObservers = new SafeIterableMap<>();
    private volatile Object mPendingData = NOT_SET;
    private final Runnable mPostValueRunnable = new Runnable() {
        /* class android.arch.lifecycle.LiveData.AnonymousClass1 */

        /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: android.arch.lifecycle.LiveData */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.lang.Runnable
        public void run() {
            Object newValue;
            synchronized (LiveData.this.mDataLock) {
                newValue = LiveData.this.mPendingData;
                LiveData.this.mPendingData = LiveData.NOT_SET;
            }
            LiveData.this.setValue(newValue);
        }
    };
    private int mVersion = -1;

    private void considerNotify(LiveData<T>.ObserverWrapper observer) {
        if (observer.mActive) {
            if (!observer.shouldBeActive()) {
                observer.activeStateChanged(false);
            } else if (observer.mLastVersion < this.mVersion) {
                observer.mLastVersion = this.mVersion;
                observer.mObserver.onChanged((Object) this.mData);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchingValue(@Nullable LiveData<T>.ObserverWrapper initiator) {
        if (this.mDispatchingValue) {
            this.mDispatchInvalidated = true;
            return;
        }
        this.mDispatchingValue = true;
        do {
            this.mDispatchInvalidated = false;
            if (initiator == null) {
                Iterator<Map.Entry<Observer<? super T>, LiveData<T>.ObserverWrapper>> iterator = this.mObservers.iteratorWithAdditions();
                while (iterator.hasNext()) {
                    considerNotify(iterator.next().getValue());
                    if (this.mDispatchInvalidated) {
                        break;
                    }
                }
            } else {
                considerNotify(initiator);
                initiator = null;
            }
        } while (this.mDispatchInvalidated);
        this.mDispatchingValue = false;
    }

    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        assertMainThread("observe");
        if (owner.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
            LiveData<T>.LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
            LiveData<T>.ObserverWrapper existing = this.mObservers.putIfAbsent(observer, wrapper);
            if (existing != null && !existing.isAttachedTo(owner)) {
                throw new IllegalArgumentException("Cannot add the same observer with different lifecycles");
            } else if (existing == null) {
                owner.getLifecycle().addObserver(wrapper);
            }
        }
    }

    @MainThread
    public void observeForever(@NonNull Observer<? super T> observer) {
        assertMainThread("observeForever");
        LiveData<T>.AlwaysActiveObserver wrapper = new AlwaysActiveObserver(observer);
        LiveData<T>.ObserverWrapper existing = this.mObservers.putIfAbsent(observer, wrapper);
        if (existing != null && (existing instanceof LifecycleBoundObserver)) {
            throw new IllegalArgumentException("Cannot add the same observer with different lifecycles");
        } else if (existing == null) {
            wrapper.activeStateChanged(true);
        }
    }

    @MainThread
    public void removeObserver(@NonNull Observer<? super T> observer) {
        assertMainThread("removeObserver");
        LiveData<T>.ObserverWrapper removed = this.mObservers.remove(observer);
        if (removed != null) {
            removed.detachObserver();
            removed.activeStateChanged(false);
        }
    }

    @MainThread
    public void removeObservers(@NonNull LifecycleOwner owner) {
        assertMainThread("removeObservers");
        Iterator<Map.Entry<Observer<? super T>, LiveData<T>.ObserverWrapper>> it = this.mObservers.iterator();
        while (it.hasNext()) {
            Map.Entry<Observer<? super T>, LiveData<T>.ObserverWrapper> entry = it.next();
            if (entry.getValue().isAttachedTo(owner)) {
                removeObserver(entry.getKey());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void postValue(T value) {
        boolean postTask;
        synchronized (this.mDataLock) {
            postTask = false;
            if (this.mPendingData == NOT_SET) {
                postTask = true;
            }
            this.mPendingData = value;
        }
        if (postTask) {
            ArchTaskExecutor.getInstance().postToMainThread(this.mPostValueRunnable);
        }
    }

    /* access modifiers changed from: protected */
    @MainThread
    public void setValue(T value) {
        assertMainThread("setValue");
        this.mVersion++;
        this.mData = value;
        dispatchingValue(null);
    }

    @Nullable
    public T getValue() {
        T t = (T) this.mData;
        if (t != NOT_SET) {
            return t;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getVersion() {
        return this.mVersion;
    }

    /* access modifiers changed from: protected */
    public void onActive() {
    }

    /* access modifiers changed from: protected */
    public void onInactive() {
    }

    public boolean hasObservers() {
        return this.mObservers.size() > 0;
    }

    public boolean hasActiveObservers() {
        return this.mActiveCount > 0;
    }

    class LifecycleBoundObserver extends LiveData<T>.ObserverWrapper implements GenericLifecycleObserver {
        @NonNull
        final LifecycleOwner mOwner;

        LifecycleBoundObserver(@NonNull LifecycleOwner owner, Observer<? super T> observer) {
            super(observer);
            this.mOwner = owner;
        }

        /* access modifiers changed from: package-private */
        @Override // android.arch.lifecycle.LiveData.ObserverWrapper
        public boolean shouldBeActive() {
            return this.mOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
        }

        @Override // android.arch.lifecycle.GenericLifecycleObserver
        public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
            if (this.mOwner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
                LiveData.this.removeObserver(this.mObserver);
            } else {
                activeStateChanged(shouldBeActive());
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.arch.lifecycle.LiveData.ObserverWrapper
        public boolean isAttachedTo(LifecycleOwner owner) {
            return this.mOwner == owner;
        }

        /* access modifiers changed from: package-private */
        @Override // android.arch.lifecycle.LiveData.ObserverWrapper
        public void detachObserver() {
            this.mOwner.getLifecycle().removeObserver(this);
        }
    }

    /* access modifiers changed from: private */
    public abstract class ObserverWrapper {
        boolean mActive;
        int mLastVersion = -1;
        final Observer<? super T> mObserver;

        /* access modifiers changed from: package-private */
        public abstract boolean shouldBeActive();

        ObserverWrapper(Observer<? super T> observer) {
            this.mObserver = observer;
        }

        /* access modifiers changed from: package-private */
        public boolean isAttachedTo(LifecycleOwner owner) {
            return false;
        }

        /* access modifiers changed from: package-private */
        public void detachObserver() {
        }

        /* access modifiers changed from: package-private */
        public void activeStateChanged(boolean newActive) {
            if (newActive != this.mActive) {
                this.mActive = newActive;
                int i = 1;
                boolean wasInactive = LiveData.this.mActiveCount == 0;
                LiveData liveData = LiveData.this;
                int i2 = liveData.mActiveCount;
                if (!this.mActive) {
                    i = -1;
                }
                liveData.mActiveCount = i2 + i;
                if (wasInactive && this.mActive) {
                    LiveData.this.onActive();
                }
                if (LiveData.this.mActiveCount == 0 && !this.mActive) {
                    LiveData.this.onInactive();
                }
                if (this.mActive) {
                    LiveData.this.dispatchingValue(this);
                }
            }
        }
    }

    private class AlwaysActiveObserver extends LiveData<T>.ObserverWrapper {
        AlwaysActiveObserver(Observer<? super T> observer) {
            super(observer);
        }

        /* access modifiers changed from: package-private */
        @Override // android.arch.lifecycle.LiveData.ObserverWrapper
        public boolean shouldBeActive() {
            return true;
        }
    }

    private static void assertMainThread(String methodName) {
        if (!ArchTaskExecutor.getInstance().isMainThread()) {
            throw new IllegalStateException("Cannot invoke " + methodName + " on a background thread");
        }
    }
}
