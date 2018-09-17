package android.ddm;

import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.view.WindowManagerGlobal;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;

public class DdmHandleViewDebug extends ChunkHandler {
    private static final int CHUNK_VULW = type("VULW");
    private static final int CHUNK_VUOP = type("VUOP");
    private static final int CHUNK_VURT = type("VURT");
    private static final int ERR_EXCEPTION = -3;
    private static final int ERR_INVALID_OP = -1;
    private static final int ERR_INVALID_PARAM = -2;
    private static final String TAG = "DdmViewDebug";
    private static final int VUOP_CAPTURE_VIEW = 1;
    private static final int VUOP_DUMP_DISPLAYLIST = 2;
    private static final int VUOP_INVOKE_VIEW_METHOD = 4;
    private static final int VUOP_PROFILE_VIEW = 3;
    private static final int VUOP_SET_LAYOUT_PARAMETER = 5;
    private static final int VURT_CAPTURE_LAYERS = 2;
    private static final int VURT_DUMP_HIERARCHY = 1;
    private static final int VURT_DUMP_THEME = 3;
    private static final DdmHandleViewDebug sInstance = new DdmHandleViewDebug();

    private DdmHandleViewDebug() {
    }

    public static void register() {
        DdmServer.registerHandler(CHUNK_VULW, sInstance);
        DdmServer.registerHandler(CHUNK_VURT, sInstance);
        DdmServer.registerHandler(CHUNK_VUOP, sInstance);
    }

    public void connected() {
    }

    public void disconnected() {
    }

    public Chunk handleChunk(Chunk request) {
        int type = request.type;
        if (type == CHUNK_VULW) {
            return listWindows();
        }
        ByteBuffer in = wrapChunk(request);
        int op = in.getInt();
        View rootView = getRootView(in);
        if (rootView == null) {
            return createFailChunk(-2, "Invalid View Root");
        }
        if (type != CHUNK_VURT) {
            View targetView = getTargetView(rootView, in);
            if (targetView == null) {
                return createFailChunk(-2, "Invalid target view");
            }
            if (type == CHUNK_VUOP) {
                switch (op) {
                    case 1:
                        return captureView(rootView, targetView);
                    case 2:
                        return dumpDisplayLists(rootView, targetView);
                    case 3:
                        return profileView(rootView, targetView);
                    case 4:
                        return invokeViewMethod(rootView, targetView, in);
                    case 5:
                        return setLayoutParameter(rootView, targetView, in);
                    default:
                        return createFailChunk(-1, "Unknown view operation: " + op);
                }
            }
            throw new RuntimeException("Unknown packet " + ChunkHandler.name(type));
        } else if (op == 1) {
            return dumpHierarchy(rootView, in);
        } else {
            if (op == 2) {
                return captureLayers(rootView);
            }
            if (op == 3) {
                return dumpTheme(rootView);
            }
            return createFailChunk(-1, "Unknown view root operation: " + op);
        }
    }

    private Chunk listWindows() {
        String name;
        int i = 0;
        String[] windowNames = WindowManagerGlobal.getInstance().getViewRootNames();
        int responseLength = 4;
        for (String name2 : windowNames) {
            responseLength = (responseLength + 4) + (name2.length() * 2);
        }
        ByteBuffer out = ByteBuffer.allocate(responseLength);
        out.order(ChunkHandler.CHUNK_ORDER);
        out.putInt(windowNames.length);
        int length = windowNames.length;
        while (i < length) {
            name2 = windowNames[i];
            out.putInt(name2.length());
            putString(out, name2);
            i++;
        }
        return new Chunk(CHUNK_VULW, out);
    }

    private View getRootView(ByteBuffer in) {
        try {
            return WindowManagerGlobal.getInstance().getRootView(getString(in, in.getInt()));
        } catch (BufferUnderflowException e) {
            return null;
        }
    }

    private View getTargetView(View root, ByteBuffer in) {
        try {
            return ViewDebug.findView(root, getString(in, in.getInt()));
        } catch (BufferUnderflowException e) {
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0063 A:{ExcHandler: java.io.IOException (r2_0 'e' java.lang.Exception), Splitter: B:13:0x0028} */
    /* JADX WARNING: Missing block: B:22:0x0063, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:24:0x0081, code:
            return createFailChunk(1, "Unexpected error while obtaining view hierarchy: " + r2.getMessage());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Chunk dumpHierarchy(View rootView, ByteBuffer in) {
        boolean skipChildren = in.getInt() > 0;
        boolean includeProperties = in.getInt() > 0;
        boolean v2 = in.hasRemaining() && in.getInt() > 0;
        long start = System.currentTimeMillis();
        ByteArrayOutputStream b = new ByteArrayOutputStream(2097152);
        if (v2) {
            try {
                ViewDebug.dumpv2(rootView, b);
            } catch (Exception e) {
            }
        } else {
            ViewDebug.dump(rootView, skipChildren, includeProperties, b);
        }
        Log.d(TAG, "Time to obtain view hierarchy (ms): " + (System.currentTimeMillis() - start));
        byte[] data = b.toByteArray();
        return new Chunk(CHUNK_VURT, data, 0, data.length);
    }

    private Chunk captureLayers(View rootView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
        DataOutputStream dos = new DataOutputStream(b);
        try {
            ViewDebug.captureLayers(rootView, dos);
            try {
                dos.close();
            } catch (IOException e) {
            }
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VURT, data, 0, data.length);
        } catch (IOException e2) {
            Chunk createFailChunk = createFailChunk(1, "Unexpected error while obtaining view hierarchy: " + e2.getMessage());
            try {
                dos.close();
            } catch (IOException e3) {
            }
            return createFailChunk;
        } catch (Throwable th) {
            try {
                dos.close();
            } catch (IOException e4) {
            }
            throw th;
        }
    }

    private Chunk dumpTheme(View rootView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
        try {
            ViewDebug.dumpTheme(rootView, b);
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VURT, data, 0, data.length);
        } catch (IOException e) {
            return createFailChunk(1, "Unexpected error while dumping the theme: " + e.getMessage());
        }
    }

    private Chunk captureView(View rootView, View targetView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
        try {
            ViewDebug.capture(rootView, b, targetView);
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VUOP, data, 0, data.length);
        } catch (IOException e) {
            return createFailChunk(1, "Unexpected error while capturing view: " + e.getMessage());
        }
    }

    private Chunk dumpDisplayLists(final View rootView, final View targetView) {
        rootView.post(new Runnable() {
            public void run() {
                ViewDebug.outputDisplayList(rootView, targetView);
            }
        });
        return null;
    }

    private Chunk invokeViewMethod(View rootView, View targetView, ByteBuffer in) {
        Class<?>[] argTypes;
        Object[] args;
        String methodName = getString(in, in.getInt());
        if (in.hasRemaining()) {
            int nArgs = in.getInt();
            argTypes = new Class[nArgs];
            args = new Object[nArgs];
            for (int i = 0; i < nArgs; i++) {
                char c = in.getChar();
                switch (c) {
                    case 'B':
                        argTypes[i] = Byte.TYPE;
                        args[i] = Byte.valueOf(in.get());
                        break;
                    case 'C':
                        argTypes[i] = Character.TYPE;
                        args[i] = Character.valueOf(in.getChar());
                        break;
                    case 'D':
                        argTypes[i] = Double.TYPE;
                        args[i] = Double.valueOf(in.getDouble());
                        break;
                    case 'F':
                        argTypes[i] = Float.TYPE;
                        args[i] = Float.valueOf(in.getFloat());
                        break;
                    case 'I':
                        argTypes[i] = Integer.TYPE;
                        args[i] = Integer.valueOf(in.getInt());
                        break;
                    case 'J':
                        argTypes[i] = Long.TYPE;
                        args[i] = Long.valueOf(in.getLong());
                        break;
                    case 'S':
                        argTypes[i] = Short.TYPE;
                        args[i] = Short.valueOf(in.getShort());
                        break;
                    case 'Z':
                        boolean z;
                        argTypes[i] = Boolean.TYPE;
                        if (in.get() == (byte) 0) {
                            z = false;
                        } else {
                            z = true;
                        }
                        args[i] = Boolean.valueOf(z);
                        break;
                    default:
                        Log.e(TAG, "arg " + i + ", unrecognized type: " + c);
                        return createFailChunk(-2, "Unsupported parameter type (" + c + ") to invoke view method.");
                }
            }
        } else {
            argTypes = new Class[0];
            args = new Object[0];
        }
        try {
            try {
                ViewDebug.invokeViewMethod(targetView, targetView.getClass().getMethod(methodName, argTypes), args);
                return null;
            } catch (Exception e) {
                Log.e(TAG, "Exception while invoking method: " + e.getCause().getMessage());
                String msg = e.getCause().getMessage();
                if (msg == null) {
                    msg = e.getCause().toString();
                }
                return createFailChunk(-3, msg);
            }
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "No such method: " + e2.getMessage());
            return createFailChunk(-2, "No such method: " + e2.getMessage());
        }
    }

    private Chunk setLayoutParameter(View rootView, View targetView, ByteBuffer in) {
        String param = getString(in, in.getInt());
        try {
            ViewDebug.setLayoutParameter(targetView, param, in.getInt());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception setting layout parameter: " + e);
            return createFailChunk(-3, "Error accessing field " + param + ":" + e.getMessage());
        }
    }

    private Chunk profileView(View rootView, View targetView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(32768);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(b), 32768);
        try {
            ViewDebug.profileViewAndChildren(targetView, bw);
            try {
                bw.close();
            } catch (IOException e) {
            }
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VUOP, data, 0, data.length);
        } catch (IOException e2) {
            Chunk createFailChunk = createFailChunk(1, "Unexpected error while profiling view: " + e2.getMessage());
            try {
                bw.close();
            } catch (IOException e3) {
            }
            return createFailChunk;
        } catch (Throwable th) {
            try {
                bw.close();
            } catch (IOException e4) {
            }
            throw th;
        }
    }
}
