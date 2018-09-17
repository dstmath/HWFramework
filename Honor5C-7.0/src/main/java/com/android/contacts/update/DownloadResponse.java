package com.android.contacts.update;

import com.android.contacts.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadResponse {
    private static final int CODE_ALREADY_NEW_VER = 200000;
    private static final int CODE_CLIENT_PARAMETER_ERROR = 100003;
    private static final int CODE_NOT_EXITS = 100001;
    private static final int CODE_OK = 0;
    private static final int CODE_PARAMETER_NULL = 100002;
    private static final int CODE_SERVER_ERROR = 100004;
    private static final String KEY_DOWNLOADURL = "downloadUrl";
    private static final String KEY_INFO = "info";
    private static final String KEY_RESULT_CODE = "resultCode";
    private static final String KEY_VER = "ver";
    private static final String TAG = null;
    private String downloadUrl;
    private int resultCode;
    private String ver;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.contacts.update.DownloadResponse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.contacts.update.DownloadResponse.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.update.DownloadResponse.<clinit>():void");
    }

    private DownloadResponse(int resultCode, String downloadUrl, String ver) {
        this.resultCode = -1;
        this.resultCode = resultCode;
        this.downloadUrl = downloadUrl;
        this.ver = ver;
    }

    public static DownloadResponse fromJson(String str) throws JSONException {
        JSONObject json = new JSONObject(str);
        return new DownloadResponse(json.getInt(KEY_RESULT_CODE), json.getString(KEY_DOWNLOADURL), json.getString(KEY_VER));
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public String getFileName() {
        return this.downloadUrl.substring(this.downloadUrl.lastIndexOf("/") + 1);
    }

    public String getVer() {
        return this.ver;
    }

    public boolean checkAvalible() {
        if (this.resultCode == 0) {
            return true;
        }
        if (!HwLog.HWDBG) {
            return false;
        }
        HwLog.d(TAG, "checkAvalible code : " + this.resultCode);
        return false;
    }
}
