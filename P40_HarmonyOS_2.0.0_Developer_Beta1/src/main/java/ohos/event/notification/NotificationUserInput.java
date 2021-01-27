package ohos.event.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ohos.aafwk.content.Intent;
import ohos.event.EventConstant;
import ohos.event.notification.NotificationConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public class NotificationUserInput implements Sequenceable {
    private static final String EXTRA_RESULTS_DATA_TYPE = "notification_user_input_resultsDataType";
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final String NOTIFICATION_USER_INPUT = "harmony_notification_user_input";
    public static final Sequenceable.Producer<NotificationUserInput> PRODUCER = $$Lambda$NotificationUserInput$u5ahNhesV9a0tanrFv06fW9htU.INSTANCE;
    private static final String TAG = "NotificationUserInput";
    private int editType;
    private String inputKey;
    private List<String> options;
    private PacMap pacMap;
    private boolean permitFreeFormInput;
    private Set<String> permitMimeTypes;
    private String tag;

    static /* synthetic */ NotificationUserInput lambda$static$0(Parcel parcel) {
        NotificationUserInput notificationUserInput = new NotificationUserInput();
        notificationUserInput.unmarshalling(parcel);
        return notificationUserInput;
    }

    private NotificationUserInput() {
    }

    private NotificationUserInput(String str, String str2, List<String> list, boolean z, Set<String> set, PacMap pacMap2, int i) {
        this.inputKey = str;
        this.tag = str2;
        this.options = list;
        this.permitFreeFormInput = z;
        this.permitMimeTypes = set;
        this.pacMap = pacMap2;
        this.editType = i;
        if (!z && i == NotificationConstant.InputEditType.EDIT_ENABLED.ordinal()) {
            throw new IllegalArgumentException("setEditType enable need setPermitFreeFormInput true");
        }
    }

    public static void addMimeInputToIntent(NotificationUserInput notificationUserInput, Intent intent, Map<String, Uri> map) {
        Uri value;
        if (notificationUserInput == null || intent == null || map == null) {
            HiLog.debug(LABEL, "The userInput, intent or results is invalid.", new Object[0]);
            return;
        }
        PacMap parcelableParam = intent.getParcelableParam(NOTIFICATION_USER_INPUT);
        if (parcelableParam == null) {
            parcelableParam = new PacMap();
        }
        for (Map.Entry<String, Uri> entry : map.entrySet()) {
            String key = entry.getKey();
            if (!(key == null || (value = entry.getValue()) == null)) {
                PacMap pacMap2 = (PacMap) parcelableParam.getPacMap(getExtraInputsKeyForData(key)).orElse(new PacMap());
                pacMap2.putString(notificationUserInput.getInputKey(), value.toString());
                parcelableParam.putPacMap(getExtraInputsKeyForData(key), pacMap2);
            }
        }
        intent.setParam(NOTIFICATION_USER_INPUT, (Sequenceable) parcelableParam);
    }

    public static void addInputsToIntent(List<NotificationUserInput> list, Intent intent, PacMap pacMap2) {
        String string;
        if (list == null || intent == null || pacMap2 == null) {
            HiLog.debug(LABEL, "The userInputs, intent or pacMap is invalid.", new Object[0]);
            return;
        }
        PacMap parcelableParam = intent.getParcelableParam(NOTIFICATION_USER_INPUT);
        if (parcelableParam == null) {
            parcelableParam = new PacMap();
        }
        for (NotificationUserInput notificationUserInput : list) {
            if (!(notificationUserInput == null || (string = pacMap2.getString(notificationUserInput.getInputKey())) == null)) {
                parcelableParam.putString(notificationUserInput.getInputKey(), string);
            }
        }
        intent.setParam(NOTIFICATION_USER_INPUT, (Sequenceable) parcelableParam);
    }

    public static Map<String, Uri> getMimeInputsFromIntent(Intent intent, String str) {
        String string;
        if (intent == null) {
            HiLog.debug(LABEL, "getMimeInputsFromIntent::intent is null.", new Object[0]);
            return null;
        }
        PacMap parcelableParam = intent.getParcelableParam(NOTIFICATION_USER_INPUT);
        if (parcelableParam == null) {
            HiLog.debug(LABEL, "getMimeInputsFromIntent::pacMap is null.", new Object[0]);
            return null;
        }
        HashMap hashMap = new HashMap();
        for (String str2 : parcelableParam.getKeys()) {
            if (str2 != null && str2.startsWith(EXTRA_RESULTS_DATA_TYPE)) {
                String substring = str2.substring(39);
                if (!substring.isEmpty()) {
                    Optional pacMap2 = parcelableParam.getPacMap(str2);
                    if (pacMap2.isPresent() && (string = ((PacMap) pacMap2.get()).getString(str)) != null && !string.isEmpty()) {
                        hashMap.put(substring, Uri.parse(string));
                    }
                }
            }
        }
        if (hashMap.isEmpty()) {
            return null;
        }
        return hashMap;
    }

    public static PacMap getInputsFromIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getParcelableParam(NOTIFICATION_USER_INPUT);
    }

    public static void setInputsSource(Intent intent, NotificationConstant.InputsSource inputsSource) {
        if (intent == null || inputsSource == null) {
            HiLog.debug(LABEL, "The intent or source is invalid.", new Object[0]);
        } else {
            intent.setParam(NotificationConstant.EXTRA_INPUTS_SOURCE, inputsSource.ordinal());
        }
    }

    public static int getInputsSource(Intent intent) {
        if (intent == null) {
            return NotificationConstant.InputsSource.FREE_FORM_INPUT.ordinal();
        }
        return intent.getIntParam(NotificationConstant.EXTRA_INPUTS_SOURCE, NotificationConstant.InputsSource.FREE_FORM_INPUT.ordinal());
    }

    public String getInputKey() {
        return this.inputKey;
    }

    public String getTag() {
        return this.tag;
    }

    public List<String> getOptions() {
        return this.options;
    }

    public Set<String> getPermitMimeTypes() {
        return this.permitMimeTypes;
    }

    public PacMap getAdditionalData() {
        return this.pacMap;
    }

    public boolean isPermitFreeFormInput() {
        return this.permitFreeFormInput;
    }

    public boolean isMimeTypeOnly() {
        return !isPermitFreeFormInput() && this.options.isEmpty() && !getPermitMimeTypes().isEmpty();
    }

    public int getEditType() {
        return this.editType;
    }

    public static final class Builder {
        private int editType = NotificationConstant.InputEditType.EDIT_AUTO.ordinal();
        private final String inputKey;
        private List<String> options = new ArrayList();
        private PacMap pacMaps = new PacMap();
        private final HashSet<String> permitDataTypes = new HashSet<>();
        private boolean permitFreeFormInput = true;
        private String tag;

        public Builder(String str) {
            if (str != null) {
                this.inputKey = str;
                return;
            }
            throw new IllegalArgumentException("The param of resultKey is null");
        }

        public Builder setTag(String str) {
            this.tag = str;
            return this;
        }

        public Builder setOptions(List<String> list) {
            if (list != null) {
                this.options = list;
            }
            return this;
        }

        public Builder setPermitMimeTypes(String str, boolean z) {
            if (str == null) {
                HiLog.debug(NotificationUserInput.LABEL, "The mimeType is invalid.", new Object[0]);
                return this;
            }
            if (z) {
                this.permitDataTypes.add(str);
            } else {
                this.permitDataTypes.remove(str);
            }
            return this;
        }

        public Builder setPermitFreeFormInput(boolean z) {
            this.permitFreeFormInput = z;
            return this;
        }

        public Builder addAdditionalData(PacMap pacMap) {
            if (pacMap != null) {
                this.pacMaps.putAll(pacMap);
            }
            return this;
        }

        public Builder setEditType(NotificationConstant.InputEditType inputEditType) {
            this.editType = inputEditType.ordinal();
            return this;
        }

        public NotificationUserInput build() {
            return new NotificationUserInput(this.inputKey, this.tag, this.options, this.permitFreeFormInput, this.permitDataTypes, this.pacMaps, this.editType);
        }
    }

    private static String getExtraInputsKeyForData(String str) {
        return EXTRA_RESULTS_DATA_TYPE + str;
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeString(this.inputKey)) {
            HiLog.warn(LABEL, "NotificationUserInput: marshalling write resultKey failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.tag)) {
            HiLog.warn(LABEL, "NotificationUserInput: marshalling write tag failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.permitFreeFormInput)) {
            HiLog.warn(LABEL, "NotificationUserInput: marshalling write permitAnyInput failed.", new Object[0]);
            return false;
        } else if (!parcel.writeStringList(this.options)) {
            HiLog.warn(LABEL, "NotificationUserInput: marshalling write selections failed.", new Object[0]);
            return false;
        } else {
            String[] strArr = new String[this.permitMimeTypes.size()];
            this.permitMimeTypes.toArray(strArr);
            if (!parcel.writeStringArray(strArr)) {
                HiLog.warn(LABEL, "NotificationUserInput: marshalling write array permitDataTypes failed.", new Object[0]);
                return false;
            }
            parcel.writeSequenceable(this.pacMap);
            if (parcel.writeInt(this.editType)) {
                return true;
            }
            HiLog.warn(LABEL, "NotificationUserInput: marshalling write editType failed.", new Object[0]);
            return false;
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        this.inputKey = parcel.readString();
        this.tag = parcel.readString();
        this.permitFreeFormInput = parcel.readBoolean();
        this.options = parcel.readStringList();
        this.permitMimeTypes = new HashSet(Arrays.asList(parcel.readStringArray()));
        this.pacMap = new PacMap();
        if (!parcel.readSequenceable(this.pacMap)) {
            HiLog.warn(LABEL, "NotificationUserInput: unmarshalling read pacMap failed.", new Object[0]);
        }
        this.editType = parcel.readInt();
        return true;
    }

    public String toString() {
        return "NotificationUserInput[ inputKey = " + this.inputKey + " tag = " + this.tag + " options = " + this.options + " permitFreeFormInput = " + this.permitFreeFormInput + " permitMimeTypes = " + this.permitMimeTypes + " editType = " + this.editType + "]";
    }
}
