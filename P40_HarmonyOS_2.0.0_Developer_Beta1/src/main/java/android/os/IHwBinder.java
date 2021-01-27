package android.os;

import android.annotation.SystemApi;

@SystemApi
public interface IHwBinder {

    public interface DeathRecipient {
        void serviceDied(long j);
    }

    @Override // android.hardware.cas.V1_0.ICas, android.internal.hidl.base.V1_0.IBase
    boolean linkToDeath(DeathRecipient deathRecipient, long j);

    IHwInterface queryLocalInterface(String str);

    void transact(int i, HwParcel hwParcel, HwParcel hwParcel2, int i2) throws RemoteException;

    @Override // android.hardware.cas.V1_0.ICas, android.internal.hidl.base.V1_0.IBase
    boolean unlinkToDeath(DeathRecipient deathRecipient);
}
