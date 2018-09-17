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
    protected static boolean HWDBG = false;
    protected static boolean HWFLOW = false;
    private static final String TAG = "TransactionSponsor";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.app.admin.TransactionSponsor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.app.admin.TransactionSponsor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.app.admin.TransactionSponsor.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean transactTo_isFunctionDisabled(int code, String transactName, ComponentName who, int userId) {
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
                binder.transact(code, _data, _reply, 0);
                _reply.readException();
                bDisabled = _reply.readInt() == 1;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
        return bDisabled;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
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
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
            return list;
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
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
            return list;
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
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
            return map;
        }
        return map;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
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
            Log.e(TAG, "transactTo " + transactName + " failed: " + localRemoteException.getMessage());
            _reply.recycle();
            _data.recycle();
            return bundle;
        }
        return bundle;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
        return bitmap;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
        return encryptionStatus;
    }
}
