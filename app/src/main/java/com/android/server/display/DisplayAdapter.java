package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import android.view.Display.ColorTransform;
import android.view.Display.Mode;
import com.android.server.display.DisplayManagerService.SyncRoot;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

abstract class DisplayAdapter {
    public static final int DISPLAY_DEVICE_EVENT_ADDED = 1;
    public static final int DISPLAY_DEVICE_EVENT_CHANGED = 2;
    public static final int DISPLAY_DEVICE_EVENT_REMOVED = 3;
    private static final AtomicInteger NEXT_COLOR_TRANSFORM_ID = null;
    private static final AtomicInteger NEXT_DISPLAY_MODE_ID = null;
    private final Context mContext;
    private final Handler mHandler;
    private final Listener mListener;
    private final String mName;
    private final SyncRoot mSyncRoot;

    /* renamed from: com.android.server.display.DisplayAdapter.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ DisplayDevice val$device;
        final /* synthetic */ int val$event;

        AnonymousClass1(DisplayDevice val$device, int val$event) {
            this.val$device = val$device;
            this.val$event = val$event;
        }

        public void run() {
            DisplayAdapter.this.mListener.onDisplayDeviceEvent(this.val$device, this.val$event);
        }
    }

    public interface Listener {
        void onDisplayDeviceEvent(DisplayDevice displayDevice, int i);

        void onTraversalRequested();
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.DisplayAdapter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.DisplayAdapter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.DisplayAdapter.<clinit>():void");
    }

    public DisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, Listener listener, String name) {
        this.mSyncRoot = syncRoot;
        this.mContext = context;
        this.mHandler = handler;
        this.mListener = listener;
        this.mName = name;
    }

    public final SyncRoot getSyncRoot() {
        return this.mSyncRoot;
    }

    public final Context getContext() {
        return this.mContext;
    }

    public final Handler getHandler() {
        return this.mHandler;
    }

    public final String getName() {
        return this.mName;
    }

    public void registerLocked() {
    }

    public void dumpLocked(PrintWriter pw) {
    }

    protected final void sendDisplayDeviceEventLocked(DisplayDevice device, int event) {
        this.mHandler.post(new AnonymousClass1(device, event));
    }

    protected final void sendTraversalRequestLocked() {
        this.mHandler.post(new Runnable() {
            public void run() {
                DisplayAdapter.this.mListener.onTraversalRequested();
            }
        });
    }

    public static Mode createMode(int width, int height, float refreshRate) {
        return new Mode(NEXT_DISPLAY_MODE_ID.getAndIncrement(), width, height, refreshRate);
    }

    public static ColorTransform createColorTransform(int colorTransform) {
        return new ColorTransform(NEXT_COLOR_TRANSFORM_ID.getAndIncrement(), colorTransform);
    }
}
