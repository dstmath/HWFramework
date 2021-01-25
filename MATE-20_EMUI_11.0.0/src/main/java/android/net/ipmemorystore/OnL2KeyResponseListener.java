package android.net.ipmemorystore;

import android.net.ipmemorystore.IOnL2KeyResponseListener;

public interface OnL2KeyResponseListener {
    void onL2KeyResponse(Status status, String str);

    static IOnL2KeyResponseListener toAIDL(OnL2KeyResponseListener listener) {
        return new IOnL2KeyResponseListener.Stub() {
            /* class android.net.ipmemorystore.OnL2KeyResponseListener.AnonymousClass1 */

            @Override // android.net.ipmemorystore.IOnL2KeyResponseListener
            public void onL2KeyResponse(StatusParcelable statusParcelable, String l2Key) {
                OnL2KeyResponseListener onL2KeyResponseListener = OnL2KeyResponseListener.this;
                if (onL2KeyResponseListener != null) {
                    onL2KeyResponseListener.onL2KeyResponse(new Status(statusParcelable), l2Key);
                }
            }

            @Override // android.net.ipmemorystore.IOnL2KeyResponseListener
            public int getInterfaceVersion() {
                return 3;
            }
        };
    }
}
