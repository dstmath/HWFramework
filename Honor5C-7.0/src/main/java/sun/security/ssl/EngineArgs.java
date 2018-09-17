package sun.security.ssl;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

class EngineArgs {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    ByteBuffer[] appData;
    private int[] appLims;
    private int[] appPoss;
    private int appRemaining;
    private int len;
    ByteBuffer netData;
    private int netLim;
    private int netPos;
    private int offset;
    private boolean wrapMethod;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.EngineArgs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.EngineArgs.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.EngineArgs.<clinit>():void");
    }

    EngineArgs(ByteBuffer[] appData, int offset, int len, ByteBuffer netData) {
        this.appRemaining = 0;
        this.wrapMethod = true;
        init(netData, appData, offset, len);
    }

    EngineArgs(ByteBuffer netData, ByteBuffer[] appData, int offset, int len) {
        this.appRemaining = 0;
        this.wrapMethod = false;
        init(netData, appData, offset, len);
    }

    private void init(ByteBuffer netData, ByteBuffer[] appData, int offset, int len) {
        if (netData == null || appData == null) {
            throw new IllegalArgumentException("src/dst is null");
        } else if (offset < 0 || len < 0 || offset > appData.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (this.wrapMethod && netData.isReadOnly()) {
            throw new ReadOnlyBufferException();
        } else {
            this.netPos = netData.position();
            this.netLim = netData.limit();
            this.appPoss = new int[appData.length];
            this.appLims = new int[appData.length];
            int i = offset;
            while (i < offset + len) {
                if (appData[i] == null) {
                    throw new IllegalArgumentException("appData[" + i + "] == null");
                } else if (this.wrapMethod || !appData[i].isReadOnly()) {
                    this.appRemaining += appData[i].remaining();
                    this.appPoss[i] = appData[i].position();
                    this.appLims[i] = appData[i].limit();
                    i++;
                } else {
                    throw new ReadOnlyBufferException();
                }
            }
            this.netData = netData;
            this.appData = appData;
            this.offset = offset;
            this.len = len;
        }
    }

    void gather(int spaceLeft) {
        for (int i = this.offset; i < this.offset + this.len && spaceLeft > 0; i++) {
            int amount = Math.min(this.appData[i].remaining(), spaceLeft);
            this.appData[i].limit(this.appData[i].position() + amount);
            this.netData.put(this.appData[i]);
            this.appRemaining -= amount;
            spaceLeft -= amount;
        }
    }

    void scatter(ByteBuffer readyData) {
        Object obj = null;
        int amountLeft = readyData.remaining();
        for (int i = this.offset; i < this.offset + this.len && amountLeft > 0; i++) {
            int amount = Math.min(this.appData[i].remaining(), amountLeft);
            readyData.limit(readyData.position() + amount);
            this.appData[i].put(readyData);
            amountLeft -= amount;
        }
        if (!-assertionsDisabled) {
            if (readyData.remaining() == 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
    }

    int getAppRemaining() {
        return this.appRemaining;
    }

    int deltaNet() {
        return this.netData.position() - this.netPos;
    }

    int deltaApp() {
        int sum = 0;
        for (int i = this.offset; i < this.offset + this.len; i++) {
            sum += this.appData[i].position() - this.appPoss[i];
        }
        return sum;
    }

    void resetPos() {
        this.netData.position(this.netPos);
        for (int i = this.offset; i < this.offset + this.len; i++) {
            this.appData[i].position(this.appPoss[i]);
        }
    }

    void resetLim() {
        this.netData.limit(this.netLim);
        for (int i = this.offset; i < this.offset + this.len; i++) {
            this.appData[i].limit(this.appLims[i]);
        }
    }
}
