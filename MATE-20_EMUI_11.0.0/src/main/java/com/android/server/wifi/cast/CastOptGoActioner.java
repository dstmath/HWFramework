package com.android.server.wifi.cast;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.cast.CastOptDeviceCfg;
import com.android.server.wifi.p2p.HwDfsMonitor;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class CastOptGoActioner {
    private static final int CMD_SET_CSA_CHANNEL_MSG = 100;
    private static final int[] COMMON_CHANNELS_2G = {1, 6, 11};
    private static final int COMMON_USING_5G_CHANNEL = 149;
    private static final int FREQ_CHANNEL_1 = 2412;
    private static final int FREQ_CHANNEL_36 = 5180;
    private static final int FREQ_DIFF_PER_CH = 5;
    private static final int INVALID_P2P_CHANNEL = -1;
    private static final int MAX_24G_CHANNEL = 13;
    private static final int MAX_5G_CHANNEL = 165;
    private static final int MIN_24G_CHANNEL = 1;
    private static final int MIN_5G_CHANNEL = 36;
    private static final int SET_CSA_CHANNEL_DELAYED_MS = 1000;
    private static final String TAG = "CastOptGoActioner";
    private static CastOptGoActioner sCastOptGoActioner = null;
    private CastOptDeviceCfg mCastOptDeviceCfg = null;
    private int[] mChannelsDfs = null;
    private Context mContext = null;
    private Handler mHandler = null;
    private HwDfsMonitor mHwDfsMonitor;
    private long mLastCsaTimestamp = 0;
    private int mLastP2pChannel = -1;
    private CastOptDeviceCfg.DeviceWifiInfo mPeerDeviceCfgInfo = null;
    private CastOptDeviceCfg.DeviceWifiInfo mSelfDeviceCfgInfo = null;

    private CastOptGoActioner(Context context, Looper looper, CastOptDeviceCfg castOptDeviceCfg) {
        this.mContext = context;
        this.mCastOptDeviceCfg = castOptDeviceCfg;
        this.mHwDfsMonitor = HwDfsMonitor.createHwDfsMonitor(this.mContext);
        initHandler(looper);
    }

    protected static CastOptGoActioner createCastOptGoActioner(Context context, Looper looper, CastOptDeviceCfg castOptDeviceCfg) {
        if (sCastOptGoActioner == null) {
            sCastOptGoActioner = new CastOptGoActioner(context, looper, castOptDeviceCfg);
        }
        return sCastOptGoActioner;
    }

    protected static CastOptGoActioner getInstance() {
        return sCastOptGoActioner;
    }

    private boolean isSupport5gChannel(List<Integer> channelList) {
        if (channelList == null || channelList.isEmpty()) {
            return false;
        }
        Integer[] lists = new Integer[channelList.size()];
        channelList.toArray(lists);
        for (Integer num : lists) {
            if (num.intValue() >= 36) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportSta5gChannel(int staChannel, List<Integer> channelList) {
        return is5gChannel(staChannel) && channelList.contains(Integer.valueOf(staChannel));
    }

    /* access modifiers changed from: protected */
    public boolean is5gChannel(int channel) {
        if (channel < 36 || channel > MAX_5G_CHANNEL) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean is24gChannel(int channel) {
        if (channel < 1 || channel > 13) {
            return false;
        }
        return true;
    }

    private int getRandom2gChannel() {
        SecureRandom random = new SecureRandom();
        int[] iArr = COMMON_CHANNELS_2G;
        return iArr[random.nextInt(iArr.length)];
    }

    private int convertChannelToFrequency(int channel) {
        if (channel >= 1 && channel <= 13) {
            return ((channel - 1) * 5) + FREQ_CHANNEL_1;
        }
        if (36 <= channel && channel <= MAX_5G_CHANNEL) {
            return ((channel - 36) * 5) + FREQ_CHANNEL_36;
        }
        HwHiLog.d(TAG, false, "never should happen", new Object[0]);
        return 0;
    }

    private int[] getRadarChannels() {
        return WifiInjector.getInstance().getWifiNative().getChannelsForBand(4);
    }

    /* access modifiers changed from: protected */
    public boolean isRadarChannel(int channel) {
        int freq = convertChannelToFrequency(channel);
        int[] iArr = this.mChannelsDfs;
        if (iArr == null) {
            return false;
        }
        for (int channelDfs : iArr) {
            if (freq == channelDfs) {
                HwHiLog.d(TAG, false, "isDfsChannel: true, DfsChannel: %{public}d", new Object[]{Integer.valueOf(channelDfs)});
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isIndoorChannel(int channel) {
        boolean ret = false;
        int freq = convertChannelToFrequency(channel);
        ClientModeImpl wsm = WifiInjector.getInstance().getClientModeImpl();
        if (wsm instanceof HwWifiStateMachine) {
            ret = ((HwWifiStateMachine) wsm).getSoftApChannelXmlParse().isIndoorChannel(freq, WifiInjector.getInstance().getWifiCountryCode().getCountryCodeSentToDriver());
        }
        HwHiLog.d(TAG, false, "isIndoorChannel: %{public}s", new Object[]{String.valueOf(ret)});
        return ret;
    }

    private int chooseCommon5gChannel(List<Integer> selfChannelList, List<Integer> peerChannelList) {
        if (selfChannelList == null || peerChannelList == null) {
            return -1;
        }
        if (selfChannelList.contains(Integer.valueOf((int) COMMON_USING_5G_CHANNEL)) && peerChannelList.contains(Integer.valueOf((int) COMMON_USING_5G_CHANNEL))) {
            return COMMON_USING_5G_CHANNEL;
        }
        Integer[] selfLists = new Integer[selfChannelList.size()];
        Integer[] peerLists = new Integer[peerChannelList.size()];
        selfChannelList.toArray(selfLists);
        peerChannelList.toArray(peerLists);
        Arrays.sort(selfLists);
        Arrays.sort(peerLists);
        int i = selfLists.length - 1;
        while (i >= 0 && !is24gChannel(selfLists[i].intValue())) {
            if (selfLists[i].intValue() != MAX_5G_CHANNEL && Arrays.binarySearch(peerLists, selfLists[i]) >= 0 && !isIndoorChannel(selfLists[i].intValue()) && !isRadarChannel(selfLists[i].intValue())) {
                HwHiLog.d(TAG, false, "founded=" + selfLists[i], new Object[0]);
                return selfLists[i].intValue();
            }
            i--;
        }
        return -1;
    }

    private boolean isChooseP2pChannelOptEnable(CastOptDeviceCfg.DeviceWifiInfo selfDeviceCfgInfo, CastOptDeviceCfg.DeviceWifiInfo peerDeviceCfgInfo) {
        if (selfDeviceCfgInfo == null || peerDeviceCfgInfo == null) {
            return false;
        }
        List<Integer> selfSupportChannels = selfDeviceCfgInfo.getP2pSupportChannel();
        List<Integer> peerSupportChannels = peerDeviceCfgInfo.getP2pSupportChannel();
        if (selfSupportChannels == null || peerSupportChannels == null || selfSupportChannels.isEmpty() || peerSupportChannels.isEmpty() || !selfDeviceCfgInfo.getCapsOfChooseP2pChannelOpt() || !peerDeviceCfgInfo.getCapsOfChooseP2pChannelOpt()) {
            return false;
        }
        return true;
    }

    private void addSupportChannel(int staChannel, List<Integer> channelList) {
        if (channelList != null && !channelList.contains(Integer.valueOf(staChannel))) {
            channelList.add(Integer.valueOf(staChannel));
        }
    }

    private void addRadarOrIndoorChannel(CastOptDeviceCfg.DeviceWifiInfo selfDeviceCfgInfo, CastOptDeviceCfg.DeviceWifiInfo peerDeviceCfgInfo) {
        if (selfDeviceCfgInfo != null && peerDeviceCfgInfo != null) {
            int selfStaChannel = selfDeviceCfgInfo.getStaChannel();
            int peerStaChannel = peerDeviceCfgInfo.getStaChannel();
            List<Integer> selfSupportChannels = selfDeviceCfgInfo.getP2pSupportChannel();
            List<Integer> peerSupportChannels = peerDeviceCfgInfo.getP2pSupportChannel();
            if (isSupport5gChannel(selfSupportChannels) && isSupport5gChannel(peerSupportChannels)) {
                if (selfDeviceCfgInfo.getCapsOfCsa() && peerDeviceCfgInfo.getCapsOfCsa() && CastOptUtils.isAllowDfsChannels()) {
                    if (isRadarChannel(selfStaChannel)) {
                        addSupportChannel(selfStaChannel, selfSupportChannels);
                        addSupportChannel(selfStaChannel, peerSupportChannels);
                    }
                    if (isRadarChannel(peerStaChannel)) {
                        addSupportChannel(peerStaChannel, selfSupportChannels);
                        addSupportChannel(peerStaChannel, peerSupportChannels);
                    }
                }
                if (CastOptUtils.isAllowIndoorChannels() && !isRadarChannel(selfStaChannel) && isIndoorChannel(selfStaChannel)) {
                    addSupportChannel(selfStaChannel, selfSupportChannels);
                    addSupportChannel(selfStaChannel, peerSupportChannels);
                }
                if (CastOptUtils.isAllowIndoorChannels() && !isRadarChannel(peerStaChannel) && isIndoorChannel(peerStaChannel)) {
                    addSupportChannel(peerStaChannel, selfSupportChannels);
                    addSupportChannel(peerStaChannel, peerSupportChannels);
                }
            }
        }
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            /* class com.android.server.wifi.cast.CastOptGoActioner.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what != 100) {
                    HwHiLog.e(CastOptGoActioner.TAG, false, "unknown msg", new Object[0]);
                } else {
                    CastOptGoActioner.this.setCsaChannel(msg.arg1);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCsaChannel(int channel) {
        WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
        if (wifiNative == null || wifiNative.mHwWifiNativeEx == null) {
            HwHiLog.e(TAG, false, "wifiNative or mHwWifiNativeEx is null", new Object[0]);
            return;
        }
        int intervalTime = (int) (SystemClock.elapsedRealtime() - this.mLastCsaTimestamp);
        if (intervalTime < 1000) {
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(100, channel, 0), (long) (1000 - intervalTime));
            return;
        }
        int ret = wifiNative.mHwWifiNativeEx.sendCmdToDriver("p2p0", 161, new byte[]{(byte) channel});
        if (ret < 0) {
            HwHiLog.e(TAG, false, "sendCmdToDriver fail, ret = %{public}d", new Object[]{Integer.valueOf(ret)});
        } else {
            this.mLastCsaTimestamp = SystemClock.elapsedRealtime();
        }
        CastOptChr castOptChr = CastOptChr.getInstance();
        if (castOptChr != null) {
            castOptChr.handleCsaAction();
        }
    }

    /* access modifiers changed from: protected */
    public void resetGoCastOptCfg() {
        if (this.mChannelsDfs != null) {
            this.mChannelsDfs = null;
        }
        this.mLastP2pChannel = -1;
        this.mLastCsaTimestamp = 0;
    }

    /* access modifiers changed from: protected */
    public void handleDeviceChannelChange() {
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (castOptManager == null || castOptManager.isCastOptWorking()) {
            int channel = getP2pRecommendChannel();
            HwHiLog.i(TAG, false, "channel = %{public}d", new Object[]{Integer.valueOf(channel)});
            if (channel == -1 || this.mLastP2pChannel == channel) {
                HwHiLog.i(TAG, false, "p2p channle is invalid or not change", new Object[0]);
                return;
            }
            this.mLastP2pChannel = channel;
            HwDfsMonitor hwDfsMonitor = this.mHwDfsMonitor;
            if (hwDfsMonitor != null && hwDfsMonitor.isDfsUsable(convertChannelToFrequency(channel))) {
                this.mHwDfsMonitor.closeGoCac(0);
                CastOptMonitor castOptMonitor = CastOptMonitor.getInstance();
                if (castOptMonitor != null) {
                    castOptMonitor.setGoSwitchChannelEnableWhenDetectRadar(true);
                }
            }
            CastOptDeviceCfg.DeviceWifiInfo deviceWifiInfo = this.mPeerDeviceCfgInfo;
            if (deviceWifiInfo != null && deviceWifiInfo.getCapsOfCsa()) {
                Handler handler = this.mHandler;
                handler.sendMessage(handler.obtainMessage(100, channel, 0));
                return;
            }
            return;
        }
        HwHiLog.i(TAG, false, "cast opt is not working", new Object[0]);
    }

    /* access modifiers changed from: protected */
    public int getP2pRecommendChannel() {
        CastOptDeviceCfg castOptDeviceCfg = this.mCastOptDeviceCfg;
        if (castOptDeviceCfg == null) {
            return -1;
        }
        this.mSelfDeviceCfgInfo = castOptDeviceCfg.getSelfDeviceCfgInfo();
        this.mPeerDeviceCfgInfo = this.mCastOptDeviceCfg.getPeerDeviceCfgInfo();
        if (!isChooseP2pChannelOptEnable(this.mSelfDeviceCfgInfo, this.mPeerDeviceCfgInfo)) {
            return -1;
        }
        List<Integer> selfSupportChannels = this.mSelfDeviceCfgInfo.getP2pSupportChannel();
        List<Integer> peerSupportChannels = this.mPeerDeviceCfgInfo.getP2pSupportChannel();
        int selfStaChannel = this.mSelfDeviceCfgInfo.getStaChannel();
        int peerStaChannel = this.mPeerDeviceCfgInfo.getStaChannel();
        boolean isSelfSupportCsa = this.mSelfDeviceCfgInfo.getCapsOfCsa();
        boolean isPeerSupportCsa = this.mPeerDeviceCfgInfo.getCapsOfCsa();
        this.mSelfDeviceCfgInfo.getCapsOfP2pRadarDetect();
        this.mChannelsDfs = getRadarChannels();
        addRadarOrIndoorChannel(this.mSelfDeviceCfgInfo, this.mPeerDeviceCfgInfo);
        HwHiLog.i(TAG, false, "selfSupportChannels=" + Arrays.toString(selfSupportChannels.toArray()) + " peerSupportChannels=" + Arrays.toString(peerSupportChannels.toArray()) + " selfStaChannel=" + selfStaChannel + " peerStaChannel=" + peerStaChannel + " isSelfSupportCsa=" + isSelfSupportCsa + " isPeerSupportCsa=" + isPeerSupportCsa, new Object[0]);
        if (isSupport5gChannel(selfSupportChannels) && isSupport5gChannel(peerSupportChannels)) {
            if (isSupportSta5gChannel(selfStaChannel, peerSupportChannels)) {
                if (!isRadarChannel(selfStaChannel) || (isRadarChannel(selfStaChannel) && isSelfSupportCsa && isPeerSupportCsa)) {
                    return selfStaChannel;
                }
                HwHiLog.e(TAG, false, "self isRadarChannel but not support CSA", new Object[0]);
            }
            if (isSupportSta5gChannel(peerStaChannel, selfSupportChannels)) {
                if (!isRadarChannel(peerStaChannel) || (isRadarChannel(peerStaChannel) && isSelfSupportCsa && isPeerSupportCsa)) {
                    return peerStaChannel;
                }
                HwHiLog.e(TAG, false, "peer isRadarChannel but not support CSA", new Object[0]);
            }
            int common5gChannel = chooseCommon5gChannel(selfSupportChannels, peerSupportChannels);
            if (is5gChannel(common5gChannel)) {
                return common5gChannel;
            }
        }
        return getRandom2gChannel();
    }
}
