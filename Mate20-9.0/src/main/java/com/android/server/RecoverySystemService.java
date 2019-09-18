package com.android.server;

import android.content.Context;
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
    /* access modifiers changed from: private */
    public static final Object sRequestLock = new Object();
    /* access modifiers changed from: private */
    public Context mContext;

    private final class BinderService extends IRecoverySystem.Stub {
        private BinderService() {
        }

        /* JADX WARNING: type inference failed for: r3v0, types: [java.lang.String] */
        /* JADX WARNING: type inference failed for: r3v3, types: [java.lang.AutoCloseable] */
        /* JADX WARNING: type inference failed for: r3v5 */
        /* JADX WARNING: type inference failed for: r3v6 */
        /* JADX WARNING: type inference failed for: r3v7 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean uncrypt(String filename, IRecoverySystemProgressListener listener) {
            FileWriter uncryptFile;
            int status;
            synchronized (RecoverySystemService.sRequestLock) {
                ? r3 = 0;
                RecoverySystemService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", r3);
                if (!checkAndWaitForUncryptService()) {
                    Slog.e(RecoverySystemService.TAG, "uncrypt service is unavailable.");
                    return false;
                }
                RecoverySystem.UNCRYPT_PACKAGE_FILE.delete();
                try {
                    uncryptFile = new FileWriter(RecoverySystem.UNCRYPT_PACKAGE_FILE);
                    uncryptFile.write(filename + "\n");
                    uncryptFile.close();
                    SystemProperties.set("ctl.start", RecoverySystemService.UNCRYPT_SOCKET);
                    LocalSocket socket = connectService();
                    if (socket == null) {
                        Slog.e(RecoverySystemService.TAG, "Failed to connect to uncrypt socket");
                        return false;
                    }
                    DataInputStream dis = null;
                    try {
                        dis = new DataInputStream(socket.getInputStream());
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        int lastStatus = Integer.MIN_VALUE;
                        while (true) {
                            status = dis.readInt();
                            if (status != lastStatus || lastStatus == Integer.MIN_VALUE) {
                                lastStatus = status;
                                if (status >= 0 && status <= 100) {
                                    Slog.i(RecoverySystemService.TAG, "uncrypt read status: " + status);
                                    if (listener != null) {
                                        try {
                                            listener.onProgress(status);
                                        } catch (RemoteException e) {
                                            r3 = dos;
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
                        r3 = dos;
                        Slog.e(RecoverySystemService.TAG, "uncrypt failed with status: " + status);
                        dos.writeInt(0);
                        r3 = dos;
                        IoUtils.closeQuietly(dis);
                        IoUtils.closeQuietly(dos);
                        IoUtils.closeQuietly(socket);
                        return false;
                    } catch (IOException e2) {
                        try {
                            Slog.e(RecoverySystemService.TAG, "IOException when reading status: ", e2);
                            return false;
                        } finally {
                            IoUtils.closeQuietly(dis);
                            IoUtils.closeQuietly(r3);
                            IoUtils.closeQuietly(socket);
                        }
                    }
                } catch (IOException e3) {
                    Slog.e(RecoverySystemService.TAG, "IOException when writing \"" + RecoverySystem.UNCRYPT_PACKAGE_FILE + "\":", e3);
                    return false;
                } catch (Throwable th) {
                    r3.addSuppressed(th);
                }
            }
            throw th;
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
            for (int retry = 0; retry < 30; retry++) {
                if (!("running".equals(SystemProperties.get(RecoverySystemService.INIT_SERVICE_UNCRYPT)) || "running".equals(SystemProperties.get(RecoverySystemService.INIT_SERVICE_SETUP_BCB)) || "running".equals(SystemProperties.get(RecoverySystemService.INIT_SERVICE_CLEAR_BCB)))) {
                    return true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Slog.w(RecoverySystemService.TAG, "Interrupted:", e);
                }
            }
            return false;
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

        /* JADX WARNING: type inference failed for: r2v0, types: [java.lang.String] */
        /* JADX WARNING: type inference failed for: r2v1, types: [java.lang.AutoCloseable] */
        /* JADX WARNING: type inference failed for: r2v3 */
        /* JADX WARNING: type inference failed for: r2v4 */
        /* JADX WARNING: Multi-variable type inference failed */
        private boolean setupOrClearBcb(boolean isSetup, String command) {
            ? r2 = 0;
            RecoverySystemService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", r2);
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
            try {
                dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
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
                r2 = dos;
                Slog.e(RecoverySystemService.TAG, "uncrypt failed with status: " + status);
                r2 = dos;
                return false;
            } catch (IOException e) {
                Slog.e(RecoverySystemService.TAG, "IOException when communicating with uncrypt:", e);
                return false;
            } finally {
                IoUtils.closeQuietly(dis);
                IoUtils.closeQuietly(r2);
                IoUtils.closeQuietly(socket);
            }
        }
    }

    public RecoverySystemService(Context context) {
        super(context);
        this.mContext = context;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.RecoverySystemService$BinderService, android.os.IBinder] */
    public void onStart() {
        publishBinderService("recovery", new BinderService());
    }
}
