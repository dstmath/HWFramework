package tmsdkobf;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/* compiled from: Unknown */
final class my implements mx {
    private String BV;
    private int TRANSACTION_call;
    private int TRANSACTION_cancelMissedCallsNotification;
    private int TRANSACTION_endCall;
    private String mName;

    public my(String str) {
        this.mName = str;
        try {
            IBinder ff = ff();
            if (ff != null) {
                this.BV = ff.getInterfaceDescriptor();
                ng.cK(this.BV + "$Stub");
                this.TRANSACTION_call = ng.h("TRANSACTION_call", 2);
                this.TRANSACTION_endCall = ng.h("TRANSACTION_endCall", 5);
                this.TRANSACTION_cancelMissedCallsNotification = ng.h("TRANSACTION_cancelMissedCallsNotification", 13);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.TRANSACTION_call = 2;
            this.TRANSACTION_endCall = 5;
            this.TRANSACTION_cancelMissedCallsNotification = 13;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean bF(int i) {
        boolean z = false;
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken(this.BV);
            obtain.writeInt(i);
            ff().transact(this.TRANSACTION_endCall, obtain, obtain2, 0);
            obtain2.readException();
            if (obtain2.readInt() != 0) {
                z = true;
            }
            obtain2.recycle();
            obtain.recycle();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            z = th;
            obtain2.recycle();
            obtain.recycle();
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean endCall() {
        boolean z = false;
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken(this.BV);
            ff().transact(this.TRANSACTION_endCall, obtain, obtain2, 0);
            obtain2.readException();
            if (obtain2.readInt() != 0) {
                z = true;
            }
            obtain2.recycle();
            obtain.recycle();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        } catch (Throwable th) {
            z = th;
            obtain2.recycle();
            obtain.recycle();
        }
        return z;
    }

    public IBinder ff() {
        return nh.getService(this.mName);
    }
}
