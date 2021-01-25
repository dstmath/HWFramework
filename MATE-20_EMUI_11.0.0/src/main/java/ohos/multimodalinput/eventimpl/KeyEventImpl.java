package ohos.multimodalinput.eventimpl;

import java.util.HashMap;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.KeyEvent;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

/* access modifiers changed from: package-private */
public class KeyEventImpl extends KeyEvent {
    private static final Map<Integer, Integer> A2Z_KEYMAP = new HashMap();
    static final String EVENT_TYPE_KEY = "KeyEvent";
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218114065, "KeyEventImpl");
    static Sequenceable.Producer<KeyEvent> PRODUCER = new Sequenceable.Producer<KeyEvent>() {
        /* class ohos.multimodalinput.eventimpl.KeyEventImpl.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public KeyEvent createFromParcel(Parcel parcel) {
            KeyEventImpl keyEventImpl = new KeyEventImpl();
            keyEventImpl.unmarshalling(parcel);
            return keyEventImpl;
        }
    };
    private static final Map<Integer, Integer> Z2A_KEYMAP = new HashMap();
    private android.view.KeyEvent androidKeyEvent;

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public String getDeviceId() {
        return "";
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getSourceDevice() {
        return -1;
    }

    static {
        A2Z_KEYMAP.put(0, -1);
        A2Z_KEYMAP.put(3, 1);
        A2Z_KEYMAP.put(4, 2);
        A2Z_KEYMAP.put(5, 3);
        A2Z_KEYMAP.put(6, 4);
        A2Z_KEYMAP.put(28, 5);
        A2Z_KEYMAP.put(79, 6);
        A2Z_KEYMAP.put(80, 7);
        A2Z_KEYMAP.put(83, 8);
        A2Z_KEYMAP.put(84, 9);
        A2Z_KEYMAP.put(85, 10);
        A2Z_KEYMAP.put(86, 11);
        A2Z_KEYMAP.put(87, 12);
        A2Z_KEYMAP.put(88, 13);
        A2Z_KEYMAP.put(89, 14);
        A2Z_KEYMAP.put(90, 15);
        A2Z_KEYMAP.put(24, 16);
        A2Z_KEYMAP.put(25, 17);
        A2Z_KEYMAP.put(26, 18);
        A2Z_KEYMAP.put(27, 19);
        A2Z_KEYMAP.put(221, 40);
        A2Z_KEYMAP.put(220, 41);
        A2Z_KEYMAP.put(231, 20);
        for (Map.Entry<Integer, Integer> entry : A2Z_KEYMAP.entrySet()) {
            Z2A_KEYMAP.put(entry.getValue(), entry.getKey());
        }
    }

    KeyEventImpl() {
    }

    KeyEventImpl(android.view.KeyEvent keyEvent) {
        this.androidKeyEvent = keyEvent;
    }

    @Override // ohos.multimodalinput.event.KeyEvent
    public boolean isKeyDown() {
        return this.androidKeyEvent.getAction() == 0;
    }

    @Override // ohos.multimodalinput.event.KeyEvent
    public int getKeyCode() {
        int keyCode = this.androidKeyEvent.getKeyCode();
        if ((this.androidKeyEvent.getSource() & 8194) == 8194) {
            HiLog.debug(LOG_LABEL, "discard keycode from mouse: %{public}d", Integer.valueOf(keyCode));
            return -1;
        } else if (A2Z_KEYMAP.containsKey(Integer.valueOf(keyCode))) {
            return A2Z_KEYMAP.get(Integer.valueOf(keyCode)).intValue();
        } else {
            return -1;
        }
    }

    @Override // ohos.multimodalinput.event.KeyEvent
    public long getKeyDownDuration() {
        return this.androidKeyEvent.getEventTime() - this.androidKeyEvent.getDownTime();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getInputDeviceId() {
        return this.androidKeyEvent.getDeviceId();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public long getOccurredTime() {
        return this.androidKeyEvent.getEventTime();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        android.os.Parcel obtain = android.os.Parcel.obtain();
        boolean z = false;
        this.androidKeyEvent.writeToParcel(obtain, 0);
        obtain.setDataPosition(0);
        if (parcel.writeString(EVENT_TYPE_KEY) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeInt(obtain.readInt()) && parcel.writeLong(obtain.readLong()) && parcel.writeLong(obtain.readLong()) && parcel.writeString(obtain.readString())) {
            z = true;
        }
        obtain.recycle();
        return z;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        android.os.Parcel obtain = android.os.Parcel.obtain();
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeInt(parcel.readInt());
        obtain.writeLong(parcel.readLong());
        obtain.writeLong(parcel.readLong());
        obtain.writeString(parcel.readString());
        obtain.setDataPosition(0);
        this.androidKeyEvent = (android.view.KeyEvent) android.view.KeyEvent.CREATOR.createFromParcel(obtain);
        obtain.recycle();
        return true;
    }

    /* access modifiers changed from: package-private */
    public android.view.KeyEvent getHostKeyEvent() {
        return this.androidKeyEvent;
    }
}
