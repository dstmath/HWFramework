package tmsdkobf;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

final class lz implements ly {
    private int TRANSACTION_call;
    private int TRANSACTION_cancelMissedCallsNotification;
    private int TRANSACTION_endCall;
    private String mName;
    private String zH;

    public lz(String str) {
        this.mName = str;
        try {
            IBinder eN = eN();
            if (eN != null) {
                this.zH = eN.getInterfaceDescriptor();
                mh.bY(this.zH + "$Stub");
                this.TRANSACTION_call = mh.e("TRANSACTION_call", 2);
                this.TRANSACTION_endCall = mh.e("TRANSACTION_endCall", 5);
                this.TRANSACTION_cancelMissedCallsNotification = mh.e("TRANSACTION_cancelMissedCallsNotification", 13);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.TRANSACTION_call = 2;
            this.TRANSACTION_endCall = 5;
            this.TRANSACTION_cancelMissedCallsNotification = 13;
        }
    }

    public boolean aP(int i) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        boolean z = false;
        try {
            obtain.writeInterfaceToken(this.zH);
            obtain.writeInt(i);
            eN().transact(this.TRANSACTION_endCall, obtain, obtain2, 0);
            obtain2.readException();
            z = obtain2.readInt() != 0;
            obtain2.recycle();
            obtain.recycle();
        } catch (RemoteException e) {
            e.printStackTrace();
            obtain2.recycle();
            obtain.recycle();
        } catch (Throwable th) {
            obtain2.recycle();
            obtain.recycle();
            throw th;
        }
        return z;
    }

    public IBinder eN() {
        return mi.getService(this.mName);
    }

    public boolean endCall() {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        boolean z = false;
        try {
            obtain.writeInterfaceToken(this.zH);
            eN().transact(this.TRANSACTION_endCall, obtain, obtain2, 0);
            obtain2.readException();
            z = obtain2.readInt() != 0;
            obtain2.recycle();
            obtain.recycle();
        } catch (RemoteException e) {
            e.printStackTrace();
            obtain2.recycle();
            obtain.recycle();
        } catch (Exception e2) {
            e2.printStackTrace();
            obtain2.recycle();
            obtain.recycle();
        } catch (Throwable th) {
            obtain2.recycle();
            obtain.recycle();
            throw th;
        }
        return z;
    }
}
