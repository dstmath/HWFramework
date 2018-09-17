package com.android.server;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.LockSettingsStorage.Callback;

public class HwLockSettingsService extends LockSettingsService {
    public static final String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
    public static final String LOCK_PASSWORD_FILE2 = "password2.key";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TAG = "HwLockSettingsService";
    public static final int transaction_checkvisitorpassword = 1002;
    public static final int transaction_setlockvisitorpassword = 1001;
    private final HwLockSettingsStorage mStorage2;

    public HwLockSettingsService(Context context) {
        super(context);
        this.mStorage2 = new HwLockSettingsStorage(context, new Callback() {
            public void initialize(SQLiteDatabase db) {
            }
        });
    }

    private void setVisitorLockPassword(String password, int userId) throws RemoteException {
        checkWritePermission(userId);
        setKeystorePassword(password, userId);
        this.mStorage2.writePasswordHash(this.mLockPatternUtils.passwordToHash(password, userId), userId);
    }

    public boolean checkVisitorPassword(String password, int userId) throws RemoteException {
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case transaction_setlockvisitorpassword /*1001*/:
                data.enforceInterface(DESCRIPTOR);
                setVisitorLockPassword(data.readString(), data.readInt());
                reply.writeInt(0);
                reply.writeNoException();
                return true;
            case transaction_checkvisitorpassword /*1002*/:
                data.enforceInterface(DESCRIPTOR);
                if (checkVisitorPassword(data.readString(), data.readInt())) {
                    reply.writeInt(0);
                } else {
                    reply.writeInt(1);
                }
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }
}
