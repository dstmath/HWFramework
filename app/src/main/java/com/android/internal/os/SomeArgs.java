package com.android.internal.os;

public final class SomeArgs {
    private static final int MAX_POOL_SIZE = 10;
    static final int WAIT_FINISHED = 2;
    static final int WAIT_NONE = 0;
    static final int WAIT_WAITING = 1;
    private static SomeArgs sPool;
    private static Object sPoolLock;
    private static int sPoolSize;
    public Object arg1;
    public Object arg2;
    public Object arg3;
    public Object arg4;
    public Object arg5;
    public Object arg6;
    public Object arg7;
    public Object arg8;
    public int argi1;
    public int argi2;
    public int argi3;
    public int argi4;
    public int argi5;
    public int argi6;
    private boolean mInPool;
    private SomeArgs mNext;
    int mWaitState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.SomeArgs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.SomeArgs.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.SomeArgs.<clinit>():void");
    }

    private SomeArgs() {
        this.mWaitState = WAIT_NONE;
    }

    public static SomeArgs obtain() {
        synchronized (sPoolLock) {
            if (sPoolSize > 0) {
                SomeArgs args = sPool;
                sPool = sPool.mNext;
                args.mNext = null;
                args.mInPool = false;
                sPoolSize--;
                return args;
            }
            SomeArgs someArgs = new SomeArgs();
            return someArgs;
        }
    }

    public void complete() {
        synchronized (this) {
            if (this.mWaitState != WAIT_WAITING) {
                throw new IllegalStateException("Not waiting");
            }
            this.mWaitState = WAIT_FINISHED;
            notifyAll();
        }
    }

    public void recycle() {
        if (this.mInPool) {
            throw new IllegalStateException("Already recycled.");
        } else if (this.mWaitState == 0) {
            synchronized (sPoolLock) {
                clear();
                if (sPoolSize < MAX_POOL_SIZE) {
                    this.mNext = sPool;
                    this.mInPool = true;
                    sPool = this;
                    sPoolSize += WAIT_WAITING;
                }
            }
        }
    }

    private void clear() {
        this.arg1 = null;
        this.arg2 = null;
        this.arg3 = null;
        this.arg4 = null;
        this.arg5 = null;
        this.arg6 = null;
        this.arg7 = null;
        this.argi1 = WAIT_NONE;
        this.argi2 = WAIT_NONE;
        this.argi3 = WAIT_NONE;
        this.argi4 = WAIT_NONE;
        this.argi5 = WAIT_NONE;
        this.argi6 = WAIT_NONE;
    }
}
