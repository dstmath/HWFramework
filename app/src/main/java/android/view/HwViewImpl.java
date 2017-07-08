package android.view;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.FastgrabConfigReader;
import android.util.BoostFramework;
import android.util.Jlog;
import com.huawei.android.bastet.IBastetManager;
import com.huawei.android.bastet.IBastetManager.Stub;

public class HwViewImpl implements IHwView {
    private static final String ATTR_ANIMATION_VIEW = "animationRootView";
    private static final String ATTR_BOOST_VIEW = "boostRootView";
    private static final String ATTR_SWITCH = "switch";
    private static final String TAG = "HwViewImpl";
    private static HwViewImpl mInstance;
    private int[] mHongbaoBoostParam;
    private int mHongbaoBoostTimeOut;
    private boolean mIsHongbaoBoostEnabled;
    private boolean mIsResLoaded;
    private BoostFramework mPerf;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.HwViewImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.HwViewImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.HwViewImpl.<clinit>():void");
    }

    public static synchronized HwViewImpl getDefault() {
        HwViewImpl hwViewImpl;
        synchronized (HwViewImpl.class) {
            if (mInstance == null) {
                mInstance = new HwViewImpl();
            }
            hwViewImpl = mInstance;
        }
        return hwViewImpl;
    }

    private HwViewImpl() {
    }

    public boolean cancelAnimation(View view, Context context) {
        if (!checkView(view, context, ATTR_ANIMATION_VIEW)) {
            return false;
        }
        AwareLog.i(TAG, "LuckyMoney Animation Canceled !");
        return true;
    }

    public void onClick(View view, Context context) {
        if (checkView(view, context, ATTR_BOOST_VIEW)) {
            cpuBoost(context);
        }
    }

    private boolean checkView(View view, Context context, String tagName) {
        if (view == null) {
            return false;
        }
        FastgrabConfigReader fastgrabConfigReader = FastgrabConfigReader.getInstance(context);
        if (fastgrabConfigReader != null && fastgrabConfigReader.getInt(ATTR_SWITCH) == 1) {
            String rootView = fastgrabConfigReader.getString(tagName);
            if (!(rootView == null || rootView.isEmpty())) {
                View root = view.getRootView();
                return root != null && root.toString().contains(rootView);
            }
        }
    }

    private void cpuBoost(Context context) {
        if (context != null) {
            IBastetManager bastet = Stub.asInterface(ServiceManager.getService("BastetService"));
            if (bastet != null) {
                try {
                    ApplicationInfo ai = context.getApplicationInfo();
                    if (ai != null) {
                        bastet.indicateAction(0, 100, ai.uid);
                        AwareLog.i(TAG, "cpu boost on uid = " + ai.uid);
                    }
                } catch (RemoteException e) {
                    AwareLog.e(TAG, "failed calling BastetService");
                }
                return;
            }
            if (!this.mIsResLoaded) {
                this.mIsHongbaoBoostEnabled = context.getResources().getBoolean(17957050);
                if (this.mIsHongbaoBoostEnabled) {
                    this.mHongbaoBoostTimeOut = context.getResources().getInteger(17694931);
                    this.mHongbaoBoostParam = context.getResources().getIntArray(17236065);
                    if (this.mPerf == null) {
                        this.mPerf = new BoostFramework();
                    }
                }
                this.mIsResLoaded = true;
            }
            if (this.mIsHongbaoBoostEnabled) {
                if (this.mPerf != null) {
                    this.mPerf.perfLockAcquire(this.mHongbaoBoostTimeOut, this.mHongbaoBoostParam);
                }
                AwareLog.i(TAG, "cpu boost via perflock for " + context.getPackageName());
            } else {
                Jlog.perfEvent(12, "", new int[0]);
                AwareLog.i(TAG, "cpu boost via perfhub for " + context.getPackageName());
            }
        }
    }
}
