package com.android.server;

import android.content.Context;
import android.net.INetd;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.IRecoverySystem;
import android.os.IRecoverySystemProgressListener;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import libcore.io.IoUtils;

public final class RecoverySystemService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String INIT_SERVICE_CLEAR_BCB = "init.svc.clear-bcb";
    private static final String INIT_SERVICE_SETUP_BCB = "init.svc.setup-bcb";
    private static final String INIT_SERVICE_UNCRYPT = "init.svc.uncrypt";
    private static final int SOCKET_CONNECTION_MAX_RETRY = 30;
    private static final String TAG = "RecoverySystemService";
    private static final String UNCRYPT_SOCKET = "uncrypt";
    private static final Object sRequestLock = new Object();
    private Context mContext;

    public RecoverySystemService(Context context) {
        super(context);
        this.mContext = context;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.RecoverySystemService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.RecoverySystemService$BinderService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("recovery", new BinderService());
    }

    private final class BinderService extends IRecoverySystem.Stub {
        private BinderService() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:55:0x010f, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x0114, code lost:
            r6 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x0115, code lost:
            r4.addSuppressed(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x0118, code lost:
            throw r5;
         */
        public boolean uncrypt(String filename, IRecoverySystemProgressListener listener) {
            LocalSocket socket;
            int lastStatus;
            int status;
            synchronized (RecoverySystemService.sRequestLock) {
                RecoverySystemService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
                if (!checkAndWaitForUncryptService()) {
                    Slog.e(RecoverySystemService.TAG, "uncrypt service is unavailable.");
                    return false;
                }
                RecoverySystem.UNCRYPT_PACKAGE_FILE.delete();
                try {
                    FileWriter uncryptFile = new FileWriter(RecoverySystem.UNCRYPT_PACKAGE_FILE);
                    uncryptFile.write(filename + "\n");
                    uncryptFile.close();
                    SystemProperties.set("ctl.start", RecoverySystemService.UNCRYPT_SOCKET);
                    socket = connectService();
                } catch (IOException e) {
                    Slog.e(RecoverySystemService.TAG, "IOException when writing \"" + RecoverySystem.UNCRYPT_PACKAGE_FILE + "\":", e);
                    return false;
                }
                if (socket == null) {
                    Slog.e(RecoverySystemService.TAG, "Failed to connect to uncrypt socket");
                    return false;
                }
                DataInputStream dis = null;
                DataOutputStream dos = null;
                try {
                    dis = new DataInputStream(socket.getInputStream());
                    dos = new DataOutputStream(socket.getOutputStream());
                    lastStatus = Integer.MIN_VALUE;
                    Slog.e(RecoverySystemService.TAG, "uncrypt failed with status: " + status);
                    dos.writeInt(0);
                    return false;
                } catch (IOException e2) {
                    Slog.e(RecoverySystemService.TAG, "IOException when reading status: ", e2);
                    return false;
                } finally {
                    IoUtils.closeQuietly(dis);
                    IoUtils.closeQuietly(dos);
                    IoUtils.closeQuietly(socket);
                }
                while (true) {
                    status = dis.readInt();
                    if (status != lastStatus || lastStatus == Integer.MIN_VALUE) {
                        lastStatus = status;
                        if (status < 0 || status > 100) {
                            break;
                        }
                        Slog.i(RecoverySystemService.TAG, "uncrypt read status: " + status);
                        if (listener != null) {
                            try {
                                listener.onProgress(status);
                            } catch (RemoteException e3) {
                                Slog.w(RecoverySystemService.TAG, "RemoteException when posting progress");
                            }
                        }
                        if (status == 100) {
                            Slog.i(RecoverySystemService.TAG, "uncrypt successfully finished.");
                            dos.writeInt(0);
                            IoUtils.closeQuietly(dis);
                            IoUtils.closeQuietly(dos);
                            IoUtils.closeQuietly(socket);
                            return true;
                        }
                    }
                }
            }
        }

        public boolean clearBcb() {
            boolean z;
            synchronized (RecoverySystemService.sRequestLock) {
                z = setupOrClearBcb(false, null);
            }
            return z;
        }

        public boolean setupBcb(String command) {
            boolean z;
            synchronized (RecoverySystemService.sRequestLock) {
                z = setupOrClearBcb(true, command);
            }
            return z;
        }

        public void rebootRecoveryWithCommand(String command) {
            synchronized (RecoverySystemService.sRequestLock) {
                if (setupOrClearBcb(true, command)) {
                    ((PowerManager) RecoverySystemService.this.mContext.getSystemService("power")).reboot("recovery");
                }
            }
        }

        private boolean checkAndWaitForUncryptService() {
            int retry = 0;
            while (true) {
                boolean busy = false;
                if (retry >= 30) {
                    return false;
                }
                String uncryptService = SystemProperties.get(RecoverySystemService.INIT_SERVICE_UNCRYPT);
                String setupBcbService = SystemProperties.get(RecoverySystemService.INIT_SERVICE_SETUP_BCB);
                String clearBcbService = SystemProperties.get(RecoverySystemService.INIT_SERVICE_CLEAR_BCB);
                if (INetd.IF_FLAG_RUNNING.equals(uncryptService) || INetd.IF_FLAG_RUNNING.equals(setupBcbService) || INetd.IF_FLAG_RUNNING.equals(clearBcbService)) {
                    busy = true;
                }
                if (!busy) {
                    return true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Slog.w(RecoverySystemService.TAG, "Interrupted:", e);
                }
                retry++;
            }
        }

        private LocalSocket connectService() {
            LocalSocket socket = new LocalSocket();
            boolean done = false;
            int retry = 0;
            while (true) {
                if (retry >= 30) {
                    break;
                }
                try {
                    socket.connect(new LocalSocketAddress(RecoverySystemService.UNCRYPT_SOCKET, LocalSocketAddress.Namespace.RESERVED));
                    done = true;
                    break;
                } catch (IOException e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        Slog.w(RecoverySystemService.TAG, "Interrupted:", e2);
                    }
                    retry++;
                }
            }
            if (done) {
                return socket;
            }
            Slog.e(RecoverySystemService.TAG, "Timed out connecting to uncrypt socket");
            return null;
        }

        private boolean setupOrClearBcb(boolean isSetup, String command) {
            RecoverySystemService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
            if (!checkAndWaitForUncryptService()) {
                Slog.e(RecoverySystemService.TAG, "uncrypt service is unavailable.");
                return false;
            }
            if (isSetup) {
                SystemProperties.set("ctl.start", "setup-bcb");
            } else {
                SystemProperties.set("ctl.start", "clear-bcb");
            }
            LocalSocket socket = connectService();
            if (socket == null) {
                Slog.e(RecoverySystemService.TAG, "Failed to connect to uncrypt socket");
                return false;
            }
            DataInputStream dis = null;
            DataOutputStream dos = null;
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                if (isSetup) {
                    byte[] cmdUtf8 = command.getBytes("UTF-8");
                    dos.writeInt(cmdUtf8.length);
                    dos.write(cmdUtf8, 0, cmdUtf8.length);
                    dos.flush();
                }
                int status = dis.readInt();
                dos.writeInt(0);
                if (status == 100) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("uncrypt ");
                    sb.append(isSetup ? "setup" : "clear");
                    sb.append(" bcb successfully finished.");
                    Slog.i(RecoverySystemService.TAG, sb.toString());
                    IoUtils.closeQuietly(dis);
                    IoUtils.closeQuietly(dos);
                    IoUtils.closeQuietly(socket);
                    return true;
                }
                Slog.e(RecoverySystemService.TAG, "uncrypt failed with status: " + status);
                return false;
            } catch (IOException e) {
                Slog.e(RecoverySystemService.TAG, "IOException when communicating with uncrypt:", e);
                return false;
            } finally {
                IoUtils.closeQuietly(dis);
                IoUtils.closeQuietly(dos);
                IoUtils.closeQuietly(socket);
            }
        }
    }
}
