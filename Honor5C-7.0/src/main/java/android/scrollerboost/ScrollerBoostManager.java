package android.scrollerboost;

import android.app.admin.DevicePolicyManager;
import android.content.pm.PackageManager;
import android.net.NetworkPolicyManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.provider.DocumentsContract.Root;
import android.rms.iaware.AwareLog;
import android.util.Jlog;

public class ScrollerBoostManager {
    private static final int BOOST_B_CPU_MIN_FREQ = 1805000;
    private static final int BOOST_DURATION = 1000;
    private static final long BOOST_TIME_LENGTH = 1000;
    private static final int DEFAULT_ENABLE_SKIPPED_FRAMES = 0;
    private static final int IPA_MAX_POWER = 3500;
    private static final int MESSAGE_RESET_AFFINITY = 1;
    private static final int SWITCH_SCROLLER_BOOST = 8;
    private static final String TAG = "ScrollerBoostManager";
    private static ScrollerBoostManager sScrollerBoostManager;
    private boolean mBoostByEachFling;
    private int mBoostCpuMinFreq;
    private int mBoostDefaultDuration;
    private int mBoostDuration;
    private boolean mBoostSwitch;
    private boolean mEnableBoostByJank;
    private long mEnableSkippedFrames;
    private int mIPAMaxPower;
    private boolean mIsBigcoreBoost;
    private long mLastBoostTime;
    private Handler mResetAffinityHandler;

    private static class ResetAffinityHandler extends Handler {
        private ResetAffinityHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ScrollerBoostManager.MESSAGE_RESET_AFFINITY) {
                int pid = Process.myPid();
                if (pid > 0) {
                    try {
                        Process.setProcessAffinity(pid, Process.PROC_TERM_MASK);
                    } catch (RuntimeException e) {
                        AwareLog.e(ScrollerBoostManager.TAG, "Exception in invoke: " + e.getMessage());
                    }
                }
            }
        }
    }

    private ScrollerBoostManager() {
        this.mLastBoostTime = 0;
        this.mEnableSkippedFrames = 0;
        this.mResetAffinityHandler = new ResetAffinityHandler();
    }

    public static synchronized ScrollerBoostManager getInstance() {
        ScrollerBoostManager scrollerBoostManager;
        synchronized (ScrollerBoostManager.class) {
            if (sScrollerBoostManager == null) {
                sScrollerBoostManager = new ScrollerBoostManager();
            }
            scrollerBoostManager = sScrollerBoostManager;
        }
        return scrollerBoostManager;
    }

    private void initBoostProperty() {
        this.mBoostDefaultDuration = SystemProperties.getInt("persist.sys.boost.durationms", BOOST_DURATION);
        if (this.mBoostDefaultDuration <= 0) {
            this.mBoostDefaultDuration = BOOST_DURATION;
        }
        this.mIsBigcoreBoost = SystemProperties.getBoolean("persist.sys.boost.isbigcore", false);
        if (this.mIsBigcoreBoost) {
            this.mBoostCpuMinFreq = (SystemProperties.getInt("persist.sys.boost.freqmin.b", BOOST_B_CPU_MIN_FREQ) / 100) + DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX;
        } else {
            this.mBoostCpuMinFreq = (SystemProperties.getInt("persist.sys.boost.freqmin.b", BOOST_B_CPU_MIN_FREQ) / 100) + Root.FLAG_EMPTY;
        }
        this.mIPAMaxPower = SystemProperties.getInt("persist.sys.boost.ipapower", IPA_MAX_POWER) + PackageManager.MATCH_ENCRYPTION_AWARE_AND_UNAWARE;
        this.mEnableSkippedFrames = (long) SystemProperties.getInt("persist.sys.boost.skipframe", DEFAULT_ENABLE_SKIPPED_FRAMES);
        this.mBoostByEachFling = SystemProperties.getBoolean("persist.sys.boost.byeachfling", false);
    }

    private void resetAffinity() {
        if (this.mResetAffinityHandler != null) {
            this.mResetAffinityHandler.sendMessageDelayed(this.mResetAffinityHandler.obtainMessage(MESSAGE_RESET_AFFINITY), (long) this.mBoostDuration);
        }
    }

    private void cancelResetAffinity() {
        if (this.mResetAffinityHandler != null) {
            this.mResetAffinityHandler.removeMessages(MESSAGE_RESET_AFFINITY);
        }
    }

    private boolean isAwareScrollerBoostEnable() {
        boolean awareEnable = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
        boolean cpuEnable = WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("persist.sys.cpuset.enable", WifiEnterpriseConfig.ENGINE_DISABLE));
        int featureFlag = SystemProperties.getInt("persist.sys.cpuset.subswitch", DEFAULT_ENABLE_SKIPPED_FRAMES);
        if (awareEnable && cpuEnable && (featureFlag & SWITCH_SCROLLER_BOOST) != 0) {
            return true;
        }
        return false;
    }

    public void init() {
        if (Jlog.isHisiChipset() && isAwareScrollerBoostEnable()) {
            initBoostProperty();
            this.mBoostSwitch = true;
        }
    }

    public boolean isBoostEnable() {
        return this.mBoostSwitch ? isPerformanceMode() : false;
    }

    public void boost(int duration) {
        if (duration <= 0 || duration > this.mBoostDefaultDuration) {
            this.mBoostDuration = this.mBoostDefaultDuration;
        } else {
            this.mBoostDuration = duration;
        }
        this.mLastBoostTime = System.currentTimeMillis();
        int pid = Process.myPid();
        if (pid > 0) {
            if (this.mIsBigcoreBoost) {
                cancelResetAffinity();
                try {
                    Process.setProcessAffinity(pid, NetworkPolicyManager.MASK_ALL_NETWORKS);
                    resetAffinity();
                } catch (RuntimeException e) {
                    AwareLog.e(TAG, "Exception in invoke: " + e.getMessage());
                    return;
                }
            }
            if (this.mEnableBoostByJank) {
                doScrollerBoost();
            }
        }
    }

    private boolean isPerformanceMode() {
        return "true".equals(SystemProperties.get("persist.sys.performance", "false"));
    }

    private void doScrollerBoost() {
        Jlog.perfEvent(StrictMode.DETECT_VM_REGISTRATION_LEAKS, ProxyInfo.LOCAL_EXCL_LIST, new int[]{this.mBoostDuration, this.mBoostCpuMinFreq, this.mIPAMaxPower});
    }

    public void updateFrameJankInfo(long skippedFrames) {
        if (!this.mBoostSwitch) {
            return;
        }
        if (this.mBoostByEachFling || !this.mEnableBoostByJank) {
            long scrollerBoostTime = System.currentTimeMillis() - this.mLastBoostTime;
            if (skippedFrames >= this.mEnableSkippedFrames && scrollerBoostTime <= BOOST_TIME_LENGTH) {
                if (!this.mBoostByEachFling) {
                    this.mEnableBoostByJank = true;
                }
                doScrollerBoost();
            }
        }
    }
}
