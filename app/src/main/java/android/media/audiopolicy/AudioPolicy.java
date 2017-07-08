package android.media.audiopolicy;

import android.Manifest.permission;
import android.content.Context;
import android.media.AudioFocusInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.IAudioService;
import android.media.audiopolicy.IAudioPolicyCallback.Stub;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;

public class AudioPolicy {
    private static final boolean DEBUG = false;
    public static final int FOCUS_POLICY_DUCKING_DEFAULT = 0;
    public static final int FOCUS_POLICY_DUCKING_IN_APP = 0;
    public static final int FOCUS_POLICY_DUCKING_IN_POLICY = 1;
    private static final int MSG_FOCUS_GRANT = 1;
    private static final int MSG_FOCUS_LOSS = 2;
    private static final int MSG_MIX_STATE_UPDATE = 3;
    private static final int MSG_POLICY_STATUS_CHANGE = 0;
    public static final int POLICY_STATUS_REGISTERED = 2;
    public static final int POLICY_STATUS_UNREGISTERED = 1;
    private static final String TAG = "AudioPolicy";
    private static IAudioService sService;
    private AudioPolicyConfig mConfig;
    private Context mContext;
    private final EventHandler mEventHandler;
    private AudioPolicyFocusListener mFocusListener;
    private final Object mLock;
    private final IAudioPolicyCallback mPolicyCb;
    private String mRegistrationId;
    private int mStatus;
    private AudioPolicyStatusListener mStatusListener;

    public static abstract class AudioPolicyFocusListener {
        public void onAudioFocusGrant(AudioFocusInfo afi, int requestResult) {
        }

        public void onAudioFocusLoss(AudioFocusInfo afi, boolean wasNotified) {
        }
    }

    public static abstract class AudioPolicyStatusListener {
        public void onStatusChange() {
        }

        public void onMixStateUpdate(AudioMix mix) {
        }
    }

    public static class Builder {
        private Context mContext;
        private AudioPolicyFocusListener mFocusListener;
        private Looper mLooper;
        private ArrayList<AudioMix> mMixes;
        private AudioPolicyStatusListener mStatusListener;

        public Builder(Context context) {
            this.mMixes = new ArrayList();
            this.mContext = context;
        }

        public Builder addMix(AudioMix mix) throws IllegalArgumentException {
            if (mix == null) {
                throw new IllegalArgumentException("Illegal null AudioMix argument");
            }
            this.mMixes.add(mix);
            return this;
        }

        public Builder setLooper(Looper looper) throws IllegalArgumentException {
            if (looper == null) {
                throw new IllegalArgumentException("Illegal null Looper argument");
            }
            this.mLooper = looper;
            return this;
        }

        public void setAudioPolicyFocusListener(AudioPolicyFocusListener l) {
            this.mFocusListener = l;
        }

        public void setAudioPolicyStatusListener(AudioPolicyStatusListener l) {
            this.mStatusListener = l;
        }

        public AudioPolicy build() {
            if (this.mStatusListener != null) {
                for (AudioMix mix : this.mMixes) {
                    mix.mCallbackFlags |= AudioPolicy.POLICY_STATUS_UNREGISTERED;
                }
            }
            return new AudioPolicy(this.mContext, this.mLooper, this.mFocusListener, this.mStatusListener, null);
        }
    }

    private class EventHandler extends Handler {
        public EventHandler(AudioPolicy ap, Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = AudioPolicy.DEBUG;
            switch (msg.what) {
                case AudioPolicy.MSG_POLICY_STATUS_CHANGE /*0*/:
                    AudioPolicy.this.onPolicyStatusChange();
                case AudioPolicy.POLICY_STATUS_UNREGISTERED /*1*/:
                    if (AudioPolicy.this.mFocusListener != null) {
                        AudioPolicy.this.mFocusListener.onAudioFocusGrant((AudioFocusInfo) msg.obj, msg.arg1);
                    }
                case AudioPolicy.POLICY_STATUS_REGISTERED /*2*/:
                    if (AudioPolicy.this.mFocusListener != null) {
                        AudioPolicyFocusListener -get1 = AudioPolicy.this.mFocusListener;
                        AudioFocusInfo audioFocusInfo = (AudioFocusInfo) msg.obj;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        -get1.onAudioFocusLoss(audioFocusInfo, z);
                    }
                case AudioPolicy.MSG_MIX_STATE_UPDATE /*3*/:
                    if (AudioPolicy.this.mStatusListener != null) {
                        AudioPolicy.this.mStatusListener.onMixStateUpdate((AudioMix) msg.obj);
                    }
                default:
                    Log.e(AudioPolicy.TAG, "Unknown event " + msg.what);
            }
        }
    }

    public AudioPolicyConfig getConfig() {
        return this.mConfig;
    }

    public boolean hasFocusListener() {
        return this.mFocusListener != null ? true : DEBUG;
    }

    private AudioPolicy(AudioPolicyConfig config, Context context, Looper looper, AudioPolicyFocusListener fl, AudioPolicyStatusListener sl) {
        this.mLock = new Object();
        this.mPolicyCb = new Stub() {
            public void notifyAudioFocusGrant(AudioFocusInfo afi, int requestResult) {
                AudioPolicy.this.sendMsg(AudioPolicy.POLICY_STATUS_UNREGISTERED, afi, requestResult);
            }

            public void notifyAudioFocusLoss(AudioFocusInfo afi, boolean wasNotified) {
                AudioPolicy.this.sendMsg(AudioPolicy.POLICY_STATUS_REGISTERED, afi, wasNotified ? AudioPolicy.POLICY_STATUS_UNREGISTERED : AudioPolicy.MSG_POLICY_STATUS_CHANGE);
            }

            public void notifyMixStateUpdate(String regId, int state) {
                for (AudioMix mix : AudioPolicy.this.mConfig.getMixes()) {
                    if (mix.getRegistration().equals(regId)) {
                        mix.mMixState = state;
                        AudioPolicy.this.sendMsg(AudioPolicy.MSG_MIX_STATE_UPDATE, mix, AudioPolicy.MSG_POLICY_STATUS_CHANGE);
                    }
                }
            }
        };
        this.mConfig = config;
        this.mStatus = POLICY_STATUS_UNREGISTERED;
        this.mContext = context;
        if (looper == null) {
            looper = Looper.getMainLooper();
        }
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            this.mEventHandler = null;
            Log.e(TAG, "No event handler due to looper without a thread");
        }
        this.mFocusListener = fl;
        this.mStatusListener = sl;
    }

    public void setRegistration(String regId) {
        synchronized (this.mLock) {
            this.mRegistrationId = regId;
            this.mConfig.setRegistration(regId);
            if (regId != null) {
                this.mStatus = POLICY_STATUS_REGISTERED;
            } else {
                this.mStatus = POLICY_STATUS_UNREGISTERED;
            }
        }
        sendMsg(MSG_POLICY_STATUS_CHANGE);
    }

    private boolean policyReadyToUse() {
        synchronized (this.mLock) {
            if (this.mStatus != POLICY_STATUS_REGISTERED) {
                Log.e(TAG, "Cannot use unregistered AudioPolicy");
                return DEBUG;
            } else if (this.mContext == null) {
                Log.e(TAG, "Cannot use AudioPolicy without context");
                return DEBUG;
            } else if (this.mRegistrationId == null) {
                Log.e(TAG, "Cannot use unregistered AudioPolicy");
                return DEBUG;
            } else {
                if (this.mContext.checkCallingOrSelfPermission(permission.MODIFY_AUDIO_ROUTING) == 0) {
                    return true;
                }
                Slog.w(TAG, "Cannot use AudioPolicy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", needs MODIFY_AUDIO_ROUTING");
                return DEBUG;
            }
        }
    }

    private void checkMixReadyToUse(AudioMix mix, boolean forTrack) throws IllegalArgumentException {
        if (mix == null) {
            String msg;
            if (forTrack) {
                msg = "Invalid null AudioMix for AudioTrack creation";
            } else {
                msg = "Invalid null AudioMix for AudioRecord creation";
            }
            throw new IllegalArgumentException(msg);
        } else if (!this.mConfig.mMixes.contains(mix)) {
            throw new IllegalArgumentException("Invalid mix: not part of this policy");
        } else if ((mix.getRouteFlags() & POLICY_STATUS_REGISTERED) != POLICY_STATUS_REGISTERED) {
            throw new IllegalArgumentException("Invalid AudioMix: not defined for loop back");
        } else if (forTrack && mix.getMixType() != POLICY_STATUS_UNREGISTERED) {
            throw new IllegalArgumentException("Invalid AudioMix: not defined for being a recording source");
        } else if (!forTrack && mix.getMixType() != 0) {
            throw new IllegalArgumentException("Invalid AudioMix: not defined for capturing playback");
        }
    }

    public int getFocusDuckingBehavior() {
        return this.mConfig.mDuckingPolicy;
    }

    public int setFocusDuckingBehavior(int behavior) throws IllegalArgumentException, IllegalStateException {
        if (behavior == 0 || behavior == POLICY_STATUS_UNREGISTERED) {
            int status;
            synchronized (this.mLock) {
                if (this.mStatus != POLICY_STATUS_REGISTERED) {
                    throw new IllegalStateException("Cannot change ducking behavior for unregistered policy");
                }
                if (behavior == POLICY_STATUS_UNREGISTERED) {
                    if (this.mFocusListener == null) {
                        throw new IllegalStateException("Cannot handle ducking without an audio focus listener");
                    }
                }
                try {
                    status = getService().setFocusPropertiesForPolicy(behavior, cb());
                    if (status == 0) {
                        this.mConfig.mDuckingPolicy = behavior;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Dead object in setFocusPropertiesForPolicy for behavior", e);
                    return -1;
                }
            }
            return status;
        }
        throw new IllegalArgumentException("Invalid ducking behavior " + behavior);
    }

    public AudioRecord createAudioRecordSink(AudioMix mix) throws IllegalArgumentException {
        if (policyReadyToUse()) {
            checkMixReadyToUse(mix, DEBUG);
            return new AudioRecord(new android.media.AudioAttributes.Builder().setInternalCapturePreset(8).addTag(addressForTag(mix)).build(), new android.media.AudioFormat.Builder(mix.getFormat()).setChannelMask(AudioFormat.inChannelMaskFromOutChannelMask(mix.getFormat().getChannelMask())).build(), AudioRecord.getMinBufferSize(mix.getFormat().getSampleRate(), 12, mix.getFormat().getEncoding()), MSG_POLICY_STATUS_CHANGE);
        }
        Log.e(TAG, "Cannot create AudioRecord sink for AudioMix");
        return null;
    }

    public AudioTrack createAudioTrackSource(AudioMix mix) throws IllegalArgumentException {
        if (policyReadyToUse()) {
            checkMixReadyToUse(mix, true);
            return new AudioTrack(new android.media.AudioAttributes.Builder().setUsage(15).addTag(addressForTag(mix)).build(), mix.getFormat(), AudioTrack.getMinBufferSize(mix.getFormat().getSampleRate(), mix.getFormat().getChannelMask(), mix.getFormat().getEncoding()), POLICY_STATUS_UNREGISTERED, MSG_POLICY_STATUS_CHANGE);
        }
        Log.e(TAG, "Cannot create AudioTrack source for AudioMix");
        return null;
    }

    public int getStatus() {
        return this.mStatus;
    }

    private void onPolicyStatusChange() {
        synchronized (this.mLock) {
            if (this.mStatusListener == null) {
                return;
            }
            AudioPolicyStatusListener l = this.mStatusListener;
            l.onStatusChange();
        }
    }

    public IAudioPolicyCallback cb() {
        return this.mPolicyCb;
    }

    private static String addressForTag(AudioMix mix) {
        return "addr=" + mix.getRegistration();
    }

    private void sendMsg(int msg) {
        if (this.mEventHandler != null) {
            this.mEventHandler.sendEmptyMessage(msg);
        }
    }

    private void sendMsg(int msg, Object obj, int i) {
        if (this.mEventHandler != null) {
            this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(msg, i, MSG_POLICY_STATUS_CHANGE, obj));
        }
    }

    private static IAudioService getService() {
        if (sService != null) {
            return sService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE));
        return sService;
    }

    public String toLogFriendlyString() {
        return new String("android.media.audiopolicy.AudioPolicy:\n") + "config=" + this.mConfig.toLogFriendlyString();
    }
}
