package com.android.internal.util;

import java.util.ArrayList;
import java.util.List;

public class CallbackRegistry<C, T, A> implements Cloneable {
    private static final String TAG = "CallbackRegistry";
    private List<C> mCallbacks = new ArrayList();
    private long mFirst64Removed = 0;
    private int mNotificationLevel;
    private final NotifierCallback<C, T, A> mNotifier;
    private long[] mRemainderRemoved;

    public static abstract class NotifierCallback<C, T, A> {
        public abstract void onNotifyCallback(C c, T t, int i, A a);
    }

    public CallbackRegistry(NotifierCallback<C, T, A> notifier) {
        this.mNotifier = notifier;
    }

    public synchronized void notifyCallbacks(T sender, int arg, A arg2) {
        this.mNotificationLevel++;
        notifyRecurseLocked(sender, arg, arg2);
        this.mNotificationLevel--;
        if (this.mNotificationLevel == 0) {
            if (this.mRemainderRemoved != null) {
                for (int i = this.mRemainderRemoved.length - 1; i >= 0; i--) {
                    long removedBits = this.mRemainderRemoved[i];
                    if (removedBits != 0) {
                        removeRemovedCallbacks((i + 1) * 64, removedBits);
                        this.mRemainderRemoved[i] = 0;
                    }
                }
            }
            if (this.mFirst64Removed != 0) {
                removeRemovedCallbacks(0, this.mFirst64Removed);
                this.mFirst64Removed = 0;
            }
        }
    }

    private void notifyFirst64Locked(T sender, int arg, A arg2) {
        notifyCallbacksLocked(sender, arg, arg2, 0, Math.min(64, this.mCallbacks.size()), this.mFirst64Removed);
    }

    private void notifyRecurseLocked(T sender, int arg, A arg2) {
        int callbackCount = this.mCallbacks.size();
        int remainderIndex = this.mRemainderRemoved == null ? -1 : this.mRemainderRemoved.length - 1;
        notifyRemainderLocked(sender, arg, arg2, remainderIndex);
        notifyCallbacksLocked(sender, arg, arg2, (remainderIndex + 2) * 64, callbackCount, 0);
    }

    private void notifyRemainderLocked(T sender, int arg, A arg2, int remainderIndex) {
        if (remainderIndex < 0) {
            notifyFirst64Locked(sender, arg, arg2);
            return;
        }
        long bits = this.mRemainderRemoved[remainderIndex];
        int startIndex = (remainderIndex + 1) * 64;
        int endIndex = Math.min(this.mCallbacks.size(), startIndex + 64);
        notifyRemainderLocked(sender, arg, arg2, remainderIndex - 1);
        notifyCallbacksLocked(sender, arg, arg2, startIndex, endIndex, bits);
    }

    private void notifyCallbacksLocked(T sender, int arg, A arg2, int startIndex, int endIndex, long bits) {
        long bitMask = 1;
        for (int i = startIndex; i < endIndex; i++) {
            if ((bits & bitMask) == 0) {
                this.mNotifier.onNotifyCallback(this.mCallbacks.get(i), sender, arg, arg2);
            }
            bitMask <<= 1;
        }
    }

    public synchronized void add(C callback) {
        int index = this.mCallbacks.lastIndexOf(callback);
        if (index < 0 || isRemovedLocked(index)) {
            this.mCallbacks.add(callback);
        }
    }

    private boolean isRemovedLocked(int index) {
        boolean z = true;
        if (index < 64) {
            if ((this.mFirst64Removed & (1 << index)) == 0) {
                z = false;
            }
            return z;
        } else if (this.mRemainderRemoved == null) {
            return false;
        } else {
            int maskIndex = (index / 64) - 1;
            if (maskIndex >= this.mRemainderRemoved.length) {
                return false;
            }
            if ((this.mRemainderRemoved[maskIndex] & (1 << (index % 64))) == 0) {
                z = false;
            }
            return z;
        }
    }

    private void removeRemovedCallbacks(int startIndex, long removed) {
        long bitMask = Long.MIN_VALUE;
        for (int i = (startIndex + 64) - 1; i >= startIndex; i--) {
            if ((removed & bitMask) != 0) {
                this.mCallbacks.remove(i);
            }
            bitMask >>>= 1;
        }
    }

    public synchronized void remove(C callback) {
        if (this.mNotificationLevel == 0) {
            this.mCallbacks.remove(callback);
        } else {
            int index = this.mCallbacks.lastIndexOf(callback);
            if (index >= 0) {
                setRemovalBitLocked(index);
            }
        }
    }

    private void setRemovalBitLocked(int index) {
        if (index < 64) {
            this.mFirst64Removed |= 1 << index;
            return;
        }
        int remainderIndex = (index / 64) - 1;
        if (this.mRemainderRemoved == null) {
            this.mRemainderRemoved = new long[(this.mCallbacks.size() / 64)];
        } else if (this.mRemainderRemoved.length < remainderIndex) {
            long[] newRemainders = new long[(this.mCallbacks.size() / 64)];
            System.arraycopy(this.mRemainderRemoved, 0, newRemainders, 0, this.mRemainderRemoved.length);
            this.mRemainderRemoved = newRemainders;
        }
        long bitMask = 1 << (index % 64);
        long[] jArr = this.mRemainderRemoved;
        jArr[remainderIndex] = jArr[remainderIndex] | bitMask;
    }

    public synchronized ArrayList<C> copyListeners() {
        ArrayList<C> callbacks;
        callbacks = new ArrayList(this.mCallbacks.size());
        int numListeners = this.mCallbacks.size();
        for (int i = 0; i < numListeners; i++) {
            if (!isRemovedLocked(i)) {
                callbacks.add(this.mCallbacks.get(i));
            }
        }
        return callbacks;
    }

    public synchronized boolean isEmpty() {
        if (this.mCallbacks.isEmpty()) {
            return true;
        }
        if (this.mNotificationLevel == 0) {
            return false;
        }
        int numListeners = this.mCallbacks.size();
        for (int i = 0; i < numListeners; i++) {
            if (!isRemovedLocked(i)) {
                return false;
            }
        }
        return true;
    }

    public synchronized void clear() {
        if (this.mNotificationLevel == 0) {
            this.mCallbacks.clear();
        } else if (!this.mCallbacks.isEmpty()) {
            for (int i = this.mCallbacks.size() - 1; i >= 0; i--) {
                setRemovalBitLocked(i);
            }
        }
    }

    public synchronized CallbackRegistry<C, T, A> clone() {
        CallbackRegistry<C, T, A> callbackRegistry;
        callbackRegistry = null;
        try {
            callbackRegistry = (CallbackRegistry) super.clone();
            callbackRegistry.mFirst64Removed = 0;
            callbackRegistry.mRemainderRemoved = null;
            callbackRegistry.mNotificationLevel = 0;
            callbackRegistry.mCallbacks = new ArrayList();
            int numListeners = this.mCallbacks.size();
            for (int i = 0; i < numListeners; i++) {
                if (!isRemovedLocked(i)) {
                    callbackRegistry.mCallbacks.add(this.mCallbacks.get(i));
                }
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return callbackRegistry;
    }
}
