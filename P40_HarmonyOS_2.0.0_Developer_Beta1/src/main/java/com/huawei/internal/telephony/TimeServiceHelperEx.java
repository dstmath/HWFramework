package com.huawei.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.TimeServiceHelper;

public class TimeServiceHelperEx {
    private ListenerEx mListenerEx;
    private TimeServiceHelper mTimeServiceHelper;

    public TimeServiceHelperEx(Context context) {
        this.mTimeServiceHelper = new TimeServiceHelper(context);
    }

    public void setListener(ListenerEx listenerEx) {
        this.mTimeServiceHelper.setListener(listenerEx.getListener());
    }

    public class ListenerEx {
        private TimeServiceHelper.Listener mListener = new TimeServiceHelper.Listener() {
            /* class com.huawei.internal.telephony.TimeServiceHelperEx.ListenerEx.AnonymousClass1 */

            public void onTimeDetectionChange(boolean enabled) {
                ListenerEx.this.onTimeDetectionChange(enabled);
            }

            public void onTimeZoneDetectionChange(boolean enabled) {
                ListenerEx.this.onTimeZoneDetectionChange(enabled);
            }
        };

        public ListenerEx() {
        }

        public void onTimeDetectionChange(boolean enabled) {
        }

        public void onTimeZoneDetectionChange(boolean enabled) {
        }

        public TimeServiceHelper.Listener getListener() {
            return this.mListener;
        }
    }

    public static void setDeviceTimeZoneStatic(Context context, String zoneId) {
        TimeServiceHelper.setDeviceTimeZoneStaticHw(context, zoneId);
    }
}
