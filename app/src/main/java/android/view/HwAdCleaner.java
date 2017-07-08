package android.view;

import android.content.res.Resources.NotFoundException;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.hsm.permission.StubController;
import java.util.ArrayList;
import java.util.List;

public class HwAdCleaner implements IHwAdCleaner {
    private static final int CODE_GET_AD_KEY_LIST = 1016;
    private static final String DESCRIPTOR_HW_AD_CLEANER = "android.view.HwAdCleaner";
    private static final String TAG = "HwAdCleaner";
    private static final int VIEW_IS_ADVIEW = 0;
    private static final int VIEW_IS_NOT_ADVIEW_OR_DISABLE = 1;
    private static final int VIEW_IS_NOT_CHECKED = -1;
    private static HwAdCleaner mInstance;
    private List<String> mAdIdList;
    private List<String> mAdViewList;
    private String mApkName;
    private boolean mEnableAdChecked;
    private boolean mEnableDebug;
    private boolean mInit;
    private boolean mRulesHasRead;

    /* renamed from: android.view.HwAdCleaner.1 */
    class AnonymousClass1 extends Thread {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void run() {
            if (HwAdCleaner.this.mEnableDebug) {
                Log.d(HwAdCleaner.TAG, "  2. Thread readAdFilterRules was created!  mApkName = " + HwAdCleaner.this.mApkName);
            }
            HwAdCleaner.this.mEnableAdChecked = HwAdCleaner.this.doReadRulesByBinder();
            HwAdCleaner.this.mRulesHasRead = true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.HwAdCleaner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.HwAdCleaner.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.HwAdCleaner.<clinit>():void");
    }

    private HwAdCleaner() {
        this.mAdViewList = null;
        this.mAdIdList = null;
        this.mApkName = "none";
        this.mEnableAdChecked = true;
        this.mEnableDebug = false;
        this.mRulesHasRead = false;
        this.mInit = true;
        this.mAdViewList = new ArrayList();
        this.mAdIdList = new ArrayList();
        int prop = SystemProperties.getInt("enable.view.adcleaner", VIEW_IS_NOT_ADVIEW_OR_DISABLE);
        if (prop == 0) {
            this.mEnableAdChecked = false;
        } else if (2 == prop) {
            this.mEnableDebug = true;
            Log.d(TAG, "1.HwAdCleaner was created!");
        }
    }

    public static synchronized HwAdCleaner getDefault() {
        HwAdCleaner hwAdCleaner;
        synchronized (HwAdCleaner.class) {
            if (mInstance == null) {
                mInstance = new HwAdCleaner();
            }
            hwAdCleaner = mInstance;
        }
        return hwAdCleaner;
    }

    public void readRulesInThread() {
        new AnonymousClass1("readAdFilterRules").start();
    }

    public boolean doReadRulesByBinder() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        if (b != null) {
            try {
                if (this.mEnableDebug) {
                    Log.d(TAG, "     3. getService NETWORKMANAGEMENT_SERVICE success!");
                }
                _data.writeInterfaceToken(DESCRIPTOR_HW_AD_CLEANER);
                _data.writeString(this.mApkName);
                b.transact(CODE_GET_AD_KEY_LIST, _data, _reply, VIEW_IS_ADVIEW);
                this.mAdViewList.clear();
                this.mAdIdList.clear();
                this.mAdViewList = _reply.createStringArrayList();
                this.mAdIdList = _reply.createStringArrayList();
                if (this.mEnableDebug) {
                    int i;
                    Log.d(TAG, "       4.1 mAdViewList size = " + this.mAdViewList.size());
                    for (i = VIEW_IS_ADVIEW; i < this.mAdViewList.size(); i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                        Log.d(TAG, "          i=" + i + ", value = " + ((String) this.mAdViewList.get(i)));
                    }
                    Log.d(TAG, "       4.2 mAdIdList size = " + this.mAdIdList.size());
                    for (i = VIEW_IS_ADVIEW; i < this.mAdIdList.size(); i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                        Log.d(TAG, "          i=" + i + ", value = " + ((String) this.mAdIdList.get(i)));
                    }
                }
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                Log.d(TAG, "RemoteException ");
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
        if (this.mAdViewList.isEmpty() && this.mAdIdList.isEmpty()) {
            return false;
        }
        return true;
    }

    public int checkAdCleaner(View inView, String appName, int uid) {
        if (!this.mEnableAdChecked) {
            return VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        if (appName == null || inView == null) {
            Log.e(TAG, "err: AppName/inview is null !");
            return VIEW_IS_NOT_CHECKED;
        }
        if (this.mInit) {
            this.mInit = false;
            this.mApkName = appName;
            if (this.mApkName.contains(StubController.APP_GOOGLE) || this.mApkName.contains("huawei")) {
                this.mEnableAdChecked = false;
                return VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            }
            readRulesInThread();
        }
        if (!this.mRulesHasRead) {
            return VIEW_IS_NOT_CHECKED;
        }
        String viewIdName;
        String viewClsName = inView.getClass().getName();
        try {
            viewIdName = inView.getContext().getResources().getResourceEntryName(inView.getId());
        } catch (NotFoundException e) {
            viewIdName = "none";
        }
        if (this.mEnableDebug) {
            Log.d(TAG, "             viewName = " + viewClsName + "@" + viewIdName);
        }
        if (!(viewClsName == null || this.mAdViewList == null)) {
            for (int i = VIEW_IS_ADVIEW; i < this.mAdViewList.size(); i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                String temp = (String) this.mAdViewList.get(i);
                boolean isMatched = false;
                if (temp.contains("@")) {
                    if (temp.equals(viewClsName + "@" + viewIdName)) {
                        isMatched = true;
                    }
                } else if (temp.equals(viewClsName)) {
                    isMatched = true;
                }
                if (isMatched) {
                    if (this.mEnableDebug) {
                        Log.d(TAG, "           5.View = " + temp + " matched !");
                    }
                    return VIEW_IS_ADVIEW;
                }
            }
        }
        if (viewIdName == null || this.mAdIdList == null || !this.mAdIdList.contains(viewIdName)) {
            return VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        if (this.mEnableDebug) {
            Log.d(TAG, "           5.Id = " + viewIdName + " matched !");
        }
        return VIEW_IS_ADVIEW;
    }
}
