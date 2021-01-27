package android.net.ipmemorystore;

import android.net.ipmemorystore.IOnStatusListener;

public interface OnStatusListener {
    void onComplete(Status status);

    static IOnStatusListener toAIDL(OnStatusListener listener) {
        return new IOnStatusListener.Stub() {
            /* class android.net.ipmemorystore.OnStatusListener.AnonymousClass1 */

            @Override // android.net.ipmemorystore.IOnStatusListener
            public void onComplete(StatusParcelable statusParcelable) {
                OnStatusListener onStatusListener = OnStatusListener.this;
                if (onStatusListener != null) {
                    onStatusListener.onComplete(new Status(statusParcelable));
                }
            }

            @Override // android.net.ipmemorystore.IOnStatusListener
            public int getInterfaceVersion() {
                return 3;
            }
        };
    }
}
