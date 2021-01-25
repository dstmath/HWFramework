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

/* access modifiers changed from: package-private */
public class ViewServer implements Runnable {
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

    ViewServer(WindowManagerService windowManager, int port) {
        this.mWindowManager = windowManager;
        this.mPort = port;
    }

    /* access modifiers changed from: package-private */
    public boolean start() throws IOException {
        if (this.mThread != null) {
            return false;
        }
        this.mServer = new ServerSocket(this.mPort, VIEW_SERVER_MAX_CONNECTIONS, InetAddress.getLocalHost());
        this.mThread = new Thread(this, "Remote View Server [port=" + this.mPort + "]");
        this.mThreadPool = Executors.newFixedThreadPool(VIEW_SERVER_MAX_CONNECTIONS);
        this.mThread.start();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean stop() {
        Thread thread = this.mThread;
        if (thread == null) {
            return false;
        }
        thread.interrupt();
        ExecutorService executorService = this.mThreadPool;
        if (executorService != null) {
            try {
                executorService.shutdownNow();
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
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isRunning() {
        Thread thread = this.mThread;
        return thread != null && thread.isAlive();
    }

    @Override // java.lang.Runnable
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

    class ViewServerWorker implements Runnable, WindowManagerService.WindowChangeListener {
        private Socket mClient;
        private boolean mNeedFocusedWindowUpdate = false;
        private boolean mNeedWindowListUpdate = false;

        public ViewServerWorker(Socket client) {
            this.mClient = client;
        }

        @Override // java.lang.Runnable
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
                try {
                    in2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Socket socket = this.mClient;
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            } catch (IOException e3) {
                Slog.w(ViewServer.LOG_TAG, "Connection error: ", e3);
                if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                Socket socket2 = this.mClient;
                if (socket2 != null) {
                    socket2.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                Socket socket3 = this.mClient;
                if (socket3 != null) {
                    try {
                        socket3.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                throw th;
            }
        }

        @Override // com.android.server.wm.WindowManagerService.WindowChangeListener
        public void windowsChanged() {
            synchronized (this) {
                this.mNeedWindowListUpdate = true;
                notifyAll();
            }
        }

        @Override // com.android.server.wm.WindowManagerService.WindowChangeListener
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
                BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(this.mClient.getOutputStream()));
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
                        out2.write("LIST UPDATE\n");
                        out2.flush();
                    }
                    if (needFocusedWindowUpdate) {
                        out2.write("ACTION_FOCUS UPDATE\n");
                        out2.flush();
                    }
                }
                try {
                    out2.close();
                } catch (IOException e) {
                }
            } catch (Exception e2) {
                if (0 != 0) {
                    try {
                        out.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
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
}
