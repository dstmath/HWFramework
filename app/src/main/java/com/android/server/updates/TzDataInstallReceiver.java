package com.android.server.updates;

import android.util.Slog;
import java.io.File;
import java.io.IOException;
import libcore.tzdata.update.TzDataBundleInstaller;

public class TzDataInstallReceiver extends ConfigUpdateInstallReceiver {
    private static final String TAG = "TZDataInstallReceiver";
    private static final File TZ_DATA_DIR = null;
    private static final String UPDATE_CONTENT_FILE_NAME = "tzdata_bundle.zip";
    private static final String UPDATE_DIR_NAME = null;
    private static final String UPDATE_METADATA_DIR_NAME = "metadata/";
    private static final String UPDATE_VERSION_FILE_NAME = "version";
    private final TzDataBundleInstaller installer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.updates.TzDataInstallReceiver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.updates.TzDataInstallReceiver.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.updates.TzDataInstallReceiver.<clinit>():void");
    }

    public TzDataInstallReceiver() {
        super(UPDATE_DIR_NAME, UPDATE_CONTENT_FILE_NAME, UPDATE_METADATA_DIR_NAME, UPDATE_VERSION_FILE_NAME);
        this.installer = new TzDataBundleInstaller(TAG, TZ_DATA_DIR);
    }

    protected void install(byte[] content, int version) throws IOException {
        Slog.i(TAG, "Timezone data install valid for this device: " + this.installer.install(content));
        super.install(content, version);
    }
}
