package defpackage;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;

/* renamed from: bv */
public class bv {
    private static boolean cn;
    private static boolean co;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: bv.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: bv.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: bv.<clinit>():void");
    }

    public static void A(String str) {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Parcel parcel = null;
        aw.i("PushLog2828", "ctrlScoket registerPackage " + str);
        if (!TextUtils.isEmpty(str)) {
            try {
                IBinder service = ServiceManager.getService("connectivity");
                if (service == null) {
                    aw.e("PushLog2828", "get connectivity service failed ");
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                        return;
                    }
                    return;
                }
                obtain = Parcel.obtain();
                try {
                    obtain.writeString(str);
                    parcel = Parcel.obtain();
                    service.transact(1001, obtain, parcel, 0);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                } catch (RemoteException e2) {
                    e = e2;
                    try {
                        aw.e("PushLog2828", "registerPackage error:" + e.getMessage());
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        throw th;
                    }
                } catch (Exception e3) {
                    th = e3;
                    aw.d("PushLog2828", "registerPackage error:", th);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                }
            } catch (RemoteException e4) {
                e = e4;
                obtain = parcel;
                aw.e("PushLog2828", "registerPackage error:" + e.getMessage());
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Exception e5) {
                th = e5;
                obtain = parcel;
                aw.d("PushLog2828", "registerPackage error:", th);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Throwable th3) {
                th = th3;
                obtain = parcel;
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                throw th;
            }
        }
    }

    public static void B(String str) {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Parcel parcel = null;
        aw.i("PushLog2828", "ctrlScoket deregisterPackage " + str);
        if (!TextUtils.isEmpty(str)) {
            try {
                IBinder service = ServiceManager.getService("connectivity");
                if (service == null) {
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                        return;
                    }
                    return;
                }
                obtain = Parcel.obtain();
                try {
                    obtain.writeString(str);
                    parcel = Parcel.obtain();
                    service.transact(1002, obtain, parcel, 0);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                } catch (RemoteException e2) {
                    e = e2;
                    try {
                        aw.e("PushLog2828", "deregisterPackage error:" + e.getMessage());
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        throw th;
                    }
                } catch (Exception e3) {
                    th = e3;
                    aw.d("PushLog2828", "deregisterPackage error:", th);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                }
            } catch (RemoteException e4) {
                e = e4;
                obtain = parcel;
                aw.e("PushLog2828", "deregisterPackage error:" + e.getMessage());
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Exception e5) {
                th = e5;
                obtain = parcel;
                aw.d("PushLog2828", "deregisterPackage error:", th);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Throwable th3) {
                th = th3;
                obtain = parcel;
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                throw th;
            }
        }
    }

    public static void c(int i, int i2) {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Parcel parcel = null;
        aw.i("PushLog2828", "ctrlSocket cmd is " + i + ", param is " + i2);
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                aw.w("PushLog2828", "get connectivity service failed ");
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                    return;
                }
                return;
            }
            obtain = Parcel.obtain();
            try {
                obtain.writeInt(Process.myPid());
                obtain.writeInt(i);
                obtain.writeInt(i2);
                parcel = Parcel.obtain();
                service.transact(1003, obtain, parcel, 0);
                aw.i("PushLog2828", "ctrlSocket success");
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (RemoteException e2) {
                e = e2;
                try {
                    aw.e("PushLog2828", "ctrlSocket error:" + e.getMessage());
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    throw th;
                }
            } catch (Exception e3) {
                th = e3;
                aw.d("PushLog2828", "ctrlSocket error:", th);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            }
        } catch (RemoteException e4) {
            e = e4;
            obtain = parcel;
            aw.e("PushLog2828", "ctrlSocket error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
        } catch (Exception e5) {
            th = e5;
            obtain = parcel;
            aw.d("PushLog2828", "ctrlSocket error:", th);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
        } catch (Throwable th3) {
            th = th3;
            obtain = parcel;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw th;
        }
    }

    public static String[] cp() {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Throwable e2;
        Parcel parcel = null;
        String[] strArr = new String[0];
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                aw.w("PushLog2828", "get connectivity service failed ");
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } else {
                obtain = Parcel.obtain();
                try {
                    parcel = Parcel.obtain();
                    service.transact(1004, obtain, parcel, 0);
                    Object readString = parcel.readString();
                    aw.i("PushLog2828", "ctrlSocket whitepackages is:" + readString);
                    if (!TextUtils.isEmpty(readString)) {
                        strArr = readString.split("\t");
                    }
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                } catch (RemoteException e3) {
                    e = e3;
                    try {
                        aw.e("PushLog2828", "ctrlSocket error:" + e.getMessage());
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        return strArr;
                    } catch (Throwable th2) {
                        th = th2;
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    e2 = e4;
                    aw.d("PushLog2828", "ctrlSocket error:", e2);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    return strArr;
                }
            }
        } catch (RemoteException e5) {
            e = e5;
            obtain = parcel;
            aw.e("PushLog2828", "ctrlSocket error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            return strArr;
        } catch (Exception e6) {
            e2 = e6;
            obtain = parcel;
            aw.d("PushLog2828", "ctrlSocket error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            return strArr;
        } catch (Throwable th3) {
            th = th3;
            obtain = parcel;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw th;
        }
        return strArr;
    }

    public static int cq() {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Throwable e2;
        Parcel parcel = null;
        int i = -1;
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                aw.w("PushLog2828", "get connectivity service failed ");
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                return i;
            }
            obtain = Parcel.obtain();
            try {
                parcel = Parcel.obtain();
                service.transact(1005, obtain, parcel, 0);
                i = parcel.readInt();
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (RemoteException e3) {
                e = e3;
                try {
                    aw.e("PushLog2828", "getCtrlSocketModel error:" + e.getMessage());
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    aw.i("PushLog2828", "ctrlSocket level is:" + i);
                    return i;
                } catch (Throwable th2) {
                    th = th2;
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e2 = e4;
                aw.d("PushLog2828", "getCtrlSocketModel error:", e2);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                aw.i("PushLog2828", "ctrlSocket level is:" + i);
                return i;
            }
            aw.i("PushLog2828", "ctrlSocket level is:" + i);
            return i;
        } catch (RemoteException e5) {
            e = e5;
            obtain = parcel;
            aw.e("PushLog2828", "getCtrlSocketModel error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            aw.i("PushLog2828", "ctrlSocket level is:" + i);
            return i;
        } catch (Exception e6) {
            e2 = e6;
            obtain = parcel;
            aw.d("PushLog2828", "getCtrlSocketModel error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            aw.i("PushLog2828", "ctrlSocket level is:" + i);
            return i;
        } catch (Throwable th3) {
            th = th3;
            obtain = parcel;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw th;
        }
    }

    private static String cr() {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Throwable e2;
        Parcel parcel = null;
        String str = "";
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                aw.w("PushLog2828", "get connectivity service failed ");
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                return str;
            }
            obtain = Parcel.obtain();
            try {
                parcel = Parcel.obtain();
                service.transact(1006, obtain, parcel, 0);
                str = parcel.readString();
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (RemoteException e3) {
                e = e3;
                try {
                    aw.e("PushLog2828", "getCtrlSocketVersion error:" + e.getMessage());
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    aw.i("PushLog2828", "ctrlSocket version is:" + str);
                    return str;
                } catch (Throwable th2) {
                    th = th2;
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e2 = e4;
                aw.d("PushLog2828", "getCtrlSocketVersion error:", e2);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                aw.i("PushLog2828", "ctrlSocket version is:" + str);
                return str;
            }
            aw.i("PushLog2828", "ctrlSocket version is:" + str);
            return str;
        } catch (RemoteException e5) {
            e = e5;
            obtain = parcel;
            aw.e("PushLog2828", "getCtrlSocketVersion error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            aw.i("PushLog2828", "ctrlSocket version is:" + str);
            return str;
        } catch (Exception e6) {
            e2 = e6;
            obtain = parcel;
            aw.d("PushLog2828", "getCtrlSocketVersion error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            aw.i("PushLog2828", "ctrlSocket version is:" + str);
            return str;
        } catch (Throwable th3) {
            th = th3;
            obtain = parcel;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw th;
        }
    }

    public static boolean cs() {
        String str = "v2";
        aw.i("PushLog2828", "enter isSupportCtrlSocketV2, mHasCheckCtrlSocketVersion:" + cn + ",mIsSupportCtrlSokceV2:" + co);
        if (!cn) {
            cn = true;
            co = str.equals(bv.cr());
            aw.i("PushLog2828", "mIsSupportCtrlSokceV2:" + co);
        }
        return co;
    }
}
