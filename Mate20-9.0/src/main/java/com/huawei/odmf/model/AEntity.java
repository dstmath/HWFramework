package com.huawei.odmf.model;

import android.text.TextUtils;
import com.huawei.odmf.core.StatementsLoader;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.Index;
import com.huawei.odmf.model.api.ObjectModel;
import java.util.ArrayList;
import java.util.List;

public class AEntity implements Entity {
    private List<AAttribute> attributes;
    private List<AEntityId> entityId;
    private String entityName;
    private String entityVersion;
    private int entityVersionCode;
    private List<Index> indexes;
    private boolean isKeyAutoIncrement;
    private StatementsLoader mStatements;
    private ObjectModel model;
    private List<ARelationship> relationships;
    private String tableName;

    public AEntity() {
        this.entityName = null;
        this.tableName = null;
        this.entityVersion = null;
        this.entityId = null;
        this.attributes = null;
        this.relationships = null;
        this.indexes = null;
        this.model = null;
        this.isKeyAutoIncrement = false;
        this.mStatements = new StatementsLoader();
    }

    public AEntity(String entityName2, String tableName2, List<AEntityId> entityId2, List<AAttribute> attributes2, List<ARelationship> relationships2, ObjectModel model2, String entityVersion2, int entityVersionCode2) {
        this.entityName = null;
        this.tableName = null;
        this.entityVersion = null;
        this.entityId = null;
        this.attributes = null;
        this.relationships = null;
        this.indexes = null;
        this.model = null;
        this.isKeyAutoIncrement = false;
        this.entityName = entityName2;
        this.tableName = tableName2;
        this.entityId = entityId2;
        this.attributes = attributes2;
        this.relationships = relationships2;
        this.model = model2;
        this.entityVersion = entityVersion2;
        this.entityVersionCode = entityVersionCode2;
        this.mStatements = new StatementsLoader();
    }

    /* access modifiers changed from: package-private */
    public void setModel(ObjectModel model2) {
        this.model = model2;
    }

    /* access modifiers changed from: package-private */
    public void setIndexes(List<Index> indexes2) {
        this.indexes = indexes2;
    }

    public List<Index> getIndexes() {
        return this.indexes;
    }

    public boolean isKeyAutoIncrement() {
        return this.isKeyAutoIncrement;
    }

    public StatementsLoader getStatements() {
        return this.mStatements;
    }

    /* access modifiers changed from: package-private */
    public void setKeyAutoIncrement(boolean isKeyAutoIncrement2) {
        this.isKeyAutoIncrement = isKeyAutoIncrement2;
    }

    public String getEntityVersion() {
        return this.entityVersion;
    }

    /* access modifiers changed from: package-private */
    public void setEntityVersion(String entityVersion2) {
        this.entityVersion = entityVersion2;
    }

    public int getEntityVersionCode() {
        return this.entityVersionCode;
    }

    public void setEntityVersionCode(int entityVersionCode2) {
        this.entityVersionCode = entityVersionCode2;
    }

    public ObjectModel getModel() {
        return this.model;
    }

    public String getEntityName() {
        return this.entityName;
    }

    /* access modifiers changed from: package-private */
    public void setEntityName(String entityName2) {
        this.entityName = entityName2;
    }

    public String getTableName() {
        return this.tableName;
    }

    /* access modifiers changed from: package-private */
    public void setTableName(String tableName2) {
        this.tableName = tableName2;
    }

    public List<AEntityId> getEntityId() {
        return this.entityId;
    }

    /* access modifiers changed from: package-private */
    public void setEntityId(List<AEntityId> entityId2) {
        this.entityId = entityId2;
    }

    public List<AAttribute> getAttributes() {
        return this.attributes;
    }

    /* access modifiers changed from: package-private */
    public void setAttributes(List<AAttribute> attributes2) {
        this.attributes = attributes2;
    }

    public List<String> getIdName() {
        if (this.entityId == null) {
            throw new ODMFIllegalStateException("entityId has not been initialized.");
        }
        List<String> result = new ArrayList<>();
        int size = this.entityId.size();
        for (int i = 0; i < size; i++) {
            result.add(this.entityId.get(i).getIdName());
        }
        return result;
    }

    static Class getClassType(String entityName2) {
        if (TextUtils.isEmpty(entityName2)) {
            throw new ODMFIllegalArgumentException("entityName is empty.");
        }
        try {
            return Class.forName(entityName2);
        } catch (ClassNotFoundException e) {
            throw new ODMFIllegalArgumentException(entityName2 + "is not a valid entity name.");
        }
    }

    public List<ARelationship> getRelationships() {
        return this.relationships;
    }

    /* access modifiers changed from: package-private */
    public void setRelationships(List<ARelationship> relationships2) {
        this.relationships = relationships2;
    }

    public boolean isAttribute(String propertyName) {
        List<AAttribute> attributeList = this.attributes;
        int size = attributeList.size();
        for (int i = 0; i < size; i++) {
            if (attributeList.get(i).getFieldName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRelationship(String propertyName) {
        for (ARelationship relationship : this.relationships) {
            if (relationship.getFieldName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    public AAttribute getAttribute(String propertyName) {
        List<AAttribute> list = this.attributes;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            AAttribute attribute = list.get(i);
            if (attribute.getFieldName().equals(propertyName)) {
                return attribute;
            }
        }
        return null;
    }

    public ARelationship getRelationship(String propertyName) {
        for (ARelationship relationship : this.relationships) {
            if (relationship.getFieldName().equals(propertyName)) {
                return relationship;
            }
        }
        return null;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AEntity entity = (AEntity) o;
        if (this.entityName != null) {
            if (!this.entityName.equals(entity.entityName)) {
                return false;
            }
        } else if (entity.entityName != null) {
            return false;
        }
        if (this.tableName != null) {
            if (!this.tableName.equals(entity.tableName)) {
                return false;
            }
        } else if (entity.tableName != null) {
            return false;
        }
        if (!this.entityId.equals(entity.entityId)) {
            return false;
        }
        if (this.attributes != null) {
            if (!this.attributes.equals(entity.attributes)) {
                return false;
            }
        } else if (entity.attributes != null) {
            return false;
        }
        if (this.relationships != null) {
            z = this.relationships.equals(entity.relationships);
        } else if (entity.relationships != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int result;
        int i;
        int i2;
        int i3 = 0;
        if (this.entityName != null) {
            result = this.entityName.hashCode();
        } else {
            result = 0;
        }
        int i4 = result * 31;
        if (this.tableName != null) {
            i = this.tableName.hashCode();
        } else {
            i = 0;
        }
        int hashCode = (((i4 + i) * 31) + this.entityId.hashCode()) * 31;
        if (this.attributes != null) {
            i2 = this.attributes.hashCode();
        } else {
            i2 = 0;
        }
        int i5 = (hashCode + i2) * 31;
        if (this.relationships != null) {
            i3 = this.relationships.hashCode();
        }
        return i5 + i3;
    }

    public String toString() {
        return "AEntity{entityName='" + this.entityName + '\'' + ", tableName='" + this.tableName + '\'' + ", entityId=" + this.entityId + ", attributes=" + this.attributes + '}';
    }
}
