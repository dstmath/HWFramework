package android.ddm;

import android.provider.SettingsStringUtil;
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
            if (type != CHUNK_VUOP) {
                throw new RuntimeException("Unknown packet " + ChunkHandler.name(type));
            } else if (op == 1) {
                return captureView(rootView, targetView);
            } else {
                if (op == 2) {
                    return dumpDisplayLists(rootView, targetView);
                }
                if (op == 3) {
                    return profileView(rootView, targetView);
                }
                if (op == 4) {
                    return invokeViewMethod(rootView, targetView, in);
                }
                if (op == 5) {
                    return setLayoutParameter(rootView, targetView, in);
                }
                return createFailChunk(-1, "Unknown view operation: " + op);
            }
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
        String[] windowNames = WindowManagerGlobal.getInstance().getViewRootNames();
        int responseLength = 4;
        for (String name : windowNames) {
            responseLength = responseLength + 4 + (name.length() * 2);
        }
        ByteBuffer out = ByteBuffer.allocate(responseLength);
        out.order(ChunkHandler.CHUNK_ORDER);
        out.putInt(windowNames.length);
        for (String name2 : windowNames) {
            out.putInt(name2.length());
            putString(out, name2);
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

    private Chunk dumpHierarchy(View rootView, ByteBuffer in) {
        boolean skipChildren = in.getInt() > 0;
        boolean includeProperties = in.getInt() > 0;
        boolean v2 = in.hasRemaining() && in.getInt() > 0;
        long start = System.currentTimeMillis();
        ByteArrayOutputStream b = new ByteArrayOutputStream(2097152);
        if (v2) {
            try {
                ViewDebug.dumpv2(rootView, b);
            } catch (IOException | InterruptedException e) {
                return createFailChunk(1, "Unexpected error while obtaining view hierarchy: " + e.getMessage());
            }
        } else {
            ViewDebug.dump(rootView, skipChildren, includeProperties, b);
        }
        long end = System.currentTimeMillis();
        Log.d(TAG, "Time to obtain view hierarchy (ms): " + (end - start));
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
            /* class android.ddm.DdmHandleViewDebug.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                ViewDebug.outputDisplayList(rootView, targetView);
            }
        });
        return null;
    }

    private Chunk invokeViewMethod(View rootView, View targetView, ByteBuffer in) {
        Object[] args;
        Class<?>[] argTypes;
        String methodName = getString(in, in.getInt());
        if (!in.hasRemaining()) {
            argTypes = new Class[0];
            args = new Object[0];
        } else {
            int nArgs = in.getInt();
            Class<?>[] argTypes2 = new Class[nArgs];
            Object[] args2 = new Object[nArgs];
            for (int i = 0; i < nArgs; i++) {
                char c = in.getChar();
                if (c == 'F') {
                    argTypes2[i] = Float.TYPE;
                    args2[i] = Float.valueOf(in.getFloat());
                } else if (c == 'S') {
                    argTypes2[i] = Short.TYPE;
                    args2[i] = Short.valueOf(in.getShort());
                } else if (c == 'Z') {
                    argTypes2[i] = Boolean.TYPE;
                    args2[i] = Boolean.valueOf(in.get() != 0);
                } else if (c == 'I') {
                    argTypes2[i] = Integer.TYPE;
                    args2[i] = Integer.valueOf(in.getInt());
                } else if (c != 'J') {
                    switch (c) {
                        case 'B':
                            argTypes2[i] = Byte.TYPE;
                            args2[i] = Byte.valueOf(in.get());
                            continue;
                        case 'C':
                            argTypes2[i] = Character.TYPE;
                            args2[i] = Character.valueOf(in.getChar());
                            continue;
                        case 'D':
                            argTypes2[i] = Double.TYPE;
                            args2[i] = Double.valueOf(in.getDouble());
                            continue;
                        default:
                            Log.e(TAG, "arg " + i + ", unrecognized type: " + c);
                            return createFailChunk(-2, "Unsupported parameter type (" + c + ") to invoke view method.");
                    }
                } else {
                    argTypes2[i] = Long.TYPE;
                    args2[i] = Long.valueOf(in.getLong());
                }
            }
            argTypes = argTypes2;
            args = args2;
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
            return createFailChunk(-3, "Error accessing field " + param + SettingsStringUtil.DELIMITER + e.getMessage());
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
