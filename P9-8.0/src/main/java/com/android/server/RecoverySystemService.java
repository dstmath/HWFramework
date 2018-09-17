package com.android.server;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.IRecoverySystem.Stub;
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

    private final class BinderService extends Stub {
        /* synthetic */ BinderService(RecoverySystemService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:36:0x0089 A:{SYNTHETIC, Splitter: B:36:0x0089} */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x009f A:{SYNTHETIC, Splitter: B:50:0x009f} */
        /* JADX WARNING: Removed duplicated region for block: B:39:0x008e A:{SYNTHETIC, Splitter: B:39:0x008e} */
        /* JADX WARNING: Missing block: B:100:?, code:
            android.util.Slog.e(com.android.server.RecoverySystemService.TAG, "uncrypt failed with status: " + r10);
            r5.writeInt(0);
     */
        /* JADX WARNING: Missing block: B:102:?, code:
            libcore.io.IoUtils.closeQuietly(r3);
            libcore.io.IoUtils.closeQuietly(r5);
            libcore.io.IoUtils.closeQuietly(r9);
     */
        /* JADX WARNING: Missing block: B:105:0x016c, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean uncrypt(String filename, IRecoverySystemProgressListener listener) {
            IOException e;
            Throwable th;
            Object dis;
            synchronized (RecoverySystemService.sRequestLock) {
                RecoverySystemService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
                if (checkAndWaitForUncryptService()) {
                    RecoverySystem.UNCRYPT_PACKAGE_FILE.delete();
                    Throwable th2 = null;
                    FileWriter uncryptFile = null;
                    try {
                        FileWriter uncryptFile2 = new FileWriter(RecoverySystem.UNCRYPT_PACKAGE_FILE);
                        try {
                            uncryptFile2.write(filename + "\n");
                            if (uncryptFile2 != null) {
                                try {
                                    uncryptFile2.close();
                                } catch (Throwable th3) {
                                    th2 = th3;
                                }
                            }
                            if (th2 != null) {
                                try {
                                    throw th2;
                                } catch (IOException e2) {
                                    e = e2;
                                    uncryptFile = uncryptFile2;
                                }
                            } else {
                                SystemProperties.set("ctl.start", RecoverySystemService.UNCRYPT_SOCKET);
                                LocalSocket socket = connectService();
                                if (socket == null) {
                                    Slog.e(RecoverySystemService.TAG, "Failed to connect to uncrypt socket");
                                    return false;
                                }
                                AutoCloseable dis2 = null;
                                DataOutputStream dos = null;
                                try {
                                    DataInputStream dis3 = new DataInputStream(socket.getInputStream());
                                    try {
                                        DataOutputStream dos2 = new DataOutputStream(socket.getOutputStream());
                                        int lastStatus = Integer.MIN_VALUE;
                                        while (true) {
                                            try {
                                                int status = dis3.readInt();
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
                                                        dos2.writeInt(0);
                                                        IoUtils.closeQuietly(dis3);
                                                        IoUtils.closeQuietly(dos2);
                                                        IoUtils.closeQuietly(socket);
                                                        return true;
                                                    }
                                                }
                                            } catch (IOException e4) {
                                                e = e4;
                                                dos = dos2;
                                                dis2 = dis3;
                                            } catch (Throwable th4) {
                                                th = th4;
                                                dos = dos2;
                                                dis = dis3;
                                            }
                                        }
                                    } catch (IOException e5) {
                                        e = e5;
                                        dis = dis3;
                                        try {
                                            Slog.e(RecoverySystemService.TAG, "IOException when reading status: ", e);
                                            IoUtils.closeQuietly(dis2);
                                            IoUtils.closeQuietly(dos);
                                            IoUtils.closeQuietly(socket);
                                            return false;
                                        } catch (Throwable th5) {
                                            th = th5;
                                            IoUtils.closeQuietly(dis2);
                                            IoUtils.closeQuietly(dos);
                                            IoUtils.closeQuietly(socket);
                                            throw th;
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        dis = dis3;
                                        IoUtils.closeQuietly(dis2);
                                        IoUtils.closeQuietly(dos);
                                        IoUtils.closeQuietly(socket);
                                        throw th;
                                    }
                                } catch (IOException e6) {
                                    e = e6;
                                    Slog.e(RecoverySystemService.TAG, "IOException when reading status: ", e);
                                    IoUtils.closeQuietly(dis2);
                                    IoUtils.closeQuietly(dos);
                                    IoUtils.closeQuietly(socket);
                                    return false;
                                }
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            uncryptFile = uncryptFile2;
                            if (uncryptFile != null) {
                            }
                            if (th2 == null) {
                            }
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        if (uncryptFile != null) {
                            try {
                                uncryptFile.close();
                            } catch (Throwable th9) {
                                if (th2 == null) {
                                    th2 = th9;
                                } else if (th2 != th9) {
                                    th2.addSuppressed(th9);
                                }
                            }
                        }
                        if (th2 == null) {
                            try {
                                throw th2;
                            } catch (IOException e7) {
                                e = e7;
                            }
                        } else {
                            throw th;
                        }
                    }
                } else {
                    Slog.e(RecoverySystemService.TAG, "uncrypt service is unavailable.");
                    return false;
                }
            }
            Slog.e(RecoverySystemService.TAG, "IOException when writing \"" + RecoverySystem.UNCRYPT_PACKAGE_FILE + "\":", e);
            return false;
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
                    return;
                }
            }
        }

        private boolean checkAndWaitForUncryptService() {
            for (int retry = 0; retry < 30; retry++) {
                boolean busy;
                String uncryptService = SystemProperties.get(RecoverySystemService.INIT_SERVICE_UNCRYPT);
                String setupBcbService = SystemProperties.get(RecoverySystemService.INIT_SERVICE_SETUP_BCB);
                String clearBcbService = SystemProperties.get(RecoverySystemService.INIT_SERVICE_CLEAR_BCB);
                if ("running".equals(uncryptService) || "running".equals(setupBcbService)) {
                    busy = true;
                } else {
                    busy = "running".equals(clearBcbService);
                }
                if (!busy) {
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
            while (retry < 30) {
                try {
                    socket.connect(new LocalSocketAddress(RecoverySystemService.UNCRYPT_SOCKET, Namespace.RESERVED));
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
            IOException e;
            Object dos;
            Object dis;
            Throwable th;
            RecoverySystemService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
            if (checkAndWaitForUncryptService()) {
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
                AutoCloseable dis2 = null;
                AutoCloseable dos2 = null;
                try {
                    DataInputStream dis3 = new DataInputStream(socket.getInputStream());
                    try {
                        DataOutputStream dos3 = new DataOutputStream(socket.getOutputStream());
                        if (isSetup) {
                            try {
                                dos3.writeInt(command.length());
                                dos3.writeBytes(command);
                                dos3.flush();
                            } catch (IOException e2) {
                                e = e2;
                                dos = dos3;
                                dis2 = dis3;
                                try {
                                    Slog.e(RecoverySystemService.TAG, "IOException when communicating with uncrypt:", e);
                                    IoUtils.closeQuietly(dis2);
                                    IoUtils.closeQuietly(dos2);
                                    IoUtils.closeQuietly(socket);
                                    return false;
                                } catch (Throwable th2) {
                                    th = th2;
                                    IoUtils.closeQuietly(dis2);
                                    IoUtils.closeQuietly(dos2);
                                    IoUtils.closeQuietly(socket);
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                dos = dos3;
                                dis2 = dis3;
                                IoUtils.closeQuietly(dis2);
                                IoUtils.closeQuietly(dos2);
                                IoUtils.closeQuietly(socket);
                                throw th;
                            }
                        }
                        int status = dis3.readInt();
                        dos3.writeInt(0);
                        if (status == 100) {
                            Slog.i(RecoverySystemService.TAG, "uncrypt " + (isSetup ? "setup" : "clear") + " bcb successfully finished.");
                            IoUtils.closeQuietly(dis3);
                            IoUtils.closeQuietly(dos3);
                            IoUtils.closeQuietly(socket);
                            return true;
                        }
                        Slog.e(RecoverySystemService.TAG, "uncrypt failed with status: " + status);
                        IoUtils.closeQuietly(dis3);
                        IoUtils.closeQuietly(dos3);
                        IoUtils.closeQuietly(socket);
                        return false;
                    } catch (IOException e3) {
                        e = e3;
                        dis2 = dis3;
                        Slog.e(RecoverySystemService.TAG, "IOException when communicating with uncrypt:", e);
                        IoUtils.closeQuietly(dis2);
                        IoUtils.closeQuietly(dos2);
                        IoUtils.closeQuietly(socket);
                        return false;
                    } catch (Throwable th4) {
                        th = th4;
                        dis2 = dis3;
                        IoUtils.closeQuietly(dis2);
                        IoUtils.closeQuietly(dos2);
                        IoUtils.closeQuietly(socket);
                        throw th;
                    }
                } catch (IOException e4) {
                    e = e4;
                    Slog.e(RecoverySystemService.TAG, "IOException when communicating with uncrypt:", e);
                    IoUtils.closeQuietly(dis2);
                    IoUtils.closeQuietly(dos2);
                    IoUtils.closeQuietly(socket);
                    return false;
                }
            }
            Slog.e(RecoverySystemService.TAG, "uncrypt service is unavailable.");
            return false;
        }
    }

    public RecoverySystemService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        publishBinderService("recovery", new BinderService(this, null));
    }
}
