package com.android.server;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.IRecoverySystem.Stub;
import android.os.IRecoverySystemProgressListener;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.usb.UsbAudioDevice;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import libcore.io.IoUtils;

public final class RecoverySystemService extends SystemService {
    private static final boolean DEBUG = false;
    private static final int SOCKET_CONNECTION_MAX_RETRY = 30;
    private static final String TAG = "RecoverySystemService";
    private static final String UNCRYPT_SOCKET = "uncrypt";
    private Context mContext;

    private final class BinderService extends Stub {
        private BinderService() {
        }

        public boolean uncrypt(String filename, IRecoverySystemProgressListener listener) {
            IOException e;
            Throwable th;
            Object dis;
            RecoverySystemService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
            RecoverySystem.UNCRYPT_PACKAGE_FILE.delete();
            Throwable th2 = null;
            FileWriter fileWriter = null;
            try {
                FileWriter uncryptFile = new FileWriter(RecoverySystem.UNCRYPT_PACKAGE_FILE);
                try {
                    uncryptFile.write(filename + "\n");
                    if (uncryptFile != null) {
                        try {
                            uncryptFile.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (IOException e2) {
                            e = e2;
                            fileWriter = uncryptFile;
                        }
                    } else {
                        SystemProperties.set("ctl.start", RecoverySystemService.UNCRYPT_SOCKET);
                        LocalSocket socket = connectService();
                        if (socket == null) {
                            Slog.e(RecoverySystemService.TAG, "Failed to connect to uncrypt socket");
                            return RecoverySystemService.DEBUG;
                        }
                        AutoCloseable autoCloseable = null;
                        DataOutputStream dos = null;
                        try {
                            DataInputStream dis2 = new DataInputStream(socket.getInputStream());
                            try {
                                int status;
                                DataOutputStream dos2 = new DataOutputStream(socket.getOutputStream());
                                int lastStatus = UsbAudioDevice.kAudioDeviceMeta_Alsa;
                                while (true) {
                                    try {
                                        status = dis2.readInt();
                                        if (status != lastStatus || lastStatus == UsbAudioDevice.kAudioDeviceMeta_Alsa) {
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
                                                IoUtils.closeQuietly(dis2);
                                                IoUtils.closeQuietly(dos2);
                                                IoUtils.closeQuietly(socket);
                                                return true;
                                            }
                                        }
                                    } catch (IOException e4) {
                                        e = e4;
                                        dos = dos2;
                                        autoCloseable = dis2;
                                    } catch (Throwable th4) {
                                        th = th4;
                                        dos = dos2;
                                        dis = dis2;
                                    }
                                }
                                Slog.e(RecoverySystemService.TAG, "uncrypt failed with status: " + status);
                                dos2.writeInt(0);
                                IoUtils.closeQuietly(dis2);
                                IoUtils.closeQuietly(dos2);
                                IoUtils.closeQuietly(socket);
                                return RecoverySystemService.DEBUG;
                            } catch (IOException e5) {
                                e = e5;
                                dis = dis2;
                                try {
                                    Slog.e(RecoverySystemService.TAG, "IOException when reading status: ", e);
                                    IoUtils.closeQuietly(autoCloseable);
                                    IoUtils.closeQuietly(dos);
                                    IoUtils.closeQuietly(socket);
                                    return RecoverySystemService.DEBUG;
                                } catch (Throwable th5) {
                                    th = th5;
                                    IoUtils.closeQuietly(autoCloseable);
                                    IoUtils.closeQuietly(dos);
                                    IoUtils.closeQuietly(socket);
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                dis = dis2;
                                IoUtils.closeQuietly(autoCloseable);
                                IoUtils.closeQuietly(dos);
                                IoUtils.closeQuietly(socket);
                                throw th;
                            }
                        } catch (IOException e6) {
                            e = e6;
                            Slog.e(RecoverySystemService.TAG, "IOException when reading status: ", e);
                            IoUtils.closeQuietly(autoCloseable);
                            IoUtils.closeQuietly(dos);
                            IoUtils.closeQuietly(socket);
                            return RecoverySystemService.DEBUG;
                        }
                    }
                } catch (Throwable th7) {
                    th = th7;
                    fileWriter = uncryptFile;
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (Throwable th8) {
                            if (th2 == null) {
                                th2 = th8;
                            } else if (th2 != th8) {
                                th2.addSuppressed(th8);
                            }
                        }
                    }
                    if (th2 == null) {
                        throw th;
                    }
                    try {
                        throw th2;
                    } catch (IOException e7) {
                        e = e7;
                        Slog.e(RecoverySystemService.TAG, "IOException when writing \"" + RecoverySystem.UNCRYPT_PACKAGE_FILE + "\": ", e);
                        return RecoverySystemService.DEBUG;
                    }
                }
            } catch (Throwable th9) {
                th = th9;
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        }

        public boolean clearBcb() {
            return setupOrClearBcb(RecoverySystemService.DEBUG, null);
        }

        public boolean setupBcb(String command) {
            return setupOrClearBcb(true, command);
        }

        private LocalSocket connectService() {
            LocalSocket socket = new LocalSocket();
            boolean done = RecoverySystemService.DEBUG;
            int retry = 0;
            while (retry < RecoverySystemService.SOCKET_CONNECTION_MAX_RETRY) {
                try {
                    socket.connect(new LocalSocketAddress(RecoverySystemService.UNCRYPT_SOCKET, Namespace.RESERVED));
                    done = true;
                    break;
                } catch (IOException e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        Slog.w(RecoverySystemService.TAG, "Interrupted: ", e2);
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
            Object obj;
            Throwable th;
            RecoverySystemService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
            if (isSetup) {
                SystemProperties.set("ctl.start", "setup-bcb");
            } else {
                SystemProperties.set("ctl.start", "clear-bcb");
            }
            LocalSocket socket = connectService();
            if (socket == null) {
                Slog.e(RecoverySystemService.TAG, "Failed to connect to uncrypt socket");
                return RecoverySystemService.DEBUG;
            }
            AutoCloseable dis = null;
            AutoCloseable autoCloseable = null;
            try {
                DataInputStream dis2 = new DataInputStream(socket.getInputStream());
                try {
                    DataOutputStream dos2 = new DataOutputStream(socket.getOutputStream());
                    if (isSetup) {
                        try {
                            dos2.writeInt(command.length());
                            dos2.writeBytes(command);
                            dos2.flush();
                        } catch (IOException e2) {
                            e = e2;
                            dos = dos2;
                            obj = dis2;
                            try {
                                Slog.e(RecoverySystemService.TAG, "IOException when communicating with uncrypt: ", e);
                                IoUtils.closeQuietly(dis);
                                IoUtils.closeQuietly(autoCloseable);
                                IoUtils.closeQuietly(socket);
                                return RecoverySystemService.DEBUG;
                            } catch (Throwable th2) {
                                th = th2;
                                IoUtils.closeQuietly(dis);
                                IoUtils.closeQuietly(autoCloseable);
                                IoUtils.closeQuietly(socket);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            dos = dos2;
                            obj = dis2;
                            IoUtils.closeQuietly(dis);
                            IoUtils.closeQuietly(autoCloseable);
                            IoUtils.closeQuietly(socket);
                            throw th;
                        }
                    }
                    int status = dis2.readInt();
                    dos2.writeInt(0);
                    if (status == 100) {
                        Slog.i(RecoverySystemService.TAG, "uncrypt " + (isSetup ? "setup" : "clear") + " bcb successfully finished.");
                        IoUtils.closeQuietly(dis2);
                        IoUtils.closeQuietly(dos2);
                        IoUtils.closeQuietly(socket);
                        return true;
                    }
                    Slog.e(RecoverySystemService.TAG, "uncrypt failed with status: " + status);
                    IoUtils.closeQuietly(dis2);
                    IoUtils.closeQuietly(dos2);
                    IoUtils.closeQuietly(socket);
                    return RecoverySystemService.DEBUG;
                } catch (IOException e3) {
                    e = e3;
                    obj = dis2;
                    Slog.e(RecoverySystemService.TAG, "IOException when communicating with uncrypt: ", e);
                    IoUtils.closeQuietly(dis);
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(socket);
                    return RecoverySystemService.DEBUG;
                } catch (Throwable th4) {
                    th = th4;
                    obj = dis2;
                    IoUtils.closeQuietly(dis);
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(socket);
                    throw th;
                }
            } catch (IOException e4) {
                e = e4;
                Slog.e(RecoverySystemService.TAG, "IOException when communicating with uncrypt: ", e);
                IoUtils.closeQuietly(dis);
                IoUtils.closeQuietly(autoCloseable);
                IoUtils.closeQuietly(socket);
                return RecoverySystemService.DEBUG;
            }
        }
    }

    public RecoverySystemService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        publishBinderService("recovery", new BinderService());
    }
}
