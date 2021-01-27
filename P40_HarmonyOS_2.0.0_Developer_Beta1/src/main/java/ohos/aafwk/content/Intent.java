package ohos.aafwk.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import ohos.bundle.ElementName;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public class Intent implements Sequenceable {
    public static final String ACTION_BOOK_FLIGHT = "ability.intent.BOOK_FLIGHT";
    public static final String ACTION_BOOK_HOTEL = "ability.intent.BOOK_HOTEL";
    public static final String ACTION_BOOK_TRAIN_TICKET = "ability.intent.BOOK_TRAIN_TICKET";
    public static final String ACTION_BUNDLE_ADD = "action.bundle.add";
    public static final String ACTION_BUNDLE_REMOVE = "action.bundle.remove";
    public static final String ACTION_BUNDLE_UPDATE = "action.bundle.update";
    public static final String ACTION_BUY = "ability.intent.BUY";
    public static final String ACTION_BUY_TAKEOUT = "ability.intent.BUY_TAKEOUT";
    public static final String ACTION_HOME = "action.system.home";
    public static final String ACTION_LOCALE_CHANGED = "ability.intent.LOCALE_CHANGED";
    public static final String ACTION_ORDER_TAXI = "ability.intent.ORDER_TAXI";
    public static final String ACTION_PLAN_ROUTE = "ability.intent.PLAN_ROUTE";
    public static final String ACTION_PLAY = "action.system.play";
    public static final String ACTION_QUERY_ALMANC = "ability.intent.QUERY_ALMANC";
    public static final String ACTION_QUERY_CONSTELLATION_FORTUNE = "ability.intent.QUERY_CONSTELLATION_FORTUNE";
    public static final String ACTION_QUERY_ENCYCLOPEDIA = "ability.intent.QUERY_ENCYCLOPEDIA";
    public static final String ACTION_QUERY_JOKE = "ability.intent.QUERY_JOKE";
    public static final String ACTION_QUERY_LOGISTICS_INFO = "ability.intent.QUERY_LOGISTICS_INFO";
    public static final String ACTION_QUERY_NEWS = "ability.intent.QUERY_NEWS";
    public static final String ACTION_QUERY_POI_INFO = "ability.intent.QUERY_POI_INFO";
    public static final String ACTION_QUERY_RECIPE = "ability.intent.QUERY_RECIPE";
    public static final String ACTION_QUERY_SPORTS_INFO = "ability.intent.QUERY_SPORTS_INFO";
    public static final String ACTION_QUERY_STOCK_INFO = "ability.intent.QUERY_STOCK_INFO";
    public static final String ACTION_QUERY_TRAFFIC_RESTRICTION = "ability.intent.QUERY_TRAFFIC_RESTRICTION";
    public static final String ACTION_QUERY_TRAVELLING_GUIDELINE = "ability.intent.QUERY_TRAVELLING_GUIDELINE";
    public static final String ACTION_QUERY_WEATHER = "ability.intent.QUERY_WEATHER";
    public static final String ACTION_SEND_LOGISTICS = "ability.intent.SEND_LOGISTICS";
    public static final String ACTION_TRANSLATE_TEXT = "ability.intent.TRANSLATE_TEXT";
    public static final String ACTION_WATCH_VIDEO_CLIPS = "ability.intent.WATCH_VIDEO_CLIPS";
    private static final int DEFAULT_STRING_BUILDER_LENGTH = 128;
    public static final String ENTITY_HOME = "entity.system.home";
    public static final String ENTITY_VIDEO = "entity.system.video";
    public static final int FLAG_ABILITYSLICE_FORWARD_RESULT = 67108864;
    public static final int FLAG_ABILITYSLICE_MULTI_DEVICE = 256;
    public static final int FLAG_ABILITY_CLEAR_MISSION = 32768;
    public static final int FLAG_ABILITY_CONTINUATION = 8;
    public static final int FLAG_ABILITY_CONTINUATION_REVERSIBLE = 1024;
    public static final int FLAG_ABILITY_FORM_ENABLED = 32;
    public static final int FLAG_ABILITY_FORWARD_RESULT = 4;
    public static final int FLAG_ABILITY_NEW_MISSION = 268435456;
    public static final int FLAG_AUTH_PERSISTABLE_URI_PERMISSION = 64;
    public static final int FLAG_AUTH_PREFIX_URI_PERMISSION = 128;
    public static final int FLAG_AUTH_READ_URI_PERMISSION = 1;
    public static final int FLAG_AUTH_WRITE_URI_PERMISSION = 2;
    public static final int FLAG_INSTALL_ON_DEMAND = 2048;
    public static final int FLAG_NOT_HARMONYOS_COMPONENT = 16;
    public static final int FLAG_START_FOREGROUND_ABILITY = 512;
    private static final String MIME_TYPE = "mime-type";
    private static final String OCT_EQUALSTO = "075";
    private static final String OCT_SEMICOLON = "073";
    private static final String PARAM_LABEL = "param.";
    public static final Sequenceable.Producer<Intent> PRODUCER = $$Lambda$Intent$Z_RFVGqfKpfHSeld_zh8AV1ric.INSTANCE;
    private static final int VALUE_NULL = -1;
    private static final int VALUE_OBJECT = 1;
    private Operation operation = new OperationBuilder().build();
    private IntentParams parameters;
    private Intent picker;

    private static class BaseOperation implements Operation {
        protected String bundleName;
        protected String deviceId;
        protected int flags;

        @Override // ohos.aafwk.content.Operation
        public void addEntity(String str) {
        }

        @Override // ohos.aafwk.content.Operation
        public String getAbilityName() {
            return null;
        }

        @Override // ohos.aafwk.content.Operation
        public String getAction() {
            return null;
        }

        @Override // ohos.aafwk.content.Operation
        public Set<String> getEntities() {
            return null;
        }

        @Override // ohos.aafwk.content.Operation
        public Uri getUri() {
            return null;
        }

        @Override // ohos.aafwk.content.Operation
        public void removeEntity(String str) {
        }

        @Override // ohos.aafwk.content.Operation
        public void setAbilityName(String str) {
        }

        @Override // ohos.aafwk.content.Operation
        public void setAction(String str) {
        }

        @Override // ohos.aafwk.content.Operation
        public void setEntities(Set<String> set) {
        }

        @Override // ohos.aafwk.content.Operation
        public void setUri(Uri uri) {
        }

        private BaseOperation() {
        }

        @Override // ohos.aafwk.content.Operation
        public void setBundleName(String str) {
            this.bundleName = str;
        }

        @Override // ohos.aafwk.content.Operation
        public String getBundleName() {
            return this.bundleName;
        }

        @Override // ohos.aafwk.content.Operation
        public void setFlags(int i) {
            this.flags = i;
        }

        @Override // ohos.aafwk.content.Operation
        public int getFlags() {
            return this.flags;
        }

        @Override // ohos.aafwk.content.Operation
        public void addFlags(int i) {
            this.flags = i | this.flags;
        }

        @Override // ohos.aafwk.content.Operation
        public void removeFlags(int i) {
            this.flags = (~i) & this.flags;
        }

        @Override // ohos.aafwk.content.Operation
        public void setDeviceId(String str) {
            this.deviceId = str;
        }

        @Override // ohos.aafwk.content.Operation
        public String getDeviceId() {
            return this.deviceId;
        }
    }

    /* access modifiers changed from: private */
    public static class ImplicitOperation extends BaseOperation {
        protected String action;
        protected Set<String> entities;
        protected Uri uri;

        public ImplicitOperation(OperationBuilder operationBuilder) {
            super();
            this.action = operationBuilder.action;
            this.entities = operationBuilder.entities;
            this.uri = operationBuilder.uri;
            this.bundleName = operationBuilder.bundleName;
            this.flags = operationBuilder.flags;
            this.deviceId = operationBuilder.deviceId;
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public void setAction(String str) {
            this.action = str;
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public String getAction() {
            return this.action;
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public void setEntities(Set<String> set) {
            this.entities = set;
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public Set<String> getEntities() {
            return this.entities;
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public void addEntity(String str) {
            if (this.entities == null) {
                this.entities = new HashSet();
            }
            this.entities.add(str);
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public void removeEntity(String str) {
            Set<String> set = this.entities;
            if (set != null) {
                set.remove(str);
                if (this.entities.size() == 0) {
                    this.entities = null;
                }
            }
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public void setUri(Uri uri2) {
            this.uri = uri2;
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public Uri getUri() {
            return this.uri;
        }
    }

    /* access modifiers changed from: private */
    public static class ExplicitOperation extends ImplicitOperation {
        private String abilityName;

        public ExplicitOperation(OperationBuilder operationBuilder) {
            super(operationBuilder);
            this.abilityName = operationBuilder.abilityName;
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public void setAbilityName(String str) {
            this.abilityName = str;
        }

        @Override // ohos.aafwk.content.Intent.BaseOperation, ohos.aafwk.content.Operation
        public String getAbilityName() {
            return this.abilityName;
        }
    }

    public static class OperationBuilder {
        private String abilityName;
        private String action;
        private String bundleName;
        private String deviceId;
        private Set<String> entities;
        private int flags;
        private Uri uri;

        public OperationBuilder withAction(String str) {
            this.action = str;
            return this;
        }

        public OperationBuilder withEntities(Set<String> set) {
            this.entities = set;
            return this;
        }

        public OperationBuilder withUri(Uri uri2) {
            this.uri = uri2;
            return this;
        }

        public OperationBuilder withBundleName(String str) {
            this.bundleName = str;
            return this;
        }

        public OperationBuilder withFlags(int i) {
            this.flags = i;
            return this;
        }

        public OperationBuilder withDeviceId(String str) {
            this.deviceId = str;
            return this;
        }

        public OperationBuilder withAbilityName(String str) {
            this.abilityName = str;
            return this;
        }

        public Operation build() {
            return this.abilityName == null ? new ImplicitOperation(this) : new ExplicitOperation(this);
        }
    }

    public Intent() {
        this.operation.setFlags(0);
    }

    public Intent(Intent intent) {
        if (intent != null) {
            this.operation.setAction(intent.getAction());
            this.operation.setFlags(intent.getFlags());
            this.operation.setBundleName(intent.getBundle());
            if (intent.getEntities() != null) {
                this.operation.setEntities(new HashSet(intent.getEntities()));
            }
            if (intent.getUri() != null) {
                this.operation.setUri(Uri.parse(intent.getUri().toString()));
            }
            if (intent.getElement() != null) {
                convertImplicitToExplicit(intent.getElement());
            }
            IntentParams intentParams = intent.parameters;
            if (intentParams != null) {
                this.parameters = new IntentParams(intentParams);
            }
            Intent intent2 = intent.picker;
            if (intent2 != null) {
                this.picker = new Intent(intent2);
            }
        }
    }

    public Object clone() {
        return new Intent(this);
    }

    public void setOperation(Operation operation2) {
        if (operation2 != null) {
            if (operation2.getAction() == null) {
                operation2.setAction(this.operation.getAction());
            }
            if (operation2.getEntities() == null && this.operation.getEntities() != null) {
                operation2.setEntities(new HashSet(this.operation.getEntities()));
            }
            if (operation2.getBundleName() == null) {
                operation2.setBundleName(this.operation.getBundleName());
            }
            if (operation2.getUri() == null) {
                operation2.setUri(this.operation.getUri());
            }
            if (operation2.getFlags() == 0) {
                operation2.setFlags(this.operation.getFlags());
            }
            if (operation2.getDeviceId() == null) {
                operation2.setDeviceId(this.operation.getDeviceId());
            }
            if (operation2.getAbilityName() == null) {
                operation2.setAbilityName(this.operation.getAbilityName());
            }
            this.operation = operation2;
        }
    }

    public Operation getOperation() {
        return this.operation;
    }

    public Intent cloneOperation() {
        Operation build = new OperationBuilder().withAction(this.operation.getAction()).withEntities(this.operation.getEntities()).withUri(this.operation.getUri()).withBundleName(this.operation.getBundleName()).withFlags(this.operation.getFlags()).withDeviceId(this.operation.getDeviceId()).withAbilityName(this.operation.getAbilityName()).build();
        Intent intent = new Intent();
        intent.setOperation(build);
        return intent;
    }

    public boolean operationEquals(Intent intent) {
        if (intent == null) {
            return false;
        }
        Operation operation2 = intent.getOperation();
        if (!Objects.equals(operation2.getBundleName(), this.operation.getBundleName()) || !Objects.equals(Integer.valueOf(operation2.getFlags()), Integer.valueOf(this.operation.getFlags())) || !Objects.equals(operation2.getEntities(), this.operation.getEntities()) || !Objects.equals(operation2.getUri(), this.operation.getUri()) || !Objects.equals(operation2.getAbilityName(), this.operation.getAbilityName()) || !Objects.equals(operation2.getDeviceId(), this.operation.getDeviceId()) || !Objects.equals(operation2.getAction(), this.operation.getAction())) {
            return false;
        }
        return true;
    }

    public int operationHashCode() {
        int flags = this.operation.getFlags();
        if (this.operation.getBundleName() != null) {
            flags += this.operation.getBundleName().hashCode();
        }
        if (this.operation.getEntities() != null) {
            flags += this.operation.getEntities().hashCode();
        }
        if (this.operation.getUri() != null) {
            flags += this.operation.getUri().hashCode();
        }
        if (this.operation.getAbilityName() != null) {
            flags += this.operation.getAbilityName().hashCode();
        }
        if (this.operation.getDeviceId() != null) {
            flags += this.operation.getDeviceId().hashCode();
        }
        return this.operation.getAction() != null ? flags + this.operation.getAction().hashCode() : flags;
    }

    private void convertImplicitToExplicit(ElementName elementName) {
        if (elementName != null) {
            setOperation(new OperationBuilder().withAction(this.operation.getAction()).withEntities(this.operation.getEntities()).withUri(this.operation.getUri()).withBundleName(elementName.getBundleName()).withFlags(this.operation.getFlags()).withDeviceId(elementName.getDeviceId()).withAbilityName(elementName.getAbilityName()).build());
            return;
        }
        this.operation.setDeviceId(null);
        this.operation.setAbilityName(null);
    }

    static /* synthetic */ Intent lambda$static$0(Parcel parcel) {
        Intent intent = new Intent();
        intent.unmarshalling(parcel);
        return intent;
    }

    public static Intent makeMainAbility(ElementName elementName) {
        Intent intent = new Intent();
        intent.setAction(ACTION_HOME);
        intent.addEntity(ENTITY_HOME);
        intent.setElement(elementName);
        return intent;
    }

    public static Intent makeRestartAbilityMission(ElementName elementName) {
        Intent makeMainAbility = makeMainAbility(elementName);
        makeMainAbility.addFlags(268468224);
        return makeMainAbility;
    }

    public static Intent makeMainAbilityPicker(String str, String str2) {
        Intent intent = new Intent();
        intent.setAction(ACTION_HOME);
        intent.addEntity(ENTITY_HOME);
        Intent intent2 = new Intent();
        intent2.setAction(str);
        intent2.addEntity(str2);
        intent.setPicker(intent2);
        return intent;
    }

    private static boolean marshallingSequenceable(Parcel parcel, Sequenceable sequenceable) {
        if (sequenceable == null) {
            if (!parcel.writeInt(-1)) {
                return false;
            }
        } else if (!parcel.writeInt(1)) {
            return false;
        } else {
            parcel.writeSequenceable(sequenceable);
        }
        return true;
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeString(this.operation.getAction())) {
            return false;
        }
        if (this.operation.getUri() == null) {
            if (!parcel.writeInt(-1)) {
                return false;
            }
        } else if (!parcel.writeInt(1) || !parcel.writeString(this.operation.getUri().toString())) {
            return false;
        }
        if (this.operation.getEntities() == null) {
            if (!parcel.writeInt(-1)) {
                return false;
            }
        } else if (!parcel.writeInt(1)) {
            return false;
        } else {
            String[] strArr = new String[this.operation.getEntities().size()];
            this.operation.getEntities().toArray(strArr);
            if (!parcel.writeStringArray(strArr)) {
                return false;
            }
        }
        if (parcel.writeInt(this.operation.getFlags()) && marshallingSequenceable(parcel, constructElement()) && marshallingSequenceable(parcel, this.parameters) && parcel.writeString(this.operation.getBundleName()) && marshallingSequenceable(parcel, this.picker)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.operation.setAction(parcel.readString());
        this.operation.setUri(null);
        if (parcel.readInt() == 1) {
            this.operation.setUri(Uri.parse(parcel.readString()));
        }
        this.operation.setEntities(null);
        if (parcel.readInt() == 1) {
            this.operation.setEntities(new HashSet(Arrays.asList(parcel.readStringArray())));
        }
        this.operation.setFlags(parcel.readInt());
        setElement(null);
        if (parcel.readInt() == 1) {
            setElement(new ElementName());
            ElementName constructElement = constructElement();
            if (!parcel.readSequenceable(constructElement)) {
                setElement(null);
                return false;
            }
            setElement(constructElement);
        }
        this.parameters = null;
        if (parcel.readInt() == 1) {
            this.parameters = new IntentParams();
            if (!parcel.readSequenceable(this.parameters)) {
                this.parameters = null;
                return false;
            }
        }
        String readString = parcel.readString();
        if (this.operation.getBundleName() == null) {
            this.operation.setBundleName(readString);
        }
        this.picker = null;
        if (parcel.readInt() == 1) {
            this.picker = new Intent();
            if (!parcel.readSequenceable(this.picker)) {
                this.picker = null;
                return false;
            }
        }
        return true;
    }

    private static boolean parseSingleParam(String str, String str2, String str3, Intent intent) {
        try {
            if ("Boolean".equals(str2)) {
                intent.setParam(str, Boolean.parseBoolean(str3));
            } else if ("Byte".equals(str2)) {
                intent.setParam(str, Byte.parseByte(str3));
            } else if ("Char".equals(str2)) {
                if (str3.length() != 1) {
                    return false;
                }
                intent.setParam(str, str3.charAt(0));
            } else if ("Short".equals(str2)) {
                intent.setParam(str, Short.parseShort(str3));
            } else if ("Int".equals(str2)) {
                intent.setParam(str, Integer.parseInt(str3));
            } else if ("Long".equals(str2)) {
                intent.setParam(str, Long.parseLong(str3));
            } else if ("Float".equals(str2)) {
                intent.setParam(str, Float.parseFloat(str3));
            } else if ("Double".equals(str2)) {
                intent.setParam(str, Double.parseDouble(str3));
            } else if ("String".equals(str2)) {
                intent.setParam(str, str3);
            } else {
                if ("CharSequence".equals(str2)) {
                    intent.setParam(str, (CharSequence) str3);
                }
                return false;
            }
            return true;
        } catch (NumberFormatException unused) {
        }
    }

    private static boolean parseArrayParam(String str, String str2, String str3, Intent intent) {
        if (str3.startsWith("[") && str3.endsWith("]")) {
            String[] split = str3.substring(1, str3.length() - 1).split(", ");
            try {
                if ("BooleanArray".equals(str2)) {
                    boolean[] zArr = new boolean[split.length];
                    for (int i = 0; i < zArr.length; i++) {
                        zArr[i] = Boolean.parseBoolean(split[i].trim());
                    }
                    intent.setParam(str, zArr);
                } else if ("ByteArray".equals(str2)) {
                    byte[] bArr = new byte[split.length];
                    for (int i2 = 0; i2 < bArr.length; i2++) {
                        bArr[i2] = Byte.parseByte(split[i2].trim());
                    }
                    intent.setParam(str, bArr);
                } else if ("CharArray".equals(str2)) {
                    char[] cArr = new char[split.length];
                    for (int i3 = 0; i3 < cArr.length; i3++) {
                        if (split[i3].length() != 1) {
                            return false;
                        }
                        cArr[i3] = split[i3].charAt(0);
                    }
                    intent.setParam(str, cArr);
                } else if ("ShortArray".equals(str2)) {
                    short[] sArr = new short[split.length];
                    for (int i4 = 0; i4 < sArr.length; i4++) {
                        sArr[i4] = Short.parseShort(split[i4].trim());
                    }
                    intent.setParam(str, sArr);
                } else if ("IntArray".equals(str2)) {
                    int[] iArr = new int[split.length];
                    for (int i5 = 0; i5 < iArr.length; i5++) {
                        iArr[i5] = Integer.parseInt(split[i5].trim());
                    }
                    intent.setParam(str, iArr);
                } else if ("LongArray".equals(str2)) {
                    long[] jArr = new long[split.length];
                    for (int i6 = 0; i6 < jArr.length; i6++) {
                        jArr[i6] = Long.parseLong(split[i6].trim());
                    }
                    intent.setParam(str, jArr);
                } else if ("FloatArray".equals(str2)) {
                    float[] fArr = new float[split.length];
                    for (int i7 = 0; i7 < fArr.length; i7++) {
                        fArr[i7] = Float.parseFloat(split[i7].trim());
                    }
                    intent.setParam(str, fArr);
                } else if ("DoubleArray".equals(str2)) {
                    double[] dArr = new double[split.length];
                    for (int i8 = 0; i8 < dArr.length; i8++) {
                        dArr[i8] = Double.parseDouble(split[i8].trim());
                    }
                    intent.setParam(str, dArr);
                } else if ("StringArray".equals(str2)) {
                    intent.setParam(str, split);
                } else if ("CharSequenceArray".equals(str2)) {
                    intent.setParam(str, (CharSequence[]) split);
                }
                return true;
            } catch (NumberFormatException unused) {
            }
        }
        return false;
    }

    private static boolean parseParam(String str, Intent intent) {
        int i;
        int lastIndexOf = str.lastIndexOf(61);
        int lastIndexOf2 = str.substring(0, lastIndexOf).lastIndexOf(46);
        if (6 >= lastIndexOf2 || (i = lastIndexOf2 + 1) >= lastIndexOf) {
            return false;
        }
        String substring = str.substring(6, lastIndexOf2);
        String substring2 = str.substring(i, lastIndexOf);
        String substring3 = str.substring(lastIndexOf + 1);
        String decode = decode(substring);
        String trim = decode(substring3).trim();
        if (trim.isEmpty()) {
            return true;
        }
        if (substring2.endsWith("Array")) {
            return parseArrayParam(decode, substring2, trim, intent);
        }
        return parseSingleParam(decode, substring2, trim, intent);
    }

    private static ElementName getElementFromIntent(Intent intent) {
        if (intent.getElement() == null) {
            intent.setElement(new ElementName("", "", ""));
        }
        return intent.getElement();
    }

    private static boolean parseUriInternal(String str, Intent intent) {
        if (str.trim().isEmpty()) {
            return true;
        }
        int indexOf = str.indexOf(61);
        if (indexOf < 0) {
            return false;
        }
        String trim = str.substring(0, indexOf).trim();
        String substring = str.substring(indexOf + 1);
        if (!substring.isEmpty()) {
            if ("action".equals(trim)) {
                intent.setAction(decode(substring));
            } else if (Constants.ELEMNAME_URL_STRING.equals(trim)) {
                intent.setUri(Uri.parse(decode(substring)));
            } else if ("entity".equals(trim)) {
                intent.addEntity(decode(substring));
            } else if ("flag".equals(trim)) {
                try {
                    intent.setFlags(Integer.decode(substring).intValue());
                } catch (NumberFormatException unused) {
                    return false;
                }
            } else if ("device".equals(trim)) {
                ElementName elementFromIntent = getElementFromIntent(intent);
                elementFromIntent.setDeviceId(decode(substring));
                intent.getOperation().setDeviceId(elementFromIntent.getDeviceId());
            } else if ("bundle".equals(trim)) {
                ElementName elementFromIntent2 = getElementFromIntent(intent);
                elementFromIntent2.setBundleName(decode(substring));
                intent.getOperation().setBundleName(elementFromIntent2.getBundleName());
            } else if ("ability".equals(trim)) {
                ElementName elementFromIntent3 = getElementFromIntent(intent);
                elementFromIntent3.setAbilityName(decode(substring));
                intent.setElement(elementFromIntent3);
                intent.getOperation().setAbilityName(elementFromIntent3.getAbilityName());
            } else if ("package".equals(trim)) {
                intent.setBundle(decode(substring));
            }
        }
        if (!trim.startsWith(PARAM_LABEL) || parseParam(str, intent)) {
            return true;
        }
        return false;
    }

    public static Intent parseUri(String str) {
        if (!(str != null && str.startsWith("#Intent;") && str.endsWith("end"))) {
            return null;
        }
        String[] split = str.split(DMSDPConfig.LIST_TO_STRING_SPLIT);
        Intent intent = new Intent();
        Intent intent2 = intent;
        boolean z = false;
        for (int i = 1; i < split.length - 1; i++) {
            if ("PICK".equals(split[i].trim())) {
                intent2 = new Intent();
                z = true;
            } else if (!parseUriInternal(split[i], intent2)) {
                return null;
            }
        }
        if (!z) {
            return intent2;
        }
        if (intent.getBundle() != null) {
            return intent;
        }
        intent.setPicker(intent2);
        return intent;
    }

    private static String decode(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.length());
        int i = 0;
        while (true) {
            if (i >= str.length()) {
                break;
            }
            char charAt = str.charAt(i);
            if (charAt != '\\') {
                sb.append(charAt);
            } else {
                i++;
                if (i >= str.length()) {
                    sb.append(PatternTokenizer.BACK_SLASH);
                    break;
                }
                char charAt2 = str.charAt(i);
                if (charAt2 == '\\') {
                    sb.append(charAt2);
                } else if (charAt2 == '0') {
                    if (str.regionMatches(i, OCT_EQUALSTO, 0, 3)) {
                        sb.append('=');
                    } else if (str.regionMatches(i, OCT_SEMICOLON, 0, 3)) {
                        sb.append(';');
                    } else {
                        sb.append(PatternTokenizer.BACK_SLASH);
                        sb.append(charAt2);
                    }
                    i += 3;
                } else {
                    sb.append(PatternTokenizer.BACK_SLASH);
                    sb.append(charAt2);
                }
            }
            i++;
        }
        return sb.toString();
    }

    private StringBuilder encode(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt == '\\') {
                sb.append("\\\\");
            } else if (charAt == '=') {
                sb.append(PatternTokenizer.BACK_SLASH);
                sb.append(OCT_EQUALSTO);
            } else if (charAt == ';') {
                sb.append(PatternTokenizer.BACK_SLASH);
                sb.append(OCT_SEMICOLON);
            } else {
                sb.append(charAt);
            }
        }
        return sb;
    }

    private void getInternalElement(StringBuilder sb) {
        if (constructElement() != null) {
            String deviceId = constructElement().getDeviceId();
            if (deviceId != null && !deviceId.isEmpty()) {
                sb.append("device=");
                sb.append((CharSequence) encode(deviceId));
                sb.append(';');
            }
            String bundleName = constructElement().getBundleName();
            if (bundleName != null && !bundleName.isEmpty()) {
                sb.append("bundle=");
                sb.append((CharSequence) encode(bundleName));
                sb.append(';');
            }
            String abilityName = constructElement().getAbilityName();
            if (abilityName != null && !abilityName.isEmpty()) {
                sb.append("ability=");
                sb.append((CharSequence) encode(abilityName));
                sb.append(';');
            }
        }
    }

    private String[] getDataTypeAndValue(Object obj) {
        String[] strArr = new String[2];
        if (obj instanceof Boolean) {
            strArr[0] = "Boolean";
            strArr[1] = obj.toString();
        } else if (obj instanceof boolean[]) {
            strArr[0] = "BooleanArray";
            strArr[1] = Arrays.toString((boolean[]) obj);
        } else if (obj instanceof Byte) {
            strArr[0] = "Byte";
            strArr[1] = obj.toString();
        } else if (obj instanceof byte[]) {
            strArr[0] = "ByteArray";
            strArr[1] = Arrays.toString((byte[]) obj);
        } else if (obj instanceof Character) {
            strArr[0] = "Char";
            strArr[1] = obj.toString();
        } else if (obj instanceof char[]) {
            strArr[0] = "CharArray";
            strArr[1] = Arrays.toString((char[]) obj);
        } else if (obj instanceof Short) {
            strArr[0] = "Short";
            strArr[1] = obj.toString();
        } else if (obj instanceof short[]) {
            strArr[0] = "ShortArray";
            strArr[1] = Arrays.toString((short[]) obj);
        } else if (obj instanceof Integer) {
            strArr[0] = "Int";
            strArr[1] = obj.toString();
        } else if (obj instanceof int[]) {
            strArr[0] = "IntArray";
            strArr[1] = Arrays.toString((int[]) obj);
        } else if (obj instanceof Long) {
            strArr[0] = "Long";
            strArr[1] = obj.toString();
        } else if (obj instanceof long[]) {
            strArr[0] = "LongArray";
            strArr[1] = Arrays.toString((long[]) obj);
        } else if (obj instanceof Float) {
            strArr[0] = "Float";
            strArr[1] = obj.toString();
        } else if (obj instanceof float[]) {
            strArr[0] = "FloatArray";
            strArr[1] = Arrays.toString((float[]) obj);
        } else if (obj instanceof Double) {
            strArr[0] = "Double";
            strArr[1] = obj.toString();
        } else if (obj instanceof double[]) {
            strArr[0] = "DoubleArray";
            strArr[1] = Arrays.toString((double[]) obj);
        } else if (obj instanceof String) {
            strArr[0] = "String";
            strArr[1] = obj.toString();
        } else if (obj instanceof String[]) {
            strArr[0] = "StringArray";
            strArr[1] = Arrays.toString((String[]) obj);
        } else if (obj instanceof CharSequence) {
            strArr[0] = "CharSequence";
            strArr[1] = obj.toString();
        } else if (!(obj instanceof CharSequence[])) {
            return null;
        } else {
            strArr[0] = "CharSequenceArray";
            strArr[1] = Arrays.toString((CharSequence[]) obj);
        }
        return strArr;
    }

    private void toUriInner(StringBuilder sb) {
        if (this.operation.getAction() != null && !this.operation.getAction().isEmpty()) {
            sb.append("action=");
            sb.append((CharSequence) encode(this.operation.getAction()));
            sb.append(';');
        }
        if (this.operation.getUri() != null) {
            sb.append("uri=");
            sb.append((CharSequence) encode(this.operation.getUri().toString()));
            sb.append(';');
        }
        if (this.operation.getEntities() != null) {
            for (String str : this.operation.getEntities()) {
                if (str != null && !str.isEmpty()) {
                    sb.append("entity=");
                    sb.append((CharSequence) encode(str));
                    sb.append(';');
                }
            }
        }
        getInternalElement(sb);
        if (this.operation.getFlags() != 0) {
            sb.append("flag=0x");
            sb.append(Integer.toHexString(this.operation.getFlags()));
            sb.append(';');
        }
        if (this.operation.getBundleName() != null && !this.operation.getBundleName().isEmpty()) {
            sb.append("package=");
            sb.append((CharSequence) encode(this.operation.getBundleName()));
            sb.append(';');
        }
        IntentParams intentParams = this.parameters;
        if (intentParams != null) {
            for (String str2 : intentParams.keySet()) {
                String[] dataTypeAndValue = getDataTypeAndValue(this.parameters.getParam(str2));
                if (dataTypeAndValue != null) {
                    sb.append(PARAM_LABEL);
                    sb.append((CharSequence) encode(str2));
                    sb.append('.');
                    sb.append(dataTypeAndValue[0]);
                    sb.append('=');
                    sb.append((CharSequence) encode(dataTypeAndValue[1]));
                    sb.append(';');
                }
            }
        }
    }

    public String toUri() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("#Intent;");
        toUriInner(sb);
        if (this.picker != null) {
            sb.append("PICK;");
            this.picker.toUriInner(sb);
        }
        sb.append("end");
        return sb.toString();
    }

    public String getAction() {
        return this.operation.getAction();
    }

    public Intent setAction(String str) {
        this.operation.setAction(str);
        return this;
    }

    public Intent setUri(Uri uri) {
        this.operation.setUri(uri);
        return this;
    }

    public Uri getUri() {
        return this.operation.getUri();
    }

    public String getUriString() {
        if (this.operation.getUri() != null) {
            return this.operation.getUri().toString();
        }
        return null;
    }

    public Intent addEntity(String str) {
        this.operation.addEntity(str);
        return this;
    }

    public void removeEntity(String str) {
        this.operation.removeEntity(str);
    }

    public boolean hasEntity(String str) {
        return this.operation.getEntities() != null && this.operation.getEntities().contains(str);
    }

    public Set<String> getEntities() {
        return this.operation.getEntities();
    }

    public int countEntities() {
        if (this.operation.getEntities() != null) {
            return this.operation.getEntities().size();
        }
        return 0;
    }

    public Intent setFlags(int i) {
        this.operation.setFlags(i);
        return this;
    }

    public Intent addFlags(int i) {
        this.operation.addFlags(i);
        return this;
    }

    public int getFlags() {
        return this.operation.getFlags();
    }

    public void removeFlags(int i) {
        this.operation.removeFlags(i);
    }

    public ElementName getElement() {
        return constructElement();
    }

    private ElementName constructElement() {
        if (this.operation.getDeviceId() == null && this.operation.getAbilityName() == null) {
            return null;
        }
        return new ElementName(this.operation.getDeviceId(), this.operation.getBundleName(), this.operation.getAbilityName());
    }

    public Intent setElement(ElementName elementName) {
        convertImplicitToExplicit(elementName);
        return this;
    }

    public Intent setElementName(String str, String str2) {
        if (str != null && str2 != null) {
            return setElement(new ElementName(null, str, str2));
        }
        throw new IllegalArgumentException("The bundleName and abilityName can't be empty.");
    }

    public Intent setElementName(String str, String str2, String str3) {
        if (str != null && str2 != null && str3 != null) {
            return setElement(new ElementName(str, str2, str3));
        }
        throw new IllegalArgumentException("The parameters can't be empty.");
    }

    public Intent setBundle(String str) {
        if (str == null || this.picker == null) {
            this.operation.setBundleName(str);
            return this;
        }
        throw new IllegalArgumentException("Can't set bundle when picker is already set");
    }

    public String getBundle() {
        return this.operation.getBundleName();
    }

    public String getScheme() {
        if (this.operation.getUri() != null) {
            return this.operation.getUri().getScheme();
        }
        return null;
    }

    public Intent setType(String str) {
        if (this.parameters == null) {
            this.parameters = new IntentParams();
        }
        this.parameters.setParam(MIME_TYPE, str);
        return this;
    }

    public String getType() {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(MIME_TYPE);
        if (param instanceof String) {
            return (String) param;
        }
        return null;
    }

    public Intent setUriAndType(Uri uri, String str) {
        setUri(uri);
        setType(str);
        return this;
    }

    public Intent formatUri(Uri uri) {
        if (uri != null) {
            return setUri(uri.getLowerCaseScheme());
        }
        throw new IllegalArgumentException("The uri can't be null.");
    }

    public static String formatMimeType(String str) {
        if (str == null) {
            return null;
        }
        String lowerCase = str.trim().toLowerCase(Locale.ROOT);
        int indexOf = lowerCase.indexOf(59);
        return indexOf != -1 ? lowerCase.substring(0, indexOf) : lowerCase;
    }

    public Intent formatUriAndType(Uri uri, String str) {
        if (uri != null) {
            return setUriAndType(uri.getLowerCaseScheme(), formatMimeType(str));
        }
        throw new IllegalArgumentException("The uri can't be null.");
    }

    public Intent formatType(String str) {
        return setType(formatMimeType(str));
    }

    public void setPicker(Intent intent) {
        if (intent != null && !intent.equals(new Intent()) && intent.equals(this)) {
            throw new IllegalArgumentException("Intent can't set picker as itself");
        } else if (intent == null || this.operation.getBundleName() == null) {
            this.picker = intent;
        } else {
            throw new IllegalArgumentException("Can't set picker when package is already set");
        }
    }

    public Intent getPicker() {
        return this.picker;
    }

    public Intent setParams(IntentParams intentParams) {
        this.parameters = intentParams;
        return this;
    }

    public IntentParams getParams() {
        return this.parameters;
    }

    public Intent setParam(String str, IntentParams intentParams) {
        return setParamInternal(str, intentParams);
    }

    public IntentParams getParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof IntentParams) {
            return (IntentParams) param;
        }
        return null;
    }

    public boolean hasParameter(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return false;
        }
        return intentParams.hasParam(str);
    }

    public boolean getBooleanParam(String str, boolean z) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return z;
        }
        Object param = intentParams.getParam(str);
        return param instanceof Boolean ? ((Boolean) param).booleanValue() : z;
    }

    public boolean[] getBooleanArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof boolean[]) {
            return (boolean[]) param;
        }
        return null;
    }

    public byte getByteParam(String str, byte b) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return b;
        }
        Object param = intentParams.getParam(str);
        return param instanceof Byte ? ((Byte) param).byteValue() : b;
    }

    public byte[] getByteArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof byte[]) {
            return (byte[]) param;
        }
        return null;
    }

    public short getShortParam(String str, short s) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return s;
        }
        Object param = intentParams.getParam(str);
        return param instanceof Short ? ((Short) param).shortValue() : s;
    }

    public short[] getShortArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof short[]) {
            return (short[]) param;
        }
        return null;
    }

    public char getCharParam(String str, char c) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return c;
        }
        Object param = intentParams.getParam(str);
        return param instanceof Character ? ((Character) param).charValue() : c;
    }

    public char[] getCharArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof char[]) {
            return (char[]) param;
        }
        return null;
    }

    public int getIntParam(String str, int i) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return i;
        }
        Object param = intentParams.getParam(str);
        return param instanceof Integer ? ((Integer) param).intValue() : i;
    }

    public int[] getIntArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof int[]) {
            return (int[]) param;
        }
        return null;
    }

    public ArrayList<Integer> getIntegerArrayListParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof ArrayList) {
            return (ArrayList) param;
        }
        return null;
    }

    public long getLongParam(String str, long j) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return j;
        }
        Object param = intentParams.getParam(str);
        return param instanceof Long ? ((Long) param).longValue() : j;
    }

    public long[] getLongArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof long[]) {
            return (long[]) param;
        }
        return null;
    }

    public float getFloatParam(String str, float f) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return f;
        }
        Object param = intentParams.getParam(str);
        return param instanceof Float ? ((Float) param).floatValue() : f;
    }

    public float[] getFloatArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof float[]) {
            return (float[]) param;
        }
        return null;
    }

    public double getDoubleParam(String str, double d) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return d;
        }
        Object param = intentParams.getParam(str);
        return param instanceof Double ? ((Double) param).doubleValue() : d;
    }

    public double[] getDoubleArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof double[]) {
            return (double[]) param;
        }
        return null;
    }

    public String getStringParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof String) {
            return (String) param;
        }
        return null;
    }

    public String[] getStringArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof String[]) {
            return (String[]) param;
        }
        return null;
    }

    public ArrayList<String> getStringArrayListParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof ArrayList) {
            return (ArrayList) param;
        }
        return null;
    }

    public CharSequence getCharSequenceParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof CharSequence) {
            return (CharSequence) param;
        }
        return null;
    }

    public CharSequence[] getCharSequenceArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof CharSequence[]) {
            return (CharSequence[]) param;
        }
        return null;
    }

    public ArrayList<CharSequence> getCharSequenceArrayListParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof ArrayList) {
            return (ArrayList) param;
        }
        return null;
    }

    public <T extends Serializable> T getSerializableParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof Serializable) {
            return (T) ((Serializable) param);
        }
        return null;
    }

    @Deprecated
    public <T extends Sequenceable> T getParcelableParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof Sequenceable) {
            return (T) ((Sequenceable) param);
        }
        return null;
    }

    public <T extends Sequenceable> T getSequenceableParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof Sequenceable) {
            return (T) ((Sequenceable) param);
        }
        return null;
    }

    @Deprecated
    public Sequenceable[] getParcelableArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof Sequenceable[]) {
            return (Sequenceable[]) param;
        }
        return null;
    }

    public Sequenceable[] getSequenceableArrayParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof Sequenceable[]) {
            return (Sequenceable[]) param;
        }
        return null;
    }

    @Deprecated
    public <T extends Sequenceable> ArrayList<T> getParcelableArrayListParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof ArrayList) {
            return (ArrayList) param;
        }
        return null;
    }

    public <T extends Sequenceable> ArrayList<T> getSequenceableArrayListParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams == null) {
            return null;
        }
        Object param = intentParams.getParam(str);
        if (param instanceof ArrayList) {
            return (ArrayList) param;
        }
        return null;
    }

    public Intent setParam(String str, boolean z) {
        return setParamInternal(str, Boolean.valueOf(z));
    }

    public Intent setParam(String str, boolean[] zArr) {
        return setParamInternal(str, zArr);
    }

    public Intent setParam(String str, byte b) {
        return setParamInternal(str, Byte.valueOf(b));
    }

    public Intent setParam(String str, byte[] bArr) {
        return setParamInternal(str, bArr);
    }

    public Intent setParam(String str, char c) {
        return setParamInternal(str, Character.valueOf(c));
    }

    public Intent setParam(String str, char[] cArr) {
        return setParamInternal(str, cArr);
    }

    public Intent setParam(String str, short s) {
        return setParamInternal(str, Short.valueOf(s));
    }

    public Intent setParam(String str, short[] sArr) {
        return setParamInternal(str, sArr);
    }

    public Intent setParam(String str, int i) {
        return setParamInternal(str, Integer.valueOf(i));
    }

    public Intent setParam(String str, int[] iArr) {
        return setParamInternal(str, iArr);
    }

    public Intent setIntegerArrayListParam(String str, ArrayList<Integer> arrayList) {
        return setParamInternal(str, arrayList);
    }

    public Intent setParam(String str, long j) {
        return setParamInternal(str, Long.valueOf(j));
    }

    public Intent setParam(String str, long[] jArr) {
        return setParamInternal(str, jArr);
    }

    public Intent setParam(String str, float f) {
        return setParamInternal(str, Float.valueOf(f));
    }

    public Intent setParam(String str, float[] fArr) {
        return setParamInternal(str, fArr);
    }

    public Intent setParam(String str, double d) {
        return setParamInternal(str, Double.valueOf(d));
    }

    public Intent setParam(String str, double[] dArr) {
        return setParamInternal(str, dArr);
    }

    public Intent setParam(String str, String str2) {
        return setParamInternal(str, str2);
    }

    public Intent setParam(String str, String[] strArr) {
        return setParamInternal(str, strArr);
    }

    public Intent setStringArrayListParam(String str, ArrayList<String> arrayList) {
        return setParamInternal(str, arrayList);
    }

    public Intent setParam(String str, CharSequence charSequence) {
        return setParamInternal(str, charSequence);
    }

    public Intent setParam(String str, CharSequence[] charSequenceArr) {
        return setParamInternal(str, charSequenceArr);
    }

    public Intent setCharSequenceArrayListParam(String str, ArrayList<CharSequence> arrayList) {
        return setParamInternal(str, arrayList);
    }

    public Intent setParam(String str, Serializable serializable) {
        return setParamInternal(str, serializable);
    }

    public Intent setParam(String str, Sequenceable sequenceable) {
        return setParamInternal(str, sequenceable);
    }

    public Intent setParam(String str, Sequenceable[] sequenceableArr) {
        return setParamInternal(str, sequenceableArr);
    }

    @Deprecated
    public <T extends Sequenceable> Intent setParcelableArrayListParam(String str, ArrayList<T> arrayList) {
        return setParamInternal(str, arrayList);
    }

    public <T extends Sequenceable> Intent setSequenceableArrayListParam(String str, ArrayList<T> arrayList) {
        return setParamInternal(str, arrayList);
    }

    private <T> Intent setParamInternal(String str, T t) {
        if (this.parameters == null) {
            this.parameters = new IntentParams();
        }
        this.parameters.setParam(str, t);
        return this;
    }

    public void removeParam(String str) {
        IntentParams intentParams = this.parameters;
        if (intentParams != null) {
            intentParams.remove(str);
            if (this.parameters.isEmpty()) {
                this.parameters = null;
            }
        }
    }

    public Intent replaceParams(IntentParams intentParams) {
        this.parameters = intentParams == null ? null : new IntentParams(intentParams);
        return this;
    }

    public Intent replaceParams(Intent intent) {
        this.parameters = (intent == null || intent.getParams() == null) ? null : new IntentParams(intent.getParams());
        return this;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Intent)) {
            return false;
        }
        Intent intent = (Intent) obj;
        if (!Objects.equals(this.operation.getAction(), intent.getAction()) || !Objects.equals(this.operation.getUri(), intent.getUri()) || !Objects.equals(this.operation.getBundleName(), intent.getBundle()) || !Objects.equals(constructElement(), intent.getElement()) || !Objects.equals(this.operation.getEntities(), intent.getEntities())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int i = 0;
        if (this.operation.getAction() != null) {
            i = 0 + this.operation.getAction().hashCode();
        }
        if (this.operation.getUri() != null) {
            i += this.operation.getUri().hashCode();
        }
        if (constructElement() != null) {
            i += constructElement().hashCode();
        }
        if (this.operation.getEntities() != null) {
            i += this.operation.getEntities().hashCode();
        }
        return this.operation.getBundleName() != null ? i + this.operation.getBundleName().hashCode() : i;
    }
}
