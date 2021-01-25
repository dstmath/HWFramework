package ohos.data.distributed.common;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.fastjson.JSONObject;

public class Schema {
    private static final String DEFAULT_SCHEMA_VERSION = "1.0";
    private static final String LABEL = "Schema";
    private static final String SCHEMA_DEFINE = "SCHEMA_DEFINE";
    private static final String SCHEMA_INDEXES = "SCHEMA_INDEXES";
    private static final String SCHEMA_MODE = "SCHEMA_MODE";
    private static final String SCHEMA_SKIPSIZE = "SCHEMA_SKIPSIZE";
    private static final String SCHEMA_VERSION = "SCHEMA_VERSION";
    private List<String> indexes = new ArrayList();
    private FieldNode rootFieldNode = new FieldNode(SCHEMA_DEFINE);
    private SchemaMode schemaMode;
    private int skipSize = 1;
    private String version = "1.0";

    public void setSchemaMode(SchemaMode schemaMode2) {
        this.schemaMode = schemaMode2;
    }

    public boolean setIndexes(List<String> list) {
        if (list == null) {
            LogPrint.error(LABEL, "setIndexes input param is null", new Object[0]);
            return false;
        }
        this.indexes = list;
        return true;
    }

    public String getVersion() {
        return this.version;
    }

    public SchemaMode getSchemaMode() {
        return this.schemaMode;
    }

    public List<String> getIndexes() {
        return this.indexes;
    }

    public FieldNode getRootFieldNode() {
        return this.rootFieldNode;
    }

    public String toJsonString() {
        if (this.schemaMode == null) {
            LogPrint.error(LABEL, "toJsonString schemaMode has not been set", new Object[0]);
            return "";
        }
        JSONObject jSONObject = new JSONObject();
        jSONObject.put(SCHEMA_VERSION, this.version);
        jSONObject.put(SCHEMA_MODE, this.schemaMode.getCode());
        jSONObject.put(this.rootFieldNode.getFieldName(), this.rootFieldNode.getValueForJson());
        jSONObject.put(SCHEMA_INDEXES, this.indexes);
        jSONObject.put(SCHEMA_SKIPSIZE, Integer.valueOf(this.skipSize));
        return jSONObject.toJSONString();
    }
}
