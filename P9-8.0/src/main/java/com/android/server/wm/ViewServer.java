package com.android.server.wm;

import android.util.Slog;
import com.android.server.wm.WindowManagerService.WindowChangeListener;
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
    private final WindowManagerService mWindowManager;

    class ViewServerWorker implements Runnable, WindowChangeListener {
        private Socket mClient;
        private boolean mNeedFocusedWindowUpdate = false;
        private boolean mNeedWindowListUpdate = false;

        public ViewServerWorker(Socket client) {
            this.mClient = client;
        }

        /* JADX WARNING: Removed duplicated region for block: B:44:0x00e2 A:{SYNTHETIC, Splitter: B:44:0x00e2} */
        /* JADX WARNING: Removed duplicated region for block: B:72:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:48:0x00e9 A:{SYNTHETIC, Splitter: B:48:0x00e9} */
        /* JADX WARNING: Removed duplicated region for block: B:56:0x00fe A:{SYNTHETIC, Splitter: B:56:0x00fe} */
        /* JADX WARNING: Removed duplicated region for block: B:60:0x0105 A:{SYNTHETIC, Splitter: B:60:0x0105} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            IOException e;
            Throwable th;
            BufferedReader in = null;
            try {
                BufferedReader in2 = new BufferedReader(new InputStreamReader(this.mClient.getInputStream()), 1024);
                try {
                    String command;
                    String parameters;
                    boolean result;
                    String request = in2.readLine();
                    int index = request.indexOf(32);
                    if (index == -1) {
                        command = request;
                        parameters = "";
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
                    }
                    if (!result) {
                        Slog.w(ViewServer.LOG_TAG, "An error occurred with the command: " + command);
                    }
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (this.mClient != null) {
                        try {
                            this.mClient.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    in = in2;
                } catch (IOException e3) {
                    e22 = e3;
                    in = in2;
                    try {
                        Slog.w(ViewServer.LOG_TAG, "Connection error: ", e22);
                        if (in != null) {
                        }
                        if (this.mClient == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        if (this.mClient != null) {
                            try {
                                this.mClient.close();
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    if (in != null) {
                    }
                    if (this.mClient != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e2222 = e4;
                Slog.w(ViewServer.LOG_TAG, "Connection error: ", e2222);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                if (this.mClient == null) {
                    try {
                        this.mClient.close();
                    } catch (IOException e222222) {
                        e222222.printStackTrace();
                    }
                }
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

        /* JADX WARNING: Removed duplicated region for block: B:22:0x0038 A:{SYNTHETIC, Splitter: B:22:0x0038} */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x0072 A:{SYNTHETIC, Splitter: B:45:0x0072} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean windowManagerAutolistLoop() {
            Throwable th;
            ViewServer.this.mWindowManager.addWindowChangeListener(this);
            BufferedWriter bufferedWriter = null;
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.mClient.getOutputStream()));
                while (!Thread.interrupted()) {
                    try {
                        boolean needWindowListUpdate = false;
                        boolean needFocusedWindowUpdate = false;
                        synchronized (this) {
                            while (!this.mNeedWindowListUpdate && (this.mNeedFocusedWindowUpdate ^ 1) != 0) {
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
                    } catch (Exception e) {
                        bufferedWriter = out;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e2) {
                            }
                        }
                        ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                        return true;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedWriter = out;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e3) {
                            }
                        }
                        ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                        throw th;
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
            } catch (Exception e5) {
                if (bufferedWriter != null) {
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                return true;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedWriter != null) {
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                throw th;
            }
            return true;
        }
    }

    ViewServer(WindowManagerService windowManager, int port) {
        this.mWindowManager = windowManager;
        this.mPort = port;
    }

    boolean start() throws IOException {
        if (this.mThread != null) {
            return false;
        }
        this.mServer = new ServerSocket(this.mPort, 10, InetAddress.getLocalHost());
        this.mThread = new Thread(this, "Remote View Server [port=" + this.mPort + "]");
        this.mThreadPool = Executors.newFixedThreadPool(10);
        this.mThread.start();
        return true;
    }

    boolean stop() {
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

    boolean isRunning() {
        return this.mThread != null ? this.mThread.isAlive() : false;
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

    /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002c A:{SYNTHETIC, Splitter: B:15:0x002c} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0036 A:{SYNTHETIC, Splitter: B:21:0x0036} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean writeValue(Socket client, String value) {
        Throwable th;
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), 8192);
            try {
                out2.write(value);
                out2.write("\n");
                out2.flush();
                boolean result = true;
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e) {
                        result = false;
                    }
                }
                out = out2;
                return result;
            } catch (Exception e2) {
                out = out2;
                if (out != null) {
                }
            } catch (Throwable th2) {
                th = th2;
                out = out2;
                if (out != null) {
                }
                throw th;
            }
        } catch (Exception e3) {
            if (out != null) {
                return false;
            }
            try {
                out.close();
                return false;
            } catch (IOException e4) {
                return false;
            }
        } catch (Throwable th3) {
            th = th3;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
    }
}
