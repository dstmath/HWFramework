package ohos.aafwk.utils.dfx.time;

public enum TimeEventType {
    LIFECYCLE_START,
    LIFECYCLE_ACTIVE,
    LIFECYCLE_INACTIVE,
    LIFECYCLE_BACKGROUND,
    LIFECYCLE_FOREGROUND,
    LIFECYCLE_STOP,
    LIFECYCLE(20),
    LOAD(5),
    ANONYMOUS(1),
    WINDOW_LOAD,
    WINDOW_SHOW,
    WINDOW_CREATE,
    WINDOW_DESTROY,
    WINDOW_SET_UI(5),
    EVENT(5);
    
    private int size;

    private TimeEventType(int i) {
        this.size = i;
    }

    private TimeEventType() {
        this.size = 0;
    }

    /* access modifiers changed from: package-private */
    public int getSize() {
        return this.size;
    }

    /* access modifiers changed from: package-private */
    public void setSize(int i) {
        this.size = i;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.aafwk.utils.dfx.time.TimeEventType$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$aafwk$utils$dfx$time$TimeEventType = new int[TimeEventType.values().length];

        static {
            try {
                $SwitchMap$ohos$aafwk$utils$dfx$time$TimeEventType[TimeEventType.LIFECYCLE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$aafwk$utils$dfx$time$TimeEventType[TimeEventType.LOAD.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    static TimeEventType[] getSubEvents(TimeEventType timeEventType) {
        int i = AnonymousClass1.$SwitchMap$ohos$aafwk$utils$dfx$time$TimeEventType[timeEventType.ordinal()];
        return i != 1 ? i != 2 ? new TimeEventType[0] : new TimeEventType[]{LOAD} : new TimeEventType[]{LIFECYCLE_START, LIFECYCLE_ACTIVE, LIFECYCLE_INACTIVE, LIFECYCLE_BACKGROUND, LIFECYCLE_FOREGROUND, LIFECYCLE_STOP};
    }
}
