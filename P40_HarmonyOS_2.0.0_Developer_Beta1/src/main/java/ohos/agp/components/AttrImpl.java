package ohos.agp.components;

import java.io.IOException;
import java.util.Objects;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.Attr;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.ElementScatter;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.app.Context;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AttrImpl implements Attr {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_AttrImpl");
    Context mContext;
    private String mFullName;
    private String mName;
    private String mNamespace;
    private Attr.AttrType mType;
    TypedAttribute mTypeAttribute;
    private String mValue;

    public AttrImpl(String str, TypedAttribute typedAttribute, Context context) {
        this.mType = Attr.AttrType.NONE;
        this.mContext = context;
        this.mName = str;
        this.mTypeAttribute = typedAttribute;
        if (typedAttribute != null) {
            this.mValue = typedAttribute.getOriginalValue();
        }
    }

    public AttrImpl(String str, String str2) {
        this(str, (String) null, str2);
    }

    public AttrImpl(String str, String str2, String str3) {
        this(null, str, str2, str3);
    }

    public AttrImpl(String str, String str2, String str3, String str4) {
        this.mType = Attr.AttrType.NONE;
        Objects.requireNonNull(str2, "FullName can't be null");
        Objects.requireNonNull(str4, "Value can't be null");
        this.mFullName = str2;
        this.mValue = str4;
        this.mNamespace = str;
        this.mName = str3;
        if (str2.contains(":")) {
            String[] split = str2.split(":");
            if (split.length == 2) {
                this.mNamespace = split[0];
                this.mName = split[1];
            } else {
                throw new IllegalArgumentException("Full Name's format is incorrect: " + str2);
            }
        }
        if (this.mName == null) {
            this.mName = this.mFullName;
        }
    }

    @Override // ohos.agp.components.Attr
    public String getName() {
        return this.mName;
    }

    @Override // ohos.agp.components.Attr
    public String getStringValue() {
        TypedAttribute typedAttribute = this.mTypeAttribute;
        if (typedAttribute != null && typedAttribute.getType() == 4) {
            try {
                return this.mTypeAttribute.getStringValue();
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.error(TAG, "getStringValue catch error", new Object[0]);
            }
        }
        String str = this.mValue;
        return str != null ? str : "";
    }

    @Override // ohos.agp.components.Attr
    public int getIntegerValue() {
        TypedAttribute typedAttribute = this.mTypeAttribute;
        if (typedAttribute != null && typedAttribute.getType() == 3) {
            try {
                return this.mTypeAttribute.getIntegerValue();
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.error(TAG, "getIntegerValue catch error", new Object[0]);
            }
        }
        return AttrHelper.convertValueToInt(this.getStringValue(), 0);
    }

    @Override // ohos.agp.components.Attr
    public boolean getBoolValue() {
        TypedAttribute typedAttribute = this.mTypeAttribute;
        if (typedAttribute != null && typedAttribute.getType() == 0) {
            try {
                return this.mTypeAttribute.getBooleanValue();
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.error(TAG, "getBoolValue catch error", new Object[0]);
            }
        }
        return AttrHelper.convertValueToBoolean(this.getStringValue(), false);
    }

    @Override // ohos.agp.components.Attr
    public float getFloatValue() {
        return AttrHelper.convertValueToFloat(getStringValue(), 0.0f);
    }

    @Override // ohos.agp.components.Attr
    public long getLongValue() {
        return AttrHelper.convertValueToLong(getStringValue(), 0);
    }

    @Override // ohos.agp.components.Attr
    public Element getElement() {
        TypedAttribute typedAttribute = this.mTypeAttribute;
        if (typedAttribute == null) {
            HiLog.error(TAG, "typedAttribute is null", new Object[0]);
            return AttrHelper.convertValueToElement(getStringValue());
        } else if (typedAttribute.getType() == 8) {
            return parseXmlInGraphic();
        } else {
            if (this.mTypeAttribute.getType() == 6) {
                try {
                    String mediaValue = this.mTypeAttribute.getMediaValue();
                    Context context = this.mContext;
                    if (context == null) {
                        HiLog.error(TAG, "mContext is null!", new Object[0]);
                        return null;
                    }
                    ResourceManager resourceManager = context.getResourceManager();
                    if (resourceManager == null) {
                        return null;
                    }
                    return AttrHelper.getElementFromPath(mediaValue, resourceManager);
                } catch (IOException | NotExistException | WrongTypeException unused) {
                    HiLog.error(TAG, "get media element path failed", new Object[0]);
                    return null;
                }
            } else if (this.mTypeAttribute.getType() != 1) {
                return AttrHelper.convertValueToElement(getStringValue());
            } else {
                ShapeElement shapeElement = new ShapeElement();
                try {
                    shapeElement.setRgbColor(RgbColor.fromArgbInt(this.mTypeAttribute.getColorValue()));
                } catch (IOException | NotExistException | WrongTypeException unused2) {
                    HiLog.error(TAG, "set Background failed using color reference.", new Object[0]);
                }
                return shapeElement;
            }
        }
    }

    private Element parseXmlInGraphic() {
        try {
            int resId = this.mTypeAttribute.getResId();
            HiLog.debug(TAG, "read graphic element resource id: %{public}d", new Object[]{Integer.valueOf(resId)});
            return ElementScatter.getInstance(this.mContext).parse(resId);
        } catch (IOException | NotExistException | WrongTypeException unused) {
            HiLog.error(TAG, "get graphic element resId failed", new Object[0]);
            return null;
        }
    }

    @Override // ohos.agp.components.Attr
    public int getDimensionValue() {
        TypedAttribute typedAttribute = this.mTypeAttribute;
        if (typedAttribute != null && typedAttribute.getType() == 2) {
            try {
                return (int) (((double) this.mTypeAttribute.getFloatValue()) + 0.5d);
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.error(TAG, "getDimensionValue catch error", new Object[0]);
            }
        }
        return AttrHelper.convertDimensionToPix(this.getStringValue(), AttrHelper.getDensity(this.mContext), 0);
    }

    @Override // ohos.agp.components.Attr
    public Color getColorValue() {
        TypedAttribute typedAttribute = this.mTypeAttribute;
        if (typedAttribute != null && typedAttribute.getType() == 1) {
            try {
                return new Color(this.mTypeAttribute.getColorValue());
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.error(TAG, "getColorValue catch error", new Object[0]);
            }
        }
        return AttrHelper.convertValueToColor(getStringValue());
    }

    @Override // ohos.agp.components.Attr
    public void setType(Attr.AttrType attrType) {
        this.mType = attrType;
    }

    @Override // ohos.agp.components.Attr
    public Attr.AttrType getType() {
        return this.mType;
    }

    @Override // ohos.agp.components.Attr
    public void setContext(Context context) {
        this.mContext = context;
    }

    @Override // ohos.agp.components.Attr
    public Context getContext() {
        return this.mContext;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AttrImpl)) {
            return false;
        }
        AttrImpl attrImpl = (AttrImpl) obj;
        return Objects.equals(this.mNamespace, attrImpl.mNamespace) && this.mFullName.equals(attrImpl.mFullName) && this.mName.equals(attrImpl.mName) && this.mValue.equals(attrImpl.mValue);
    }

    public int hashCode() {
        return Objects.hash(this.mNamespace, this.mFullName, this.mName, this.mValue);
    }

    public String toString() {
        return "AttrImpl{mNamespace='" + this.mNamespace + PatternTokenizer.SINGLE_QUOTE + ", mFullName='" + this.mFullName + PatternTokenizer.SINGLE_QUOTE + ", mName='" + this.mName + PatternTokenizer.SINGLE_QUOTE + ", mValue='" + this.mValue + PatternTokenizer.SINGLE_QUOTE + '}';
    }
}
