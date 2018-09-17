package android.mtp;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScanner;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.opengl.EGL14;
import android.opengl.GLES11;
import android.os.BatteryManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.OpenableColumns;
import android.provider.Settings.Global;
import android.provider.Settings.NameValueTable;
import android.provider.SettingsEx;
import android.provider.VoicemailContract.Voicemails;
import android.rms.AppAssociate;
import android.rms.iaware.AwareConstant.Database.HwUserData;
import android.security.KeyChain;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MtpDatabase implements AutoCloseable {
    static final int[] AUDIO_PROPERTIES = null;
    private static final int DEVICE_PROPERTIES_DATABASE_VERSION = 1;
    static final int[] FILE_PROPERTIES = null;
    private static final String FORMAT_PARENT_WHERE = "format=? AND parent=?";
    private static final String[] FORMAT_PROJECTION = null;
    private static final String FORMAT_WHERE = "format=?";
    private static final String[] ID_PROJECTION = null;
    private static final String ID_WHERE = "_id=?";
    static final int[] IMAGE_PROPERTIES = null;
    private static final String[] OBJECT_ALL_COLUMNS_PROJECTION = null;
    private static final String[] OBJECT_INFO_PROJECTION = null;
    private static final String PARENT_WHERE = "parent=?";
    private static final String[] PATH_FORMAT_PROJECTION = null;
    private static final String[] PATH_PROJECTION = null;
    private static final String PATH_WHERE = "_data=?";
    private static final String STORAGE_FORMAT_PARENT_WHERE = "storage_id=? AND format=? AND parent=?";
    private static final String STORAGE_FORMAT_WHERE = "storage_id=? AND format=?";
    private static final String STORAGE_PARENT_WHERE = "storage_id=? AND parent=?";
    private static final String STORAGE_WHERE = "storage_id=?";
    private static final String TAG = "MtpDatabase";
    static final int[] VIDEO_PROPERTIES = null;
    private int mBatteryLevel;
    private BroadcastReceiver mBatteryReceiver;
    private int mBatteryScale;
    private final CloseGuard mCloseGuard;
    private final AtomicBoolean mClosed;
    private final Context mContext;
    private boolean mDatabaseModified;
    private SharedPreferences mDeviceProperties;
    private final ContentProviderClient mMediaProvider;
    private final MediaScanner mMediaScanner;
    private final String mMediaStoragePath;
    private long mNativeContext;
    private final Uri mObjectsUri;
    private final String mPackageName;
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByFormat;
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByProperty;
    private MtpServer mServer;
    private final HashMap<String, MtpStorage> mStorageMap;
    private final String[] mSubDirectories;
    private String mSubDirectoriesWhere;
    private String[] mSubDirectoriesWhereArgs;
    private final String mVolumeName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.mtp.MtpDatabase.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.mtp.MtpDatabase.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.<clinit>():void");
    }

    private int deleteFile(int r15) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0112 in list []
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
        r14 = this;
        r0 = 1;
        r14.mDatabaseModified = r0;
        r12 = 0;
        r10 = 0;
        r7 = 0;
        r0 = r14.mMediaProvider;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1 = r14.mObjectsUri;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r2 = PATH_FORMAT_PROJECTION;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = "_id=?";	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r4 = 1;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r4 = new java.lang.String[r4];	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r5 = java.lang.Integer.toString(r15);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r6 = 0;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r4[r6] = r5;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r5 = 0;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r6 = 0;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r7 = r0.query(r1, r2, r3, r4, r5, r6);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        if (r7 == 0) goto L_0x003d;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
    L_0x0021:
        r0 = r7.moveToNext();	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        if (r0 == 0) goto L_0x003d;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
    L_0x0027:
        r0 = 1;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r12 = r7.getString(r0);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r0 = 3;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r10 = r7.getInt(r0);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        if (r12 == 0) goto L_0x0035;
    L_0x0033:
        if (r10 != 0) goto L_0x0045;
    L_0x0035:
        r0 = 8194; // 0x2002 float:1.1482E-41 double:4.0484E-320;
        if (r7 == 0) goto L_0x003c;
    L_0x0039:
        r7.close();
    L_0x003c:
        return r0;
    L_0x003d:
        r0 = 8201; // 0x2009 float:1.1492E-41 double:4.052E-320;
        if (r7 == 0) goto L_0x0044;
    L_0x0041:
        r7.close();
    L_0x0044:
        return r0;
    L_0x0045:
        r0 = r14.isStorageSubDirectory(r12);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        if (r0 == 0) goto L_0x0053;
    L_0x004b:
        r0 = 8205; // 0x200d float:1.1498E-41 double:4.054E-320;
        if (r7 == 0) goto L_0x0052;
    L_0x004f:
        r7.close();
    L_0x0052:
        return r0;
    L_0x0053:
        r0 = 12289; // 0x3001 float:1.722E-41 double:6.0716E-320;
        if (r10 != r0) goto L_0x00a4;
    L_0x0057:
        r0 = r14.mVolumeName;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r13 = android.provider.MediaStore.Files.getMtpObjectsUri(r0);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r0 = r14.mMediaProvider;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1 = "_data LIKE ?1 AND lower(substr(_data,1,?2))=lower(?3)";	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r2 = 3;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r2 = new java.lang.String[r2];	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3.<init>();	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = r3.append(r12);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r4 = "/%";	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = r3.append(r4);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = r3.toString();	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r4 = 0;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r2[r4] = r3;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = r12.length();	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = r3 + 1;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = java.lang.Integer.toString(r3);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r4 = 1;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r2[r4] = r3;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3.<init>();	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = r3.append(r12);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r4 = "/";	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = r3.append(r4);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r3 = r3.toString();	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r4 = 2;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r2[r4] = r3;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r8 = r0.delete(r13, r1, r2);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
    L_0x00a4:
        r0 = r14.mVolumeName;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r2 = (long) r15;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r13 = android.provider.MediaStore.Files.getMtpObjectsUri(r0, r2);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r0 = r14.mMediaProvider;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1 = 0;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r2 = 0;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r0 = r0.delete(r13, r1, r2);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        if (r0 <= 0) goto L_0x0113;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
    L_0x00b5:
        r0 = 12289; // 0x3001 float:1.722E-41 double:6.0716E-320;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        if (r10 == r0) goto L_0x00dd;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
    L_0x00b9:
        r0 = java.util.Locale.US;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r0 = r12.toLowerCase(r0);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1 = "/.nomedia";	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r0 = r0.endsWith(r1);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        if (r0 == 0) goto L_0x00dd;
    L_0x00c8:
        r0 = "/";	 Catch:{ RemoteException -> 0x00e5 }
        r0 = r12.lastIndexOf(r0);	 Catch:{ RemoteException -> 0x00e5 }
        r1 = 0;	 Catch:{ RemoteException -> 0x00e5 }
        r11 = r12.substring(r1, r0);	 Catch:{ RemoteException -> 0x00e5 }
        r0 = r14.mMediaProvider;	 Catch:{ RemoteException -> 0x00e5 }
        r1 = "unhide";	 Catch:{ RemoteException -> 0x00e5 }
        r2 = 0;	 Catch:{ RemoteException -> 0x00e5 }
        r0.call(r1, r11, r2);	 Catch:{ RemoteException -> 0x00e5 }
    L_0x00dd:
        r0 = 8193; // 0x2001 float:1.1481E-41 double:4.048E-320;
        if (r7 == 0) goto L_0x00e4;
    L_0x00e1:
        r7.close();
    L_0x00e4:
        return r0;
    L_0x00e5:
        r9 = move-exception;
        r0 = "MtpDatabase";	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1.<init>();	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r2 = "failed to unhide/rescan for ";	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1 = r1.append(r2);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1 = r1.append(r12);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1 = r1.toString();	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        android.util.Log.e(r0, r1);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        goto L_0x00dd;
    L_0x0101:
        r9 = move-exception;
        r0 = "MtpDatabase";	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r1 = "RemoteException in deleteFile";	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        android.util.Log.e(r0, r1, r9);	 Catch:{ RemoteException -> 0x0101, all -> 0x011b }
        r0 = 8194; // 0x2002 float:1.1482E-41 double:4.0484E-320;
        if (r7 == 0) goto L_0x0112;
    L_0x010f:
        r7.close();
    L_0x0112:
        return r0;
    L_0x0113:
        r0 = 8201; // 0x2009 float:1.1492E-41 double:4.052E-320;
        if (r7 == 0) goto L_0x011a;
    L_0x0117:
        r7.close();
    L_0x011a:
        return r0;
    L_0x011b:
        r0 = move-exception;
        if (r7 == 0) goto L_0x0121;
    L_0x011e:
        r7.close();
    L_0x0121:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.deleteFile(int):int");
    }

    private int getObjectFilePath(int r13, char[] r14, long[] r15) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0098 in list []
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
        r12 = this;
        r11 = 8193; // 0x2001 float:1.1481E-41 double:4.048E-320;
        r3 = 1;
        r2 = 0;
        if (r13 != 0) goto L_0x0022;
    L_0x0006:
        r0 = r12.mMediaStoragePath;
        r1 = r12.mMediaStoragePath;
        r1 = r1.length();
        r0.getChars(r2, r1, r14, r2);
        r0 = r12.mMediaStoragePath;
        r0 = r0.length();
        r14[r0] = r2;
        r0 = 0;
        r15[r2] = r0;
        r0 = 12289; // 0x3001 float:1.722E-41 double:6.0716E-320;
        r15[r3] = r0;
        return r11;
    L_0x0022:
        r0 = android.common.HwFrameworkFactory.getHwMtpDatabaseManager();
        r10 = r0.hwGetObjectFilePath(r13, r14, r15);
        r0 = -1;
        if (r10 == r0) goto L_0x002e;
    L_0x002d:
        return r11;
    L_0x002e:
        r7 = 0;
        r0 = r12.mMediaProvider;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r1 = r12.mObjectsUri;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r2 = PATH_FORMAT_PROJECTION;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r3 = "_id=?";	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r4 = 1;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r4 = new java.lang.String[r4];	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r5 = java.lang.Integer.toString(r13);	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r6 = 0;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r4[r6] = r5;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r5 = 0;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r6 = 0;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r7 = r0.query(r1, r2, r3, r4, r5, r6);	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        if (r7 == 0) goto L_0x007f;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
    L_0x004a:
        r0 = r7.moveToNext();	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        if (r0 == 0) goto L_0x007f;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
    L_0x0050:
        r0 = 1;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r9 = r7.getString(r0);	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r0 = r9.length();	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r1 = 0;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r2 = 0;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r9.getChars(r1, r0, r14, r2);	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r0 = r9.length();	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r1 = 0;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r14[r0] = r1;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r0 = new java.io.File;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r0.<init>(r9);	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r0 = r0.length();	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r2 = 0;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r15[r2] = r0;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r0 = 3;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r0 = r7.getLong(r0);	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r2 = 1;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r15[r2] = r0;	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        if (r7 == 0) goto L_0x007e;
    L_0x007b:
        r7.close();
    L_0x007e:
        return r11;
    L_0x007f:
        r0 = 8201; // 0x2009 float:1.1492E-41 double:4.052E-320;
        if (r7 == 0) goto L_0x0086;
    L_0x0083:
        r7.close();
    L_0x0086:
        return r0;
    L_0x0087:
        r8 = move-exception;
        r0 = "MtpDatabase";	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r1 = "RemoteException in getObjectFilePath";	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        android.util.Log.e(r0, r1, r8);	 Catch:{ RemoteException -> 0x0087, all -> 0x0099 }
        r0 = 8194; // 0x2002 float:1.1482E-41 double:4.0484E-320;
        if (r7 == 0) goto L_0x0098;
    L_0x0095:
        r7.close();
    L_0x0098:
        return r0;
    L_0x0099:
        r0 = move-exception;
        if (r7 == 0) goto L_0x009f;
    L_0x009c:
        r7.close();
    L_0x009f:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.getObjectFilePath(int, char[], long[]):int");
    }

    private int getObjectFormat(int r12) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x004e in list []
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
        r11 = this;
        r10 = -1;
        r0 = android.common.HwFrameworkFactory.getHwMtpDatabaseManager();
        r9 = r0.hwGetObjectFormat(r12);
        if (r9 == r10) goto L_0x000c;
    L_0x000b:
        return r9;
    L_0x000c:
        r7 = 0;
        r0 = r11.mMediaProvider;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r1 = r11.mObjectsUri;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r2 = FORMAT_PROJECTION;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r3 = "_id=?";	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r4 = 1;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r4 = new java.lang.String[r4];	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r5 = java.lang.Integer.toString(r12);	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r6 = 0;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r4[r6] = r5;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r5 = 0;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r6 = 0;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r7 = r0.query(r1, r2, r3, r4, r5, r6);	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        if (r7 == 0) goto L_0x0039;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
    L_0x0028:
        r0 = r7.moveToNext();	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        if (r0 == 0) goto L_0x0039;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
    L_0x002e:
        r0 = 1;	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r0 = r7.getInt(r0);	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        if (r7 == 0) goto L_0x0038;
    L_0x0035:
        r7.close();
    L_0x0038:
        return r0;
    L_0x0039:
        if (r7 == 0) goto L_0x003e;
    L_0x003b:
        r7.close();
    L_0x003e:
        return r10;
    L_0x003f:
        r8 = move-exception;
        r0 = "MtpDatabase";	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        r1 = "RemoteException in getObjectFilePath";	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        android.util.Log.e(r0, r1, r8);	 Catch:{ RemoteException -> 0x003f, all -> 0x004f }
        if (r7 == 0) goto L_0x004e;
    L_0x004b:
        r7.close();
    L_0x004e:
        return r10;
    L_0x004f:
        r0 = move-exception;
        if (r7 == 0) goto L_0x0055;
    L_0x0052:
        r7.close();
    L_0x0055:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.getObjectFormat(int):int");
    }

    private final native void native_finalize();

    private final native void native_setup();

    private int renameFile(int r19, java.lang.String r20) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0047 in list []
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
        r18 = this;
        r8 = 0;
        r14 = 0;
        r1 = 1;
        r5 = new java.lang.String[r1];
        r1 = java.lang.Integer.toString(r19);
        r2 = 0;
        r5[r2] = r1;
        r0 = r18;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r1 = r0.mMediaProvider;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r0 = r18;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r2 = r0.mObjectsUri;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r3 = PATH_PROJECTION;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r4 = "_id=?";	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r6 = 0;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r7 = 0;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r8 = r1.query(r2, r3, r4, r5, r6, r7);	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        if (r8 == 0) goto L_0x002c;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
    L_0x0021:
        r1 = r8.moveToNext();	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        if (r1 == 0) goto L_0x002c;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
    L_0x0027:
        r1 = 1;	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r14 = r8.getString(r1);	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
    L_0x002c:
        if (r8 == 0) goto L_0x0031;
    L_0x002e:
        r8.close();
    L_0x0031:
        if (r14 != 0) goto L_0x004f;
    L_0x0033:
        r1 = 8201; // 0x2009 float:1.1492E-41 double:4.052E-320;
        return r1;
    L_0x0036:
        r9 = move-exception;
        r1 = "MtpDatabase";	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r2 = "RemoteException in getObjectFilePath";	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        android.util.Log.e(r1, r2, r9);	 Catch:{ RemoteException -> 0x0036, all -> 0x0048 }
        r1 = 8194; // 0x2002 float:1.1482E-41 double:4.0484E-320;
        if (r8 == 0) goto L_0x0047;
    L_0x0044:
        r8.close();
    L_0x0047:
        return r1;
    L_0x0048:
        r1 = move-exception;
        if (r8 == 0) goto L_0x004e;
    L_0x004b:
        r8.close();
    L_0x004e:
        throw r1;
    L_0x004f:
        r0 = r18;
        r1 = r0.isStorageSubDirectory(r14);
        if (r1 == 0) goto L_0x005a;
    L_0x0057:
        r1 = 8205; // 0x200d float:1.1498E-41 double:4.054E-320;
        return r1;
    L_0x005a:
        r13 = new java.io.File;
        r13.<init>(r14);
        r1 = 47;
        r10 = r14.lastIndexOf(r1);
        r1 = 1;
        if (r10 > r1) goto L_0x006b;
    L_0x0068:
        r1 = 8194; // 0x2002 float:1.1482E-41 double:4.0484E-320;
        return r1;
    L_0x006b:
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = r10 + 1;
        r3 = 0;
        r2 = r14.substring(r3, r2);
        r1 = r1.append(r2);
        r0 = r20;
        r1 = r1.append(r0);
        r12 = r1.toString();
        r11 = new java.io.File;
        r11.<init>(r12);
        r15 = r13.renameTo(r11);
        if (r15 != 0) goto L_0x00bf;
    L_0x0090:
        r1 = "MtpDatabase";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "renaming ";
        r2 = r2.append(r3);
        r2 = r2.append(r14);
        r3 = " to ";
        r2 = r2.append(r3);
        r2 = r2.append(r12);
        r3 = " failed";
        r2 = r2.append(r3);
        r2 = r2.toString();
        android.util.Log.w(r1, r2);
        r1 = 8194; // 0x2002 float:1.1482E-41 double:4.0484E-320;
        return r1;
    L_0x00bf:
        r17 = new android.content.ContentValues;
        r17.<init>();
        r1 = "_data";
        r0 = r17;
        r0.put(r1, r12);
        r16 = 0;
        r0 = r18;	 Catch:{ RemoteException -> 0x010c }
        r1 = r0.mMediaProvider;	 Catch:{ RemoteException -> 0x010c }
        r0 = r18;	 Catch:{ RemoteException -> 0x010c }
        r2 = r0.mObjectsUri;	 Catch:{ RemoteException -> 0x010c }
        r3 = "_id=?";	 Catch:{ RemoteException -> 0x010c }
        r0 = r17;	 Catch:{ RemoteException -> 0x010c }
        r16 = r1.update(r2, r0, r3, r5);	 Catch:{ RemoteException -> 0x010c }
    L_0x00df:
        if (r16 != 0) goto L_0x0117;
    L_0x00e1:
        r1 = "MtpDatabase";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Unable to update path for ";
        r2 = r2.append(r3);
        r2 = r2.append(r14);
        r3 = " to ";
        r2 = r2.append(r3);
        r2 = r2.append(r12);
        r2 = r2.toString();
        android.util.Log.e(r1, r2);
        r11.renameTo(r13);
        r1 = 8194; // 0x2002 float:1.1482E-41 double:4.0484E-320;
        return r1;
    L_0x010c:
        r9 = move-exception;
        r1 = "MtpDatabase";
        r2 = "RemoteException in mMediaProvider.update";
        android.util.Log.e(r1, r2, r9);
        goto L_0x00df;
    L_0x0117:
        r1 = r11.isDirectory();
        if (r1 == 0) goto L_0x015e;
    L_0x011d:
        r1 = r13.getName();
        r2 = ".";
        r1 = r1.startsWith(r2);
        if (r1 == 0) goto L_0x0133;
    L_0x012a:
        r1 = ".";
        r1 = r12.startsWith(r1);
        if (r1 == 0) goto L_0x0136;
    L_0x0133:
        r1 = 8193; // 0x2001 float:1.1481E-41 double:4.048E-320;
        return r1;
    L_0x0136:
        r0 = r18;	 Catch:{ RemoteException -> 0x0142 }
        r1 = r0.mMediaProvider;	 Catch:{ RemoteException -> 0x0142 }
        r2 = "unhide";	 Catch:{ RemoteException -> 0x0142 }
        r3 = 0;	 Catch:{ RemoteException -> 0x0142 }
        r1.call(r2, r12, r3);	 Catch:{ RemoteException -> 0x0142 }
        goto L_0x0133;
    L_0x0142:
        r9 = move-exception;
        r1 = "MtpDatabase";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "failed to unhide/rescan for ";
        r2 = r2.append(r3);
        r2 = r2.append(r12);
        r2 = r2.toString();
        android.util.Log.e(r1, r2);
        goto L_0x0133;
    L_0x015e:
        r1 = r13.getName();
        r2 = java.util.Locale.US;
        r1 = r1.toLowerCase(r2);
        r2 = ".nomedia";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0180;
    L_0x0171:
        r1 = java.util.Locale.US;
        r1 = r12.toLowerCase(r1);
        r2 = ".nomedia";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x018c;
    L_0x0180:
        r0 = r18;
        r1 = r0.mMediaScanner;
        r2 = 12288; // 0x3000 float:1.7219E-41 double:6.071E-320;
        r0 = r19;
        r1.scanMtpFile(r12, r0, r2);
        goto L_0x0133;
    L_0x018c:
        r0 = r18;	 Catch:{ RemoteException -> 0x019c }
        r1 = r0.mMediaProvider;	 Catch:{ RemoteException -> 0x019c }
        r2 = "unhide";	 Catch:{ RemoteException -> 0x019c }
        r3 = r13.getParent();	 Catch:{ RemoteException -> 0x019c }
        r4 = 0;	 Catch:{ RemoteException -> 0x019c }
        r1.call(r2, r3, r4);	 Catch:{ RemoteException -> 0x019c }
        goto L_0x0133;
    L_0x019c:
        r9 = move-exception;
        r1 = "MtpDatabase";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "failed to unhide/rescan for ";
        r2 = r2.append(r3);
        r2 = r2.append(r12);
        r2 = r2.toString();
        android.util.Log.e(r1, r2);
        goto L_0x0133;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.renameFile(int, java.lang.String):int");
    }

    public MtpDatabase(Context context, String volumeName, String storagePath, String[] subDirectories) {
        this.mClosed = new AtomicBoolean();
        this.mCloseGuard = CloseGuard.get();
        this.mStorageMap = new HashMap();
        this.mPropertyGroupsByProperty = new HashMap();
        this.mPropertyGroupsByFormat = new HashMap();
        this.mBatteryReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                    MtpDatabase.this.mBatteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                    int newLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    if (newLevel != MtpDatabase.this.mBatteryLevel) {
                        MtpDatabase.this.mBatteryLevel = newLevel;
                        if (MtpDatabase.this.mServer != null) {
                            MtpDatabase.this.mServer.sendDevicePropertyChanged(MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL);
                        }
                    }
                }
            }
        };
        native_setup();
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        this.mMediaProvider = context.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        this.mVolumeName = volumeName;
        this.mMediaStoragePath = storagePath;
        this.mObjectsUri = Files.getMtpObjectsUri(volumeName).buildUpon().appendQueryParameter("nonotify", WifiEnterpriseConfig.ENGINE_ENABLE).build();
        this.mMediaScanner = new MediaScanner(context, this.mVolumeName);
        this.mSubDirectories = subDirectories;
        if (subDirectories != null) {
            int i;
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            int count = subDirectories.length;
            for (i = 0; i < count; i += DEVICE_PROPERTIES_DATABASE_VERSION) {
                builder.append("_data=? OR _data LIKE ?");
                if (i != count - 1) {
                    builder.append(" OR ");
                }
            }
            builder.append(")");
            this.mSubDirectoriesWhere = builder.toString();
            this.mSubDirectoriesWhereArgs = new String[(count * 2)];
            int j = 0;
            for (i = 0; i < count; i += DEVICE_PROPERTIES_DATABASE_VERSION) {
                String path = subDirectories[i];
                int i2 = j + DEVICE_PROPERTIES_DATABASE_VERSION;
                this.mSubDirectoriesWhereArgs[j] = path;
                j = i2 + DEVICE_PROPERTIES_DATABASE_VERSION;
                this.mSubDirectoriesWhereArgs[i2] = path + "/%";
            }
        }
        initDeviceProperties(context);
        this.mCloseGuard.open("close");
    }

    public void setServer(MtpServer server) {
        this.mServer = server;
        try {
            this.mContext.unregisterReceiver(this.mBatteryReceiver);
        } catch (IllegalArgumentException e) {
        }
        if (server != null) {
            this.mContext.registerReceiver(this.mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }

    public void close() {
        HwFrameworkFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            this.mMediaScanner.close();
            this.mMediaProvider.close();
            native_finalize();
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
            close();
        } finally {
            super.finalize();
        }
    }

    public void addStorage(MtpStorage storage) {
        this.mStorageMap.put(storage.getPath(), storage);
    }

    public void removeStorage(MtpStorage storage) {
        this.mStorageMap.remove(storage.getPath());
    }

    private void initDeviceProperties(Context context) {
        String devicePropertiesName = "device-properties";
        this.mDeviceProperties = context.getSharedPreferences("device-properties", 0);
        if (context.getDatabasePath("device-properties").exists()) {
            SQLiteDatabase sQLiteDatabase = null;
            Cursor cursor = null;
            try {
                sQLiteDatabase = context.openOrCreateDatabase("device-properties", 0, null);
                if (sQLiteDatabase != null) {
                    cursor = sQLiteDatabase.query("properties", new String[]{HwUserData._ID, "code", NameValueTable.VALUE}, null, null, null, null, null);
                    if (cursor != null) {
                        Editor e = this.mDeviceProperties.edit();
                        while (cursor.moveToNext()) {
                            e.putString(cursor.getString(DEVICE_PROPERTIES_DATABASE_VERSION), cursor.getString(2));
                        }
                        e.commit();
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
            } catch (Exception e2) {
                Log.e(TAG, "failed to migrate device properties", e2);
                context.deleteDatabase("device-properties");
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
            }
            context.deleteDatabase("device-properties");
        }
    }

    private boolean inStorageSubDirectory(String path) {
        if (this.mSubDirectories == null) {
            return true;
        }
        if (path == null) {
            return false;
        }
        boolean allowed = false;
        int pathLength = path.length();
        for (int i = 0; i < this.mSubDirectories.length && !allowed; i += DEVICE_PROPERTIES_DATABASE_VERSION) {
            String subdir = this.mSubDirectories[i];
            int subdirLength = subdir.length();
            if (subdirLength < pathLength && path.charAt(subdirLength) == '/' && path.startsWith(subdir)) {
                allowed = true;
            }
        }
        return allowed;
    }

    private boolean isStorageSubDirectory(String path) {
        if (this.mSubDirectories == null) {
            return false;
        }
        for (int i = 0; i < this.mSubDirectories.length; i += DEVICE_PROPERTIES_DATABASE_VERSION) {
            if (path.equals(this.mSubDirectories[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean inStorageRoot(String path) {
        try {
            String canonical = new File(path).getCanonicalPath();
            for (String root : this.mStorageMap.keySet()) {
                if (canonical.startsWith(root)) {
                    return true;
                }
            }
        } catch (IOException e) {
        }
        return false;
    }

    private int beginSendObject(String path, int format, int parent, int storageId, long size, long modified) {
        if (!inStorageRoot(path)) {
            Log.e(TAG, "attempt to put file outside of storage area: " + path);
            return -1;
        } else if (!inStorageSubDirectory(path)) {
            return -1;
        } else {
            if (path != null) {
                Cursor cursor = null;
                try {
                    ContentProviderClient contentProviderClient = this.mMediaProvider;
                    Uri uri = this.mObjectsUri;
                    String[] strArr = ID_PROJECTION;
                    String str = PATH_WHERE;
                    String[] strArr2 = new String[DEVICE_PROPERTIES_DATABASE_VERSION];
                    strArr2[0] = path;
                    cursor = contentProviderClient.query(uri, strArr, str, strArr2, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        Log.w(TAG, "file already exists in beginSendObject: " + path);
                        int hwBeginSendObject = HwFrameworkFactory.getHwMtpDatabaseManager().hwBeginSendObject(path, cursor);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return hwBeginSendObject;
                    } else if (cursor != null) {
                        cursor.close();
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in beginSendObject", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            this.mDatabaseModified = true;
            ContentValues values = new ContentValues();
            values.put(Voicemails._DATA, path);
            values.put(FileColumns.FORMAT, Integer.valueOf(format));
            values.put(FileColumns.PARENT, Integer.valueOf(parent));
            values.put(FileColumns.STORAGE_ID, Integer.valueOf(storageId));
            values.put(OpenableColumns.SIZE, Long.valueOf(size));
            values.put(PlaylistsColumns.DATE_MODIFIED, Long.valueOf(modified));
            try {
                Uri uri2 = this.mMediaProvider.insert(this.mObjectsUri, values);
                if (uri2 != null) {
                    return Integer.parseInt((String) uri2.getPathSegments().get(2));
                }
                return -1;
            } catch (RemoteException e2) {
                Log.e(TAG, "RemoteException in beginSendObject", e2);
                return -1;
            }
        }
    }

    private void endSendObject(String path, int handle, int format, boolean succeeded) {
        if (!succeeded) {
            deleteFile(handle);
        } else if (format == MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST) {
            String name = path;
            int lastSlash = path.lastIndexOf(47);
            if (lastSlash >= 0) {
                name = path.substring(lastSlash + DEVICE_PROPERTIES_DATABASE_VERSION);
            }
            if (name.endsWith(".pla")) {
                name = name.substring(0, name.length() - 4);
            }
            ContentValues values = new ContentValues((int) DEVICE_PROPERTIES_DATABASE_VERSION);
            values.put(Voicemails._DATA, path);
            values.put(KeyChain.EXTRA_NAME, name);
            values.put(FileColumns.FORMAT, Integer.valueOf(format));
            values.put(PlaylistsColumns.DATE_MODIFIED, Long.valueOf(System.currentTimeMillis() / 1000));
            values.put(MediaColumns.MEDIA_SCANNER_NEW_OBJECT_ID, Integer.valueOf(handle));
            try {
                Uri insert = this.mMediaProvider.insert(Playlists.EXTERNAL_CONTENT_URI, values);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in endSendObject", e);
            }
        } else {
            this.mMediaScanner.scanMtpFile(path, handle, format);
        }
    }

    private Cursor createObjectQuery(int storageID, int format, int parent) throws RemoteException {
        String where;
        String[] strArr;
        if (storageID == -1) {
            if (format == 0) {
                if (parent == 0) {
                    where = null;
                    strArr = null;
                } else {
                    if (parent == -1) {
                        parent = 0;
                    }
                    where = PARENT_WHERE;
                    strArr = new String[DEVICE_PROPERTIES_DATABASE_VERSION];
                    strArr[0] = Integer.toString(parent);
                }
            } else if (parent == 0) {
                where = FORMAT_WHERE;
                strArr = new String[DEVICE_PROPERTIES_DATABASE_VERSION];
                strArr[0] = Integer.toString(format);
            } else {
                if (parent == -1) {
                    parent = 0;
                }
                where = FORMAT_PARENT_WHERE;
                strArr = new String[]{Integer.toString(format), Integer.toString(parent)};
            }
        } else if (format == 0) {
            if (parent == 0) {
                where = STORAGE_WHERE;
                strArr = new String[DEVICE_PROPERTIES_DATABASE_VERSION];
                strArr[0] = Integer.toString(storageID);
            } else {
                if (parent == -1) {
                    parent = 0;
                }
                where = STORAGE_PARENT_WHERE;
                strArr = new String[]{Integer.toString(storageID), Integer.toString(parent)};
            }
        } else if (parent == 0) {
            where = STORAGE_FORMAT_WHERE;
            strArr = new String[]{Integer.toString(storageID), Integer.toString(format)};
        } else {
            if (parent == -1) {
                parent = 0;
            }
            where = STORAGE_FORMAT_PARENT_WHERE;
            strArr = new String[]{Integer.toString(storageID), Integer.toString(format), Integer.toString(parent)};
        }
        if (this.mSubDirectoriesWhere != null) {
            if (where == null) {
                where = this.mSubDirectoriesWhere;
                strArr = this.mSubDirectoriesWhereArgs;
            } else {
                where = where + " AND " + this.mSubDirectoriesWhere;
                String[] newWhereArgs = new String[(strArr.length + this.mSubDirectoriesWhereArgs.length)];
                int i = 0;
                while (i < strArr.length) {
                    newWhereArgs[i] = strArr[i];
                    i += DEVICE_PROPERTIES_DATABASE_VERSION;
                }
                for (int j = 0; j < this.mSubDirectoriesWhereArgs.length; j += DEVICE_PROPERTIES_DATABASE_VERSION) {
                    newWhereArgs[i] = this.mSubDirectoriesWhereArgs[j];
                    i += DEVICE_PROPERTIES_DATABASE_VERSION;
                }
                strArr = newWhereArgs;
            }
        }
        return this.mMediaProvider.query(this.mObjectsUri, ID_PROJECTION, where, strArr, null, null);
    }

    private int[] getObjectList(int storageID, int format, int parent) {
        Cursor cursor = null;
        try {
            cursor = createObjectQuery(storageID, format, parent);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            int count = cursor.getCount();
            if (count > 0) {
                int[] result = new int[count];
                for (int i = 0; i < count; i += DEVICE_PROPERTIES_DATABASE_VERSION) {
                    cursor.moveToNext();
                    result[i] = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
                return result;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getNumObjects(int storageID, int format, int parent) {
        Cursor cursor = null;
        try {
            cursor = createObjectQuery(storageID, format, parent);
            if (cursor != null) {
                int count = cursor.getCount();
                if (cursor != null) {
                    cursor.close();
                }
                return count;
            }
            if (cursor != null) {
                cursor.close();
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getNumObjects", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int[] getSupportedPlaybackFormats() {
        return new int[]{GLES11.GL_CLIP_PLANE0, GLES11.GL_CLIP_PLANE1, GLES11.GL_CLIP_PLANE4, GLES11.GL_CLIP_PLANE5, EGL14.EGL_BAD_DISPLAY, EGL14.EGL_BAD_MATCH, EGL14.EGL_BAD_NATIVE_WINDOW, MtpConstants.FORMAT_EXIF_JPEG, MtpConstants.FORMAT_TIFF_EP, MtpConstants.FORMAT_BMP, MtpConstants.FORMAT_GIF, MtpConstants.FORMAT_JFIF, MtpConstants.FORMAT_PNG, MtpConstants.FORMAT_TIFF, MtpConstants.FORMAT_WMA, MtpConstants.FORMAT_OGG, MtpConstants.FORMAT_AAC, MtpConstants.FORMAT_MP4_CONTAINER, MtpConstants.FORMAT_MP2, MtpConstants.FORMAT_3GP_CONTAINER, MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST, MtpConstants.FORMAT_WPL_PLAYLIST, MtpConstants.FORMAT_M3U_PLAYLIST, MtpConstants.FORMAT_PLS_PLAYLIST, MtpConstants.FORMAT_XML_DOCUMENT, MtpConstants.FORMAT_FLAC, MtpConstants.FORMAT_DNG, MtpConstants.FORMAT_BMP};
    }

    private int[] getSupportedCaptureFormats() {
        return null;
    }

    public int[] getSupportedObjectProperties(int format) {
        switch (format) {
            case EGL14.EGL_BAD_DISPLAY /*12296*/:
            case EGL14.EGL_BAD_MATCH /*12297*/:
            case MtpConstants.FORMAT_WMA /*47361*/:
            case MtpConstants.FORMAT_OGG /*47362*/:
            case MtpConstants.FORMAT_AAC /*47363*/:
                return (int[]) AUDIO_PROPERTIES.clone();
            case EGL14.EGL_BAD_NATIVE_WINDOW /*12299*/:
            case MtpConstants.FORMAT_WMV /*47489*/:
            case MtpConstants.FORMAT_3GP_CONTAINER /*47492*/:
                return (int[]) VIDEO_PROPERTIES.clone();
            case MtpConstants.FORMAT_EXIF_JPEG /*14337*/:
            case MtpConstants.FORMAT_BMP /*14340*/:
            case MtpConstants.FORMAT_GIF /*14343*/:
            case MtpConstants.FORMAT_PNG /*14347*/:
            case MtpConstants.FORMAT_DNG /*14353*/:
                return (int[]) IMAGE_PROPERTIES.clone();
            default:
                return (int[]) FILE_PROPERTIES.clone();
        }
    }

    private int[] getSupportedDeviceProperties() {
        return new int[]{MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER, MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME, MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE, MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL};
    }

    private void cacheObjectAllInfos(int handle) {
        if (handle > 0 && HwFrameworkFactory.getHwMtpDatabaseManager().hwGetSavedObjectHandle() != handle) {
            Cursor cursor = null;
            try {
                ContentProviderClient contentProviderClient = this.mMediaProvider;
                Uri uri = this.mObjectsUri;
                String[] strArr = OBJECT_ALL_COLUMNS_PROJECTION;
                String str = ID_WHERE;
                String[] strArr2 = new String[DEVICE_PROPERTIES_DATABASE_VERSION];
                strArr2[0] = Integer.toString(handle);
                cursor = contentProviderClient.query(uri, strArr, str, strArr2, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    HwFrameworkFactory.getHwMtpDatabaseManager().hwSaveCurrentObject(cursor);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in cacheObjectAllInfos", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private MtpPropertyList getObjectPropertyList(int handle, int format, int property, int groupCode, int depth) {
        MtpPropertyList result;
        MtpPropertyGroup propertyGroup;
        if (depth == 0) {
            cacheObjectAllInfos(handle);
            result = HwFrameworkFactory.getHwMtpDatabaseManager().getObjectPropertyList(this, handle, format, property, groupCode);
            if (result != null) {
                return result;
            }
        }
        if (property == -1) {
            if (!(format != 0 || handle == 0 || handle == -1)) {
                format = getObjectFormat(handle);
            }
            if (depth == 0 && handle != -1) {
                result = HwFrameworkFactory.getHwMtpDatabaseManager().getObjectPropertyList(handle, format, getSupportedObjectProperties(format));
                if (result != null) {
                    return result;
                }
            }
            propertyGroup = (MtpPropertyGroup) this.mPropertyGroupsByFormat.get(Integer.valueOf(format));
            if (propertyGroup == null) {
                propertyGroup = new MtpPropertyGroup(this, this.mMediaProvider, this.mVolumeName, getSupportedObjectProperties(format));
                this.mPropertyGroupsByFormat.put(Integer.valueOf(format), propertyGroup);
            }
        } else {
            if (depth == 0 && handle != -1) {
                int[] propList = new int[DEVICE_PROPERTIES_DATABASE_VERSION];
                propList[0] = property;
                result = HwFrameworkFactory.getHwMtpDatabaseManager().getObjectPropertyList(handle, format, propList);
                if (result != null) {
                    return result;
                }
            }
            propertyGroup = (MtpPropertyGroup) this.mPropertyGroupsByProperty.get(Integer.valueOf(property));
            if (propertyGroup == null) {
                int[] propertyList = new int[DEVICE_PROPERTIES_DATABASE_VERSION];
                propertyList[0] = property;
                propertyGroup = new MtpPropertyGroup(this, this.mMediaProvider, this.mVolumeName, propertyList);
                this.mPropertyGroupsByProperty.put(Integer.valueOf(property), propertyGroup);
            }
        }
        return propertyGroup.getPropertyList(handle, format, depth);
    }

    private int setObjectProperty(int handle, int property, long intValue, String stringValue) {
        if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetSavedObjectHandle() == handle) {
            HwFrameworkFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        }
        switch (property) {
            case MtpConstants.PROPERTY_OBJECT_FILE_NAME /*56327*/:
                return renameFile(handle, stringValue);
            default:
                return MtpConstants.RESPONSE_OBJECT_PROP_NOT_SUPPORTED;
        }
    }

    private int getDeviceProperty(int property, long[] outIntValue, char[] outStringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE /*20483*/:
                Display display = ((WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW)).getDefaultDisplay();
                String imageSize = Integer.toString(display.getMaximumSizeDimension()) + "x" + Integer.toString(display.getMaximumSizeDimension());
                imageSize.getChars(0, imageSize.length(), outStringValue, 0);
                outStringValue[imageSize.length()] = '\u0000';
                return MtpConstants.RESPONSE_OK;
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER /*54273*/:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME /*54274*/:
                String value = this.mDeviceProperties.getString(Integer.toString(property), ProxyInfo.LOCAL_EXCL_LIST);
                if (property == MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME) {
                    if (Global.getInt(this.mContext.getContentResolver(), SettingsEx.Global.DB_KEY_UNIFIED_DEVICE_NAME_UPDATED, 0) == DEVICE_PROPERTIES_DATABASE_VERSION) {
                        value = Global.getString(this.mContext.getContentResolver(), SettingsEx.Global.DB_KEY_UNIFIED_DEVICE_NAME);
                        Global.putInt(this.mContext.getContentResolver(), SettingsEx.Global.DB_KEY_UNIFIED_DEVICE_NAME_UPDATED, 0);
                        Editor e = this.mDeviceProperties.edit();
                        e.putString(Integer.toString(property), value);
                        e.commit();
                    }
                    if (value == null || value.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
                        value = SystemProperties.get("ro.config.marketing_name");
                    }
                    if (value == null || value.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
                        value = SystemProperties.get("ro.product.model");
                    }
                }
                int length = value.length();
                if (length > Process.PROC_TERM_MASK) {
                    length = Process.PROC_TERM_MASK;
                }
                value.getChars(0, length, outStringValue, 0);
                outStringValue[length] = '\u0000';
                return MtpConstants.RESPONSE_OK;
            default:
                return MtpConstants.RESPONSE_DEVICE_PROP_NOT_SUPPORTED;
        }
    }

    private int setDeviceProperty(int property, long intValue, String stringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER /*54273*/:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME /*54274*/:
                int i;
                Editor e = this.mDeviceProperties.edit();
                e.putString(Integer.toString(property), stringValue);
                if (e.commit()) {
                    i = MtpConstants.RESPONSE_OK;
                } else {
                    i = MtpConstants.RESPONSE_GENERAL_ERROR;
                }
                return i;
            default:
                return MtpConstants.RESPONSE_DEVICE_PROP_NOT_SUPPORTED;
        }
    }

    private boolean getObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outCreatedModified) {
        cacheObjectAllInfos(handle);
        if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetObjectInfo(handle, outStorageFormatParent, outName, outCreatedModified)) {
            return true;
        }
        Cursor cursor = null;
        try {
            ContentProviderClient contentProviderClient = this.mMediaProvider;
            Uri uri = this.mObjectsUri;
            String[] strArr = OBJECT_INFO_PROJECTION;
            String str = ID_WHERE;
            String[] strArr2 = new String[DEVICE_PROPERTIES_DATABASE_VERSION];
            strArr2[0] = Integer.toString(handle);
            cursor = contentProviderClient.query(uri, strArr, str, strArr2, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            outStorageFormatParent[0] = cursor.getInt(DEVICE_PROPERTIES_DATABASE_VERSION);
            outStorageFormatParent[DEVICE_PROPERTIES_DATABASE_VERSION] = cursor.getInt(2);
            outStorageFormatParent[2] = cursor.getInt(3);
            String path = cursor.getString(4);
            int lastSlash = path.lastIndexOf(47);
            int start = lastSlash >= 0 ? lastSlash + DEVICE_PROPERTIES_DATABASE_VERSION : 0;
            int end = path.length();
            if (end - start > Process.PROC_TERM_MASK) {
                end = start + Process.PROC_TERM_MASK;
            }
            path.getChars(start, end, outName, 0);
            outName[end - start] = '\u0000';
            outCreatedModified[0] = cursor.getLong(5);
            outCreatedModified[DEVICE_PROPERTIES_DATABASE_VERSION] = cursor.getLong(6);
            if (outCreatedModified[0] == 0) {
                outCreatedModified[0] = outCreatedModified[DEVICE_PROPERTIES_DATABASE_VERSION];
            }
            if (cursor != null) {
                cursor.close();
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectInfo", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int[] getObjectReferences(int handle) {
        if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetObjectReferences(handle)) {
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(Files.getMtpReferencesUri(this.mVolumeName, (long) handle).buildUpon().appendQueryParameter("nonotify", WifiEnterpriseConfig.ENGINE_ENABLE).build(), ID_PROJECTION, null, null, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            int count = cursor.getCount();
            if (count > 0) {
                int[] result = new int[count];
                for (int i = 0; i < count; i += DEVICE_PROPERTIES_DATABASE_VERSION) {
                    cursor.moveToNext();
                    result[i] = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
                return result;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int setObjectReferences(int handle, int[] references) {
        this.mDatabaseModified = true;
        Uri uri = Files.getMtpReferencesUri(this.mVolumeName, (long) handle);
        int count = references.length;
        ContentValues[] valuesList = new ContentValues[count];
        for (int i = 0; i < count; i += DEVICE_PROPERTIES_DATABASE_VERSION) {
            ContentValues values = new ContentValues();
            values.put(HwUserData._ID, Integer.valueOf(references[i]));
            valuesList[i] = values;
        }
        try {
            if (this.mMediaProvider.bulkInsert(uri, valuesList) > 0) {
                return MtpConstants.RESPONSE_OK;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setObjectReferences", e);
        }
        return MtpConstants.RESPONSE_GENERAL_ERROR;
    }

    private void sessionStarted() {
        this.mDatabaseModified = false;
    }

    private void sessionEnded() {
        if (this.mDatabaseModified) {
            this.mContext.sendBroadcast(new Intent(MediaStore.ACTION_MTP_SESSION_END));
            this.mDatabaseModified = false;
        }
        HwFrameworkFactory.getHwMtpDatabaseManager().hwClearSavedObject();
    }
}
