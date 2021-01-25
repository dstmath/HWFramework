package com.android.server.hdmi;

import android.util.FastImmutableArraySet;
import android.util.SparseArray;

/* access modifiers changed from: package-private */
public final class HdmiCecMessageCache {
    private static final FastImmutableArraySet<Integer> CACHEABLE_OPCODES = new FastImmutableArraySet<>(new Integer[]{71, 132, 135, 158});
    private final SparseArray<SparseArray<HdmiCecMessage>> mCache = new SparseArray<>();

    HdmiCecMessageCache() {
    }

    public HdmiCecMessage getMessage(int address, int opcode) {
        SparseArray<HdmiCecMessage> messages = this.mCache.get(address);
        if (messages == null) {
            return null;
        }
        return messages.get(opcode);
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
            SparseArray<HdmiCecMessage> messages = this.mCache.get(source);
            if (messages == null) {
                messages = new SparseArray<>();
                this.mCache.put(source, messages);
            }
            messages.put(opcode, message);
        }
    }

    private boolean isCacheable(int opcode) {
        return CACHEABLE_OPCODES.contains(Integer.valueOf(opcode));
    }
}
