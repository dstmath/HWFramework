package ohos.bundle;

import java.util.HashMap;
import java.util.Map;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ShortcutIntent implements Sequenceable {
    public static final Sequenceable.Producer<ShortcutIntent> PRODUCER = $$Lambda$ShortcutIntent$O9nYek7Qo674FKz1njirBTsgaZU.INSTANCE;
    private Map<String, String> params = new HashMap();
    private String targetBundle = "";
    private String targetClass = "";

    public ShortcutIntent() {
    }

    public ShortcutIntent(ShortcutIntent shortcutIntent) {
        this.targetBundle = shortcutIntent.targetBundle;
        this.targetClass = shortcutIntent.targetClass;
    }

    public ShortcutIntent(String str, String str2) {
        this.targetBundle = str;
        this.targetClass = str2;
    }

    static /* synthetic */ ShortcutIntent lambda$static$0(Parcel parcel) {
        ShortcutIntent shortcutIntent = new ShortcutIntent();
        shortcutIntent.unmarshalling(parcel);
        return shortcutIntent;
    }

    public void setTargetBundle(String str) {
        this.targetBundle = str;
    }

    public String getTargetBundle() {
        return this.targetBundle;
    }

    public void setTargetClass(String str) {
        this.targetClass = str;
    }

    public String getTargetClass() {
        return this.targetClass;
    }

    public Map<String, String> getParams() {
        return this.params;
    }

    public void addParam(String str, String str2) {
        this.params.put(str, str2);
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel.writeString(this.targetBundle) && parcel.writeString(this.targetClass)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.targetBundle = parcel.readString();
        this.targetClass = parcel.readString();
        return true;
    }
}
