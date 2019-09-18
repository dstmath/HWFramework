package com.android.server.wm;

import android.util.Slog;
import com.android.server.wm.WindowManagerService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ViewServer implements Runnable {
    private static final String COMMAND_PROTOCOL_VERSION = "PROTOCOL";
    private static final String COMMAND_SERVER_VERSION = "SERVER";
    private static final String COMMAND_WINDOW_MANAGER_AUTOLIST = "AUTOLIST";
    private static final String COMMAND_WINDOW_MANAGER_GET_FOCUS = "GET_FOCUS";
    private static final String COMMAND_WINDOW_MANAGER_LIST = "LIST";
    private static final String LOG_TAG = "WindowManager";
    private static final String VALUE_PROTOCOL_VERSION = "4";
    private static final String VALUE_SERVER_VERSION = "4";
    public static final int VIEW_SERVER_DEFAULT_PORT = 4939;
    private static final int VIEW_SERVER_MAX_CONNECTIONS = 10;
    private final int mPort;
    private ServerSocket mServer;
    private Thread mThread;
    private ExecutorService mThreadPool;
    /* access modifiers changed from: private */
    public final WindowManagerService mWindowManager;

    class ViewServerWorker implements Runnable, WindowManagerService.WindowChangeListener {
        private Socket mClient;
        private boolean mNeedFocusedWindowUpdate = false;
        private boolean mNeedWindowListUpdate = false;

        public ViewServerWorker(Socket client) {
            this.mClient = client;
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x0097 A[Catch:{ IOException -> 0x00c7, all -> 0x00c5 }] */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x00ba A[SYNTHETIC, Splitter:B:31:0x00ba] */
        /* JADX WARNING: Removed duplicated region for block: B:61:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
        public void run() {
            String parameters;
            String command;
            boolean result;
            BufferedReader in = null;
            try {
                BufferedReader in2 = new BufferedReader(new InputStreamReader(this.mClient.getInputStream()), 1024);
                String request = in2.readLine();
                int index = request.indexOf(32);
                if (index == -1) {
                    command = request;
                    parameters = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                } else {
                    command = request.substring(0, index);
                    parameters = request.substring(index + 1);
                }
                if (ViewServer.COMMAND_PROTOCOL_VERSION.equalsIgnoreCase(command)) {
                    result = ViewServer.writeValue(this.mClient, "4");
                } else if (ViewServer.COMMAND_SERVER_VERSION.equalsIgnoreCase(command)) {
                    result = ViewServer.writeValue(this.mClient, "4");
                } else if (ViewServer.COMMAND_WINDOW_MANAGER_LIST.equalsIgnoreCase(command)) {
                    result = ViewServer.this.mWindowManager.viewServerListWindows(this.mClient);
                } else if (ViewServer.COMMAND_WINDOW_MANAGER_GET_FOCUS.equalsIgnoreCase(command)) {
                    result = ViewServer.this.mWindowManager.viewServerGetFocusedWindow(this.mClient);
                } else if (ViewServer.COMMAND_WINDOW_MANAGER_AUTOLIST.equalsIgnoreCase(command)) {
                    result = windowManagerAutolistLoop();
                } else {
                    result = ViewServer.this.mWindowManager.viewServerWindowCommand(this.mClient, command, parameters);
                    if (!result) {
                        Slog.w(ViewServer.LOG_TAG, "An error occurred with the command: " + command);
                    }
                    in2.close();
                    if (this.mClient == null) {
                        try {
                            this.mClient.close();
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        return;
                    }
                }
                if (!result) {
                }
                try {
                    in2.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                if (this.mClient == null) {
                }
            } catch (IOException e3) {
                Slog.w(ViewServer.LOG_TAG, "Connection error: ", e3);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                if (this.mClient != null) {
                    this.mClient.close();
                }
            } catch (Throwable th) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                if (this.mClient != null) {
                    try {
                        this.mClient.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                throw th;
            }
        }

        public void windowsChanged() {
            synchronized (this) {
                this.mNeedWindowListUpdate = true;
                notifyAll();
            }
        }

        public void focusChanged() {
            synchronized (this) {
                this.mNeedFocusedWindowUpdate = true;
                notifyAll();
            }
        }

        private boolean windowManagerAutolistLoop() {
            ViewServer.this.mWindowManager.addWindowChangeListener(this);
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(this.mClient.getOutputStream()));
                while (!Thread.interrupted()) {
                    boolean needWindowListUpdate = false;
                    boolean needFocusedWindowUpdate = false;
                    synchronized (this) {
                        while (!this.mNeedWindowListUpdate && !this.mNeedFocusedWindowUpdate) {
                            wait();
                        }
                        if (this.mNeedWindowListUpdate) {
                            this.mNeedWindowListUpdate = false;
                            needWindowListUpdate = true;
                        }
                        if (this.mNeedFocusedWindowUpdate) {
                            this.mNeedFocusedWindowUpdate = false;
                            needFocusedWindowUpdate = true;
                        }
                    }
                    if (needWindowListUpdate) {
                        out.write("LIST UPDATE\n");
                        out.flush();
                    }
                    if (needFocusedWindowUpdate) {
                        out.write("ACTION_FOCUS UPDATE\n");
                        out.flush();
                    }
                }
                try {
                    out.close();
                } catch (IOException e) {
                }
            } catch (Exception e2) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Throwable th) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                throw th;
            }
            ViewServer.this.mWindowManager.removeWindowChangeListener(this);
            return true;
        }
    }

    ViewServer(WindowManagerService windowManager, int port) {
        this.mWindowManager = windowManager;
        this.mPort = port;
    }

    /* access modifiers changed from: package-private */
    public boolean start() throws IOException {
        if (this.mThread != null) {
            return false;
        }
        this.mServer = new ServerSocket(this.mPort, 10, InetAddress.getLocalHost());
        this.mThread = new Thread(this, "Remote View Server [port=" + this.mPort + "]");
        this.mThreadPool = Executors.newFixedThreadPool(10);
        this.mThread.start();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean stop() {
        if (this.mThread != null) {
            this.mThread.interrupt();
            if (this.mThreadPool != null) {
                try {
                    this.mThreadPool.shutdownNow();
                } catch (SecurityException e) {
                    Slog.w(LOG_TAG, "Could not stop all view server threads");
                }
            }
            this.mThreadPool = null;
            this.mThread = null;
            try {
                this.mServer.close();
                this.mServer = null;
                return true;
            } catch (IOException e2) {
                Slog.w(LOG_TAG, "Could not close the view server");
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isRunning() {
        return this.mThread != null && this.mThread.isAlive();
    }

    public void run() {
        while (Thread.currentThread() == this.mThread) {
            try {
                Socket client = this.mServer.accept();
                if (this.mThreadPool != null) {
                    this.mThreadPool.submit(new ViewServerWorker(client));
                } else {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e2) {
                Slog.w(LOG_TAG, "Connection error: ", e2);
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean writeValue(Socket client, String value) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), 8192);
            out.write(value);
            out.write("\n");
            out.flush();
            try {
                out.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (Exception e2) {
            if (out == null) {
                return false;
            }
            out.close();
            return false;
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
    }
}
