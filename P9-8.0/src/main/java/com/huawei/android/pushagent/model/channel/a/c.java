package com.huawei.android.pushagent.model.channel.a;

import java.io.InputStream;

class c extends InputStream {
    private byte[] bt = null;
    private int bu = 0;
    private InputStream bv;
    final /* synthetic */ b bw;

    public c(b bVar, InputStream inputStream) {
        this.bw = bVar;
        this.bv = inputStream;
    }

    /* JADX WARNING: Missing block: B:10:0x001c, code:
            if (r5.bt == null) goto L_0x0043;
     */
    /* JADX WARNING: Missing block: B:12:0x0021, code:
            if (r5.bt.length <= 0) goto L_0x0043;
     */
    /* JADX WARNING: Missing block: B:14:0x0028, code:
            if (r5.bu >= r5.bt.length) goto L_0x003a;
     */
    /* JADX WARNING: Missing block: B:15:0x002a, code:
            r0 = r5.bt;
            r1 = r5.bu;
            r5.bu = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:16:0x0036, code:
            return r0[r1] & 255;
     */
    /* JADX WARNING: Missing block: B:20:0x003a, code:
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "bufferByte has read end , need read bytes from socket");
     */
    /* JADX WARNING: Missing block: B:21:0x0043, code:
            r5.bt = null;
            r5.bu = 0;
     */
    /* JADX WARNING: Missing block: B:22:0x0049, code:
            if (r5.bv == null) goto L_0x00bb;
     */
    /* JADX WARNING: Missing block: B:23:0x004b, code:
            r0 = r5.bv.read();
     */
    /* JADX WARNING: Missing block: B:24:0x0051, code:
            if (-1 != r0) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:25:0x0053, code:
            com.huawei.android.pushagent.utils.d.c.sj("PushLog2951", "read -1 from inputstream");
     */
    /* JADX WARNING: Missing block: B:26:0x005c, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:28:0x005f, code:
            if (48 != r0) goto L_0x0096;
     */
    /* JADX WARNING: Missing block: B:29:0x0061, code:
            r5.bt = com.huawei.android.pushagent.utils.a.e.nx(com.huawei.android.pushagent.model.channel.a.b.hj(r5.bv), com.huawei.android.pushagent.model.channel.a.b.hi());
     */
    /* JADX WARNING: Missing block: B:30:0x0073, code:
            if (r5.bt == null) goto L_0x007a;
     */
    /* JADX WARNING: Missing block: B:32:0x0078, code:
            if (r5.bt.length != 0) goto L_0x0089;
     */
    /* JADX WARNING: Missing block: B:33:0x007a, code:
            com.huawei.android.pushagent.utils.d.c.sj("PushLog2951", "ase decrypt serverkey error");
            com.huawei.android.pushagent.a.a.xv(87);
     */
    /* JADX WARNING: Missing block: B:34:0x0088, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:35:0x0089, code:
            r0 = r5.bt;
            r1 = r5.bu;
            r5.bu = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:36:0x0095, code:
            return r0[r1] & 255;
     */
    /* JADX WARNING: Missing block: B:37:0x0096, code:
            com.huawei.android.pushagent.utils.d.c.sj("PushLog2951", "read secure message error, return -1, cmdId: " + com.huawei.android.pushagent.utils.d.b.sc((byte) r0));
            com.huawei.android.pushagent.a.a.xv(88);
     */
    /* JADX WARNING: Missing block: B:38:0x00ba, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:39:0x00bb, code:
            com.huawei.android.pushagent.utils.d.c.sj("PushLog2951", "secureInputStream is null, return -1");
     */
    /* JADX WARNING: Missing block: B:40:0x00c4, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read() {
        synchronized (this.bw) {
            if (!this.bw.bn) {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "secure socket is not initialized, can not read any data");
                return -1;
            }
        }
    }
}
