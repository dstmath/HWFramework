package android.net.ipmemorystore;

import android.net.ipmemorystore.IOnSameL3NetworkResponseListener;

public interface OnSameL3NetworkResponseListener {
    void onSameL3NetworkResponse(Status status, SameL3NetworkResponse sameL3NetworkResponse);

    static IOnSameL3NetworkResponseListener toAIDL(OnSameL3NetworkResponseListener listener) {
        return new IOnSameL3NetworkResponseListener.Stub() {
            /* class android.net.ipmemorystore.OnSameL3NetworkResponseListener.AnonymousClass1 */

            @Override // android.net.ipmemorystore.IOnSameL3NetworkResponseListener
            public void onSameL3NetworkResponse(StatusParcelable statusParcelable, SameL3NetworkResponseParcelable sameL3NetworkResponseParcelable) {
                OnSameL3NetworkResponseListener onSameL3NetworkResponseListener = OnSameL3NetworkResponseListener.this;
                if (onSameL3NetworkResponseListener != null) {
                    onSameL3NetworkResponseListener.onSameL3NetworkResponse(new Status(statusParcelable), new SameL3NetworkResponse(sameL3NetworkResponseParcelable));
                }
            }

            @Override // android.net.ipmemorystore.IOnSameL3NetworkResponseListener
            public int getInterfaceVersion() {
                return 3;
            }
        };
    }
}
