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
    protected static final boolean HWDBG = false;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "TransactionSponsor";

    /* access modifiers changed from: package-private */
    public void transactTo_uninstallPackage(int code, String transactName, ComponentName who, String packageName, boolean keepData, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + "to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                int i = 1;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(packageName);
                if (!keepData) {
                    i = 0;
                }
                _data.writeInt(i);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_isFunctionDisabled(int code, String transactName, ComponentName who, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                boolean z = true;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 1) {
                    z = false;
                }
                bDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_setFunctionDisabled(int code, String transactName, ComponentName who, boolean disabled, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + "to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                int i = 1;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                if (!disabled) {
                    i = 0;
                }
                _data.writeInt(i);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return false;
    }

    /* access modifiers changed from: package-private */
    public void transactTo_execCommand(int code, String transactName, ComponentName who, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact: " + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    /* access modifiers changed from: package-private */
    public void transactTo_execCommand(int code, String transactName, ComponentName who, String param, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact: " + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(param);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_execCommand(int code, String transactName, ComponentName who, List<String> param, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.code is " + code);
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeStringList(param);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return false;
    }

    /* access modifiers changed from: package-private */
    public void transactTo_execCommand(int code, String transactName, ComponentName who, Map param, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.code is " + code);
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeMap(param);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    /* access modifiers changed from: package-private */
    public void transactTo_execCommand(int code, String transactName, ComponentName who, Map param1, String param2, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service. code is " + code);
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeMap(param1);
                _data.writeString(param2);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    /* access modifiers changed from: package-private */
    public List<String> transactTo_getListFunction(int code, String transactName, ComponentName who, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        List<String> stringList = null;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + "to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                stringList = new ArrayList<>();
                _reply.readStringList(stringList);
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return stringList;
    }

    /* access modifiers changed from: package-private */
    public List<String> transactTo_queryApn(int code, String transactName, ComponentName who, Map param, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        List<String> stringList = null;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeMap(param);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                stringList = new ArrayList<>();
                _reply.readStringList(stringList);
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return stringList;
    }

    /* access modifiers changed from: package-private */
    public Map<String, String> transactTo_getApnInfo(int code, String transactName, ComponentName who, String param, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        Map<String, String> map = null;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(param);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                map = new HashMap<>();
                _reply.readMap(map, null);
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return map;
    }

    /* access modifiers changed from: package-private */
    public void transactTo_configExchangeMail(int code, String transactName, ComponentName who, Bundle para, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + "to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                para.writeToParcel(_data, 0);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    /* access modifiers changed from: package-private */
    public Bundle transactTo_getMailProviderForDomain(int code, String transactName, ComponentName who, String domain, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        Bundle para = null;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + "to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(domain);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 0) {
                    para = new Bundle();
                    para.readFromParcel(_reply);
                }
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return para;
    }

    /* access modifiers changed from: package-private */
    public void transactTo_setDefaultLauncher(int code, String transactName, ComponentName who, String packageName, String className, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + "to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(packageName);
                _data.writeString(className);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    /* access modifiers changed from: package-private */
    public Bitmap transactTo_captureScreen(int code, String transactName, ComponentName who, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        Bitmap bitmap = null;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + "to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 0) {
                    bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(_reply);
                }
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bitmap;
    }

    /* access modifiers changed from: package-private */
    public int transactTo_getSDCardEncryptionStatus(int code, String transactName, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int encryptionStatus = 0;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                encryptionStatus = _reply.readInt();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return encryptionStatus;
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_setPolicy(int code, String policyName, String transactName, ComponentName who, int userId, Bundle policyData, int customType) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                boolean z = true;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(userId);
                _data.writeString(policyName);
                _data.writeBundle(policyData);
                _data.writeInt(customType);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 1) {
                    z = false;
                }
                bDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bDisabled;
    }

    /* access modifiers changed from: package-private */
    public Bundle transactTo_getPolicy(int code, String policyName, Bundle keyWords, String transactName, ComponentName who, int userId, int customType) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        Bundle policyData = null;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(userId);
                _data.writeString(policyName);
                _data.writeBundle(keyWords);
                _data.writeInt(customType);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                policyData = _reply.readBundle();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return policyData;
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_removePolicy(int code, String policyName, String transactName, ComponentName who, int userId, int customType) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                boolean z = true;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(userId);
                _data.writeString(policyName);
                _data.writeInt(customType);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 1) {
                    z = false;
                }
                bDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_hasHwPolicy(int code, String transactName, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                _data.writeInt(userId);
                boolean z = false;
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() == 1) {
                    z = true;
                }
                bDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bDisabled;
    }

    /* access modifiers changed from: package-private */
    public void transactTo_setAccountDisabled(int code, String transactName, ComponentName who, String accountType, boolean disabled, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                int i = 1;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(accountType);
                if (!disabled) {
                    i = 0;
                }
                _data.writeInt(i);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_isAccountDisabled(int code, String transactName, ComponentName who, String accountType, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                boolean z = true;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(accountType);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 1) {
                    z = false;
                }
                bDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_formatSDCard(int code, String transactName, ComponentName who, String diskId, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                boolean z = true;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(diskId);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 1) {
                    z = false;
                }
                bDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_installCertificateWithType(int code, String transactName, ComponentName who, int type, byte[] certBuffer, String name, String password, int flag, boolean requestAccess, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                int i = 1;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                try {
                    _data.writeInt(type);
                    _data.writeInt(certBuffer.length);
                    _data.writeByteArray(certBuffer);
                } catch (RemoteException e) {
                    localRemoteException = e;
                    try {
                        Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                        _reply.recycle();
                        _data.recycle();
                        return bDisabled;
                    } catch (Throwable th) {
                        th = th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
                try {
                    _data.writeString(name);
                    try {
                        _data.writeString(password);
                    } catch (RemoteException e2) {
                        localRemoteException = e2;
                        Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                        _reply.recycle();
                        _data.recycle();
                        return bDisabled;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (RemoteException e3) {
                    localRemoteException = e3;
                    Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                    _reply.recycle();
                    _data.recycle();
                    return bDisabled;
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
                try {
                    _data.writeInt(flag);
                    if (!requestAccess) {
                        i = 0;
                    }
                    try {
                        _data.writeInt(i);
                        try {
                            _data.writeInt(userId);
                        } catch (RemoteException e4) {
                            localRemoteException = e4;
                            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                            _reply.recycle();
                            _data.recycle();
                            return bDisabled;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (RemoteException e5) {
                        localRemoteException = e5;
                        Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                        _reply.recycle();
                        _data.recycle();
                        return bDisabled;
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        binder.transact(code, _data, _reply, 0);
                        _reply.readException();
                        boolean z = true;
                        if (_reply.readInt() != 1) {
                            z = false;
                        }
                        bDisabled = z;
                    } catch (RemoteException e6) {
                        localRemoteException = e6;
                        Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                        _reply.recycle();
                        _data.recycle();
                        return bDisabled;
                    }
                } catch (RemoteException e7) {
                    localRemoteException = e7;
                    Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                    _reply.recycle();
                    _data.recycle();
                    return bDisabled;
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
        } catch (RemoteException e8) {
            localRemoteException = e8;
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
            return bDisabled;
        } catch (Throwable th8) {
            th = th8;
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_setCarrierLockScreenPassword(int code, String transactName, ComponentName who, String password, String phoneNumber, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                boolean z = true;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(password);
                _data.writeString(phoneNumber);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 1) {
                    z = false;
                }
                bDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactTo_clearCarrierLockScreenPassword(int code, String transactName, ComponentName who, String password, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                _data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                boolean z = true;
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeString(password);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 1) {
                    z = false;
                }
                bDisabled = z;
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return bDisabled;
    }

    /* access modifiers changed from: package-private */
    public boolean transactToSetDefaultDataCard(int code, String transactName, ComponentName who, int slotId, Message response, int userId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean isSuccess = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                if (HWFLOW) {
                    Log.i(TAG, "Transact:" + transactName + " to device policy manager service.");
                }
                data.writeInterfaceToken(ConstantValue.DESCRIPTOR);
                boolean z = true;
                if (who != null) {
                    data.writeInt(1);
                    who.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                if (response != null) {
                    data.writeInt(1);
                    response.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                data.writeInt(slotId);
                data.writeInt(userId);
                binder.transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 1) {
                    z = false;
                }
                isSuccess = z;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "transactTo " + transactName + " failed.");
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
