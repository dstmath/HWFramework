package ohos.global.resource.solidxml;

import java.io.IOException;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.ResourceManagerImpl;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public class TypedAttributeImpl extends TypedAttribute {
    private static final String[][] HARMONY_REFERENCE_PREFIX = {new String[]{"@ohos:bool/", "@ohos:color/", "@ohos:dimen/", "@ohos:integer/", "@ohos:string/", "@ohos:layout/", "@ohos:drawable/", "@ohos:theme/", "@ohos:graphic/"}, new String[]{"$ohos:boolean:", "$ohos:color:", "$ohos:float:", "$ohos:integer:", "$ohos:string:", "$ohos:layout:", "$ohos:media:", "$ohos:pattern:", "$ohos:graphic:"}};
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "TypedAttributeImpl");
    private static final String[][] REFERENCE_PREFIX = {new String[]{"@bool/", "@color/", "@dimen/", "@integer/", "@string/", "@layout/", "@drawable/", "@theme/", "@graphic/"}, new String[]{"$boolean:", "$color:", "$float:", "$integer:", "$string:", "$layout:", "$media:", "$pattern:", "$graphic:"}};
    private String attrName;
    private String attrValue;
    private boolean isHarmonyPrefix = false;
    private ResourceManager resManager;
    int type = -1;
    String unit = "px";
    int version = 0;

    public TypedAttributeImpl(ResourceManager resourceManager, String str, String str2) {
        this.resManager = resourceManager;
        this.attrName = str;
        this.attrValue = str2;
        if (this.attrValue != null) {
            int length = REFERENCE_PREFIX[0].length;
            for (int i = 0; i < length; i++) {
                if (this.attrValue.startsWith(REFERENCE_PREFIX[0][i])) {
                    this.type = i;
                    this.version = 0;
                    return;
                } else if (this.attrValue.startsWith(REFERENCE_PREFIX[1][i])) {
                    this.type = i;
                    this.version = 1;
                    return;
                } else if (this.attrValue.startsWith(HARMONY_REFERENCE_PREFIX[0][i])) {
                    this.isHarmonyPrefix = true;
                    this.version = 0;
                    this.type = i;
                    return;
                } else if (this.attrValue.startsWith(HARMONY_REFERENCE_PREFIX[1][i])) {
                    this.isHarmonyPrefix = true;
                    this.version = 1;
                    this.type = i;
                    return;
                }
            }
        }
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public int getType() {
        return this.type;
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public boolean getBooleanValue() throws NotExistException, IOException, WrongTypeException {
        String str = this.attrValue;
        if (str == null || str.length() == 0 || this.resManager == null) {
            throw new NotExistException("value not exist");
        } else if (!this.attrValue.startsWith("@") && !this.attrValue.startsWith("$")) {
            return Boolean.parseBoolean(this.attrValue);
        } else {
            return this.resManager.getElement(tryDereferrence(this.attrValue)).getBoolean();
        }
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public int getColorValue() throws NotExistException, IOException, WrongTypeException {
        String str = this.attrValue;
        if (str == null || str.length() == 0 || this.resManager == null) {
            throw new NotExistException("value not exist");
        } else if (!this.attrValue.startsWith("@") && !this.attrValue.startsWith("$")) {
            return 0;
        } else {
            return this.resManager.getElement(tryDereferrence(this.attrValue)).getColor();
        }
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public int getIntegerValue() throws NotExistException, IOException, WrongTypeException {
        String str = this.attrValue;
        if (str == null || str.length() == 0 || this.resManager == null) {
            throw new NotExistException("value not exist");
        }
        try {
            if (!this.attrValue.startsWith("@")) {
                if (!this.attrValue.startsWith("$")) {
                    return Integer.parseInt(this.attrValue);
                }
            }
            return this.resManager.getElement(tryDereferrence(this.attrValue)).getInteger();
        } catch (NumberFormatException unused) {
            throw new WrongTypeException("not a valid integer");
        }
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public String getStringValue() throws NotExistException, IOException, WrongTypeException {
        String str = this.attrValue;
        if (str == null || str.length() == 0 || this.resManager == null || (!this.attrValue.startsWith("@") && !this.attrValue.startsWith("$"))) {
            return this.attrValue;
        }
        return this.resManager.getElement(tryDereferrence(this.attrValue)).getString();
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public Pattern getPatternValue() throws NotExistException, IOException, WrongTypeException {
        String str = this.attrValue;
        if (str == null || ((!str.startsWith("@") && !this.attrValue.startsWith("$")) || this.resManager == null)) {
            throw new NotExistException("value not exist");
        }
        return this.resManager.getElement(tryDereferrence(this.attrValue)).getPattern();
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public SolidXml getLayoutValue() throws NotExistException, IOException, WrongTypeException {
        String str;
        String str2 = this.attrValue;
        if (!(str2 == null || str2.length() == 0 || this.resManager == null)) {
            if (this.isHarmonyPrefix) {
                str = subAttrValue(this.attrValue, HARMONY_REFERENCE_PREFIX[this.version]);
            } else {
                str = subAttrValue(this.attrValue, REFERENCE_PREFIX[this.version]);
            }
            if (str == null) {
                return null;
            }
            try {
                return this.resManager.getSolidXml(Integer.valueOf(Integer.parseInt(str)).intValue());
            } catch (NumberFormatException unused) {
                HiLog.error(LABEL, "dereferrence format error", new Object[0]);
            }
        }
        return null;
    }

    private String subAttrValue(String str, String[] strArr) {
        int length;
        int i = this.type;
        if (i < 0 || i >= strArr.length || (length = strArr[i].length()) < 0 || length > str.length()) {
            return null;
        }
        return str.substring(length);
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public String getOriginalValue() {
        return this.attrValue;
    }

    private int tryDereferrence(String str) throws NotExistException, IOException, WrongTypeException {
        int i;
        String str2;
        if (str == null || str.length() == 0 || this.resManager == null || (i = this.type) == -1) {
            return 0;
        }
        if (this.isHarmonyPrefix) {
            str2 = str.substring(HARMONY_REFERENCE_PREFIX[this.version][i].length());
        } else {
            str2 = str.substring(REFERENCE_PREFIX[this.version][i].length());
        }
        Integer num = -1;
        try {
            num = Integer.valueOf(Integer.parseInt(str2));
        } catch (NumberFormatException unused) {
            HiLog.error(LABEL, "dereferrence format error", new Object[0]);
        }
        return num.intValue();
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public float getFloatValue() throws NotExistException, IOException, WrongTypeException {
        String str = this.attrValue;
        if (str == null || str.length() == 0 || this.resManager == null) {
            throw new NotExistException("value not exist");
        } else if (this.attrValue.startsWith("@") || this.attrValue.startsWith("$")) {
            return this.resManager.getElement(tryDereferrence(this.attrValue)).getFloat();
        } else {
            ResourceManager resourceManager = this.resManager;
            if (resourceManager instanceof ResourceManagerImpl) {
                return ((ResourceManagerImpl) resourceManager).parseFloat(this.attrValue);
            }
            return ConstantValue.MIN_ZOOM_VALUE;
        }
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public String getMediaValue() throws NotExistException, IOException, WrongTypeException {
        String str = this.attrValue;
        if (str == null || str.length() == 0 || this.resManager == null) {
            throw new NotExistException("value not exist");
        } else if (!this.attrValue.startsWith("@") && !this.attrValue.startsWith("$")) {
            return this.attrValue;
        } else {
            return this.resManager.getElement(tryDereferrence(this.attrValue)).getString();
        }
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public String getName() {
        return this.attrName;
    }

    @Override // ohos.global.resource.solidxml.TypedAttribute
    public int getResId() throws NotExistException, IOException, WrongTypeException {
        if (this.attrValue.startsWith("$")) {
            return tryDereferrence(this.attrValue);
        }
        return 0;
    }
}
