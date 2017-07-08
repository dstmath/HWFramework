package com.android.server.hdmi;

import android.util.FastImmutableArraySet;
import android.util.SparseArray;

final class HdmiCecMessageCache {
    private static final FastImmutableArraySet<Integer> CACHEABLE_OPCODES = null;
    private final SparseArray<SparseArray<HdmiCecMessage>> mCache;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.hdmi.HdmiCecMessageCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.hdmi.HdmiCecMessageCache.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.hdmi.HdmiCecMessageCache.<clinit>():void");
    }

    HdmiCecMessageCache() {
        this.mCache = new SparseArray();
    }

    public HdmiCecMessage getMessage(int address, int opcode) {
        SparseArray<HdmiCecMessage> messages = (SparseArray) this.mCache.get(address);
        if (messages == null) {
            return null;
        }
        return (HdmiCecMessage) messages.get(opcode);
    }

    public void flushMessagesFrom(int address) {
        this.mCache.remove(address);
    }

    public void flushAll() {
        this.mCache.clear();
    }

    public void cacheMessage(HdmiCecMessage message) {
        int opcode = message.getOpcode();
        if (isCacheable(opcode)) {
            int source = message.getSource();
            SparseArray<HdmiCecMessage> messages = (SparseArray) this.mCache.get(source);
            if (messages == null) {
                messages = new SparseArray();
                this.mCache.put(source, messages);
            }
            messages.put(opcode, message);
        }
    }

    private boolean isCacheable(int opcode) {
        return CACHEABLE_OPCODES.contains(Integer.valueOf(opcode));
    }
}
