package com.android.server.audio;

import android.media.AudioManager;
import android.media.AudioSystem;
import com.android.server.audio.AudioDeviceInventory;
import com.android.server.audio.AudioEventLogger;

public class AudioServiceEvents {

    /* access modifiers changed from: package-private */
    public static final class PhoneStateEvent extends AudioEventLogger.Event {
        final int mActualMode;
        final int mOwnerPid;
        final String mPackage;
        final int mRequestedMode;
        final int mRequesterPid;

        PhoneStateEvent(String callingPackage, int requesterPid, int requestedMode, int ownerPid, int actualMode) {
            this.mPackage = callingPackage;
            this.mRequesterPid = requesterPid;
            this.mRequestedMode = requestedMode;
            this.mOwnerPid = ownerPid;
            this.mActualMode = actualMode;
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            return "setMode(" + AudioSystem.modeToString(this.mRequestedMode) + ") from package=" + this.mPackage + " pid=" + this.mRequesterPid + " selected mode=" + AudioSystem.modeToString(this.mActualMode) + " by pid=" + this.mOwnerPid;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class WiredDevConnectEvent extends AudioEventLogger.Event {
        final AudioDeviceInventory.WiredDeviceConnectionState mState;

        WiredDevConnectEvent(AudioDeviceInventory.WiredDeviceConnectionState state) {
            this.mState = state;
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            return "setWiredDeviceConnectionState( type:" + Integer.toHexString(this.mState.mType) + " state:" + AudioSystem.deviceStateToString(this.mState.mState) + " addr:" + this.mState.mAddress + " name:" + this.mState.mName + ") from " + this.mState.mCaller;
        }
    }

    static final class ForceUseEvent extends AudioEventLogger.Event {
        final int mConfig;
        final String mReason;
        final int mUsage;

        ForceUseEvent(int usage, int config, String reason) {
            this.mUsage = usage;
            this.mConfig = config;
            this.mReason = reason;
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            return "setForceUse(" + AudioSystem.forceUseUsageToString(this.mUsage) + ", " + AudioSystem.forceUseConfigToString(this.mConfig) + ") due to " + this.mReason;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class VolumeEvent extends AudioEventLogger.Event {
        static final int VOL_ADJUST_STREAM_VOL = 1;
        static final int VOL_ADJUST_SUGG_VOL = 0;
        static final int VOL_ADJUST_VOL_UID = 5;
        static final int VOL_MODE_CHANGE_HEARING_AID = 7;
        static final int VOL_SET_AVRCP_VOL = 4;
        static final int VOL_SET_HEARING_AID_VOL = 3;
        static final int VOL_SET_STREAM_VOL = 2;
        static final int VOL_VOICE_ACTIVITY_HEARING_AID = 6;
        final String mCaller;
        final int mOp;
        final int mStream;
        final int mVal1;
        final int mVal2;

        VolumeEvent(int op, int stream, int val1, int val2, String caller) {
            this.mOp = op;
            this.mStream = stream;
            this.mVal1 = val1;
            this.mVal2 = val2;
            this.mCaller = caller;
        }

        VolumeEvent(int op, int index, int gainDb) {
            this.mOp = op;
            this.mVal1 = index;
            this.mVal2 = gainDb;
            this.mStream = -1;
            this.mCaller = null;
        }

        VolumeEvent(int op, int index) {
            this.mOp = op;
            this.mVal1 = index;
            this.mVal2 = 0;
            this.mStream = -1;
            this.mCaller = null;
        }

        VolumeEvent(int op, boolean voiceActive, int stream, int index) {
            this.mOp = op;
            this.mStream = stream;
            this.mVal1 = index;
            this.mVal2 = voiceActive ? 1 : 0;
            this.mCaller = null;
        }

        VolumeEvent(int op, int mode, int stream, int index) {
            this.mOp = op;
            this.mStream = stream;
            this.mVal1 = index;
            this.mVal2 = mode;
            this.mCaller = null;
        }

        @Override // com.android.server.audio.AudioEventLogger.Event
        public String eventToString() {
            switch (this.mOp) {
                case 0:
                    return "adjustSuggestedStreamVolume(sugg:" + AudioSystem.streamToString(this.mStream) + " dir:" + AudioManager.adjustToString(this.mVal1) + " flags:0x" + Integer.toHexString(this.mVal2) + ") from " + this.mCaller;
                case 1:
                    return "adjustStreamVolume(stream:" + AudioSystem.streamToString(this.mStream) + " dir:" + AudioManager.adjustToString(this.mVal1) + " flags:0x" + Integer.toHexString(this.mVal2) + ") from " + this.mCaller;
                case 2:
                    return "setStreamVolume(stream:" + AudioSystem.streamToString(this.mStream) + " index:" + this.mVal1 + " flags:0x" + Integer.toHexString(this.mVal2) + ") from " + this.mCaller;
                case 3:
                    return "setHearingAidVolume: index:" + this.mVal1 + " gain dB:" + this.mVal2;
                case 4:
                    return "setAvrcpVolume: index:" + this.mVal1;
                case 5:
                    return "adjustStreamVolumeForUid(stream:" + AudioSystem.streamToString(this.mStream) + " dir:" + AudioManager.adjustToString(this.mVal1) + " flags:0x" + Integer.toHexString(this.mVal2) + ") from " + this.mCaller;
                case 6:
                    StringBuilder sb = new StringBuilder("Voice activity change (");
                    sb.append(this.mVal2 == 1 ? "active" : "inactive");
                    sb.append(") causes setting HEARING_AID volume to idx:");
                    sb.append(this.mVal1);
                    sb.append(" stream:");
                    sb.append(AudioSystem.streamToString(this.mStream));
                    return sb.toString();
                case 7:
                    return "setMode(" + AudioSystem.modeToString(this.mVal2) + ") causes setting HEARING_AID volume to idx:" + this.mVal1 + " stream:" + AudioSystem.streamToString(this.mStream);
                default:
                    return "FIXME invalid op:" + this.mOp;
            }
        }
    }
}
