package com.huawei.android.net.netlink;

import android.net.netlink.StructNlMsgHdr;
import com.huawei.annotation.HwSystemApi;
import java.nio.ByteBuffer;

@HwSystemApi
public class StructNlMsgHdrEx {
    public static final short NLM_F_DUMP = 768;
    public static final short NLM_F_MATCH = 512;
    public static final short NLM_F_REQUEST = 1;
    public static final short NLM_F_ROOT = 256;
    public static final int STRUCT_SIZE = 16;
    private final StructNlMsgHdr mStructNlMsgHdr;

    public StructNlMsgHdrEx() {
        this(new StructNlMsgHdr());
    }

    private StructNlMsgHdrEx(StructNlMsgHdr struct) {
        this.mStructNlMsgHdr = struct;
    }

    public static StructNlMsgHdrEx parse(ByteBuffer byteBuffer) {
        return new StructNlMsgHdrEx(StructNlMsgHdr.parse(byteBuffer));
    }

    public static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return StructNlMsgHdr.hasAvailableSpace(byteBuffer);
    }

    public boolean isNlMsgHdrNull() {
        return this.mStructNlMsgHdr == null;
    }

    public void setNlmsgLen(int length) {
        this.mStructNlMsgHdr.nlmsg_len = length;
    }

    public void setNlmsgType(short type) {
        this.mStructNlMsgHdr.nlmsg_type = type;
    }

    public void setNlmsgFlag(short flags) {
        this.mStructNlMsgHdr.nlmsg_flags = flags;
    }

    public void setNlmsgSeq(int seq) {
        this.mStructNlMsgHdr.nlmsg_seq = seq;
    }

    public void setNlmsgPid(int pid) {
        this.mStructNlMsgHdr.nlmsg_pid = pid;
    }

    public void pack(ByteBuffer byteBuffer) {
        this.mStructNlMsgHdr.pack(byteBuffer);
    }
}
