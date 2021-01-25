package ohos.bundle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ShortcutInfo implements Sequenceable {
    public static final Sequenceable.Producer<ShortcutInfo> PRODUCER = $$Lambda$ShortcutInfo$MsAYSLTdD1A3lHk8XtJXzMQwkM.INSTANCE;
    private static final int SHORTCUT_INTENT_MAX = 1024;
    private String bundleName;
    private String disableMessage;
    private String hostAbility;
    private String icon;
    private int iconId = -1;
    private InputStream iconStream;
    private String id;
    private List<ShortcutIntent> intents = new ArrayList();
    private String label;
    private int labelId = -1;

    public ShortcutInfo() {
    }

    public ShortcutInfo(ShortcutInfo shortcutInfo) {
        this.id = shortcutInfo.id;
        this.bundleName = shortcutInfo.bundleName;
        this.hostAbility = shortcutInfo.hostAbility;
        this.icon = shortcutInfo.icon;
        this.iconId = shortcutInfo.iconId;
        this.label = shortcutInfo.label;
        this.labelId = shortcutInfo.labelId;
        this.intents = shortcutInfo.intents;
        this.disableMessage = shortcutInfo.disableMessage;
        this.iconStream = shortcutInfo.iconStream;
    }

    static /* synthetic */ ShortcutInfo lambda$static$0(Parcel parcel) {
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        shortcutInfo.unmarshalling(parcel);
        return shortcutInfo;
    }

    public void setId(String str) {
        this.id = str;
    }

    public String getId() {
        return this.id;
    }

    public void setBundleName(String str) {
        this.bundleName = str;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setHostAbilityName(String str) {
        this.hostAbility = str;
    }

    public String getHostAbilityName() {
        return this.hostAbility;
    }

    public void setIcon(String str) {
        this.icon = str;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setLabel(String str) {
        this.label = str;
    }

    public String getLabel() {
        return this.label;
    }

    public void setDisableMessage(String str) {
        this.disableMessage = str;
    }

    public String getDisableMessage() {
        return this.disableMessage;
    }

    public void setIntents(List<ShortcutIntent> list) {
        this.intents = list;
    }

    public List<ShortcutIntent> getIntents() {
        return this.intents;
    }

    public void setIconStream(InputStream inputStream) {
        this.iconStream = inputStream;
    }

    public InputStream getIconStream() {
        return this.iconStream;
    }

    public void setShortcutIconId(int i) {
        this.iconId = i;
    }

    public int getShortcutIconId() {
        return this.iconId;
    }

    public int getShortcutLabelId() {
        return this.labelId;
    }

    public boolean marshalling(Parcel parcel) {
        if (!(parcel.writeString(this.id) && parcel.writeString(this.bundleName) && parcel.writeString(this.hostAbility) && parcel.writeString(this.icon) && parcel.writeInt(this.iconId) && parcel.writeString(this.label) && parcel.writeInt(this.labelId) && parcel.writeInt(this.intents.size()))) {
            return false;
        }
        for (ShortcutIntent shortcutIntent : this.intents) {
            parcel.writeSequenceable(shortcutIntent);
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.id = parcel.readString();
        this.bundleName = parcel.readString();
        this.hostAbility = parcel.readString();
        this.icon = parcel.readString();
        this.iconId = parcel.readInt();
        this.label = parcel.readString();
        this.labelId = parcel.readInt();
        int readInt = parcel.readInt();
        if (readInt > 1024) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            ShortcutIntent shortcutIntent = new ShortcutIntent();
            if (!parcel.readSequenceable(shortcutIntent)) {
                return false;
            }
            this.intents.add(shortcutIntent);
        }
        return true;
    }
}
