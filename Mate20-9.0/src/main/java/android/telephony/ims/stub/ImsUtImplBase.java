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
        public void close() throws RemoteException {
            ImsUtImplBase.this.close();
        }

        public int queryCallBarring(int cbType) throws RemoteException {
            return ImsUtImplBase.this.queryCallBarring(cbType);
        }

        public int queryCallForward(int condition, String number) throws RemoteException {
            return ImsUtImplBase.this.queryCallForward(condition, number);
        }

        public int queryCallWaiting() throws RemoteException {
            return ImsUtImplBase.this.queryCallWaiting();
        }

        public int queryCLIR() throws RemoteException {
            return ImsUtImplBase.this.queryCLIR();
        }

        public int queryCLIP() throws RemoteException {
            return ImsUtImplBase.this.queryCLIP();
        }

        public int queryCOLR() throws RemoteException {
            return ImsUtImplBase.this.queryCOLR();
        }

        public int queryCOLP() throws RemoteException {
            return ImsUtImplBase.this.queryCOLP();
        }

        public int transact(Bundle ssInfo) throws RemoteException {
            return ImsUtImplBase.this.transact(ssInfo);
        }

        public int updateCallBarring(int cbType, int action, String[] barrList) throws RemoteException {
            return ImsUtImplBase.this.updateCallBarring(cbType, action, barrList);
        }

        public int updateCallForward(int action, int condition, String number, int serviceClass, int timeSeconds) throws RemoteException {
            return ImsUtImplBase.this.updateCallForward(action, condition, number, serviceClass, timeSeconds);
        }

        public int updateCallWaiting(boolean enable, int serviceClass) throws RemoteException {
            return ImsUtImplBase.this.updateCallWaiting(enable, serviceClass);
        }

        public int updateCLIR(int clirMode) throws RemoteException {
            return ImsUtImplBase.this.updateCLIR(clirMode);
        }

        public int updateCLIP(boolean enable) throws RemoteException {
            return ImsUtImplBase.this.updateCLIP(enable);
        }

        public int updateCOLR(int presentation) throws RemoteException {
            return ImsUtImplBase.this.updateCOLR(presentation);
        }

        public int updateCOLP(boolean enable) throws RemoteException {
            return ImsUtImplBase.this.updateCOLP(enable);
        }

        public void setListener(IImsUtListener listener) throws RemoteException {
            ImsUtImplBase.this.setListener(new ImsUtListener(listener));
        }

        public int queryCallBarringForServiceClass(int cbType, int serviceClass) throws RemoteException {
            return ImsUtImplBase.this.queryCallBarringForServiceClass(cbType, serviceClass);
        }

        public int updateCallBarringForServiceClass(int cbType, int action, String[] barrList, int serviceClass) throws RemoteException {
            return ImsUtImplBase.this.updateCallBarringForServiceClass(cbType, action, barrList, serviceClass);
        }

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
