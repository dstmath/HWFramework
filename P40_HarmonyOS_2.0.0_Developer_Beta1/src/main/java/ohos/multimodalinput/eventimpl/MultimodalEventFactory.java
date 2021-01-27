package ohos.multimodalinput.eventimpl;

import android.view.KeyEvent;
import android.view.MotionEvent;
import java.util.Optional;
import ohos.global.icu.text.Collator;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.utils.Parcel;

public class MultimodalEventFactory {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218114065, "MultimodalEventFactory");

    private MultimodalEventFactory() {
    }

    public static Optional<MultimodalEvent> createEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            HiLog.error(LOG_LABEL, "invalid input android keyEvent", new Object[0]);
            return Optional.empty();
        }
        int source = motionEvent.getSource();
        if ((source & Collator.ReorderCodes.SYMBOL) == 4098) {
            return Optional.of(new TouchEventImpl(motionEvent));
        }
        if ((source & 8194) == 8194) {
            return Optional.of(new MouseEventImpl(motionEvent));
        }
        if ((source & 4194304) == 4194304) {
            return Optional.of(new RotationEventImpl(motionEvent));
        }
        HiLog.warn(LOG_LABEL, "unsupported event source: %{public}d", Integer.valueOf(source));
        return Optional.empty();
    }

    public static Optional<MultimodalEvent> createEvent(KeyEvent keyEvent) {
        if (keyEvent == null) {
            HiLog.error(LOG_LABEL, "invalid input android keyEvent", new Object[0]);
            return Optional.empty();
        }
        int keyCode = keyEvent.getKeyCode();
        if (KeyBoardEventImpl.isKeyBoardKeyCode(keyCode)) {
            return Optional.of(new KeyBoardEventImpl(keyEvent));
        }
        if (BuiltinKeyEventImpl.isBuiltInKeyCode(keyCode)) {
            return Optional.of(new BuiltinKeyEventImpl(keyEvent));
        }
        return Optional.of(new KeyEventImpl(keyEvent));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002c, code lost:
        if (r1.equals("KeyBoardEvent") == false) goto L_0x0039;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003c  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0050  */
    public static Optional<MultimodalEvent> createEvent(Parcel parcel) {
        boolean z = false;
        if (parcel == null) {
            HiLog.error(LOG_LABEL, "invalid input parcel", new Object[0]);
            return Optional.empty();
        }
        String readString = parcel.readString();
        int hashCode = readString.hashCode();
        if (hashCode != 541813659) {
            if (hashCode == 665678579) {
            }
        } else if (readString.equals("KeyEvent")) {
            z = true;
            if (z) {
                return Optional.of(KeyBoardEventImpl.PRODUCER.createFromParcel(parcel));
            }
            if (!z) {
                return Optional.empty();
            }
            return Optional.of(KeyEventImpl.PRODUCER.createFromParcel(parcel));
        }
        z = true;
        if (z) {
        }
    }

    public static Optional<KeyEvent> getHostKeyEvent(MultimodalEvent multimodalEvent) {
        if (multimodalEvent == null) {
            HiLog.error(LOG_LABEL, "event is null", new Object[0]);
            return Optional.empty();
        }
        KeyEvent keyEvent = null;
        if (multimodalEvent instanceof KeyEventImpl) {
            keyEvent = ((KeyEventImpl) multimodalEvent).getHostKeyEvent();
        } else if (multimodalEvent instanceof KeyBoardEventImpl) {
            keyEvent = ((KeyBoardEventImpl) multimodalEvent).getHostKeyEvent();
        } else {
            HiLog.error(LOG_LABEL, "invalid multimodal event, source:%{public}d", Integer.valueOf(multimodalEvent.getSourceDevice()));
        }
        if (keyEvent == null) {
            return Optional.empty();
        }
        return Optional.of(keyEvent);
    }

    public static Optional<MotionEvent> getHostMotionEvent(MultimodalEvent multimodalEvent) {
        if (multimodalEvent == null) {
            HiLog.error(LOG_LABEL, "event is null", new Object[0]);
            return Optional.empty();
        }
        MotionEvent motionEvent = null;
        if (multimodalEvent instanceof TouchEventImpl) {
            motionEvent = ((TouchEventImpl) multimodalEvent).getHostMotionEvent();
        } else if (multimodalEvent instanceof MouseEventImpl) {
            motionEvent = ((MouseEventImpl) multimodalEvent).getHostMotionEvent();
        } else if (multimodalEvent instanceof RotationEventImpl) {
            motionEvent = ((RotationEventImpl) multimodalEvent).getHostMotionEvent();
        } else {
            HiLog.error(LOG_LABEL, "invalid multimodal event, source:%{public}d", Integer.valueOf(multimodalEvent.getSourceDevice()));
        }
        if (motionEvent == null) {
            return Optional.empty();
        }
        return Optional.of(motionEvent);
    }

    public static Optional<ohos.multimodalinput.event.KeyEvent> createKeyEvent(int i, int i2) {
        if ((i != 0 && i != 1) || i2 <= 0 || i2 > 10008) {
            HiLog.error(LOG_LABEL, "unsupported create keyevent, action: %{public}d, keyCode: %{public}d", Integer.valueOf(i), Integer.valueOf(i2));
            return Optional.empty();
        } else if (KeyBoardEventImpl.isOhosKeyBoardKeyCode(i2)) {
            return Optional.of(new KeyBoardEventImpl(i, i2));
        } else {
            if (BuiltinKeyEventImpl.isOhosBuiltInKeyCode(i2)) {
                return Optional.of(new BuiltinKeyEventImpl(i, i2));
            }
            return Optional.of(new KeyEventImpl(i, i2));
        }
    }
}
