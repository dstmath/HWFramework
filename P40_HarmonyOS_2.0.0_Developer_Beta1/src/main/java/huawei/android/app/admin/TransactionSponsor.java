package huawei.android.app.admin;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionSponsor {
    private static final boolean IS_HWFLOW = ((Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) ? true : IS_HWFLOW);
    private static final String TAG = "TransactionSponsor";
    private static IBinder sBinder = null;

    private void logTransaction(int code) {
        if (IS_HWFLOW) {
            Log.i(TAG, "Transact: " + code + " to device policy manager service.");
        }
    }

    private static synchronized IBinder getBinderInstance() {
        IBinder iBinder;
        synchronized (TransactionSponsor.class) {
            if (sBinder == null) {
                Log.i(TAG, "init device policy service.");
                sBinder = ServiceManager.getService("device_policy");
                if (sBinder == null) {
                    Log.e(TAG, "init device policy service failed.");
                }
            }
            iBinder = sBinder;
        }
        return iBinder;
    }

    private void writeAdminInfo(ComponentName admin, Parcel data) {
        data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
        if (admin != null) {
            data.writeInt(1);
            admin.writeToParcel(data, 0);
            return;
        }
        data.writeInt(0);
    }

    /* access modifiers changed from: package-private */
    public void transactToUninstallPackage(int code, ComponentName admin, String packageName, boolean isKeepData, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeString(packageName);
                data.writeInt(isKeepData ? 1 : 0);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    /* access modifiers changed from: package-private */
    public boolean transactToIsFunctionDisabled(int code, ComponentName admin, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean isDisabled = IS_HWFLOW;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeInt(userId);
                IBinder iBinder = sBinder;
                boolean z = IS_HWFLOW;
                iBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 1) {
                    z = true;
                }
                isDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactToSetFunctionDisabled(int code, ComponentName admin, boolean isDisabled, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeInt(isDisabled ? 1 : 0);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return IS_HWFLOW;
    }

    /* access modifiers changed from: package-private */
    public void transactToExecCommand(int code, ComponentName admin, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    /* access modifiers changed from: package-private */
    public void transactToExecCommand(int code, ComponentName admin, String param, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeString(param);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    /* access modifiers changed from: package-private */
    public boolean transactToExecCommand(int code, ComponentName admin, List<String> param, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeStringList(param);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return true;
    }

    /* access modifiers changed from: package-private */
    public void transactToExecCommand(int code, ComponentName admin, Map param, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeMap(param);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    /* access modifiers changed from: package-private */
    public void transactToExecCommand(int code, ComponentName admin, Map param1, String param2, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeMap(param1);
                data.writeString(param2);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    /* access modifiers changed from: package-private */
    public List<String> transactToGetListFunction(int code, ComponentName admin, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<String> stringList = null;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                stringList = new ArrayList<>();
                reply.readStringList(stringList);
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return stringList;
    }

    /* access modifiers changed from: package-private */
    public List<String> transactToQueryApn(int code, ComponentName admin, Map param, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<String> stringList = null;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeMap(param);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                stringList = new ArrayList<>();
                reply.readStringList(stringList);
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return stringList;
    }

    /* access modifiers changed from: package-private */
    public Map<String, String> transactToGetApnInfo(int code, ComponentName admin, String param, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Map<String, String> map = null;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeString(param);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                map = new HashMap<>();
                reply.readMap(map, null);
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return map;
    }

    /* access modifiers changed from: package-private */
    public void transactToConfigExchangeMail(int code, ComponentName admin, Bundle para, int userId) {
        if (para != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                if (getBinderInstance() != null) {
                    logTransaction(code);
                    writeAdminInfo(admin, data);
                    para.writeToParcel(data, 0);
                    data.writeInt(userId);
                    sBinder.transact(code, data, reply, 0);
                    reply.readException();
                }
            } catch (RemoteException localRemoteException) {
                Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
        }
    }

    /* access modifiers changed from: package-private */
    public Bundle transactToGetMailProviderForDomain(int code, ComponentName admin, String domain, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Bundle para = null;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeString(domain);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 0) {
                    para = new Bundle();
                    para.readFromParcel(reply);
                }
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return para;
    }

    /* access modifiers changed from: package-private */
    public void transactToSetDefaultLauncher(int code, ComponentName admin, String packageName, String className, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeString(packageName);
                data.writeString(className);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    /* access modifiers changed from: package-private */
    public Bitmap transactToCaptureScreen(int code, ComponentName admin, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Bitmap bitmap = null;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 0) {
                    bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(reply);
                }
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return bitmap;
    }

    /* access modifiers changed from: package-private */
    public int transactToGetSdCardEncryptionStatus(int code, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int encryptionStatus = 0;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                encryptionStatus = reply.readInt();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return encryptionStatus;
    }

    /* access modifiers changed from: package-private */
    public boolean transactToSetPolicy(Bundle bundle, ComponentName admin, int userId, Bundle policyData, int customType) {
        boolean z = IS_HWFLOW;
        if (bundle == null) {
            return IS_HWFLOW;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int code = bundle.getInt("code");
        boolean isDisabled = IS_HWFLOW;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeInt(userId);
                data.writeString(bundle.getString("name"));
                data.writeBundle(policyData);
                data.writeInt(customType);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 1) {
                    z = true;
                }
                isDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isDisabled;
    }

    /* access modifiers changed from: package-private */
    public Bundle transactToGetPolicy(Bundle bundle, Bundle keyWords, ComponentName admin, int userId, int customType) {
        if (bundle == null) {
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int code = bundle.getInt("code");
        Bundle policyData = null;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeInt(userId);
                data.writeString(bundle.getString("name"));
                data.writeBundle(keyWords);
                data.writeInt(customType);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                policyData = reply.readBundle();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return policyData;
    }

    /* access modifiers changed from: package-private */
    public boolean transactToRemovePolicy(int code, String policyName, ComponentName admin, int userId, int customType) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean isDisabled = IS_HWFLOW;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeInt(userId);
                data.writeString(policyName);
                data.writeInt(customType);
                IBinder iBinder = sBinder;
                boolean z = IS_HWFLOW;
                iBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 1) {
                    z = true;
                }
                isDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactToHasHwPolicy(int code, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean isDisabled = IS_HWFLOW;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                data.writeInt(userId);
                IBinder iBinder = sBinder;
                boolean z = IS_HWFLOW;
                iBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 1) {
                    z = true;
                }
                isDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isDisabled;
    }

    /* access modifiers changed from: package-private */
    public void transactToSetAccountDisabled(int code, ComponentName admin, String accountType, boolean isDisabled, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeString(accountType);
                data.writeInt(isDisabled ? 1 : 0);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    /* access modifiers changed from: package-private */
    public boolean transactToIsAccountDisabled(int code, ComponentName admin, String accountType, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean isDisabled = IS_HWFLOW;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeString(accountType);
                data.writeInt(userId);
                IBinder iBinder = sBinder;
                boolean z = IS_HWFLOW;
                iBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 1) {
                    z = true;
                }
                isDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactToFormatSdCard(int code, ComponentName admin, String diskId, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean isDisabled = IS_HWFLOW;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                data.writeString(diskId);
                data.writeInt(userId);
                IBinder iBinder = sBinder;
                boolean z = IS_HWFLOW;
                iBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 1) {
                    z = true;
                }
                isDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isDisabled;
    }

    private void writeCertificateInfo(Bundle bundle, byte[] certBuffer, Parcel data) {
        if (bundle != null) {
            int type = bundle.getInt("type");
            String name = bundle.getString("name");
            String password = bundle.getString("password");
            int flag = bundle.getInt("flag");
            boolean z = bundle.getBoolean("isRequestAccess");
            data.writeInt(type);
            data.writeInt(certBuffer.length);
            data.writeByteArray(certBuffer);
            data.writeString(name);
            data.writeString(password);
            data.writeInt(flag);
            data.writeInt(z ? 1 : 0);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean transactToInstallCertificateWithType(int code, ComponentName admin, Bundle bundle, byte[] certBuffer, int userId) {
        boolean z = IS_HWFLOW;
        if (bundle == null) {
            return IS_HWFLOW;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean isDisabled = IS_HWFLOW;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                writeCertificateInfo(bundle, certBuffer, data);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 1) {
                    z = true;
                }
                isDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + code + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactToSetDefaultDataCard(int code, ComponentName admin, int slotId, Message response, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean isSuccess = IS_HWFLOW;
        try {
            if (getBinderInstance() != null) {
                logTransaction(code);
                writeAdminInfo(admin, data);
                boolean z = true;
                if (response != null) {
                    data.writeInt(1);
                    response.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                data.writeInt(slotId);
                data.writeInt(userId);
                sBinder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 1) {
                    z = false;
                }
                isSuccess = z;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "transactTo " + code + " failed.");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isSuccess;
    }
}
