package huawei.android.hwutil;

import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static final int BUFFER_BYTE = 4096;
    private static final int BUFF_SIZE = 10240;
    private static final String EXCLUDE_ENTRY = "..";
    private static final int MAX_ENTRY_THEME_SIZE = 200000000;
    private static final String TAG = "ZipUtil";

    /*  JADX ERROR: JadxRuntimeException in pass: SSATransform
        jadx.core.utils.exceptions.JadxRuntimeException: Unknown predecessor block by arg (r22_38 ?) in PHI: PHI: (r22_41 ?) = (r22_38 ?), (r22_35 ?), (r22_35 ?) binds: {(r22_35 ?)=B:113:0x021b}
        	at jadx.core.dex.instructions.PhiInsn.replaceArg(PhiInsn.java:78)
        	at jadx.core.dex.visitors.ssa.SSATransform.inlinePhiInsn(SSATransform.java:392)
        	at jadx.core.dex.visitors.ssa.SSATransform.replacePhiWithMove(SSATransform.java:360)
        	at jadx.core.dex.visitors.ssa.SSATransform.fixPhiWithSameArgs(SSATransform.java:300)
        	at jadx.core.dex.visitors.ssa.SSATransform.fixUselessPhi(SSATransform.java:275)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:61)
        	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static boolean upZipSelectedFile(java.io.File r26, java.lang.String r27, java.lang.String r28) throws java.util.zip.ZipException, java.io.IOException {
        /*
        r10 = 0;
        if (r26 != 0) goto L_0x0004;
    L_0x0003:
        return r10;
    L_0x0004:
        r20 = new java.io.File;
        r0 = r20;
        r1 = r27;
        r0.<init>(r1);
        r22 = r20.exists();
        if (r22 != 0) goto L_0x0025;
    L_0x0013:
        r22 = "ive";
        r23 = "add audio dir";
        android.util.Log.d(r22, r23);
        r22 = r20.mkdir();
        if (r22 != 0) goto L_0x0025;
    L_0x0022:
        r22 = 0;
        return r22;
    L_0x0025:
        r21 = new java.util.zip.ZipFile;
        r0 = r21;
        r1 = r26;
        r0.<init>(r1);
        if (r21 != 0) goto L_0x0033;
    L_0x0030:
        r22 = 0;
        return r22;
    L_0x0033:
        r8 = r21.entries();
    L_0x0037:
        r22 = r8.hasMoreElements();
        if (r22 == 0) goto L_0x0264;
    L_0x003d:
        r9 = r8.nextElement();
        r9 = (java.util.zip.ZipEntry) r9;
        r22 = r9.getName();
        r0 = r22;
        r1 = r28;
        r22 = r0.contains(r1);
        if (r22 == 0) goto L_0x0037;
    L_0x0051:
        r22 = new java.lang.StringBuilder;
        r22.<init>();
        r0 = r22;
        r1 = r27;
        r22 = r0.append(r1);
        r23 = r9.getName();
        r22 = r22.append(r23);
        r11 = r22.toString();
        r4 = new java.io.File;
        r4.<init>(r11);
        r22 = "..";
        r0 = r22;
        r22 = r11.contains(r0);
        if (r22 != 0) goto L_0x0037;
    L_0x007a:
        r22 = r9.isDirectory();
        if (r22 != 0) goto L_0x0037;
    L_0x0080:
        r16 = 1;
        if (r4 == 0) goto L_0x0037;
    L_0x0084:
        r22 = r4.exists();
        r22 = r22 ^ 1;
        if (r22 == 0) goto L_0x0037;
    L_0x008c:
        r12 = r4.getParentFile();
        if (r12 == 0) goto L_0x009e;
    L_0x0092:
        r22 = r12.exists();
        r22 = r22 ^ 1;
        if (r22 == 0) goto L_0x009e;
    L_0x009a:
        r16 = r12.mkdirs();
    L_0x009e:
        if (r16 == 0) goto L_0x00a4;
    L_0x00a0:
        r16 = r4.createNewFile();	 Catch:{ IOException -> 0x00fe }
    L_0x00a4:
        if (r16 == 0) goto L_0x0037;
    L_0x00a6:
        r13 = 0;
        r17 = 0;
        r22 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;
        r0 = r22;	 Catch:{ IOException -> 0x0271, Exception -> 0x015b }
        r3 = new byte[r0];	 Catch:{ IOException -> 0x0271, Exception -> 0x015b }
        r19 = 0;	 Catch:{ IOException -> 0x0271, Exception -> 0x015b }
        r0 = r21;	 Catch:{ IOException -> 0x0271, Exception -> 0x015b }
        r13 = r0.getInputStream(r9);	 Catch:{ IOException -> 0x0271, Exception -> 0x015b }
        r18 = new java.io.FileOutputStream;	 Catch:{ IOException -> 0x0271, Exception -> 0x015b }
        r0 = r18;	 Catch:{ IOException -> 0x0271, Exception -> 0x015b }
        r0.<init>(r4);	 Catch:{ IOException -> 0x0271, Exception -> 0x015b }
    L_0x00be:
        r19 = r13.read(r3);	 Catch:{ IOException -> 0x00d0, Exception -> 0x0274, all -> 0x026d }
        if (r19 <= 0) goto L_0x0109;	 Catch:{ IOException -> 0x00d0, Exception -> 0x0274, all -> 0x026d }
    L_0x00c4:
        r22 = 0;	 Catch:{ IOException -> 0x00d0, Exception -> 0x0274, all -> 0x026d }
        r0 = r18;	 Catch:{ IOException -> 0x00d0, Exception -> 0x0274, all -> 0x026d }
        r1 = r22;	 Catch:{ IOException -> 0x00d0, Exception -> 0x0274, all -> 0x026d }
        r2 = r19;	 Catch:{ IOException -> 0x00d0, Exception -> 0x0274, all -> 0x026d }
        r0.write(r3, r1, r2);	 Catch:{ IOException -> 0x00d0, Exception -> 0x0274, all -> 0x026d }
        goto L_0x00be;
    L_0x00d0:
        r7 = move-exception;
        r17 = r18;
    L_0x00d3:
        r22 = "ZipUtil";	 Catch:{ all -> 0x020f }
        r23 = new java.lang.StringBuilder;	 Catch:{ all -> 0x020f }
        r23.<init>();	 Catch:{ all -> 0x020f }
        r24 = "IOException is ";	 Catch:{ all -> 0x020f }
        r23 = r23.append(r24);	 Catch:{ all -> 0x020f }
        r0 = r23;	 Catch:{ all -> 0x020f }
        r23 = r0.append(r7);	 Catch:{ all -> 0x020f }
        r23 = r23.toString();	 Catch:{ all -> 0x020f }
        android.util.Log.e(r22, r23);	 Catch:{ all -> 0x020f }
        if (r13 == 0) goto L_0x00f4;
    L_0x00f1:
        r13.close();	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
    L_0x00f4:
        r13 = 0;
    L_0x00f5:
        if (r17 == 0) goto L_0x00fa;
    L_0x00f7:
        r17.close();	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
    L_0x00fa:
        r17 = 0;
        goto L_0x0037;
    L_0x00fe:
        r5 = move-exception;
        r22 = "ZipUtil";
        r23 = " create new file failed";
        android.util.Log.e(r22, r23);
        goto L_0x00a4;
    L_0x0109:
        if (r13 == 0) goto L_0x010e;
    L_0x010b:
        r13.close();	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
    L_0x010e:
        r13 = 0;
        if (r18 == 0) goto L_0x00fa;
    L_0x0111:
        r18.close();	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
        goto L_0x00fa;
    L_0x0115:
        r15 = move-exception;
        r22 = "ZipUtil";	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
        r23 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
        r23.<init>();	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
        r24 = "ioexception is ";	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
        r23 = r23.append(r24);	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
        r0 = r23;	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
        r23 = r0.append(r15);	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
        r23 = r23.toString();	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
        android.util.Log.e(r22, r23);	 Catch:{ IOException -> 0x0115, all -> 0x0157 }
    L_0x0132:
        r17 = 0;
        goto L_0x0037;
    L_0x0136:
        r14 = move-exception;
        r22 = "ZipUtil";	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
        r23 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
        r23.<init>();	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
        r24 = "ioe is ";	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
        r23 = r23.append(r24);	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
        r0 = r23;	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
        r23 = r0.append(r14);	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
        r23 = r23.toString();	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
        android.util.Log.e(r22, r23);	 Catch:{ IOException -> 0x0136, all -> 0x0154 }
        goto L_0x010e;
    L_0x0154:
        r22 = move-exception;
        r13 = 0;
        throw r22;
    L_0x0157:
        r22 = move-exception;
        r17 = 0;
        throw r22;
    L_0x015b:
        r6 = move-exception;
    L_0x015c:
        r22 = "ZipUtil";	 Catch:{ all -> 0x020f }
        r23 = new java.lang.StringBuilder;	 Catch:{ all -> 0x020f }
        r23.<init>();	 Catch:{ all -> 0x020f }
        r24 = "Exception is ";	 Catch:{ all -> 0x020f }
        r23 = r23.append(r24);	 Catch:{ all -> 0x020f }
        r0 = r23;	 Catch:{ all -> 0x020f }
        r23 = r0.append(r6);	 Catch:{ all -> 0x020f }
        r23 = r23.toString();	 Catch:{ all -> 0x020f }
        android.util.Log.e(r22, r23);	 Catch:{ all -> 0x020f }
        if (r13 == 0) goto L_0x017d;
    L_0x017a:
        r13.close();	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
    L_0x017d:
        r13 = 0;
    L_0x017e:
        if (r17 == 0) goto L_0x00fa;
    L_0x0180:
        r17.close();	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        goto L_0x00fa;
    L_0x0185:
        r15 = move-exception;
        r22 = "ZipUtil";	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        r23 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        r23.<init>();	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        r24 = "ioexception is ";	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        r23 = r23.append(r24);	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        r0 = r23;	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        r23 = r0.append(r15);	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        r23 = r23.toString();	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        android.util.Log.e(r22, r23);	 Catch:{ IOException -> 0x0185, all -> 0x01a3 }
        goto L_0x0132;
    L_0x01a3:
        r22 = move-exception;
        r17 = 0;
        throw r22;
    L_0x01a7:
        r14 = move-exception;
        r22 = "ZipUtil";	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
        r23 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
        r23.<init>();	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
        r24 = "ioe is ";	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
        r23 = r23.append(r24);	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
        r0 = r23;	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
        r23 = r0.append(r14);	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
        r23 = r23.toString();	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
        android.util.Log.e(r22, r23);	 Catch:{ IOException -> 0x01a7, all -> 0x01c6 }
        r13 = 0;
        goto L_0x017e;
    L_0x01c6:
        r22 = move-exception;
        r13 = 0;
        throw r22;
    L_0x01c9:
        r14 = move-exception;
        r22 = "ZipUtil";	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
        r23 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
        r23.<init>();	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
        r24 = "ioe is ";	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
        r23 = r23.append(r24);	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
        r0 = r23;	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
        r23 = r0.append(r14);	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
        r23 = r23.toString();	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
        android.util.Log.e(r22, r23);	 Catch:{ IOException -> 0x01c9, all -> 0x01e9 }
        r13 = 0;
        goto L_0x00f5;
    L_0x01e9:
        r22 = move-exception;
        r13 = 0;
        throw r22;
    L_0x01ec:
        r15 = move-exception;
        r22 = "ZipUtil";	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
        r23 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
        r23.<init>();	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
        r24 = "ioexception is ";	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
        r23 = r23.append(r24);	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
        r0 = r23;	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
        r23 = r0.append(r15);	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
        r23 = r23.toString();	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
        android.util.Log.e(r22, r23);	 Catch:{ IOException -> 0x01ec, all -> 0x020b }
        goto L_0x0132;
    L_0x020b:
        r22 = move-exception;
        r17 = 0;
        throw r22;
    L_0x020f:
        r22 = move-exception;
    L_0x0210:
        if (r13 == 0) goto L_0x0215;
    L_0x0212:
        r13.close();	 Catch:{ IOException -> 0x021e, all -> 0x023d }
    L_0x0215:
        r13 = 0;
    L_0x0216:
        if (r17 == 0) goto L_0x021b;
    L_0x0218:
        r17.close();	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
    L_0x021b:
        r17 = 0;
    L_0x021d:
        throw r22;
    L_0x021e:
        r14 = move-exception;
        r23 = "ZipUtil";	 Catch:{ IOException -> 0x021e, all -> 0x023d }
        r24 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x021e, all -> 0x023d }
        r24.<init>();	 Catch:{ IOException -> 0x021e, all -> 0x023d }
        r25 = "ioe is ";	 Catch:{ IOException -> 0x021e, all -> 0x023d }
        r24 = r24.append(r25);	 Catch:{ IOException -> 0x021e, all -> 0x023d }
        r0 = r24;	 Catch:{ IOException -> 0x021e, all -> 0x023d }
        r24 = r0.append(r14);	 Catch:{ IOException -> 0x021e, all -> 0x023d }
        r24 = r24.toString();	 Catch:{ IOException -> 0x021e, all -> 0x023d }
        android.util.Log.e(r23, r24);	 Catch:{ IOException -> 0x021e, all -> 0x023d }
        r13 = 0;
        goto L_0x0216;
    L_0x023d:
        r22 = move-exception;
        r13 = 0;
        throw r22;
    L_0x0240:
        r15 = move-exception;
        r23 = "ZipUtil";	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
        r24 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
        r24.<init>();	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
        r25 = "ioexception is ";	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
        r24 = r24.append(r25);	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
        r0 = r24;	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
        r24 = r0.append(r15);	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
        r24 = r24.toString();	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
        android.util.Log.e(r23, r24);	 Catch:{ IOException -> 0x0240, all -> 0x0260 }
        r17 = 0;
        goto L_0x021d;
    L_0x0260:
        r22 = move-exception;
        r17 = 0;
        throw r22;
    L_0x0264:
        r21.close();	 Catch:{ IOException -> 0x0268 }
    L_0x0267:
        return r10;
    L_0x0268:
        r5 = move-exception;
        r5.printStackTrace();
        goto L_0x0267;
    L_0x026d:
        r22 = move-exception;
        r17 = r18;
        goto L_0x0210;
    L_0x0271:
        r7 = move-exception;
        goto L_0x00d3;
    L_0x0274:
        r6 = move-exception;
        r17 = r18;
        goto L_0x015c;
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwutil.ZipUtil.upZipSelectedFile(java.io.File, java.lang.String, java.lang.String):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0034 A:{SYNTHETIC, Splitter: B:18:0x0034} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0039 A:{SYNTHETIC, Splitter: B:21:0x0039} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x003e A:{SYNTHETIC, Splitter: B:24:0x003e} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0075 A:{SYNTHETIC, Splitter: B:53:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x007a A:{SYNTHETIC, Splitter: B:56:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x007f A:{SYNTHETIC, Splitter: B:59:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0034 A:{SYNTHETIC, Splitter: B:18:0x0034} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0039 A:{SYNTHETIC, Splitter: B:21:0x0039} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x003e A:{SYNTHETIC, Splitter: B:24:0x003e} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0075 A:{SYNTHETIC, Splitter: B:53:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x007a A:{SYNTHETIC, Splitter: B:56:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x007f A:{SYNTHETIC, Splitter: B:59:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0034 A:{SYNTHETIC, Splitter: B:18:0x0034} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0039 A:{SYNTHETIC, Splitter: B:21:0x0039} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x003e A:{SYNTHETIC, Splitter: B:24:0x003e} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0075 A:{SYNTHETIC, Splitter: B:53:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x007a A:{SYNTHETIC, Splitter: B:56:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x007f A:{SYNTHETIC, Splitter: B:59:0x007f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean zipFiles(Collection<File> resFileList, File zipFile) {
        FileNotFoundException e;
        Throwable th;
        ZipOutputStream zipout = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(zipFile);
            try {
                BufferedOutputStream bos2 = new BufferedOutputStream(fos2, BUFF_SIZE);
                try {
                    ZipOutputStream zipout2 = new ZipOutputStream(bos2);
                    try {
                        for (File resFile : resFileList) {
                            zipFile(resFile, zipout2, "");
                        }
                        if (zipout2 != null) {
                            try {
                                zipout2.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        if (fos2 != null) {
                            try {
                                fos2.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        if (bos2 != null) {
                            try {
                                bos2.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        return true;
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        bos = bos2;
                        fos = fos2;
                        zipout = zipout2;
                        try {
                            e.printStackTrace();
                            if (zipout != null) {
                            }
                            if (fos != null) {
                            }
                            if (bos != null) {
                            }
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            if (zipout != null) {
                                try {
                                    zipout.close();
                                } catch (IOException e2222) {
                                    e2222.printStackTrace();
                                }
                            }
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e22222) {
                                    e22222.printStackTrace();
                                }
                            }
                            if (bos != null) {
                                try {
                                    bos.close();
                                } catch (IOException e222222) {
                                    e222222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bos = bos2;
                        fos = fos2;
                        zipout = zipout2;
                        if (zipout != null) {
                        }
                        if (fos != null) {
                        }
                        if (bos != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    bos = bos2;
                    fos = fos2;
                    e.printStackTrace();
                    if (zipout != null) {
                    }
                    if (fos != null) {
                    }
                    if (bos != null) {
                    }
                    return false;
                } catch (Throwable th4) {
                    th = th4;
                    bos = bos2;
                    fos = fos2;
                    if (zipout != null) {
                    }
                    if (fos != null) {
                    }
                    if (bos != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                fos = fos2;
                e.printStackTrace();
                if (zipout != null) {
                }
                if (fos != null) {
                }
                if (bos != null) {
                }
                return false;
            } catch (Throwable th5) {
                th = th5;
                fos = fos2;
                if (zipout != null) {
                }
                if (fos != null) {
                }
                if (bos != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e = e6;
            e.printStackTrace();
            if (zipout != null) {
                try {
                    zipout.close();
                } catch (IOException e2222222) {
                    e2222222.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e22222222) {
                    e22222222.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e222222222) {
                    e222222222.printStackTrace();
                }
            }
            return false;
        }
    }

    public static void zipFiles(Collection<File> resFileList, File zipFile, String comment) throws IOException {
        ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFF_SIZE));
        for (File resFile : resFileList) {
            zipFile(resFile, zipout, "");
        }
        zipout.setComment(comment);
        zipout.close();
    }

    public static void unZipFile(File zipFile, String folderPath) {
        Throwable th;
        Exception e;
        File desDir = new File(folderPath);
        if (desDir.exists() || desDir.mkdirs()) {
            int i = 0;
            ZipFile zipFile2 = null;
            try {
                ZipFile zipFile3 = new ZipFile(zipFile);
                try {
                    Enumeration<?> entries = zipFile3.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        if (entry.getName() == null || !entry.getName().contains(EXCLUDE_ENTRY)) {
                            String str = new String((folderPath + File.separator + entry.getName()).getBytes("8859_1"), "UTF-8");
                            i++;
                            Log.i(TAG, i + "======file=====:" + str);
                            File desFile = new File(str);
                            if (entry.isDirectory()) {
                                continue;
                            } else {
                                boolean newOk = true;
                                if (!desFile.exists()) {
                                    File fileParentDir = desFile.getParentFile();
                                    if (!(fileParentDir == null || (fileParentDir.exists() ^ 1) == 0)) {
                                        newOk = fileParentDir.mkdirs();
                                    }
                                    if (newOk) {
                                        newOk = desFile.createNewFile();
                                    }
                                }
                                if (newOk || desFile.exists()) {
                                    OutputStream outputStream = null;
                                    InputStream inputStream = null;
                                    try {
                                        inputStream = zipFile3.getInputStream(entry);
                                        OutputStream out = new FileOutputStream(desFile);
                                        try {
                                            byte[] buffer = new byte[BUFF_SIZE];
                                            while (true) {
                                                int realLength = inputStream.read(buffer);
                                                if (realLength <= 0) {
                                                    break;
                                                }
                                                out.write(buffer, 0, realLength);
                                            }
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e2) {
                                                    e2.printStackTrace();
                                                }
                                            }
                                            if (out != null) {
                                                try {
                                                    out.close();
                                                } catch (IOException e22) {
                                                    e22.printStackTrace();
                                                }
                                            } else {
                                                continue;
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            outputStream = out;
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e222) {
                                                    e222.printStackTrace();
                                                }
                                            }
                                            if (outputStream != null) {
                                                try {
                                                    outputStream.close();
                                                } catch (IOException e2222) {
                                                    e2222.printStackTrace();
                                                }
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                            }
                        }
                    }
                    Log.i(TAG, "unzip end");
                    if (zipFile3 != null) {
                        try {
                            zipFile3.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    zipFile2 = zipFile3;
                } catch (Exception e3) {
                    e = e3;
                    zipFile2 = zipFile3;
                } catch (Throwable th4) {
                    th = th4;
                    zipFile2 = zipFile3;
                }
            } catch (Exception e4) {
                e = e4;
                try {
                    e.printStackTrace();
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e222222) {
                            e222222.printStackTrace();
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e2222222) {
                            e2222222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:28:0x0065, code:
            r11 = r11 + r9;
     */
    /* JADX WARNING: Missing block: B:29:0x0069, code:
            if (r11 < MAX_ENTRY_THEME_SIZE) goto L_0x009d;
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            android.util.Log.e(TAG, "isZipError total checkZipIsSize true " + r17);
     */
    /* JADX WARNING: Missing block: B:33:0x0088, code:
            if (r10 == null) goto L_0x008d;
     */
    /* JADX WARNING: Missing block: B:35:?, code:
            r10.close();
     */
    /* JADX WARNING: Missing block: B:40:0x0093, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:41:0x0094, code:
            r4.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            r10.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isZipError(String zipFile) {
        ZipFile zf;
        Throwable th;
        if (zipFile == null) {
            return true;
        }
        ZipFile zf2 = null;
        InputStream in = null;
        int total = 0;
        try {
            zf = new ZipFile(zipFile);
            try {
                Enumeration<?> entries = zf.entries();
                while (entries.hasMoreElements()) {
                    in = zf.getInputStream((ZipEntry) entries.nextElement());
                    int entrySzie = 0;
                    byte[] buffer = new byte[4096];
                    while (true) {
                        int bytesRead = in.read(buffer);
                        if (bytesRead < 0) {
                            break;
                        }
                        entrySzie += bytesRead;
                        if (entrySzie >= MAX_ENTRY_THEME_SIZE) {
                            Log.e(TAG, "isZipError entry checkZipIsSize true " + zipFile);
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (zf != null) {
                                try {
                                    zf.close();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                            return true;
                        }
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
                return false;
            } catch (IOException e4) {
                zf2 = zf;
            } catch (Exception e5) {
                zf2 = zf;
            } catch (Throwable th2) {
                th = th2;
                zf2 = zf;
            }
        } catch (IOException e6) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
            if (zf2 != null) {
                try {
                    zf2.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            return true;
        } catch (Exception e7) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
            if (zf2 != null) {
                try {
                    zf2.close();
                } catch (Exception e2222) {
                    e2222.printStackTrace();
                }
            }
            return true;
        } catch (Throwable th3) {
            th = th3;
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            if (zf2 != null) {
                try {
                    zf2.close();
                } catch (Exception e22222) {
                    e22222.printStackTrace();
                }
            }
            throw th;
        }
        if (zf != null) {
            try {
                zf.close();
            } catch (Exception e222222) {
                e222222.printStackTrace();
            }
        }
        return true;
        return true;
    }

    public static void unZipDirectory(String zipFile, String entryName, String desPath) {
        Throwable th;
        Exception e;
        File desDir = new File(desPath);
        if (desDir.exists() || desDir.mkdirs()) {
            ZipFile zipFile2 = null;
            try {
                ZipFile zipFile3 = new ZipFile(zipFile);
                try {
                    Enumeration<?> entries = zipFile3.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        if (!(entry.getName() == null || (entry.getName().contains(EXCLUDE_ENTRY) ^ 1) == 0 || !entry.getName().contains(entryName))) {
                            File desFile = new File(new String((desPath + File.separator + entry.getName()).getBytes("8859_1"), "UTF-8"));
                            if (entry.isDirectory()) {
                                continue;
                            } else {
                                boolean newOk = true;
                                if (!desFile.exists()) {
                                    File fileParentDir = desFile.getParentFile();
                                    if (!(fileParentDir == null || (fileParentDir.exists() ^ 1) == 0)) {
                                        newOk = fileParentDir.mkdirs();
                                    }
                                    if (newOk) {
                                        newOk = desFile.createNewFile();
                                    }
                                }
                                if (newOk || desFile.exists()) {
                                    OutputStream outputStream = null;
                                    InputStream inputStream = null;
                                    try {
                                        inputStream = zipFile3.getInputStream(entry);
                                        OutputStream out = new FileOutputStream(desFile);
                                        try {
                                            byte[] buffer = new byte[BUFF_SIZE];
                                            while (true) {
                                                int realLength = inputStream.read(buffer);
                                                if (realLength <= 0) {
                                                    break;
                                                }
                                                out.write(buffer, 0, realLength);
                                            }
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e2) {
                                                    e2.printStackTrace();
                                                }
                                            }
                                            if (out != null) {
                                                try {
                                                    out.close();
                                                } catch (IOException e22) {
                                                    e22.printStackTrace();
                                                }
                                            } else {
                                                continue;
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            outputStream = out;
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e222) {
                                                    e222.printStackTrace();
                                                }
                                            }
                                            if (outputStream != null) {
                                                try {
                                                    outputStream.close();
                                                } catch (IOException e2222) {
                                                    e2222.printStackTrace();
                                                }
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                            }
                        }
                    }
                    if (zipFile3 != null) {
                        try {
                            zipFile3.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    zipFile2 = zipFile3;
                } catch (Exception e3) {
                    e = e3;
                    zipFile2 = zipFile3;
                } catch (Throwable th4) {
                    th = th4;
                    zipFile2 = zipFile3;
                }
            } catch (Exception e4) {
                e = e4;
                try {
                    e.printStackTrace();
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e222222) {
                            e222222.printStackTrace();
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e2222222) {
                            e2222222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        }
    }

    public static ArrayList<String> getEntriesNames(File zipFile) throws ZipException, IOException {
        ArrayList<String> entryNames = new ArrayList();
        ZipFile zf = new ZipFile(zipFile);
        try {
            Enumeration<?> entries = zf.entries();
            while (entries.hasMoreElements()) {
                String entryName = getEntryName((ZipEntry) entries.nextElement());
                if (entryName != null) {
                    entryNames.add(entryName);
                }
            }
            try {
                zf.close();
            } catch (Exception e1) {
                Log.e(TAG, "Exception e1: " + e1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception e: " + e);
            try {
                zf.close();
            } catch (Exception e12) {
                Log.e(TAG, "Exception e1: " + e12);
            }
        } catch (Throwable th) {
            try {
                zf.close();
            } catch (Exception e122) {
                Log.e(TAG, "Exception e1: " + e122);
            }
            throw th;
        }
        return entryNames;
    }

    public static String getEntryComment(ZipEntry entry) throws UnsupportedEncodingException {
        return new String(entry.getComment().getBytes("GB2312"), "8859_1");
    }

    public static String getEntryName(ZipEntry entry) throws UnsupportedEncodingException {
        if (entry.getName() == null || !entry.getName().contains(EXCLUDE_ENTRY)) {
            return new String(entry.getName().getBytes("GB2312"), "8859_1");
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x0093 A:{SYNTHETIC, Splitter: B:41:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:81:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0098 A:{SYNTHETIC, Splitter: B:44:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00bf A:{SYNTHETIC, Splitter: B:59:0x00bf} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00c4 A:{SYNTHETIC, Splitter: B:62:0x00c4} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0093 A:{SYNTHETIC, Splitter: B:41:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0098 A:{SYNTHETIC, Splitter: B:44:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:81:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00bf A:{SYNTHETIC, Splitter: B:59:0x00bf} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00c4 A:{SYNTHETIC, Splitter: B:62:0x00c4} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0093 A:{SYNTHETIC, Splitter: B:41:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:81:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0098 A:{SYNTHETIC, Splitter: B:44:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00bf A:{SYNTHETIC, Splitter: B:59:0x00bf} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00c4 A:{SYNTHETIC, Splitter: B:62:0x00c4} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void zipFile(File resFile, ZipOutputStream zipout, String rootpath) {
        Exception e;
        Throwable th;
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        try {
            String rootpath2 = new String((rootpath + (rootpath.trim().length() == 0 ? "" : File.separator) + resFile.getName()).getBytes("8859_1"), "GB2312");
            try {
                if (resFile.isDirectory()) {
                    File[] fileList = resFile.listFiles();
                    if (fileList != null) {
                        for (File file : fileList) {
                            zipFile(file, zipout, rootpath2);
                        }
                    }
                    zipout.putNextEntry(new ZipEntry(rootpath2));
                } else {
                    BufferedInputStream bis2;
                    byte[] buffer = new byte[BUFF_SIZE];
                    FileInputStream fis2 = new FileInputStream(resFile);
                    try {
                        bis2 = new BufferedInputStream(fis2, BUFF_SIZE);
                    } catch (Exception e2) {
                        e = e2;
                        fis = fis2;
                        rootpath = rootpath2;
                        try {
                            e.printStackTrace();
                            if (bis != null) {
                            }
                            if (fis == null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (bis != null) {
                            }
                            if (fis != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fis = fis2;
                        rootpath = rootpath2;
                        if (bis != null) {
                        }
                        if (fis != null) {
                        }
                        throw th;
                    }
                    try {
                        zipout.putNextEntry(new ZipEntry(rootpath2));
                        while (true) {
                            int realLength = bis2.read(buffer);
                            if (realLength == -1) {
                                break;
                            }
                            zipout.write(buffer, 0, realLength);
                        }
                        bis2.close();
                        zipout.flush();
                        zipout.closeEntry();
                        fis = fis2;
                        bis = bis2;
                    } catch (Exception e3) {
                        e = e3;
                        fis = fis2;
                        bis = bis2;
                        rootpath = rootpath2;
                        e.printStackTrace();
                        if (bis != null) {
                        }
                        if (fis == null) {
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        fis = fis2;
                        bis = bis2;
                        rootpath = rootpath2;
                        if (bis != null) {
                            try {
                                bis.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException e42) {
                                e42.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e422) {
                        e422.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e4222) {
                        e4222.printStackTrace();
                    }
                }
                rootpath = rootpath2;
            } catch (Exception e5) {
                e = e5;
                rootpath = rootpath2;
                e.printStackTrace();
                if (bis != null) {
                }
                if (fis == null) {
                }
            } catch (Throwable th5) {
                th = th5;
                rootpath = rootpath2;
                if (bis != null) {
                }
                if (fis != null) {
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            e.printStackTrace();
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e42222) {
                    e42222.printStackTrace();
                }
            }
            if (fis == null) {
                try {
                    fis.close();
                } catch (IOException e422222) {
                    e422222.printStackTrace();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x006d A:{SYNTHETIC, Splitter: B:27:0x006d} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0072 A:{SYNTHETIC, Splitter: B:30:0x0072} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0077 A:{SYNTHETIC, Splitter: B:33:0x0077} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00b3 A:{SYNTHETIC, Splitter: B:62:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00b8 A:{SYNTHETIC, Splitter: B:65:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00bd A:{SYNTHETIC, Splitter: B:68:0x00bd} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00b3 A:{SYNTHETIC, Splitter: B:62:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00b8 A:{SYNTHETIC, Splitter: B:65:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00bd A:{SYNTHETIC, Splitter: B:68:0x00bd} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void unZipFile(String zipFile, String entryName, String desPath) {
        Exception e;
        Throwable th;
        ZipFile zf = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            ZipFile zf2 = new ZipFile(zipFile);
            try {
                ZipEntry zEntry = zf2.getEntry(entryName);
                if (!(zEntry == null || entryName == null)) {
                    if (!entryName.contains(EXCLUDE_ENTRY)) {
                        in = zf2.getInputStream(zEntry);
                        OutputStream out2 = new FileOutputStream(new String((desPath + File.separator + entryName).getBytes("8859_1"), "GB2312"));
                        try {
                            byte[] buffer = new byte[4096];
                            while (true) {
                                int realLength = in.read(buffer);
                                if (realLength <= 0) {
                                    break;
                                }
                                out2.write(buffer, 0, realLength);
                            }
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                            if (out2 != null) {
                                try {
                                    out2.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            if (zf2 != null) {
                                try {
                                    zf2.close();
                                } catch (IOException e222) {
                                    e222.printStackTrace();
                                }
                            }
                        } catch (Exception e3) {
                            e = e3;
                            out = out2;
                            zf = zf2;
                            try {
                                e.printStackTrace();
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException e2222) {
                                        e2222.printStackTrace();
                                    }
                                }
                                if (out != null) {
                                    try {
                                        out.close();
                                    } catch (IOException e22222) {
                                        e22222.printStackTrace();
                                    }
                                }
                                if (zf != null) {
                                    try {
                                        zf.close();
                                    } catch (IOException e222222) {
                                        e222222.printStackTrace();
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (in != null) {
                                }
                                if (out != null) {
                                }
                                if (zf != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            out = out2;
                            zf = zf2;
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e2222222) {
                                    e2222222.printStackTrace();
                                }
                            }
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e22222222) {
                                    e22222222.printStackTrace();
                                }
                            }
                            if (zf != null) {
                                try {
                                    zf.close();
                                } catch (IOException e222222222) {
                                    e222222222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    }
                }
                if (zf2 != null) {
                    try {
                        zf2.close();
                    } catch (IOException e2222222222) {
                        e2222222222.printStackTrace();
                    }
                }
            } catch (Exception e4) {
                e = e4;
                zf = zf2;
            } catch (Throwable th4) {
                th = th4;
                zf = zf2;
                if (in != null) {
                }
                if (out != null) {
                }
                if (zf != null) {
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            e.printStackTrace();
            if (in != null) {
            }
            if (out != null) {
            }
            if (zf != null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0076 A:{SYNTHETIC, Splitter: B:31:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x007b A:{SYNTHETIC, Splitter: B:34:0x007b} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0080 A:{SYNTHETIC, Splitter: B:37:0x0080} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00b5 A:{SYNTHETIC, Splitter: B:64:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00ba A:{SYNTHETIC, Splitter: B:67:0x00ba} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x00bf A:{SYNTHETIC, Splitter: B:70:0x00bf} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00b5 A:{SYNTHETIC, Splitter: B:64:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00ba A:{SYNTHETIC, Splitter: B:67:0x00ba} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x00bf A:{SYNTHETIC, Splitter: B:70:0x00bf} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void unZipFile(File zipFile, String desPath, String entryName) {
        Exception e;
        Throwable th;
        if (!(zipFile == null || entryName == null)) {
            if (!entryName.contains(EXCLUDE_ENTRY)) {
                InputStream inStream = null;
                OutputStream outStream = null;
                ZipFile zf = null;
                try {
                    ZipFile zf2 = new ZipFile(zipFile);
                    try {
                        ZipEntry zEntry = zf2.getEntry(entryName);
                        if (zEntry == null) {
                            if (zf2 != null) {
                                try {
                                    zf2.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                            return;
                        }
                        inStream = zf2.getInputStream(zEntry);
                        OutputStream outStream2 = new FileOutputStream(new String((desPath + File.separator + entryName).getBytes("8859_1"), "GB2312"));
                        try {
                            byte[] buffer = new byte[4096];
                            while (true) {
                                int realLength = inStream.read(buffer);
                                if (realLength <= 0) {
                                    break;
                                }
                                outStream2.write(buffer, 0, realLength);
                            }
                            if (zf2 != null) {
                                try {
                                    zf2.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            if (inStream != null) {
                                try {
                                    inStream.close();
                                } catch (IOException e222) {
                                    e222.printStackTrace();
                                }
                            }
                            if (outStream2 != null) {
                                try {
                                    outStream2.close();
                                } catch (IOException e2222) {
                                    e2222.printStackTrace();
                                }
                            }
                        } catch (Exception e3) {
                            e = e3;
                            zf = zf2;
                            outStream = outStream2;
                            try {
                                e.printStackTrace();
                                if (zf != null) {
                                    try {
                                        zf.close();
                                    } catch (IOException e22222) {
                                        e22222.printStackTrace();
                                    }
                                }
                                if (inStream != null) {
                                    try {
                                        inStream.close();
                                    } catch (IOException e222222) {
                                        e222222.printStackTrace();
                                    }
                                }
                                if (outStream != null) {
                                    try {
                                        outStream.close();
                                    } catch (IOException e2222222) {
                                        e2222222.printStackTrace();
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (zf != null) {
                                }
                                if (inStream != null) {
                                }
                                if (outStream != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            zf = zf2;
                            outStream = outStream2;
                            if (zf != null) {
                                try {
                                    zf.close();
                                } catch (IOException e22222222) {
                                    e22222222.printStackTrace();
                                }
                            }
                            if (inStream != null) {
                                try {
                                    inStream.close();
                                } catch (IOException e222222222) {
                                    e222222222.printStackTrace();
                                }
                            }
                            if (outStream != null) {
                                try {
                                    outStream.close();
                                } catch (IOException e2222222222) {
                                    e2222222222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (Exception e4) {
                        e = e4;
                        zf = zf2;
                    } catch (Throwable th4) {
                        th = th4;
                        zf = zf2;
                        if (zf != null) {
                        }
                        if (inStream != null) {
                        }
                        if (outStream != null) {
                        }
                        throw th;
                    }
                } catch (Exception e5) {
                    e = e5;
                    e.printStackTrace();
                    if (zf != null) {
                    }
                    if (inStream != null) {
                    }
                    if (outStream != null) {
                    }
                }
            }
        }
    }
}
