package android.hardware.hdmi;

import android.hardware.hdmi.HdmiRecordSources.RecordSource;

public abstract class HdmiRecordListener {

    public static class TimerStatusData {
        private int mDurationHour;
        private int mDurationMinute;
        private int mExtraError;
        private int mMediaInfo;
        private int mNotProgrammedError;
        private boolean mOverlapped;
        private boolean mProgrammed;
        private int mProgrammedInfo;

        static TimerStatusData parseFrom(int result) {
            boolean z = true;
            TimerStatusData data = new TimerStatusData();
            data.mOverlapped = ((result >> 31) & 1) != 0;
            data.mMediaInfo = (result >> 29) & 3;
            if (((result >> 28) & 1) == 0) {
                z = false;
            }
            data.mProgrammed = z;
            if (data.mProgrammed) {
                data.mProgrammedInfo = (result >> 24) & 15;
                data.mDurationHour = bcdByteToInt((byte) ((result >> 16) & 255));
                data.mDurationMinute = bcdByteToInt((byte) ((result >> 8) & 255));
            } else {
                data.mNotProgrammedError = (result >> 24) & 15;
                data.mDurationHour = bcdByteToInt((byte) ((result >> 16) & 255));
                data.mDurationMinute = bcdByteToInt((byte) ((result >> 8) & 255));
            }
            data.mExtraError = result & 255;
            return data;
        }

        private static int bcdByteToInt(byte value) {
            return ((((value >> 4) & 15) * 10) + value) & 15;
        }

        private TimerStatusData() {
        }

        public boolean isOverlapped() {
            return this.mOverlapped;
        }

        public int getMediaInfo() {
            return this.mMediaInfo;
        }

        public boolean isProgrammed() {
            return this.mProgrammed;
        }

        public int getProgrammedInfo() {
            if (isProgrammed()) {
                return this.mProgrammedInfo;
            }
            throw new IllegalStateException("No programmed info. Call getNotProgammedError() instead.");
        }

        public int getNotProgammedError() {
            if (!isProgrammed()) {
                return this.mNotProgrammedError;
            }
            throw new IllegalStateException("Has no not-programmed error. Call getProgrammedInfo() instead.");
        }

        public int getDurationHour() {
            return this.mDurationHour;
        }

        public int getDurationMinute() {
            return this.mDurationMinute;
        }

        public int getExtraError() {
            return this.mExtraError;
        }
    }

    public abstract RecordSource onOneTouchRecordSourceRequested(int i);

    public void onOneTouchRecordResult(int recorderAddress, int result) {
    }

    public void onTimerRecordingResult(int recorderAddress, TimerStatusData data) {
    }

    public void onClearTimerRecordingResult(int recorderAddress, int result) {
    }
}
