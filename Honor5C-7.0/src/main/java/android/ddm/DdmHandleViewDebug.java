package android.ddm;

import android.media.ToneGenerator;
import android.os.StrictMode;
import android.provider.DocumentsContract.Document;
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
    private static final int CHUNK_VULW = 0;
    private static final int CHUNK_VUOP = 0;
    private static final int CHUNK_VURT = 0;
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
    private static final DdmHandleViewDebug sInstance = null;

    /* renamed from: android.ddm.DdmHandleViewDebug.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ View val$rootView;
        final /* synthetic */ View val$targetView;

        AnonymousClass1(View val$rootView, View val$targetView) {
            this.val$rootView = val$rootView;
            this.val$targetView = val$targetView;
        }

        public void run() {
            ViewDebug.outputDisplayList(this.val$rootView, this.val$targetView);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.ddm.DdmHandleViewDebug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.ddm.DdmHandleViewDebug.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleViewDebug.<clinit>():void");
    }

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
            return createFailChunk(ERR_INVALID_PARAM, "Invalid View Root");
        }
        if (type != CHUNK_VURT) {
            View targetView = getTargetView(rootView, in);
            if (targetView == null) {
                return createFailChunk(ERR_INVALID_PARAM, "Invalid target view");
            }
            if (type == CHUNK_VUOP) {
                switch (op) {
                    case VURT_DUMP_HIERARCHY /*1*/:
                        return captureView(rootView, targetView);
                    case VURT_CAPTURE_LAYERS /*2*/:
                        return dumpDisplayLists(rootView, targetView);
                    case VURT_DUMP_THEME /*3*/:
                        return profileView(rootView, targetView);
                    case VUOP_INVOKE_VIEW_METHOD /*4*/:
                        return invokeViewMethod(rootView, targetView, in);
                    case VUOP_SET_LAYOUT_PARAMETER /*5*/:
                        return setLayoutParameter(rootView, targetView, in);
                    default:
                        return createFailChunk(ERR_INVALID_OP, "Unknown view operation: " + op);
                }
            }
            throw new RuntimeException("Unknown packet " + ChunkHandler.name(type));
        } else if (op == VURT_DUMP_HIERARCHY) {
            return dumpHierarchy(rootView, in);
        } else {
            if (op == VURT_CAPTURE_LAYERS) {
                return captureLayers(rootView);
            }
            if (op == VURT_DUMP_THEME) {
                return dumpTheme(rootView);
            }
            return createFailChunk(ERR_INVALID_OP, "Unknown view root operation: " + op);
        }
    }

    private Chunk listWindows() {
        int i;
        int i2 = CHUNK_VURT;
        String[] windowNames = WindowManagerGlobal.getInstance().getViewRootNames();
        int responseLength = VUOP_INVOKE_VIEW_METHOD;
        for (i = CHUNK_VURT; i < windowNames.length; i += VURT_DUMP_HIERARCHY) {
            responseLength = (responseLength + VUOP_INVOKE_VIEW_METHOD) + (windowNames[i].length() * VURT_CAPTURE_LAYERS);
        }
        ByteBuffer out = ByteBuffer.allocate(responseLength);
        out.order(ChunkHandler.CHUNK_ORDER);
        out.putInt(windowNames.length);
        i = windowNames.length;
        while (i2 < i) {
            String name = windowNames[i2];
            out.putInt(name.length());
            putString(out, name);
            i2 += VURT_DUMP_HIERARCHY;
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
        ByteArrayOutputStream b = new ByteArrayOutputStream(StrictMode.PENALTY_DROPBOX);
        if (v2) {
            try {
                ViewDebug.dumpv2(rootView, b);
            } catch (Exception e) {
                return createFailChunk(VURT_DUMP_HIERARCHY, "Unexpected error while obtaining view hierarchy: " + e.getMessage());
            }
        }
        ViewDebug.dump(rootView, skipChildren, includeProperties, b);
        Log.d(TAG, "Time to obtain view hierarchy (ms): " + (System.currentTimeMillis() - start));
        byte[] data = b.toByteArray();
        return new Chunk(CHUNK_VURT, data, CHUNK_VURT, data.length);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Chunk captureLayers(View rootView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(Document.FLAG_SUPPORTS_REMOVE);
        DataOutputStream dos = new DataOutputStream(b);
        try {
            ViewDebug.captureLayers(rootView, dos);
            try {
                dos.close();
            } catch (IOException e) {
            }
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VURT, data, CHUNK_VURT, data.length);
        } catch (IOException e2) {
            Chunk createFailChunk = createFailChunk(VURT_DUMP_HIERARCHY, "Unexpected error while obtaining view hierarchy: " + e2.getMessage());
            return createFailChunk;
        } catch (Throwable th) {
            try {
                dos.close();
            } catch (IOException e3) {
            }
        }
    }

    private Chunk dumpTheme(View rootView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(Document.FLAG_SUPPORTS_REMOVE);
        try {
            ViewDebug.dumpTheme(rootView, b);
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VURT, data, CHUNK_VURT, data.length);
        } catch (IOException e) {
            return createFailChunk(VURT_DUMP_HIERARCHY, "Unexpected error while dumping the theme: " + e.getMessage());
        }
    }

    private Chunk captureView(View rootView, View targetView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(Document.FLAG_SUPPORTS_REMOVE);
        try {
            ViewDebug.capture(rootView, b, targetView);
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VUOP, data, CHUNK_VURT, data.length);
        } catch (IOException e) {
            return createFailChunk(VURT_DUMP_HIERARCHY, "Unexpected error while capturing view: " + e.getMessage());
        }
    }

    private Chunk dumpDisplayLists(View rootView, View targetView) {
        rootView.post(new AnonymousClass1(rootView, targetView));
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
            for (int i = CHUNK_VURT; i < nArgs; i += VURT_DUMP_HIERARCHY) {
                char c = in.getChar();
                switch (c) {
                    case ToneGenerator.TONE_CDMA_MED_SLS /*66*/:
                        argTypes[i] = Byte.TYPE;
                        args[i] = Byte.valueOf(in.get());
                        break;
                    case ToneGenerator.TONE_CDMA_LOW_SLS /*67*/:
                        argTypes[i] = Character.TYPE;
                        args[i] = Character.valueOf(in.getChar());
                        break;
                    case ToneGenerator.TONE_CDMA_HIGH_S_X4 /*68*/:
                        argTypes[i] = Double.TYPE;
                        args[i] = Double.valueOf(in.getDouble());
                        break;
                    case ToneGenerator.TONE_CDMA_LOW_S_X4 /*70*/:
                        argTypes[i] = Float.TYPE;
                        args[i] = Float.valueOf(in.getFloat());
                        break;
                    case ToneGenerator.TONE_CDMA_LOW_PBX_L /*73*/:
                        argTypes[i] = Integer.TYPE;
                        args[i] = Integer.valueOf(in.getInt());
                        break;
                    case ToneGenerator.TONE_CDMA_HIGH_PBX_SS /*74*/:
                        argTypes[i] = Long.TYPE;
                        args[i] = Long.valueOf(in.getLong());
                        break;
                    case ToneGenerator.TONE_CDMA_HIGH_PBX_S_X4 /*83*/:
                        argTypes[i] = Short.TYPE;
                        args[i] = Short.valueOf(in.getShort());
                        break;
                    case ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE /*90*/:
                        boolean z;
                        argTypes[i] = Boolean.TYPE;
                        if (in.get() == null) {
                            z = false;
                        } else {
                            z = true;
                        }
                        args[i] = Boolean.valueOf(z);
                        break;
                    default:
                        Log.e(TAG, "arg " + i + ", unrecognized type: " + c);
                        return createFailChunk(ERR_INVALID_PARAM, "Unsupported parameter type (" + c + ") to invoke view method.");
                }
            }
        } else {
            argTypes = new Class[CHUNK_VURT];
            args = new Object[CHUNK_VURT];
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
                return createFailChunk(ERR_EXCEPTION, msg);
            }
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "No such method: " + e2.getMessage());
            return createFailChunk(ERR_INVALID_PARAM, "No such method: " + e2.getMessage());
        }
    }

    private Chunk setLayoutParameter(View rootView, View targetView, ByteBuffer in) {
        String param = getString(in, in.getInt());
        try {
            ViewDebug.setLayoutParameter(targetView, param, in.getInt());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception setting layout parameter: " + e);
            return createFailChunk(ERR_EXCEPTION, "Error accessing field " + param + ":" + e.getMessage());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Chunk profileView(View rootView, View targetView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(Document.FLAG_ARCHIVE);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(b), Document.FLAG_ARCHIVE);
        try {
            ViewDebug.profileViewAndChildren(targetView, bw);
            try {
                bw.close();
            } catch (IOException e) {
            }
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VUOP, data, CHUNK_VURT, data.length);
        } catch (IOException e2) {
            Chunk createFailChunk = createFailChunk(VURT_DUMP_HIERARCHY, "Unexpected error while profiling view: " + e2.getMessage());
            return createFailChunk;
        } catch (Throwable th) {
            try {
                bw.close();
            } catch (IOException e3) {
            }
        }
    }
}
