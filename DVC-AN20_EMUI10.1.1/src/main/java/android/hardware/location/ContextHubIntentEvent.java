package android.hardware.location;

import android.annotation.SystemApi;
import android.content.Intent;
import com.android.internal.util.Preconditions;

@SystemApi
public class ContextHubIntentEvent {
    private final ContextHubInfo mContextHubInfo;
    private final int mEventType;
    private final int mNanoAppAbortCode;
    private final long mNanoAppId;
    private final NanoAppMessage mNanoAppMessage;

    private ContextHubIntentEvent(ContextHubInfo contextHubInfo, int eventType, long nanoAppId, NanoAppMessage nanoAppMessage, int nanoAppAbortCode) {
        this.mContextHubInfo = contextHubInfo;
        this.mEventType = eventType;
        this.mNanoAppId = nanoAppId;
        this.mNanoAppMessage = nanoAppMessage;
        this.mNanoAppAbortCode = nanoAppAbortCode;
    }

    private ContextHubIntentEvent(ContextHubInfo contextHubInfo, int eventType) {
        this(contextHubInfo, eventType, -1, null, -1);
    }

    private ContextHubIntentEvent(ContextHubInfo contextHubInfo, int eventType, long nanoAppId) {
        this(contextHubInfo, eventType, nanoAppId, null, -1);
    }

    private ContextHubIntentEvent(ContextHubInfo contextHubInfo, int eventType, long nanoAppId, NanoAppMessage nanoAppMessage) {
        this(contextHubInfo, eventType, nanoAppId, nanoAppMessage, -1);
    }

    private ContextHubIntentEvent(ContextHubInfo contextHubInfo, int eventType, long nanoAppId, int nanoAppAbortCode) {
        this(contextHubInfo, eventType, nanoAppId, null, nanoAppAbortCode);
    }

    public static ContextHubIntentEvent fromIntent(Intent intent) {
        Preconditions.checkNotNull(intent, "Intent cannot be null");
        hasExtraOrThrow(intent, ContextHubManager.EXTRA_CONTEXT_HUB_INFO);
        ContextHubInfo info = (ContextHubInfo) intent.getParcelableExtra(ContextHubManager.EXTRA_CONTEXT_HUB_INFO);
        if (info != null) {
            int eventType = getIntExtraOrThrow(intent, ContextHubManager.EXTRA_EVENT_TYPE);
            switch (eventType) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    long nanoAppId = getLongExtraOrThrow(intent, ContextHubManager.EXTRA_NANOAPP_ID);
                    if (eventType == 5) {
                        hasExtraOrThrow(intent, ContextHubManager.EXTRA_MESSAGE);
                        NanoAppMessage message = (NanoAppMessage) intent.getParcelableExtra(ContextHubManager.EXTRA_MESSAGE);
                        if (message != null) {
                            return new ContextHubIntentEvent(info, eventType, nanoAppId, message);
                        }
                        throw new IllegalArgumentException("NanoAppMessage extra was null");
                    } else if (eventType == 4) {
                        return new ContextHubIntentEvent(info, eventType, nanoAppId, getIntExtraOrThrow(intent, ContextHubManager.EXTRA_NANOAPP_ABORT_CODE));
                    } else {
                        return new ContextHubIntentEvent(info, eventType, nanoAppId);
                    }
                case 6:
                    return new ContextHubIntentEvent(info, eventType);
                default:
                    throw new IllegalArgumentException("Unknown intent event type " + eventType);
            }
        } else {
            throw new IllegalArgumentException("ContextHubInfo extra was null");
        }
    }

    public int getEventType() {
        return this.mEventType;
    }

    public ContextHubInfo getContextHubInfo() {
        return this.mContextHubInfo;
    }

    public long getNanoAppId() {
        if (this.mEventType != 6) {
            return this.mNanoAppId;
        }
        throw new UnsupportedOperationException("Cannot invoke getNanoAppId() on Context Hub reset event");
    }

    public int getNanoAppAbortCode() {
        if (this.mEventType == 4) {
            return this.mNanoAppAbortCode;
        }
        throw new UnsupportedOperationException("Cannot invoke getNanoAppAbortCode() on non-abort event: " + this.mEventType);
    }

    public NanoAppMessage getNanoAppMessage() {
        if (this.mEventType == 5) {
            return this.mNanoAppMessage;
        }
        throw new UnsupportedOperationException("Cannot invoke getNanoAppMessage() on non-message event: " + this.mEventType);
    }

    public String toString() {
        String out = "ContextHubIntentEvent[eventType = " + this.mEventType + ", contextHubId = " + this.mContextHubInfo.getId();
        if (this.mEventType != 6) {
            out = out + ", nanoAppId = 0x" + Long.toHexString(this.mNanoAppId);
        }
        if (this.mEventType == 4) {
            out = out + ", nanoAppAbortCode = " + this.mNanoAppAbortCode;
        }
        if (this.mEventType == 5) {
            out = out + ", nanoAppMessage = " + this.mNanoAppMessage;
        }
        return out + "]";
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (object == this) {
            return true;
        }
        if (!(object instanceof ContextHubIntentEvent)) {
            return false;
        }
        ContextHubIntentEvent other = (ContextHubIntentEvent) object;
        if (other.getEventType() != this.mEventType || !other.getContextHubInfo().equals(this.mContextHubInfo)) {
            return false;
        }
        boolean isEqual = true;
        try {
            if (this.mEventType != 6) {
                isEqual = true & (other.getNanoAppId() == this.mNanoAppId);
            }
            if (this.mEventType == 4) {
                if (other.getNanoAppAbortCode() != this.mNanoAppAbortCode) {
                    z = false;
                }
                isEqual = z & isEqual;
            }
            if (this.mEventType == 5) {
                return other.getNanoAppMessage().equals(this.mNanoAppMessage) & isEqual;
            }
            return isEqual;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    private static void hasExtraOrThrow(Intent intent, String extra) {
        if (!intent.hasExtra(extra)) {
            throw new IllegalArgumentException("Intent did not have extra: " + extra);
        }
    }

    private static int getIntExtraOrThrow(Intent intent, String extra) {
        hasExtraOrThrow(intent, extra);
        return intent.getIntExtra(extra, -1);
    }

    private static long getLongExtraOrThrow(Intent intent, String extra) {
        hasExtraOrThrow(intent, extra);
        return intent.getLongExtra(extra, -1);
    }
}
