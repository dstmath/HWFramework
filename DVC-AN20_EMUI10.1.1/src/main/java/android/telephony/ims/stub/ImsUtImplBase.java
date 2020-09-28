package android.telephony.ims.stub;

import android.annotation.SystemApi;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.ims.ImsUtListener;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.IImsUtListener;

@SystemApi
public class ImsUtImplBase {
    private IImsUt.Stub mServiceImpl = new IImsUt.Stub() {
        /* class android.telephony.ims.stub.ImsUtImplBase.AnonymousClass1 */

        @Override // com.android.ims.internal.IImsUt
        public void close() throws RemoteException {
            ImsUtImplBase.this.close();
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCallBarring(int cbType) throws RemoteException {
            return ImsUtImplBase.this.queryCallBarring(cbType);
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCallForward(int condition, String number) throws RemoteException {
            return ImsUtImplBase.this.queryCallForward(condition, number);
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCallWaiting() throws RemoteException {
            return ImsUtImplBase.this.queryCallWaiting();
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCLIR() throws RemoteException {
            return ImsUtImplBase.this.queryCLIR();
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCLIP() throws RemoteException {
            return ImsUtImplBase.this.queryCLIP();
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCOLR() throws RemoteException {
            return ImsUtImplBase.this.queryCOLR();
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCOLP() throws RemoteException {
            return ImsUtImplBase.this.queryCOLP();
        }

        @Override // com.android.ims.internal.IImsUt
        public int transact(Bundle ssInfo) throws RemoteException {
            return ImsUtImplBase.this.transact(ssInfo);
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCallBarring(int cbType, int action, String[] barrList) throws RemoteException {
            return ImsUtImplBase.this.updateCallBarring(cbType, action, barrList);
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCallForward(int action, int condition, String number, int serviceClass, int timeSeconds) throws RemoteException {
            return ImsUtImplBase.this.updateCallForward(action, condition, number, serviceClass, timeSeconds);
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCallWaiting(boolean enable, int serviceClass) throws RemoteException {
            return ImsUtImplBase.this.updateCallWaiting(enable, serviceClass);
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCLIR(int clirMode) throws RemoteException {
            return ImsUtImplBase.this.updateCLIR(clirMode);
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCLIP(boolean enable) throws RemoteException {
            return ImsUtImplBase.this.updateCLIP(enable);
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCOLR(int presentation) throws RemoteException {
            return ImsUtImplBase.this.updateCOLR(presentation);
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCOLP(boolean enable) throws RemoteException {
            return ImsUtImplBase.this.updateCOLP(enable);
        }

        @Override // com.android.ims.internal.IImsUt
        public void setListener(IImsUtListener listener) throws RemoteException {
            ImsUtImplBase.this.setListener(new ImsUtListener(listener));
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCallBarringForServiceClass(int cbType, int serviceClass) throws RemoteException {
            return ImsUtImplBase.this.queryCallBarringForServiceClass(cbType, serviceClass);
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCallBarringForServiceClass(int cbType, int action, String[] barrList, int serviceClass) throws RemoteException {
            return ImsUtImplBase.this.updateCallBarringForServiceClass(cbType, action, barrList, serviceClass);
        }

        @Override // com.android.ims.internal.IImsUt
        public IBinder getHwInnerService() {
            return ImsUtImplBase.this.getHwInnerService();
        }
    };

    public void close() {
    }

    public int queryCallBarring(int cbType) {
        return -1;
    }

    public int queryCallBarringForServiceClass(int cbType, int serviceClass) {
        return -1;
    }

    public int queryCallForward(int condition, String number) {
        return -1;
    }

    public int queryCallForwardForServiceClass(int condition, String number, int serviceClass) {
        return -1;
    }

    public int queryCallWaiting() {
        return -1;
    }

    public int queryCLIR() {
        return queryClir();
    }

    public int queryCLIP() {
        return queryClip();
    }

    public int queryCOLR() {
        return queryColr();
    }

    public int queryCOLP() {
        return queryColp();
    }

    public int queryClir() {
        return -1;
    }

    public int queryClip() {
        return -1;
    }

    public int queryColr() {
        return -1;
    }

    public int queryColp() {
        return -1;
    }

    public int transact(Bundle ssInfo) {
        return -1;
    }

    public int updateCallBarring(int cbType, int action, String[] barrList) {
        return -1;
    }

    public int updateCallBarringForServiceClass(int cbType, int action, String[] barrList, int serviceClass) {
        return -1;
    }

    public int updateCallForward(int action, int condition, String number, int serviceClass, int timeSeconds) {
        return 0;
    }

    public int updateCallWaiting(boolean enable, int serviceClass) {
        return -1;
    }

    public int updateCLIR(int clirMode) {
        return updateClir(clirMode);
    }

    public int updateCLIP(boolean enable) {
        return updateClip(enable);
    }

    public int updateCOLR(int presentation) {
        return updateColr(presentation);
    }

    public int updateCOLP(boolean enable) {
        return updateColp(enable);
    }

    public int updateClir(int clirMode) {
        return -1;
    }

    public int updateClip(boolean enable) {
        return -1;
    }

    public int updateColr(int presentation) {
        return -1;
    }

    public int updateColp(boolean enable) {
        return -1;
    }

    public void setListener(ImsUtListener listener) {
    }

    public IImsUt getInterface() {
        return this.mServiceImpl;
    }

    public IBinder getHwInnerService() {
        return null;
    }
}
