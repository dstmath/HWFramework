package com.huawei.haptic;

import android.content.Context;
import android.os.Binder;
import android.os.IVibratorService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.os.IHwVibrator;
import com.huawei.haptic.HwHapticChannel;
import com.huawei.haptic.HwHapticCurve;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwHapticPlayer {
    private static final String ADJUST_CURVE = "adjustcurve";
    public static final float ADJUST_CURVE_INTENSITY_MAXIMUM = 1000000.0f;
    private static final int ADJUST_CURVE_NUM_MAXIMUM = 2;
    public static final float ADJUST_CURVE_SHARPNESS_MAXIMUM = 2.0f;
    public static final float ADJUST_CURVE_SHARPNESS_MINIMUM = -2.0f;
    private static final String ADJUST_POINT = "adjustpoint";
    private static final List<String> ADJUST_POINT_PARAMETER_LIST = Collections.unmodifiableList(Arrays.asList("time", PARAMETERS));
    private static final String AUTHOR = "Author";
    private static final String CHANNELS = "channels";
    private static final String CHANNEL_A = "hapticA";
    private static final String CHANNEL_A_B = "hapticAB";
    private static final String CHANNEL_B = "hapticB";
    private static final String CHANNEL_DEFAULT = "default";
    private static final String CHANNEL_ITEM = "channel";
    private static final int CHANNEL_NUM_MAXIMUM = 2;
    private static final String CONTINUOUS = "continuous";
    private static final String CREATED = "Created";
    private static final int CURVE_TYPE_TRANSIENT = 1;
    private static final String DESCRIPTION = "Description";
    private static final String DURATION = "duration";
    public static final int DURATION_MAXIMUM = 1800000;
    public static final int HAPTIC_DEFAULT_ARRAY_SIZE = 0;
    public static final int HAPTIC_ERROR_INVALID_PARAMS = -2;
    public static final int HAPTIC_ERROR_INVALID_TOKEN = -1;
    public static final int HAPTIC_MAX_ARRAY_SIZE = 64;
    public static final int HAPTIC_STATE_COMPLETED = 3;
    public static final int HAPTIC_STATE_ERROR = 16;
    public static final int HAPTIC_STATE_PLAYING = 1;
    public static final int HAPTIC_STATE_STOPPED = 2;
    private static final String INTENSITY = "intensity";
    private static final int INTENSITY_SHARPNESS_UPPER_LIMIT = 1;
    private static final Singleton<IHwHapticPlayer> I_HW_HAPTIC_PLAYER_SINGLETON = new Singleton<IHwHapticPlayer>() {
        /* class com.huawei.haptic.HwHapticPlayer.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwHapticPlayer create() {
            try {
                IVibratorService vibratorService = IVibratorService.Stub.asInterface(ServiceManager.getService(Context.VIBRATOR_SERVICE));
                if (vibratorService != null) {
                    return IHwVibrator.Stub.asInterface(vibratorService.getHwInnerService()).getHwHapticPlayer();
                }
                Log.e(HwHapticPlayer.TAG, "failed to connect VibratorService!");
                return null;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final String METADATA = "metadata";
    private static final List<String> METADATA_PARAMETER_LIST = Collections.unmodifiableList(Arrays.asList(TITLE, CREATED, DESCRIPTION, AUTHOR));
    private static final String PARAMETERS = "parameters";
    private static final int PARAMETER_ITEM_MAX_LENGTH = 1;
    private static final int PARA_INVALID = -1;
    private static final String SHARPNESS = "sharpness";
    private static final String SLICE = "slice";
    private static final List<String> SLICE_PARAMETER_LIST = Collections.unmodifiableList(Arrays.asList("time", "type", "duration", INTENSITY, SHARPNESS));
    private static final float STATIC_ADJUST_SHARPNESS_MAXIMUM = 1.0f;
    private static final float STATIC_ADJUST_SHARPNESS_MINIMUM = -1.0f;
    private static final String TAG = "HwHapticPlayer";
    private static final String TIME = "time";
    public static final int TIMESTAMP_MAXIMUM = 1800000;
    public static final float TIMESTAMP_MINIMUM = 0.0f;
    private static final int TIME_INVALID = -1;
    private static final String TITLE = "Title";
    private static final List<String> TOP_PARAMETER_LIST = Collections.unmodifiableList(Arrays.asList("version", CHANNELS, "metadata", WAVEFORM));
    private static final String TRANSIENT = "transient";
    private static final String TYPE = "type";
    private static final String VERSION = "version";
    private static final String WAVE = "wave";
    private static final String WAVEFORM = "waveform";
    private static final List<String> WAVEFORM_PARAMETER_LIST = Collections.unmodifiableList(Arrays.asList(WAVE, "channel", ADJUST_CURVE));
    private HwHapticAttributes mHapticAttr;
    private HwHapticWave mHapticWave = null;
    private boolean mIsLooping = false;
    private OnPlayStateListener mOnPlayStateListener;
    private String mPackageName;
    private final Object mPlaybackLock = new Object();
    private final Binder mToken = new Binder();
    private int mUid;

    public interface OnPlayStateListener {
        void onState(int i, int i2);
    }

    public static IHwHapticPlayer getService() {
        return I_HW_HAPTIC_PLAYER_SINGLETON.get();
    }

    public boolean setHapticWave(int uid, String opPkg, HwHapticAttributes attr, HwHapticWave wave) {
        if (!checkDefineWave(attr, wave)) {
            return false;
        }
        this.mHapticWave = wave;
        this.mUid = uid;
        this.mPackageName = opPkg;
        this.mHapticAttr = attr;
        return true;
    }

    public boolean setHapticWave(int uid, String opPkg, HwHapticAttributes attr, InputStream inputStream) {
        boolean z;
        if (attr == null) {
            z = false;
        } else if (inputStream == null) {
            z = false;
        } else {
            String res = parseStream(inputStream);
            if ("".equals(res)) {
                Log.e(TAG, "Failed to read inputStream");
                return false;
            }
            this.mUid = uid;
            this.mPackageName = opPkg;
            this.mHapticAttr = attr;
            this.mHapticWave = new HwHapticWave();
            try {
                JSONObject jsonObject = new JSONObject(res);
                if (!checkParameterList(jsonObject, TOP_PARAMETER_LIST)) {
                    Log.e(TAG, "the top parameter list is invalid");
                    return false;
                }
                if (parseVersion(jsonObject)) {
                    if (parseMetadata(jsonObject)) {
                        if (jsonObject.has(CHANNELS)) {
                            if (jsonObject.has(WAVEFORM)) {
                                int channelsNum = jsonObject.getInt(CHANNELS);
                                JSONArray channelArr = jsonObject.getJSONArray(WAVEFORM);
                                List<String> channelStrList = parseChannel(channelArr, channelsNum);
                                if (channelArr.length() != channelStrList.size()) {
                                    return false;
                                }
                                if (channelStrList.isEmpty()) {
                                    return false;
                                }
                                int index = 0;
                                while (index < channelArr.length()) {
                                    HwHapticChannel.Builder channelBuilder = getBuilder(channelArr.getJSONObject(index), channelStrList.get(index));
                                    if (channelBuilder == null) {
                                        Log.e(TAG, "getBuilder is failed");
                                        return false;
                                    }
                                    this.mHapticWave.addHapticChannel(channelBuilder.build());
                                    index++;
                                    channelArr = channelArr;
                                }
                                return true;
                            }
                        }
                        Log.e(TAG, "the channels or waveform parameter is not exist");
                        return false;
                    }
                }
                Log.e(TAG, "the version or metadata parameter is invalid");
                return false;
            } catch (JSONException e) {
                Log.e(TAG, "Parse json is failed or invalid parameter" + e.toString());
                return false;
            }
        }
        Log.e(TAG, "attr or inputStream parameter cannot be null");
        return z;
    }

    public String getMetadata(String key) {
        return this.mHapticWave.getMetadata(key);
    }

    public void setLooping(boolean looping) {
        synchronized (this.mPlaybackLock) {
            try {
                getService().setLooping(this.mToken, looping);
            } catch (RemoteException e) {
                Log.e(TAG, "setLooping fail with RemoteException");
            }
            this.mIsLooping = looping;
        }
    }

    public boolean isLooping() {
        boolean z;
        synchronized (this.mPlaybackLock) {
            z = this.mIsLooping;
        }
        return z;
    }

    public void setSwapHapticPos(boolean swap) {
    }

    public boolean isSwapHapticPos() {
        return false;
    }

    public boolean isPlaying() {
        try {
            return getService().isPlaying(this.mToken);
        } catch (RemoteException e) {
            Log.e(TAG, "isPlaying fail with RemoteException");
            return false;
        }
    }

    public int play() {
        if (this.mHapticWave == null) {
            Log.e(TAG, "Invalid parameters of haptic wave!");
            return -2;
        }
        try {
            return getService().play(this.mToken, this.mUid, this.mPackageName, this.mHapticAttr, this.mHapticWave);
        } catch (RemoteException e) {
            Log.e(TAG, "play fail with RemoteException");
            return 0;
        }
    }

    public void stop() {
        try {
            getService().stop(this.mToken);
        } catch (RemoteException e) {
            Log.e(TAG, "stop fail with RemoteException");
        }
    }

    public int getDuration() {
        try {
            return getService().getDuration(this.mToken);
        } catch (RemoteException e) {
            Log.e(TAG, "getDuration fail with RemoteException");
            return -1;
        }
    }

    public boolean setDynamicCurve(int type, int channelId, HwHapticCurve curve) {
        Log.i(TAG, "setDynamicCurve begin, type: " + type + " channelId: " + channelId);
        try {
            return getService().setDynamicCurve(this.mToken, type, channelId, curve);
        } catch (RemoteException e) {
            Log.e(TAG, "setDynamicCurve fail with RemoteException");
            return false;
        }
    }

    public void setOnPlayStateListener(OnPlayStateListener listener) {
        this.mOnPlayStateListener = listener;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0034  */
    public static boolean checkDefineWave(HwHapticAttributes attribute, HwHapticWave wave) {
        if (attribute == null || wave == null) {
            Log.e(TAG, "the attr or wave parameter cannot be null");
            return false;
        } else if (wave.mHapticChannels == null || wave.mHapticChannels.isEmpty()) {
            Log.e(TAG, "the wave mHapticChannels is null or empty");
            return false;
        } else if (wave.mHapticChannels.size() > 2) {
            Log.e(TAG, "the wave channel number is too many");
            return false;
        } else {
            int tempChannelId = -1;
            Iterator<HwHapticChannel> it = wave.mHapticChannels.iterator();
            while (it.hasNext()) {
                HwHapticChannel hapticChannel = it.next();
                int channelId = hapticChannel.getChannelId();
                if (!checkChannelId(channelId, tempChannelId)) {
                    return false;
                }
                tempChannelId = channelId;
                int totalDuration = hapticChannel.getDuration();
                List<HwHapticChannel.HwHapticSlice> hapticSliceList = hapticChannel.mHapticSlices;
                if (hapticSliceList == null || hapticSliceList.isEmpty()) {
                    Log.e(TAG, "the wave mHapticSlices is null or empty");
                    return false;
                } else if (hapticSliceList.size() > 64) {
                    Log.e(TAG, "the wave mHapticSlices number is too many");
                    return false;
                } else if (!checkDefineSlice(hapticSliceList, totalDuration)) {
                    Log.e(TAG, "the wave define slice is invalid");
                    return false;
                } else {
                    if (!checkDefineCurve(hapticChannel.mIntensityCurve, INTENSITY) || !checkDefineCurve(hapticChannel.mSharpnessCurve, SHARPNESS)) {
                        Log.e(TAG, "the wave define curve is invalid");
                        return false;
                    }
                    while (it.hasNext()) {
                    }
                }
            }
            return true;
        }
    }

    private static boolean checkChannelId(int channelId, int tempChannelId) {
        if (channelId < 0 || channelId > 3) {
            Log.e(TAG, "the wave channelId is invalid");
            return false;
        } else if (tempChannelId != channelId) {
            return true;
        } else {
            Log.e(TAG, "the wave channelId must be different");
            return false;
        }
    }

    private static boolean checkDefineSlice(List<HwHapticChannel.HwHapticSlice> hapticSlices, int totalDuration) {
        hapticSlices.sort($$Lambda$HwHapticPlayer$MvCOBVpB4SLQG0KlwwvFN37vtUc.INSTANCE);
        int tempTimeStamp = -1;
        for (int index = 0; index < hapticSlices.size(); index++) {
            HwHapticChannel.HwHapticSlice slice = hapticSlices.get(index);
            int time = slice.mTimeStamp;
            int type = slice.mType;
            int duration = slice.mDuration;
            float intensity = slice.mIntensity;
            float sharpness = slice.mSharpness;
            Log.i(TAG, "the wave define slice, time: " + time + " type: " + type + " duration: " + duration + " intensity: " + intensity + " sharpness: " + sharpness);
            if (!checkSliceParameter(time, type, duration, intensity, sharpness, totalDuration)) {
                Log.e(TAG, "the wave define slice parameter value is invalid");
                return false;
            } else if (tempTimeStamp == time) {
                Log.e(TAG, "the value of time must be different");
                return false;
            } else if (index + 1 >= hapticSlices.size() || slice.mTimeStamp + slice.mDuration <= hapticSlices.get(index + 1).mTimeStamp) {
                tempTimeStamp = time;
            } else {
                Log.e(TAG, "the define duration value is invalid");
                return false;
            }
        }
        return true;
    }

    static /* synthetic */ int lambda$checkDefineSlice$0(HwHapticChannel.HwHapticSlice o1, HwHapticChannel.HwHapticSlice o2) {
        return o1.mTimeStamp - o2.mTimeStamp;
    }

    private static boolean checkDefineCurve(HwHapticCurve adjustCurve, String curveType) {
        if (adjustCurve == null) {
            return true;
        }
        List<HwHapticCurve.HwAdjustPoint> adjustPointList = adjustCurve.mAdjustPoints;
        if (adjustPointList == null || adjustPointList.isEmpty()) {
            Log.e(TAG, "the adjust point is null or empty, curve type is " + curveType);
            return false;
        } else if (adjustPointList.size() > 64) {
            Log.e(TAG, "the adjust point number is too many, curve type is " + curveType);
            return false;
        } else {
            adjustPointList.sort($$Lambda$HwHapticPlayer$aR5ByREichvd4rleIN_gpzdJLfE.INSTANCE);
            int tempTimeStamp = -1;
            for (HwHapticCurve.HwAdjustPoint adjustPoint : adjustPointList) {
                int time = adjustPoint.mTimeStamp;
                if (time < 0 || time > 1800000) {
                    Log.e(TAG, "the adjust time value is invalid, curve type is " + curveType);
                    return false;
                } else if (tempTimeStamp == time) {
                    Log.e(TAG, "the value of time must be different");
                    return false;
                } else {
                    tempTimeStamp = time;
                    float parameter = adjustPoint.mValue;
                    if (INTENSITY.equals(curveType) && (parameter < 0.0f || parameter > 1000000.0f)) {
                        Log.e(TAG, "the intensity adjust parameter value is invalid");
                        return false;
                    } else if (SHARPNESS.equals(curveType) && (parameter < -1.0f || parameter > 1.0f)) {
                        Log.e(TAG, "the sharpness adjust parameter value is invalid");
                        return false;
                    }
                }
            }
            return true;
        }
    }

    static /* synthetic */ int lambda$checkDefineCurve$1(HwHapticCurve.HwAdjustPoint o1, HwHapticCurve.HwAdjustPoint o2) {
        return o1.mTimeStamp - o2.mTimeStamp;
    }

    private static boolean checkSliceParameter(int time, int type, int duration, float intensity, float sharpness, int totalDuration) {
        if (time < 0 || time > 1800000) {
            Log.e(TAG, "time value is invalid, time is: " + time);
            return false;
        } else if (type != 1 && type != 2) {
            Log.e(TAG, "type value is invalid, type is: " + type);
            return false;
        } else if (duration < 0 || duration > 1800000) {
            Log.e(TAG, "duration value is invalid, duration: " + duration);
            return false;
        } else if (intensity < 0.0f || intensity > 1.0f) {
            Log.e(TAG, "intensity value is invalid, intensity is: " + intensity);
            return false;
        } else if (sharpness < 0.0f || sharpness > 1.0f) {
            Log.e(TAG, "sharpness value is invalid, sharpness is: " + sharpness);
            return false;
        } else if (totalDuration >= 0 && totalDuration <= 1800000) {
            return true;
        } else {
            Log.e(TAG, "the wave total duration is invalid, duration: " + totalDuration);
            return false;
        }
    }

    private boolean checkParameterList(JSONObject item, List<String> parameterList) throws JSONException {
        if (item.names() == null) {
            Log.e(TAG, "the JSONObject doesn't contain any sub-item");
            return false;
        }
        for (int index = 0; index < ((JSONArray) Objects.requireNonNull(item.names())).length(); index++) {
            if (!parameterList.contains(((JSONArray) Objects.requireNonNull(item.names())).getString(index))) {
                return false;
            }
        }
        return true;
    }

    private HwHapticChannel.Builder getBuilder(JSONObject subObj, String channelStr) throws JSONException {
        if (!checkParameterList(subObj, WAVEFORM_PARAMETER_LIST)) {
            Log.e(TAG, "the waveform item parameter list is invalid");
            return null;
        } else if (!subObj.has(WAVE)) {
            Log.e(TAG, "the wave parameter is not exist");
            return null;
        } else {
            List<HwHapticChannel.HwHapticSlice> sliceList = getSlice(subObj.getJSONArray(WAVE));
            if (sliceList.isEmpty() || sliceList.size() > 64) {
                Log.e(TAG, "the wave slice parameter is invalid or number is too many");
                return null;
            }
            HwHapticChannel.Builder channelBuilder = getWaveBuilder(sliceList);
            if (channelBuilder == null) {
                Log.e(TAG, "the wave duration parameter value is invalid");
                return null;
            } else if (!hasChannelId(channelStr, channelBuilder)) {
                Log.e(TAG, "the waveform channel id is invalid");
                return null;
            } else if (!subObj.has(ADJUST_CURVE)) {
                Log.w(TAG, "the adjustcurve parameter is missing");
                return channelBuilder;
            } else if (getAdjustCurve(channelBuilder, subObj.getJSONArray(ADJUST_CURVE))) {
                return channelBuilder;
            } else {
                Log.e(TAG, "the adjustcurve parameter is invalid");
                return null;
            }
        }
    }

    private List<HwHapticChannel.HwHapticSlice> getSlice(JSONArray waveArr) throws JSONException {
        HwHapticPlayer hwHapticPlayer = this;
        HashSet<Integer> timeSet = new HashSet<>();
        List<HwHapticChannel.HwHapticSlice> sliceList = new ArrayList<>();
        int totalDuration = 0;
        int index = 0;
        while (index < waveArr.length()) {
            JSONObject sliceObj = waveArr.getJSONObject(index);
            if (sliceObj.length() != 1) {
                Log.e(TAG, "the wave item length is invalid");
                return Collections.emptyList();
            } else if (!sliceObj.has("slice")) {
                Log.e(TAG, "the wave slice parameter is not exist");
                return Collections.emptyList();
            } else {
                JSONObject item = new JSONObject(sliceObj.getString("slice"));
                if (!hwHapticPlayer.checkParameterList(item, SLICE_PARAMETER_LIST)) {
                    Log.e(TAG, "the wave slice parameter list is invalid");
                    return Collections.emptyList();
                }
                int type = hwHapticPlayer.parseType(item);
                if (type == -1) {
                    Log.e(TAG, "the type parameter is invalid");
                    return Collections.emptyList();
                }
                int time = hwHapticPlayer.parseTimeDuration(item, "time", 0);
                int duration = hwHapticPlayer.parseTimeDuration(item, "duration", type);
                float intensity = (float) hwHapticPlayer.parseIntensitySharpness(item, INTENSITY);
                float sharpness = (float) hwHapticPlayer.parseIntensitySharpness(item, SHARPNESS);
                totalDuration += duration;
                if (!checkSliceParameter(time, type, duration, intensity, sharpness, totalDuration)) {
                    Log.e(TAG, "the wave slice parameter value is invalid");
                    return Collections.emptyList();
                }
                Log.i(TAG, "the wave slice parameter, time: " + time + " type: " + type + " duration: " + duration + " intensity: " + intensity + " sharpness: " + sharpness);
                sliceList.add(new HwHapticChannel.HwHapticSlice(time, type, duration, intensity, sharpness));
                timeSet.add(Integer.valueOf(time));
                index++;
                hwHapticPlayer = this;
            }
        }
        if (sliceList.size() == timeSet.size()) {
            return sliceList;
        }
        Log.e(TAG, "the wave time parameter value is invalid");
        return Collections.emptyList();
    }

    private HwHapticChannel.Builder getWaveBuilder(List<HwHapticChannel.HwHapticSlice> sliceList) {
        sliceList.sort($$Lambda$HwHapticPlayer$toNYlyFJZISNA57YUpRd9tTa94Q.INSTANCE);
        HwHapticChannel.Builder channelBuilder = new HwHapticChannel.Builder();
        for (int index = 0; index < sliceList.size(); index++) {
            HwHapticChannel.HwHapticSlice slice = sliceList.get(index);
            if (index + 1 >= sliceList.size() || slice.mTimeStamp + slice.mDuration <= sliceList.get(index + 1).mTimeStamp) {
                channelBuilder.addSlice(slice);
            } else {
                Log.e(TAG, "the duration value is invalid");
                return null;
            }
        }
        return channelBuilder;
    }

    static /* synthetic */ int lambda$getWaveBuilder$2(HwHapticChannel.HwHapticSlice o1, HwHapticChannel.HwHapticSlice o2) {
        return o1.mTimeStamp - o2.mTimeStamp;
    }

    private HwHapticCurve.Builder getCurveBuilder(List<Integer> curveTime, List<Double> curveValue) {
        HwHapticCurve.Builder curveBuilder = new HwHapticCurve.Builder();
        List<Integer> intensityTimeList = new ArrayList<>(curveTime);
        Collections.sort(curveTime);
        for (Integer integer : curveTime) {
            int timeNo = intensityTimeList.indexOf(integer);
            int time = integer.intValue();
            float parameters = curveValue.get(timeNo).floatValue();
            Log.i(TAG, "  time: " + time + "  parameters: " + parameters);
            curveBuilder.addHwAdjustPoint(new HwHapticCurve.HwAdjustPoint(time, parameters));
        }
        return curveBuilder;
    }

    private boolean getAdjustCurve(HwHapticChannel.Builder channelBuilder, JSONArray curveArr) throws JSONException {
        if (curveArr.length() > 2) {
            Log.e(TAG, "the adjustcurve sub-item parameters cannot exceed two");
            return false;
        }
        boolean[] flag = {false, false};
        for (int index = 0; index < curveArr.length(); index++) {
            JSONObject curvePara = curveArr.getJSONObject(index);
            if (curvePara.length() != 1) {
                Log.e(TAG, "the curve item length is invalid");
                return false;
            }
            if (curvePara.has(INTENSITY)) {
                if (flag[0]) {
                    Log.e(TAG, "the intensity parameter is redundant");
                    return false;
                }
                flag[0] = true;
                if (!obtainAdjustPoint(channelBuilder, curvePara, INTENSITY)) {
                    Log.e(TAG, "obtain the intensity adjust point is failed");
                    return false;
                }
            } else if (!curvePara.has(SHARPNESS)) {
                Log.e(TAG, "the curve item parameter is invalid");
                return false;
            } else if (flag[1]) {
                Log.e(TAG, "the sharpness parameter is redundant");
                return false;
            } else {
                flag[1] = true;
                if (!obtainAdjustPoint(channelBuilder, curvePara, SHARPNESS)) {
                    Log.e(TAG, "obtain the sharpness adjust point is failed");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean obtainAdjustPoint(HwHapticChannel.Builder channelBuilder, JSONObject curvePara, String curveType) throws JSONException {
        if (INTENSITY.equals(curveType)) {
            List<Integer> curveIntensityTime = new ArrayList<>();
            List<Double> curveIntensityValue = new ArrayList<>();
            if (parseAdjustCurve(curveIntensityTime, curveIntensityValue, curvePara, INTENSITY) == -1) {
                Log.e(TAG, "the curve intensity parameter value is invalid");
                return false;
            }
            channelBuilder.setIntensityCurve(getCurveBuilder(curveIntensityTime, curveIntensityValue).build());
            return true;
        }
        List<Integer> curveSharpnessTime = new ArrayList<>();
        List<Double> curveSharpnessValue = new ArrayList<>();
        if (parseAdjustCurve(curveSharpnessTime, curveSharpnessValue, curvePara, SHARPNESS) == -1) {
            Log.e(TAG, "the curve sharpness parameter value is invalid");
            return false;
        }
        channelBuilder.setSharpnessCurve(getCurveBuilder(curveSharpnessTime, curveSharpnessValue).build());
        return true;
    }

    private boolean hasChannelId(String channelStr, HwHapticChannel.Builder channelBuilder) {
        if ("default".equals(channelStr)) {
            channelBuilder.setChannelId(0);
        } else if (CHANNEL_A.equals(channelStr)) {
            channelBuilder.setChannelId(1);
        } else if (CHANNEL_B.equals(channelStr)) {
            channelBuilder.setChannelId(2);
        } else if (CHANNEL_A_B.equals(channelStr)) {
            channelBuilder.setChannelId(3);
        } else {
            Log.e(TAG, "the waveform channel id is invalid");
            return false;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0023, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0029, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002c, code lost:
        throw r3;
     */
    private String parseStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while (true) {
                String line = bf.readLine();
                if (line == null) {
                    bf.close();
                    break;
                }
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to open file");
        }
        return stringBuilder.toString();
    }

    private boolean parseVersion(JSONObject jsonObject) {
        if (!jsonObject.has("version")) {
            Log.e(TAG, "the version parameter is not exist");
            return true;
        }
        try {
            jsonObject.getDouble("version");
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "the version parameter is invalid");
            return false;
        }
    }

    private boolean parseMetadata(JSONObject jsonObject) throws JSONException {
        if (!jsonObject.has("metadata")) {
            Log.e(TAG, "the metadata parameter is not exist");
            return true;
        }
        JSONObject item = new JSONObject(jsonObject.getString("metadata"));
        if (item.length() == 0) {
            Log.e(TAG, "the metadata all parameters' attribute values are default");
            return true;
        } else if (!checkParameterList(item, METADATA_PARAMETER_LIST)) {
            Log.e(TAG, "the metadata parameter list is invalid");
            return false;
        } else {
            if (item.has(TITLE)) {
                this.mHapticWave.setMetadata(TITLE, item.getString(TITLE));
            }
            if (item.has(CREATED)) {
                this.mHapticWave.setMetadata(CREATED, item.getString(CREATED));
            }
            if (item.has(DESCRIPTION)) {
                this.mHapticWave.setMetadata(DESCRIPTION, item.getString(DESCRIPTION));
            }
            if (item.has(AUTHOR)) {
                this.mHapticWave.setMetadata(AUTHOR, item.getString(AUTHOR));
            }
            return true;
        }
    }

    private List<String> parseChannel(JSONArray channelArr, int channelsNum) throws JSONException {
        List<String> channelStrList = new ArrayList<>();
        if (channelArr.length() > 2 || channelsNum > 2) {
            Log.e(TAG, "the wave channel number is too many");
            return Collections.emptyList();
        }
        for (int index = 0; index < channelArr.length(); index++) {
            JSONObject subObj = channelArr.getJSONObject(index);
            if (subObj.has("channel")) {
                channelStrList.add(subObj.getString("channel"));
            } else {
                Log.i(TAG, "the wave channel parameter is empty");
                channelStrList.add("default");
            }
        }
        if (channelsNum == 1 && channelArr.length() == 1) {
            String channelStrTemp = channelStrList.get(0);
            if (CHANNEL_A_B.equals(channelStrTemp)) {
                Log.e(TAG, "the channels and channel parameter are inconsistent");
                return Collections.emptyList();
            } else if (CHANNEL_A.equals(channelStrTemp)) {
                channelStrList.set(0, CHANNEL_A);
            } else if (CHANNEL_B.equals(channelStrTemp)) {
                channelStrList.set(0, CHANNEL_B);
            } else {
                Log.i(TAG, "the channel parameter is default");
                channelStrList.set(0, "default");
            }
        } else if (channelsNum == 2 && channelArr.length() == 1) {
            if (!CHANNEL_A_B.equals(channelStrList.get(0))) {
                Log.e(TAG, "the channels and channel parameter are inconsistent");
                return Collections.emptyList();
            }
            channelStrList.set(0, CHANNEL_A_B);
        } else if (channelsNum != 2 || channelArr.length() != 2) {
            Log.e(TAG, "the channels or channel parameter is invalid");
            return Collections.emptyList();
        } else if ((CHANNEL_A.equals(channelStrList.get(0)) && CHANNEL_B.equals(channelStrList.get(1))) || (CHANNEL_A.equals(channelStrList.get(1)) && CHANNEL_B.equals(channelStrList.get(0)))) {
            return channelStrList;
        } else {
            Log.e(TAG, "the channel parameter is invalid");
            return Collections.emptyList();
        }
        return channelStrList;
    }

    private int parseType(JSONObject item) throws JSONException {
        if (!item.has("type")) {
            Log.e(TAG, "parseType is failed, the type para is not exist");
            return -1;
        }
        String typeStr = item.getString("type");
        if ("transient".equals(typeStr)) {
            return 1;
        }
        if (CONTINUOUS.equals(typeStr)) {
            return 2;
        }
        Log.e(TAG, "the type parameter is not exist");
        return -1;
    }

    private int parseTimeDuration(JSONObject item, String para, int type) throws JSONException {
        if ("time".equals(para)) {
            if (item.has("time")) {
                return item.getInt(para);
            }
            Log.e(TAG, "the time parameter is not exist");
            return -1;
        } else if (!"duration".equals(para)) {
            Log.e(TAG, "parseTimeTypeDuration is failed, the para is invalid");
            return -1;
        } else if (item.has("duration")) {
            return item.getInt(para);
        } else {
            if (type == 1) {
                Log.i(TAG, "the duration parameter is default");
                return 0;
            }
            Log.e(TAG, "the duration parameter is not exist");
            return -1;
        }
    }

    private double parseIntensitySharpness(JSONObject item, String para) throws JSONException {
        if (INTENSITY.equals(para)) {
            if (item.has(INTENSITY)) {
                return item.getDouble(para);
            }
            Log.e(TAG, "the intensity parameter is not exist");
            return 0.0d;
        } else if (!SHARPNESS.equals(para)) {
            Log.e(TAG, "parseIntensitySharpness is failed, the para is invalid");
            return -1.0d;
        } else if (item.has(SHARPNESS)) {
            return item.getDouble(para);
        } else {
            Log.e(TAG, "the sharpness parameter is not exist");
            return 0.0d;
        }
    }

    private int parseAdjustCurve(List<Integer> curveTime, List<Double> curveValue, JSONObject curvePara, String para) throws JSONException {
        JSONObject temp = new JSONObject(curvePara.getString(para));
        if (temp.length() != 1) {
            Log.e(TAG, "the " + para + " item length is invalid");
            return -1;
        } else if (!temp.has(ADJUST_POINT)) {
            Log.e(TAG, "the adjustpoint parameter is not exist");
            return -1;
        } else {
            JSONArray adjustPointArr = temp.getJSONArray(ADJUST_POINT);
            HashSet<Integer> timeSet = new HashSet<>();
            for (int index = 0; index < adjustPointArr.length(); index++) {
                JSONObject item = adjustPointArr.getJSONObject(index);
                if (!checkParameterList(item, ADJUST_POINT_PARAMETER_LIST)) {
                    Log.e(TAG, "the adjustpoint parameter list is invalid");
                    return -1;
                }
                if (item.has("time")) {
                    if (item.has(PARAMETERS)) {
                        int timeTemp = item.getInt("time");
                        double paraTemp = item.getDouble(PARAMETERS);
                        if (timeTemp >= 0) {
                            if (timeTemp <= 1800000) {
                                if (INTENSITY.equals(para) && (paraTemp < 0.0d || paraTemp > 1000000.0d)) {
                                    Log.e(TAG, "the curve intensity value is invalid");
                                    return -1;
                                } else if (!SHARPNESS.equals(para) || (paraTemp >= -1.0d && paraTemp <= 1.0d)) {
                                    timeSet.add(Integer.valueOf(timeTemp));
                                    curveTime.add(Integer.valueOf(timeTemp));
                                    curveValue.add(Double.valueOf(paraTemp));
                                } else {
                                    Log.e(TAG, "the curve sharpness value is invalid");
                                    return -1;
                                }
                            }
                        }
                        Log.e(TAG, "the curve time value is invalid");
                        return -1;
                    }
                }
                Log.e(TAG, "the adjustpoint time or parameters is not exist");
                return -1;
            }
            if (curveTime.size() == timeSet.size()) {
                return 0;
            }
            Log.e(TAG, "the curve time value is invalid");
            return -1;
        }
    }
}
