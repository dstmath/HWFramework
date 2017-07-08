package android.os;

import android.system.OsConstants;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Locale;

class CommonTimeUtils {
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -4;
    public static final int ERROR_DEAD_OBJECT = -7;
    public static final int SUCCESS = 0;
    private String mInterfaceDesc;
    private IBinder mRemote;

    public CommonTimeUtils(IBinder remote, String interfaceDesc) {
        this.mRemote = remote;
        this.mInterfaceDesc = interfaceDesc;
    }

    public int transactGetInt(int method_code, int error_ret_val) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            this.mRemote.transact(method_code, data, reply, 0);
            int ret_val = reply.readInt() == 0 ? reply.readInt() : error_ret_val;
            reply.recycle();
            data.recycle();
            return ret_val;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public int transactSetInt(int method_code, int val) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int readInt;
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            data.writeInt(val);
            this.mRemote.transact(method_code, data, reply, 0);
            readInt = reply.readInt();
            return readInt;
        } catch (RemoteException e) {
            readInt = ERROR_DEAD_OBJECT;
            return readInt;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public long transactGetLong(int method_code, long error_ret_val) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            this.mRemote.transact(method_code, data, reply, 0);
            long ret_val = reply.readInt() == 0 ? reply.readLong() : error_ret_val;
            reply.recycle();
            data.recycle();
            return ret_val;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public int transactSetLong(int method_code, long val) {
        int readInt;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            data.writeLong(val);
            this.mRemote.transact(method_code, data, reply, 0);
            readInt = reply.readInt();
            return readInt;
        } catch (RemoteException e) {
            readInt = ERROR_DEAD_OBJECT;
            return readInt;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public String transactGetString(int method_code, String error_ret_val) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            this.mRemote.transact(method_code, data, reply, 0);
            String ret_val = reply.readInt() == 0 ? reply.readString() : error_ret_val;
            reply.recycle();
            data.recycle();
            return ret_val;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public int transactSetString(int method_code, String val) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int readInt;
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            data.writeString(val);
            this.mRemote.transact(method_code, data, reply, 0);
            readInt = reply.readInt();
            return readInt;
        } catch (RemoteException e) {
            readInt = ERROR_DEAD_OBJECT;
            return readInt;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public InetSocketAddress transactGetSockaddr(int method_code) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        InetSocketAddress inetSocketAddress = null;
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            this.mRemote.transact(method_code, data, reply, 0);
            if (reply.readInt() == 0) {
                int port = 0;
                String addrStr = null;
                int type = reply.readInt();
                if (OsConstants.AF_INET == type) {
                    int addr = reply.readInt();
                    port = reply.readInt();
                    Object[] objArr = new Object[4];
                    objArr[0] = Integer.valueOf((addr >> 24) & Process.PROC_TERM_MASK);
                    objArr[1] = Integer.valueOf((addr >> 16) & Process.PROC_TERM_MASK);
                    objArr[2] = Integer.valueOf((addr >> 8) & Process.PROC_TERM_MASK);
                    objArr[3] = Integer.valueOf(addr & Process.PROC_TERM_MASK);
                    addrStr = String.format(Locale.US, "%d.%d.%d.%d", objArr);
                } else if (OsConstants.AF_INET6 == type) {
                    int addr1 = reply.readInt();
                    int addr2 = reply.readInt();
                    int addr3 = reply.readInt();
                    int addr4 = reply.readInt();
                    port = reply.readInt();
                    int flowinfo = reply.readInt();
                    int scope_id = reply.readInt();
                    addrStr = String.format(Locale.US, "[%04X:%04X:%04X:%04X:%04X:%04X:%04X:%04X]", new Object[]{Integer.valueOf((addr1 >> 16) & PowerManager.WAKE_LOCK_LEVEL_MASK), Integer.valueOf(PowerManager.WAKE_LOCK_LEVEL_MASK & addr1), Integer.valueOf((addr2 >> 16) & PowerManager.WAKE_LOCK_LEVEL_MASK), Integer.valueOf(PowerManager.WAKE_LOCK_LEVEL_MASK & addr2), Integer.valueOf((addr3 >> 16) & PowerManager.WAKE_LOCK_LEVEL_MASK), Integer.valueOf(PowerManager.WAKE_LOCK_LEVEL_MASK & addr3), Integer.valueOf((addr4 >> 16) & PowerManager.WAKE_LOCK_LEVEL_MASK), Integer.valueOf(PowerManager.WAKE_LOCK_LEVEL_MASK & addr4)});
                }
                if (addrStr != null) {
                    inetSocketAddress = new InetSocketAddress(addrStr, port);
                }
            }
            reply.recycle();
            data.recycle();
            return inetSocketAddress;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public int transactSetSockaddr(int method_code, InetSocketAddress addr) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int ret_val = ERROR;
        try {
            data.writeInterfaceToken(this.mInterfaceDesc);
            if (addr == null) {
                data.writeInt(0);
            } else {
                data.writeInt(1);
                InetAddress a = addr.getAddress();
                byte[] b = a.getAddress();
                int p = addr.getPort();
                if (a instanceof Inet4Address) {
                    int v4addr = ((((b[0] & Process.PROC_TERM_MASK) << 24) | ((b[1] & Process.PROC_TERM_MASK) << 16)) | ((b[2] & Process.PROC_TERM_MASK) << 8)) | (b[3] & Process.PROC_TERM_MASK);
                    data.writeInt(OsConstants.AF_INET);
                    data.writeInt(v4addr);
                    data.writeInt(p);
                } else if (a instanceof Inet6Address) {
                    Inet6Address v6 = (Inet6Address) a;
                    data.writeInt(OsConstants.AF_INET6);
                    for (int i = 0; i < 4; i++) {
                        data.writeInt(((((b[(i * 4) + 0] & Process.PROC_TERM_MASK) << 24) | ((b[(i * 4) + 1] & Process.PROC_TERM_MASK) << 16)) | ((b[(i * 4) + 2] & Process.PROC_TERM_MASK) << 8)) | (b[(i * 4) + 3] & Process.PROC_TERM_MASK));
                    }
                    data.writeInt(p);
                    data.writeInt(0);
                    data.writeInt(v6.getScopeId());
                } else {
                    reply.recycle();
                    data.recycle();
                    return ERROR_BAD_VALUE;
                }
            }
            this.mRemote.transact(method_code, data, reply, 0);
            ret_val = reply.readInt();
        } catch (RemoteException e) {
            ret_val = ERROR_DEAD_OBJECT;
        } finally {
            reply.recycle();
            data.recycle();
        }
        return ret_val;
    }
}
