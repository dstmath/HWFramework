package com.huawei.android.pushagent.utils.tools;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import com.huawei.android.pushagent.utils.d.c;

public class d {
    private static boolean fc = false;
    private static boolean fd = false;

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x008e  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void qs(String str) {
        RemoteException e;
        Throwable e2;
        Parcel parcel = null;
        c.sh("PushLog2951", "ctrlScoket registerPackage " + str);
        if (!TextUtils.isEmpty(str)) {
            Parcel obtain;
            try {
                IBinder service = ServiceManager.getService("connectivity");
                if (service == null) {
                    c.sf("PushLog2951", "get connectivity service failed ");
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
                } catch (RemoteException e3) {
                    e = e3;
                    c.sf("PushLog2951", "registerPackage error:" + e.getMessage());
                    if (obtain != null) {
                    }
                    if (parcel != null) {
                    }
                } catch (Exception e4) {
                    e2 = e4;
                    try {
                        c.se("PushLog2951", "registerPackage error:", e2);
                        if (obtain != null) {
                        }
                        if (parcel != null) {
                        }
                    } catch (Throwable th) {
                        e2 = th;
                        if (obtain != null) {
                        }
                        if (parcel != null) {
                        }
                        throw e2;
                    }
                }
            } catch (RemoteException e5) {
                e = e5;
                obtain = null;
                c.sf("PushLog2951", "registerPackage error:" + e.getMessage());
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Exception e6) {
                e2 = e6;
                obtain = null;
                c.se("PushLog2951", "registerPackage error:", e2);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Throwable th2) {
                e2 = th2;
                obtain = null;
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                throw e2;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x005a  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0092  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void qt(String str) {
        RemoteException e;
        Throwable e2;
        Parcel parcel = null;
        c.sh("PushLog2951", "ctrlScoket deregisterPackage " + str);
        if (!TextUtils.isEmpty(str)) {
            Parcel obtain;
            try {
                IBinder service = ServiceManager.getService("connectivity");
                if (service != null) {
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
                    } catch (RemoteException e3) {
                        e = e3;
                        c.sf("PushLog2951", "deregisterPackage error:" + e.getMessage());
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                    } catch (Exception e4) {
                        e2 = e4;
                        try {
                            c.se("PushLog2951", "deregisterPackage error:", e2);
                            if (obtain != null) {
                                obtain.recycle();
                            }
                            if (parcel != null) {
                                parcel.recycle();
                            }
                        } catch (Throwable th) {
                            e2 = th;
                            if (obtain != null) {
                                obtain.recycle();
                            }
                            if (parcel != null) {
                                parcel.recycle();
                            }
                            throw e2;
                        }
                    }
                }
            } catch (RemoteException e5) {
                e = e5;
                obtain = null;
                c.sf("PushLog2951", "deregisterPackage error:" + e.getMessage());
                if (obtain != null) {
                }
                if (parcel != null) {
                }
            } catch (Exception e6) {
                e2 = e6;
                obtain = null;
                c.se("PushLog2951", "deregisterPackage error:", e2);
                if (obtain != null) {
                }
                if (parcel != null) {
                }
            } catch (Throwable th2) {
                e2 = th2;
                obtain = null;
                if (obtain != null) {
                }
                if (parcel != null) {
                }
                throw e2;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x007a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void qp(int i, int i2) {
        RemoteException e;
        Throwable e2;
        Parcel parcel = null;
        c.sh("PushLog2951", "ctrlSocket cmd is " + i + ", param is " + i2);
        Parcel obtain;
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                c.sj("PushLog2951", "get connectivity service failed ");
                return;
            }
            obtain = Parcel.obtain();
            try {
                obtain.writeInt(Process.myPid());
                obtain.writeInt(i);
                obtain.writeInt(i2);
                parcel = Parcel.obtain();
                service.transact(1003, obtain, parcel, 0);
                c.sh("PushLog2951", "ctrlSocket success");
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (RemoteException e3) {
                e = e3;
                c.sf("PushLog2951", "ctrlSocket error:" + e.getMessage());
                if (obtain != null) {
                }
                if (parcel != null) {
                }
            } catch (Exception e4) {
                e2 = e4;
                try {
                    c.se("PushLog2951", "ctrlSocket error:", e2);
                    if (obtain != null) {
                    }
                    if (parcel != null) {
                    }
                } catch (Throwable th) {
                    e2 = th;
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    throw e2;
                }
            }
        } catch (RemoteException e5) {
            e = e5;
            obtain = null;
            c.sf("PushLog2951", "ctrlSocket error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
        } catch (Exception e6) {
            e2 = e6;
            obtain = null;
            c.se("PushLog2951", "ctrlSocket error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
        } catch (Throwable th2) {
            e2 = th2;
            obtain = null;
            if (obtain != null) {
            }
            if (parcel != null) {
            }
            throw e2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a6  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0099  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String[] qr() {
        String[] strArr;
        RemoteException e;
        Throwable e2;
        Parcel parcel = null;
        String[] strArr2 = new String[0];
        Parcel obtain;
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                c.sj("PushLog2951", "get connectivity service failed ");
                return strArr2;
            }
            obtain = Parcel.obtain();
            try {
                parcel = Parcel.obtain();
                service.transact(1004, obtain, parcel, 0);
                Object readString = parcel.readString();
                c.sh("PushLog2951", "ctrlSocket whitepackages is:" + readString);
                if (TextUtils.isEmpty(readString)) {
                    strArr = strArr2;
                } else {
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
                c.sf("PushLog2951", "ctrlSocket error:" + e.getMessage());
                if (obtain != null) {
                }
                if (parcel != null) {
                }
                strArr = strArr2;
                return strArr;
            } catch (Exception e4) {
                e2 = e4;
                try {
                    c.se("PushLog2951", "ctrlSocket error:", e2);
                    if (obtain != null) {
                    }
                    if (parcel != null) {
                    }
                    strArr = strArr2;
                    return strArr;
                } catch (Throwable th) {
                    e2 = th;
                    if (obtain != null) {
                    }
                    if (parcel != null) {
                    }
                    throw e2;
                }
            }
            return strArr;
        } catch (RemoteException e5) {
            e = e5;
            obtain = parcel;
            c.sf("PushLog2951", "ctrlSocket error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            strArr = strArr2;
            return strArr;
        } catch (Exception e6) {
            e2 = e6;
            obtain = parcel;
            c.se("PushLog2951", "ctrlSocket error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            strArr = strArr2;
            return strArr;
        } catch (Throwable th2) {
            e2 = th2;
            obtain = parcel;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw e2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x005e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int qq() {
        int readInt;
        RemoteException e;
        Throwable e2;
        Parcel parcel = null;
        Parcel obtain;
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                c.sj("PushLog2951", "get connectivity service failed ");
                return -1;
            }
            obtain = Parcel.obtain();
            try {
                parcel = Parcel.obtain();
                service.transact(1005, obtain, parcel, 0);
                readInt = parcel.readInt();
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (RemoteException e3) {
                e = e3;
            } catch (Exception e4) {
                e2 = e4;
                try {
                    c.se("PushLog2951", "getCtrlSocketModel error:", e2);
                    if (obtain != null) {
                    }
                    if (parcel != null) {
                    }
                    readInt = -1;
                    c.sh("PushLog2951", "ctrlSocket level is:" + readInt);
                    return readInt;
                } catch (Throwable th) {
                    e2 = th;
                    if (obtain != null) {
                    }
                    if (parcel != null) {
                    }
                    throw e2;
                }
            }
            c.sh("PushLog2951", "ctrlSocket level is:" + readInt);
            return readInt;
        } catch (RemoteException e5) {
            e = e5;
            obtain = null;
            c.sf("PushLog2951", "getCtrlSocketModel error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            readInt = -1;
            c.sh("PushLog2951", "ctrlSocket level is:" + readInt);
            return readInt;
        } catch (Exception e6) {
            e2 = e6;
            obtain = null;
            c.se("PushLog2951", "getCtrlSocketModel error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            readInt = -1;
            c.sh("PushLog2951", "ctrlSocket level is:" + readInt);
            return readInt;
        } catch (Throwable th2) {
            e2 = th2;
            obtain = null;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw e2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0099  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0060  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String qu() {
        String readString;
        RemoteException e;
        Throwable e2;
        Parcel parcel = null;
        String str = "";
        Parcel obtain;
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                c.sj("PushLog2951", "get connectivity service failed ");
                return str;
            }
            obtain = Parcel.obtain();
            try {
                parcel = Parcel.obtain();
                service.transact(1006, obtain, parcel, 0);
                readString = parcel.readString();
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (RemoteException e3) {
                e = e3;
            } catch (Exception e4) {
                e2 = e4;
                try {
                    c.se("PushLog2951", "getCtrlSocketVersion error:", e2);
                    if (obtain != null) {
                    }
                    if (parcel != null) {
                    }
                    readString = str;
                    c.sh("PushLog2951", "ctrlSocket version is:" + readString);
                    return readString;
                } catch (Throwable th) {
                    e2 = th;
                    if (obtain != null) {
                    }
                    if (parcel != null) {
                    }
                    throw e2;
                }
            }
            c.sh("PushLog2951", "ctrlSocket version is:" + readString);
            return readString;
        } catch (RemoteException e5) {
            e = e5;
            obtain = null;
            c.sf("PushLog2951", "getCtrlSocketVersion error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            readString = str;
            c.sh("PushLog2951", "ctrlSocket version is:" + readString);
            return readString;
        } catch (Exception e6) {
            e2 = e6;
            obtain = null;
            c.se("PushLog2951", "getCtrlSocketVersion error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            readString = str;
            c.sh("PushLog2951", "ctrlSocket version is:" + readString);
            return readString;
        } catch (Throwable th2) {
            e2 = th2;
            obtain = null;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw e2;
        }
    }

    public static boolean qo() {
        String str = "v2";
        c.sh("PushLog2951", "enter isSupportCtrlSocketV2, mHasCheckCtrlSocketVersion:" + fc + ",mIsSupportCtrlSokceV2:" + fd);
        if (!fc) {
            fc = true;
            fd = str.equals(qu());
            c.sh("PushLog2951", "mIsSupportCtrlSokceV2:" + fd);
        }
        return fd;
    }
}
