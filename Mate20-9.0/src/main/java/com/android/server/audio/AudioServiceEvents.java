package com.android.server.audio;

import android.media.AudioManager;
import android.media.AudioSystem;
import com.android.server.audio.AudioEventLogger;
import com.android.server.audio.AudioService;

public class AudioServiceEvents {

    static final class ForceUseEvent extends AudioEventLogger.Event {
        final int mConfig;
        final String mReason;
        final int mUsage;

        ForceUseEvent(int usage, int config, String reason) {
            this.mUsage = usage;
            this.mConfig = config;
            this.mReason = reason;
        }

        public String eventToString() {
            return "setForceUse(" + AudioSystem.forceUseUsageToString(this.mUsage) + ", " + AudioSystem.forceUseConfigToString(this.mConfig) + ") due to " + this.mReason;
        }
    }

    static final class PhoneStateEvent extends AudioEventLogger.Event {
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

        public String eventToString() {
            return "setMode(" + AudioSystem.modeToString(this.mRequestedMode) + ") from package=" + this.mPackage + " pid=" + this.mRequesterPid + " selected mode=" + AudioSystem.modeToString(this.mActualMode) + " by pid=" + this.mOwnerPid;
        }
    }

    static final class VolumeEvent extends AudioEventLogger.Event {
        static final int VOL_ADJUST_STREAM_VOL = 1;
        static final int VOL_ADJUST_SUGG_VOL = 0;
        static final int VOL_SET_STREAM_VOL = 2;
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

        public String eventToString() {
            switch (this.mOp) {
                case 0:
                    return "adjustSuggestedStreamVolume(sugg:" + AudioSystem.streamToString(this.mStream) + " dir:" + AudioManager.adjustToString(this.mVal1) + " flags:0x" + Integer.toHexString(this.mVal2) + ") from " + this.mCaller;
                case 1:
                    return "adjustStreamVolume(stream:" + AudioSystem.streamToString(this.mStream) + " dir:" + AudioManager.adjustToString(this.mVal1) + " flags:0x" + Integer.toHexString(this.mVal2) + ") from " + this.mCaller;
                case 2:
                    return "setStreamVolume(stream:" + AudioSystem.streamToString(this.mStream) + " index:" + this.mVal1 + " flags:0x" + Integer.toHexString(this.mVal2) + ") from " + this.mCaller;
                default:
                    return "FIXME invalid op:" + this.mOp;
            }
        }
    }

    static final class WiredDevConnectEvent extends AudioEventLogger.Event {
        final AudioService.WiredDeviceConnectionState mState;

        WiredDevConnectEvent(AudioService.WiredDeviceConnectionState state) {
            this.mState = state;
        }

        public String eventToString() {
            return "setWiredDeviceConnectionState(" + " type:" + Integer.toHexString(this.mState.mType) + " state:" + AudioSystem.deviceStateToString(this.mState.mState) + " addr:" + this.mState.mAddress + " name:" + this.mState.mName + ") from " + this.mState.mCaller;
        }
    }
}
