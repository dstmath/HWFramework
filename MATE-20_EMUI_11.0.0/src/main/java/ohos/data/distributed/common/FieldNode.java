package ohos.data.distributed.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import ohos.utils.fastjson.JSONObject;

public class FieldNode {
    private static final String LABEL = "FieldNode";
    private Object defaultValue = null;
    private String fieldName;
    private List<FieldNode> fields = new ArrayList();
    private boolean isNullable = true;
    private boolean isWithDefaultValue = false;
    private FieldValueType type = null;

    public FieldNode(String str) {
        this.fieldName = str;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public boolean appendChild(FieldNode fieldNode) {
        if (fieldNode == null) {
            LogPrint.error(LABEL, "appendChild input param is null", new Object[0]);
            return false;
        }
        this.fields.add(fieldNode);
        return true;
    }

    public List<FieldNode> getChildren() {
        return this.fields;
    }

    public FieldValueType getType() {
        return this.type;
    }

    public void setType(FieldValueType fieldValueType) {
        this.type = fieldValueType;
    }

    public boolean isNullable() {
        return this.isNullable;
    }

    public boolean setNullable(boolean z) {
        if (z || !this.isWithDefaultValue || this.defaultValue != null) {
            this.isNullable = z;
            return true;
        }
        LogPrint.error(LABEL, "Already set defaultValue as null", new Object[0]);
        return false;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public boolean setDefault(String str) {
        if (this.type != FieldValueType.STRING) {
            LogPrint.error(LABEL, "setDefaultString type is null or not match", new Object[0]);
            return false;
        } else if (str == null) {
            LogPrint.error(LABEL, "setDefaultString input param is null", new Object[0]);
            return false;
        } else {
            this.isWithDefaultValue = true;
            this.defaultValue = str;
            return true;
        }
    }

    public boolean setDefault(int i) {
        if (this.type != FieldValueType.INTEGER) {
            LogPrint.error(LABEL, "setDefaultInt type is null or not match", new Object[0]);
            return false;
        }
        this.isWithDefaultValue = true;
        this.defaultValue = Integer.valueOf(i);
        return true;
    }

    public boolean setDefault(long j) {
        if (this.type != FieldValueType.LONG) {
            LogPrint.error(LABEL, "setDefaultLong type is null or not match", new Object[0]);
            return false;
        }
        this.isWithDefaultValue = true;
        this.defaultValue = Long.valueOf(j);
        return true;
    }

    public boolean setDefault(double d) {
        if (this.type != FieldValueType.DOUBLE) {
            LogPrint.error(LABEL, "setDefaultDouble type is null or not match", new Object[0]);
            return false;
        }
        this.isWithDefaultValue = true;
        this.defaultValue = Double.valueOf(d);
        return true;
    }

    public boolean setDefault(boolean z) {
        if (this.type != FieldValueType.BOOLEAN) {
            LogPrint.error(LABEL, "setDefaultBoolean type is null or not match", new Object[0]);
            return false;
        }
        this.isWithDefaultValue = true;
        this.defaultValue = Boolean.valueOf(z);
        return true;
    }

    public boolean setDefaultNull() {
        if (!this.isNullable) {
            LogPrint.error(LABEL, "this was set not nullable", new Object[0]);
            return false;
        } else if (this.type == FieldValueType.JSON_ARRAY || this.type == FieldValueType.JSON_OBJECT) {
            LogPrint.error(LABEL, "type is not nullable", new Object[0]);
            return false;
        } else {
            this.isWithDefaultValue = true;
            this.defaultValue = null;
            return true;
        }
    }

    public Object getValueForJson() {
        return isLeaf() ? getLeafValueForJson() : getNonLeafValueForJson();
    }

    private boolean isLeaf() {
        return this.fields.isEmpty();
    }

    private Object getLeafValueForJson() {
        if (getType() == FieldValueType.JSON_ARRAY) {
            return Collections.emptyList();
        }
        if (getType() == FieldValueType.JSON_OBJECT) {
            return new JSONObject();
        }
        return fieldValueAsString();
    }

    private JSONObject getNonLeafValueForJson() {
        JSONObject jSONObject = new JSONObject();
        for (FieldNode fieldNode : this.fields) {
            jSONObject.put(fieldNode.getFieldName(), fieldNode.getValueForJson());
        }
        return jSONObject;
    }

    private String fieldValueAsString() {
        if (this.type == null) {
            LogPrint.error(LABEL, "type is not set", new Object[0]);
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.type.getCode());
        if (!this.isNullable) {
            sb.append(",NOT NULL");
        }
        if (this.isWithDefaultValue) {
            sb.append(",DEFAULT ");
            if (this.defaultValue == null) {
                sb.append("null");
            } else if (this.type == FieldValueType.STRING) {
                sb.append("'");
                sb.append(this.defaultValue);
                sb.append("'");
            } else if (this.type == FieldValueType.DOUBLE) {
                sb.append(String.format(Locale.ENGLISH, "%.12f", this.defaultValue));
            } else {
                sb.append(this.defaultValue);
            }
        }
        return sb.toString();
    }
}
