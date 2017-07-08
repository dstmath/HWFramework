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
    private static final String LOG_TAG = null;
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
        private boolean mNeedFocusedWindowUpdate;
        private boolean mNeedWindowListUpdate;

        public ViewServerWorker(Socket client) {
            this.mClient = client;
            this.mNeedWindowListUpdate = false;
            this.mNeedFocusedWindowUpdate = false;
        }

        public void run() {
            IOException e;
            Throwable th;
            BufferedReader bufferedReader = null;
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(this.mClient.getInputStream()), DumpState.DUMP_PROVIDERS);
                try {
                    String command;
                    String parameters;
                    boolean result;
                    String request = in.readLine();
                    int index = request.indexOf(32);
                    if (index == -1) {
                        command = request;
                        parameters = "";
                    } else {
                        command = request.substring(0, index);
                        parameters = request.substring(index + 1);
                    }
                    if (ViewServer.COMMAND_PROTOCOL_VERSION.equalsIgnoreCase(command)) {
                        result = ViewServer.writeValue(this.mClient, ViewServer.VALUE_SERVER_VERSION);
                    } else if (ViewServer.COMMAND_SERVER_VERSION.equalsIgnoreCase(command)) {
                        result = ViewServer.writeValue(this.mClient, ViewServer.VALUE_SERVER_VERSION);
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
                    if (in != null) {
                        try {
                            in.close();
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
                    bufferedReader = in;
                } catch (IOException e3) {
                    e22 = e3;
                    bufferedReader = in;
                    try {
                        Slog.w(ViewServer.LOG_TAG, "Connection error: ", e22);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
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
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        if (this.mClient != null) {
                            try {
                                this.mClient.close();
                            } catch (IOException e222222) {
                                e222222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = in;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (this.mClient != null) {
                        this.mClient.close();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e222222 = e4;
                Slog.w(ViewServer.LOG_TAG, "Connection error: ", e222222);
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (this.mClient != null) {
                    this.mClient.close();
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean windowManagerAutolistLoop() {
            Throwable th;
            ViewServer.this.mWindowManager.addWindowChangeListener(this);
            BufferedWriter bufferedWriter = null;
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.mClient.getOutputStream()));
                loop0:
                while (!Thread.interrupted()) {
                    try {
                        boolean needWindowListUpdate = false;
                        boolean needFocusedWindowUpdate = false;
                        synchronized (this) {
                            while (true) {
                                if (!this.mNeedWindowListUpdate && !this.mNeedFocusedWindowUpdate) {
                                    wait();
                                }
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
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedWriter = out;
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e2) {
                    }
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
            } catch (Exception e3) {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e4) {
                    }
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                return true;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e5) {
                    }
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                throw th;
            }
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.ViewServer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.ViewServer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.ViewServer.<clinit>():void");
    }

    ViewServer(WindowManagerService windowManager, int port) {
        this.mWindowManager = windowManager;
        this.mPort = port;
    }

    boolean start() throws IOException {
        if (this.mThread != null) {
            return false;
        }
        this.mServer = new ServerSocket(this.mPort, VIEW_SERVER_MAX_CONNECTIONS, InetAddress.getLocalHost());
        this.mThread = new Thread(this, "Remote View Server [port=" + this.mPort + "]");
        this.mThreadPool = Executors.newFixedThreadPool(VIEW_SERVER_MAX_CONNECTIONS);
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

    private static boolean writeValue(Socket client, String value) {
        Throwable th;
        BufferedWriter bufferedWriter = null;
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), DumpState.DUMP_PREFERRED_XML);
            try {
                out.write(value);
                out.write("\n");
                out.flush();
                boolean result = true;
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        result = false;
                    }
                }
                bufferedWriter = out;
                return result;
            } catch (Exception e2) {
                bufferedWriter = out;
                if (bufferedWriter != null) {
                    return false;
                }
                try {
                    bufferedWriter.close();
                    return false;
                } catch (IOException e3) {
                    return false;
                }
            } catch (Throwable th2) {
                th = th2;
                bufferedWriter = out;
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } catch (Exception e5) {
            if (bufferedWriter != null) {
                return false;
            }
            bufferedWriter.close();
            return false;
        } catch (Throwable th3) {
            th = th3;
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            throw th;
        }
    }
}
