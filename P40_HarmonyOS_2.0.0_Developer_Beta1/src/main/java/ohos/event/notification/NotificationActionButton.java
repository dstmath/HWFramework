package ohos.event.notification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ohos.event.EventConstant;
import ohos.event.intentagent.IntentAgent;
import ohos.event.notification.NotificationConstant;
import ohos.event.notification.NotificationUserInput;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class NotificationActionButton implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final int MAX_INPUTS = 256;
    private static final String ONLY_USER_INPUTS = "extra_data_only_user_input";
    public static final Sequenceable.Producer<NotificationActionButton> PRODUCER = $$Lambda$NotificationActionButton$C_Zm2fg3NQuG7iaPzjOimWKThs.INSTANCE;
    private static final String TAG = "NotificationActionButton";
    private static final String UNMARSHALLING_INPUT_KEY = "unmarshalling";
    private boolean autoCreatedReplies;
    private PacMap extras;
    private PixelMap icon;
    private IntentAgent intentAgent;
    private boolean isContextual;
    private int semanticActionButton;
    private String title;
    private List<NotificationUserInput> userInputs;

    static /* synthetic */ NotificationActionButton lambda$static$0(Parcel parcel) {
        NotificationActionButton notificationActionButton = new NotificationActionButton();
        notificationActionButton.unmarshalling(parcel);
        return notificationActionButton;
    }

    private NotificationActionButton() {
        this.autoCreatedReplies = true;
    }

    private NotificationActionButton(PixelMap pixelMap, String str, IntentAgent intentAgent2, PacMap pacMap, int i, boolean z, List<NotificationUserInput> list, boolean z2) {
        this.autoCreatedReplies = true;
        this.icon = pixelMap;
        this.title = str;
        this.intentAgent = intentAgent2;
        this.extras = pacMap == null ? new PacMap() : pacMap;
        this.semanticActionButton = i;
        this.autoCreatedReplies = z;
        this.userInputs = list;
        this.isContextual = z2;
    }

    public PixelMap getIcon() {
        return this.icon;
    }

    public PacMap getAdditionalData() {
        return this.extras;
    }

    public List<NotificationUserInput> getUserInputs() {
        return this.userInputs;
    }

    public List<NotificationUserInput> getMimeTypeOnlyUserInputs() {
        ArrayList sequenceableList = this.extras.getSequenceableList(ONLY_USER_INPUTS);
        if (sequenceableList == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        Iterator it = sequenceableList.iterator();
        while (it.hasNext()) {
            NotificationUserInput notificationUserInput = (Sequenceable) it.next();
            if (notificationUserInput instanceof NotificationUserInput) {
                arrayList.add(notificationUserInput);
            }
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        return arrayList;
    }

    public boolean isAutoCreatedReplies() {
        return this.autoCreatedReplies;
    }

    public boolean isContextDependent() {
        return this.isContextual;
    }

    public String getTitle() {
        return this.title;
    }

    public IntentAgent getIntentAgent() {
        return this.intentAgent;
    }

    public int getSemanticActionButton() {
        return this.semanticActionButton;
    }

    public static final class Builder {
        private boolean autoCreatedReplies;
        private PacMap extras;
        private PixelMap icon;
        private IntentAgent intentAgent;
        private boolean isContextual;
        private int semanticActionButton;
        private String title;
        private List<NotificationUserInput> userInputs;

        public Builder(PixelMap pixelMap, String str, IntentAgent intentAgent2) {
            this(pixelMap, str, intentAgent2, new PacMap(), NotificationConstant.SemanticActionButton.NONE_ACTION_BUTTON.ordinal(), true, null);
        }

        public Builder(NotificationActionButton notificationActionButton) {
            this(notificationActionButton.getIcon(), notificationActionButton.getTitle(), notificationActionButton.getIntentAgent(), notificationActionButton.getAdditionalData(), notificationActionButton.getSemanticActionButton(), notificationActionButton.isAutoCreatedReplies(), notificationActionButton.getUserInputs());
        }

        private Builder(PixelMap pixelMap, String str, IntentAgent intentAgent2, PacMap pacMap, int i, boolean z, List<NotificationUserInput> list) {
            this.autoCreatedReplies = true;
            this.icon = pixelMap;
            this.title = str;
            this.intentAgent = intentAgent2;
            this.extras = pacMap;
            this.semanticActionButton = i;
            this.autoCreatedReplies = z;
            if (list != null && !list.isEmpty()) {
                this.userInputs = new ArrayList();
                this.userInputs.addAll(list);
            }
        }

        public Builder addNotificationUserInput(NotificationUserInput notificationUserInput) {
            if (notificationUserInput == null) {
                HiLog.debug(NotificationActionButton.LABEL, "The userInput is invalid.", new Object[0]);
                return this;
            }
            if (this.userInputs == null) {
                this.userInputs = new ArrayList();
            }
            this.userInputs.add(notificationUserInput);
            return this;
        }

        public Builder setContextDependent(boolean z) {
            this.isContextual = z;
            return this;
        }

        public Builder setSemanticActionButton(NotificationConstant.SemanticActionButton semanticActionButton2) {
            if (semanticActionButton2 != null) {
                this.semanticActionButton = semanticActionButton2.ordinal();
            }
            return this;
        }

        public Builder setAutoCreatedReplies(boolean z) {
            this.autoCreatedReplies = z;
            return this;
        }

        public Builder addAdditionalData(PacMap pacMap) {
            if (pacMap != null) {
                this.extras.putAll(pacMap);
            }
            return this;
        }

        public NotificationActionButton build() {
            if (this.isContextual) {
                if (this.icon == null) {
                    throw new IllegalArgumentException("The context of ActionButton must contains a valid icon.");
                } else if (this.intentAgent == null) {
                    throw new IllegalArgumentException("The ActionButton's context must contains a valid IntentAgent");
                }
            }
            ArrayList arrayList = new ArrayList();
            ArrayList sequenceableList = this.extras.getSequenceableList(NotificationActionButton.ONLY_USER_INPUTS);
            if (sequenceableList != null) {
                Iterator it = sequenceableList.iterator();
                while (it.hasNext()) {
                    Sequenceable sequenceable = (Sequenceable) it.next();
                    if (sequenceable instanceof NotificationUserInput) {
                        arrayList.add(sequenceable);
                    }
                }
            }
            ArrayList arrayList2 = new ArrayList();
            List<NotificationUserInput> list = this.userInputs;
            if (list != null) {
                for (NotificationUserInput notificationUserInput : list) {
                    if (notificationUserInput != null) {
                        if (notificationUserInput.isMimeTypeOnly()) {
                            arrayList.add(notificationUserInput);
                        } else {
                            arrayList2.add(notificationUserInput);
                        }
                    }
                }
            }
            if (!arrayList.isEmpty()) {
                this.extras.putSequenceableObjectList(NotificationActionButton.ONLY_USER_INPUTS, arrayList);
            }
            if (arrayList2.isEmpty()) {
                arrayList2 = null;
            }
            return new NotificationActionButton(this.icon, this.title, this.intentAgent, this.extras, this.semanticActionButton, this.autoCreatedReplies, arrayList2, this.isContextual);
        }
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeString(this.title)) {
            HiLog.warn(LABEL, "NotificationActionButton: marshalling write title failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.semanticActionButton)) {
            HiLog.warn(LABEL, "NotificationActionButton: marshalling write semanticActionButton failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.autoCreatedReplies)) {
            HiLog.warn(LABEL, "NotificationActionButton: marshalling write permitCreatedAnswers failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.isContextual)) {
            HiLog.warn(LABEL, "NotificationActionButton: marshalling write hasContextual failed.", new Object[0]);
            return false;
        } else {
            parcel.writeSequenceable(this.intentAgent);
            List<NotificationUserInput> list = this.userInputs;
            if (list == null || list.isEmpty()) {
                if (!parcel.writeInt(-1)) {
                    HiLog.warn(LABEL, "NotificationActionButton: marshalling write null userInputs failed.", new Object[0]);
                    return false;
                }
            } else if (!parcel.writeInt(this.userInputs.size())) {
                HiLog.warn(LABEL, "NotificationActionButton: marshalling write size of userInputs failed.", new Object[0]);
                return false;
            } else {
                for (NotificationUserInput notificationUserInput : this.userInputs) {
                    parcel.writeSequenceable(notificationUserInput);
                }
            }
            parcel.writeSequenceable(this.extras);
            return true;
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        this.title = parcel.readString();
        this.semanticActionButton = parcel.readInt();
        this.autoCreatedReplies = parcel.readBoolean();
        this.isContextual = parcel.readBoolean();
        this.intentAgent = new IntentAgent(null);
        if (!parcel.readSequenceable(this.intentAgent)) {
            HiLog.warn(LABEL, "NotificationActionButton: unmarshalling read intentAgent failed.", new Object[0]);
            this.intentAgent = null;
        }
        int readInt = parcel.readInt();
        if (readInt > 256) {
            HiLog.warn(LABEL, "NotificationActionButton: unmarshalling user input oversize.", new Object[0]);
            return false;
        }
        if (readInt != -1) {
            this.userInputs = new ArrayList();
            for (int i = 0; i < readInt; i++) {
                NotificationUserInput build = new NotificationUserInput.Builder(UNMARSHALLING_INPUT_KEY).build();
                if (!parcel.readSequenceable(build)) {
                    return false;
                }
                this.userInputs.add(build);
            }
        }
        if (parcel.readSequenceable(this.extras)) {
            return true;
        }
        HiLog.warn(LABEL, "NotificationActionButton: unmarshalling read extras failed.", new Object[0]);
        return false;
    }

    public String toString() {
        return "NotificationActionButton[ title = " + this.title + " semanticActionButton = " + this.semanticActionButton + " autoCreatedReplies = " + this.autoCreatedReplies + " userInputs = " + this.userInputs + " isContextual = " + this.isContextual + "]";
    }
}
