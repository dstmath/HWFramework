package android.content.pm;

import android.app.ActivityManagerNative;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.PackageParserException;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.huawei.hsm.permission.StubController;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwPackageParser implements IHwPackageParser {
    private static final String APP_NAME = "app";
    private static final String ATTR_NAME = "name";
    private static final String CUST_FILE_DIR = "system/etc";
    private static final String CUST_FILE_NAME = "benchmar_app.xml";
    private static final boolean FASTBOOT_UNLOCK = false;
    private static final boolean HIDE_PRODUCT_INFO = false;
    public static final boolean IS_SUPPORT_CLONE_APP = false;
    private static final int MAX_NUM = 500;
    private static final String TAG = "BENCHMAR_APP";
    private static final String TAG_ARRAY = "array";
    private static final String TAG_ARRAYITEM = "value";
    private static final String TAG_DEVICE = "device";
    private static final String TAG_ITEM = "item";
    private static Set<String> mBenchmarkApp;
    private static HwPackageParser mInstance;
    private static final Object mInstanceLock = null;
    private static final Object mLock = null;
    private static final HashMap<String, Object> sAppMap = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.HwPackageParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.HwPackageParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.HwPackageParser.<clinit>():void");
    }

    protected void HwPackageParser() {
    }

    public static HwPackageParser getDefault() {
        if (mInstance == null) {
            synchronized (mInstanceLock) {
                if (mInstance == null) {
                    mInstance = new HwPackageParser();
                }
            }
        }
        return mInstance;
    }

    public void initMetaData(Activity a) {
        String navigationHide = a.metaData.getString("hwc-navi");
        if (navigationHide == null) {
            return;
        }
        if (navigationHide.startsWith("ro.config")) {
            a.info.navigationHide = SystemProperties.getBoolean(navigationHide, IS_SUPPORT_CLONE_APP);
            return;
        }
        a.info.navigationHide = true;
    }

    private boolean readBenchmarkAppFromXml(HashMap<String, Object> sMap, String fileDir, String fileName) {
        Throwable th;
        File mFile = new File(fileDir, fileName);
        InputStream inputStream = null;
        if (!mFile.exists()) {
            return IS_SUPPORT_CLONE_APP;
        }
        if (mFile.canRead()) {
            try {
                InputStream inputStream2 = new FileInputStream(mFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(inputStream2, null);
                    boolean parsingArray = IS_SUPPORT_CLONE_APP;
                    ArrayList<String> array = new ArrayList();
                    Object arrayName = null;
                    int i = 0;
                    while (true) {
                        int i2 = i + 1;
                        if (i <= MAX_NUM) {
                            XmlUtils.nextElement(parser);
                            String element = parser.getName();
                            if (element == null) {
                                break;
                            }
                            if (parsingArray) {
                                if (!element.equals(TAG_ARRAYITEM)) {
                                    sMap.put(arrayName, array.toArray(new String[array.size()]));
                                    parsingArray = IS_SUPPORT_CLONE_APP;
                                }
                            }
                            if (element.equals(TAG_ARRAY)) {
                                parsingArray = true;
                                array.clear();
                                arrayName = parser.getAttributeValue(null, ATTR_NAME);
                            } else {
                                if (!element.equals(TAG_ITEM)) {
                                    if (!element.equals(TAG_ARRAYITEM)) {
                                        continue;
                                    }
                                }
                                Object name = null;
                                if (!parsingArray) {
                                    name = parser.getAttributeValue(null, ATTR_NAME);
                                }
                                if (parser.next() == 4) {
                                    String value = parser.getText();
                                    if (element.equals(TAG_ITEM)) {
                                        sMap.put(name, value);
                                    } else if (parsingArray) {
                                        array.add(value);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                            }
                            i = i2;
                        } else {
                            break;
                        }
                    }
                    if (parsingArray) {
                        sMap.put(arrayName, array.toArray(new String[array.size()]));
                    }
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e) {
                            Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                        }
                    }
                    inputStream = inputStream2;
                } catch (XmlPullParserException e2) {
                    inputStream = inputStream2;
                } catch (IOException e3) {
                    inputStream = inputStream2;
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = inputStream2;
                }
            } catch (XmlPullParserException e4) {
                try {
                    Log.w(TAG, "readBenchmarkAppFromXml  XmlPullParserException");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e5) {
                            Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                        }
                    }
                    Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                    return true;
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                            Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                        }
                    }
                    throw th;
                }
            } catch (IOException e7) {
                Log.w(TAG, "readBenchmarkAppFromXml  IOException");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                        Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                    }
                }
                Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                return true;
            }
            Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
            return true;
        }
        Log.w(TAG, "benchmar_app.xml not found! name maybe not right!");
        return IS_SUPPORT_CLONE_APP;
    }

    public void needStopApp(String packageName, File packageDir) throws PackageParserException {
        if (HIDE_PRODUCT_INFO || FASTBOOT_UNLOCK) {
            synchronized (mLock) {
                if (mBenchmarkApp == null) {
                    mBenchmarkApp = new HashSet();
                    readBenchmarkAppFromXml(sAppMap, CUST_FILE_DIR, CUST_FILE_NAME);
                    String[] BenchmarkApp = (String[]) sAppMap.get(APP_NAME);
                    if (BenchmarkApp != null) {
                        for (Object add : BenchmarkApp) {
                            mBenchmarkApp.add(add);
                        }
                    }
                }
                for (String appName : mBenchmarkApp) {
                    if (packageName.contains(appName)) {
                        throw new PackageParserException(-2, "Inconsistent package " + packageName + " in " + packageDir);
                    }
                }
            }
        }
    }

    public void changeApplicationEuidIfNeeded(ApplicationInfo ai, int flags) {
        if (IS_SUPPORT_CLONE_APP && (StubController.PERMISSION_MOBILEDATE & flags) != 0 && isPackageCloned(ai.packageName, UserHandle.getUserId(ai.uid))) {
            Log.i(TAG, "generateApplicationInfo for cloned app: " + ai.packageName);
            ai.euid = 2147383647;
            String str = ai.deviceProtectedDataDir + File.separator + "_hwclone";
            ai.deviceProtectedDataDir = str;
            ai.deviceEncryptedDataDir = str;
            str = ai.credentialProtectedDataDir + File.separator + "_hwclone";
            ai.credentialProtectedDataDir = str;
            ai.credentialEncryptedDataDir = str;
            ai.dataDir += File.separator + "_hwclone";
        }
    }

    private static boolean isPackageCloned(String packageName, int userId) {
        boolean res = IS_SUPPORT_CLONE_APP;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeString(packageName);
            data.writeInt(userId);
            ActivityManagerNative.getDefault().asBinder().transact(505, data, reply, 0);
            reply.readException();
            res = reply.readInt() != 0 ? true : IS_SUPPORT_CLONE_APP;
            data.recycle();
            reply.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "isPackageCloned", e);
        }
        return res;
    }
}
