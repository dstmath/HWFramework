package com.android.server.location;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.HwServiceFactory;
import java.util.Properties;
import java.util.Random;

public class GpsXtraDownloader {
    private static final int CONNECTION_TIMEOUT_MS = 0;
    private static final String DEFAULT_USER_AGENT = "Android";
    private static final long MAXIMUM_CONTENT_LENGTH_BYTES = 1000000;
    private static final String TAG = "GpsXtraDownloader";
    private int mByteCount;
    private IHwGpsLogServices mHwGpsLogServices;
    private int mNextServerIndex;
    private final String mUserAgent;
    private final String[] mXtraServers;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.GpsXtraDownloader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.GpsXtraDownloader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsXtraDownloader.<clinit>():void");
    }

    protected byte[] doDownload(java.lang.String r22) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x012e in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r21 = this;
        r14 = "GpsXtraDownloader";
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = "Downloading XTRA data from ";
        r15 = r15.append(r16);
        r0 = r22;
        r15 = r15.append(r0);
        r15 = r15.toString();
        android.util.Log.i(r14, r15);
        r14 = 0;
        r0 = r21;
        r0.mByteCount = r14;
        r2 = 0;
        r10 = new java.net.URL;	 Catch:{ IOException -> 0x0067 }
        r0 = r22;	 Catch:{ IOException -> 0x0067 }
        r10.<init>(r0);	 Catch:{ IOException -> 0x0067 }
        r6 = r10.openConnection();	 Catch:{ IOException -> 0x0067 }
        r14 = 3000; // 0xbb8 float:4.204E-42 double:1.482E-320;	 Catch:{ IOException -> 0x0067 }
        r6.setConnectTimeout(r14);	 Catch:{ IOException -> 0x0067 }
        r14 = 3000; // 0xbb8 float:4.204E-42 double:1.482E-320;	 Catch:{ IOException -> 0x0067 }
        r6.setReadTimeout(r14);	 Catch:{ IOException -> 0x0067 }
        r14 = r6.getContentLength();	 Catch:{ IOException -> 0x0067 }
        r0 = r21;	 Catch:{ IOException -> 0x0067 }
        r0.mByteCount = r14;	 Catch:{ IOException -> 0x0067 }
        r14 = "GpsXtraDownloader";	 Catch:{ IOException -> 0x0067 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0067 }
        r15.<init>();	 Catch:{ IOException -> 0x0067 }
        r16 = "mByteCount size is ";	 Catch:{ IOException -> 0x0067 }
        r15 = r15.append(r16);	 Catch:{ IOException -> 0x0067 }
        r0 = r21;	 Catch:{ IOException -> 0x0067 }
        r0 = r0.mByteCount;	 Catch:{ IOException -> 0x0067 }
        r16 = r0;	 Catch:{ IOException -> 0x0067 }
        r15 = r15.append(r16);	 Catch:{ IOException -> 0x0067 }
        r15 = r15.toString();	 Catch:{ IOException -> 0x0067 }
        android.util.Log.i(r14, r15);	 Catch:{ IOException -> 0x0067 }
        r0 = r21;
        r14 = r0.mByteCount;
        if (r14 > 0) goto L_0x006d;
    L_0x0065:
        r14 = 0;
        return r14;
    L_0x0067:
        r9 = move-exception;
        r9.printStackTrace();
        r14 = 0;
        return r14;
    L_0x006d:
        r7 = 0;
        r14 = new java.net.URL;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r0 = r22;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14.<init>(r0);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = r14.openConnection();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r0 = r14;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r0 = (java.net.HttpURLConnection) r0;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r7 = r0;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = "Accept";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r7.setRequestProperty(r14, r15);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = "x-wap-profile";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = "http://www.openmobilealliance.org/tech/profiles/UAPROF/ccppschema-20021212#";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r7.setRequestProperty(r14, r15);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = CONNECTION_TIMEOUT_MS;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r7.setConnectTimeout(r14);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = 120000; // 0x1d4c0 float:1.68156E-40 double:5.9288E-319;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r7.setReadTimeout(r14);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r7.connect();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = "GpsXtraDownloader";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15.<init>();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r16 = "the connection timeout:";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r15.append(r16);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r16 = r7.getConnectTimeout();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r15.append(r16);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r15.toString();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        android.util.Log.i(r14, r15);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r13 = r7.getResponseCode();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        if (r13 == r14) goto L_0x00e4;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x00c3:
        r14 = "GpsXtraDownloader";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15.<init>();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r16 = "HTTP error downloading gps XTRA: ";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r15.append(r16);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r15.append(r13);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r15.toString();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        android.util.Log.i(r14, r15);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = 0;
        if (r7 == 0) goto L_0x00e3;
    L_0x00e0:
        r7.disconnect();
    L_0x00e3:
        return r14;
    L_0x00e4:
        r15 = 0;
        r11 = 0;
        r11 = r7.getInputStream();	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r5 = new java.io.ByteArrayOutputStream;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r5.<init>();	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r14 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r4 = new byte[r14];	 Catch:{ all -> 0x01b1, all -> 0x0150 }
    L_0x00f3:
        r8 = r11.read(r4);	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r14 = -1;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        if (r8 == r14) goto L_0x0139;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
    L_0x00fa:
        r14 = 0;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r5.write(r4, r14, r8);	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r14 = r5.size();	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r0 = (long) r14;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r16 = r0;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r18 = 1000000; // 0xf4240 float:1.401298E-39 double:4.940656E-318;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r14 = (r16 > r18 ? 1 : (r16 == r18 ? 0 : -1));	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        if (r14 <= 0) goto L_0x00f3;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
    L_0x010c:
        r14 = "GpsXtraDownloader";	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r16 = "XTRA file too large";	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        r0 = r16;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        android.util.Log.d(r14, r0);	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        if (r11 == 0) goto L_0x011c;
    L_0x0119:
        r11.close();	 Catch:{ Throwable -> 0x0130 }
    L_0x011c:
        if (r15 == 0) goto L_0x0132;
    L_0x011e:
        throw r15;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x011f:
        r12 = move-exception;
        r14 = "GpsXtraDownloader";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = "Error downloading gps XTRA: ";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        android.util.Log.i(r14, r15, r12);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        if (r7 == 0) goto L_0x012e;
    L_0x012b:
        r7.disconnect();
    L_0x012e:
        r14 = 0;
        return r14;
    L_0x0130:
        r15 = move-exception;
        goto L_0x011c;
    L_0x0132:
        r14 = 0;
        if (r7 == 0) goto L_0x0138;
    L_0x0135:
        r7.disconnect();
    L_0x0138:
        return r14;
    L_0x0139:
        r3 = r5.toByteArray();	 Catch:{ all -> 0x01b1, all -> 0x0150 }
        if (r11 == 0) goto L_0x0142;
    L_0x013f:
        r11.close();	 Catch:{ Throwable -> 0x014c }
    L_0x0142:
        if (r15 == 0) goto L_0x016d;
    L_0x0144:
        throw r15;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x0145:
        r14 = move-exception;
        if (r7 == 0) goto L_0x014b;
    L_0x0148:
        r7.disconnect();
    L_0x014b:
        throw r14;
    L_0x014c:
        r15 = move-exception;
        goto L_0x0142;
    L_0x014e:
        r14 = move-exception;
        throw r14;	 Catch:{ all -> 0x01b1, all -> 0x0150 }
    L_0x0150:
        r15 = move-exception;
        r20 = r15;
        r15 = r14;
        r14 = r20;
    L_0x0156:
        if (r11 == 0) goto L_0x015b;
    L_0x0158:
        r11.close();	 Catch:{ Throwable -> 0x015e }
    L_0x015b:
        if (r15 == 0) goto L_0x016c;
    L_0x015d:
        throw r15;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x015e:
        r16 = move-exception;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        if (r15 != 0) goto L_0x0164;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x0161:
        r15 = r16;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        goto L_0x015b;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x0164:
        r0 = r16;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        if (r15 == r0) goto L_0x015b;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x0168:
        r15.addSuppressed(r16);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        goto L_0x015b;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x016c:
        throw r14;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x016d:
        r14 = "GpsXtraDownloader";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15.<init>();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r16 = "the getReadTimeout:";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r15.append(r16);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r16 = r7.getReadTimeout();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r15.append(r16);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r15.toString();	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        android.util.Log.i(r14, r15);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r0 = r21;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = r0.mByteCount;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = r3.length;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        if (r14 != r15) goto L_0x01a1;	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
    L_0x0192:
        r14 = "GpsXtraDownloader";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = "lto downloader process ok";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        android.util.Log.i(r14, r15);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        if (r7 == 0) goto L_0x01a0;
    L_0x019d:
        r7.disconnect();
    L_0x01a0:
        return r3;
    L_0x01a1:
        r14 = "GpsXtraDownloader";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r15 = "lto downloader process error";	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        android.util.Log.e(r14, r15);	 Catch:{ IOException -> 0x011f, all -> 0x0145 }
        r14 = 0;
        if (r7 == 0) goto L_0x01b0;
    L_0x01ad:
        r7.disconnect();
    L_0x01b0:
        return r14;
    L_0x01b1:
        r14 = move-exception;
        goto L_0x0156;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsXtraDownloader.doDownload(java.lang.String):byte[]");
    }

    GpsXtraDownloader(Properties properties) {
        int count = CONNECTION_TIMEOUT_MS;
        String server1 = properties.getProperty("XTRA_SERVER_1");
        String server2 = properties.getProperty("XTRA_SERVER_2");
        String server3 = properties.getProperty("XTRA_SERVER_3");
        if (server1 != null) {
            count = 1;
        }
        if (server2 != null) {
            count++;
        }
        if (server3 != null) {
            count++;
        }
        String agent = properties.getProperty("XTRA_USER_AGENT");
        if (TextUtils.isEmpty(agent)) {
            this.mUserAgent = DEFAULT_USER_AGENT;
        } else {
            this.mUserAgent = agent;
        }
        if (count == 0) {
            Log.e(TAG, "No XTRA servers were specified in the GPS configuration");
            this.mXtraServers = null;
            return;
        }
        int count2;
        this.mXtraServers = new String[count];
        if (server1 != null) {
            this.mXtraServers[CONNECTION_TIMEOUT_MS] = server1;
            count2 = 1;
        } else {
            count2 = CONNECTION_TIMEOUT_MS;
        }
        if (server2 != null) {
            count = count2 + 1;
            this.mXtraServers[count2] = server2;
            count2 = count;
        }
        if (server3 != null) {
            count = count2 + 1;
            this.mXtraServers[count2] = server3;
        } else {
            count = count2;
        }
        this.mNextServerIndex = new Random().nextInt(count);
    }

    byte[] downloadXtraData() {
        byte[] result = null;
        int startIndex = this.mNextServerIndex;
        if (this.mXtraServers == null) {
            return null;
        }
        while (result == null) {
            result = doDownload(this.mXtraServers[this.mNextServerIndex]);
            this.mNextServerIndex++;
            if (this.mNextServerIndex == this.mXtraServers.length) {
                this.mNextServerIndex = CONNECTION_TIMEOUT_MS;
            }
            if (this.mNextServerIndex == startIndex) {
                break;
            }
        }
        this.mHwGpsLogServices = HwServiceFactory.getNewHwGpsLogService();
        if (this.mHwGpsLogServices != null) {
            boolean xtraStatus = true;
            if (result == null && this.mByteCount != -1) {
                xtraStatus = false;
            }
            this.mHwGpsLogServices.updateXtraDloadStatus(xtraStatus);
        }
        return result;
    }
}
