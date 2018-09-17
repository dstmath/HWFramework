package huawei.android.app.admin;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
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
    protected static final boolean HWFLOW;
    private static final String TAG = "TransactionSponsor";

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    void transactTo_uninstallPackage(int code, String transactName, ComponentName who, String packageName, boolean keepData, int userId) {
        int i = 1;
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
                if (!keepData) {
                    i = 0;
                }
                _data.writeInt(i);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    boolean transactTo_isFunctionDisabled(int code, String transactName, ComponentName who, int userId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean bDisabled = false;
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
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bDisabled;
    }

    boolean transactTo_setFunctionDisabled(int code, String transactName, ComponentName who, boolean disabled, int userId) {
        int i = 1;
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
                if (!disabled) {
                    i = 0;
                }
                _data.writeInt(i);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return false;
    }

    void transactTo_execCommand(int code, String transactName, ComponentName who, int userId) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    void transactTo_execCommand(int code, String transactName, ComponentName who, String param, int userId) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    boolean transactTo_execCommand(int code, String transactName, ComponentName who, List<String> param, int userId) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return false;
    }

    void transactTo_execCommand(int code, String transactName, ComponentName who, Map param, int userId) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    void transactTo_execCommand(int code, String transactName, ComponentName who, Map param1, String param2, int userId) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    List<String> transactTo_getListFunction(int code, String transactName, ComponentName who, int userId) {
        RemoteException localRemoteException;
        Throwable th;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        List<String> list = null;
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
                List<String> stringList = new ArrayList();
                try {
                    _reply.readStringList(stringList);
                    list = stringList;
                } catch (RemoteException e) {
                    localRemoteException = e;
                    list = stringList;
                    try {
                        Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                        _reply.recycle();
                        _data.recycle();
                        return list;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException e2) {
            localRemoteException = e2;
        }
        return list;
    }

    List<String> transactTo_queryApn(int code, String transactName, ComponentName who, Map param, int userId) {
        RemoteException localRemoteException;
        Throwable th;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        List<String> list = null;
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
                List<String> stringList = new ArrayList();
                try {
                    _reply.readStringList(stringList);
                    list = stringList;
                } catch (RemoteException e) {
                    localRemoteException = e;
                    list = stringList;
                    try {
                        Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                        _reply.recycle();
                        _data.recycle();
                        return list;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException e2) {
            localRemoteException = e2;
        }
        return list;
    }

    Map<String, String> transactTo_getApnInfo(int code, String transactName, ComponentName who, String param, int userId) {
        RemoteException localRemoteException;
        Throwable th;
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
                Map<String, String> map2 = new HashMap();
                try {
                    _reply.readMap(map2, null);
                    map = map2;
                } catch (RemoteException e) {
                    localRemoteException = e;
                    map = map2;
                    try {
                        Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                        _reply.recycle();
                        _data.recycle();
                        return map;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException e2) {
            localRemoteException = e2;
        }
        return map;
    }

    void transactTo_configExchangeMail(int code, String transactName, ComponentName who, Bundle para, int userId) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    Bundle transactTo_getMailProviderForDomain(int code, String transactName, ComponentName who, String domain, int userId) {
        RemoteException localRemoteException;
        Throwable th;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        Bundle bundle = null;
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
                    Bundle para = new Bundle();
                    try {
                        para.readFromParcel(_reply);
                        bundle = para;
                    } catch (RemoteException e) {
                        localRemoteException = e;
                        bundle = para;
                        try {
                            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
                            _reply.recycle();
                            _data.recycle();
                            return bundle;
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                }
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException e2) {
            localRemoteException = e2;
        }
        return bundle;
    }

    void transactTo_setDefaultLauncher(int code, String transactName, ComponentName who, String packageName, String className, int userId) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    Bitmap transactTo_captureScreen(int code, String transactName, ComponentName who, int userId) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bitmap;
    }

    int transactTo_getSDCardEncryptionStatus(int code, String transactName, int userId) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return encryptionStatus;
    }

    boolean transactTo_setPolicy(int code, String policyName, String transactName, ComponentName who, int userId, Bundle policyData, int customType) {
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
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bDisabled;
    }

    Bundle transactTo_getPolicy(int code, String policyName, Bundle keyWords, String transactName, ComponentName who, int userId, int customType) {
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
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return policyData;
    }

    boolean transactTo_removePolicy(int code, String policyName, String transactName, ComponentName who, int userId, int customType) {
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
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bDisabled;
    }

    boolean transactTo_hasHwPolicy(int code, String transactName, int userId) {
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
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bDisabled;
    }

    void transactTo_setAccountDisabled(int code, String transactName, ComponentName who, String accountType, boolean disabled, int userId) {
        int i = 1;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
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
                _data.writeString(accountType);
                if (!disabled) {
                    i = 0;
                }
                _data.writeInt(i);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
    }

    boolean transactTo_isAccountDisabled(int code, String transactName, ComponentName who, String accountType, int userId) {
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
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bDisabled;
    }

    boolean transactTo_formatSDCard(int code, String transactName, ComponentName who, String diskId, int userId) {
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
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bDisabled;
    }

    boolean transactTo_installCertificateWithType(int code, String transactName, ComponentName who, int type, byte[] certBuffer, String name, String password, int flag, boolean requestAccess, int userId) {
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
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(type);
                _data.writeInt(certBuffer.length);
                _data.writeByteArray(certBuffer);
                _data.writeString(name);
                _data.writeString(password);
                _data.writeInt(flag);
                _data.writeInt(requestAccess ? 1 : 0);
                _data.writeInt(userId);
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bDisabled;
    }

    boolean transactTo_setCarrierLockScreenPassword(int code, String transactName, ComponentName who, String password, String phoneNumber, int userId) {
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
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bDisabled;
    }

    boolean transactTo_clearCarrierLockScreenPassword(int code, String transactName, ComponentName who, String password, int userId) {
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
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return bDisabled;
    }
}
