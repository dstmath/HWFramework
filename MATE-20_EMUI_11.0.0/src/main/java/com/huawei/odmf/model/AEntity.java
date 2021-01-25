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
    private static final int MULTIPLIER = 31;
    private List<AAttribute> attributes;
    private List<AEntityId> entityIds;
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
        this.entityIds = null;
        this.attributes = null;
        this.relationships = null;
        this.indexes = null;
        this.model = null;
        this.isKeyAutoIncrement = false;
        this.mStatements = new StatementsLoader();
    }

    public AEntity(String str, String str2, List<AEntityId> list, List<AAttribute> list2, List<ARelationship> list3, ObjectModel objectModel, String str3, int i) {
        this.entityName = null;
        this.tableName = null;
        this.entityVersion = null;
        this.entityIds = null;
        this.attributes = null;
        this.relationships = null;
        this.indexes = null;
        this.model = null;
        this.isKeyAutoIncrement = false;
        this.entityName = str;
        this.tableName = str2;
        this.entityIds = list;
        this.attributes = list2;
        this.relationships = list3;
        this.model = objectModel;
        this.entityVersion = str3;
        this.entityVersionCode = i;
        this.mStatements = new StatementsLoader();
    }

    @Override // com.huawei.odmf.model.api.Entity
    public StatementsLoader getStatements() {
        return this.mStatements;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public ObjectModel getModel() {
        return this.model;
    }

    /* access modifiers changed from: package-private */
    public void setModel(ObjectModel objectModel) {
        this.model = objectModel;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public List<Index> getIndexes() {
        return this.indexes;
    }

    /* access modifiers changed from: package-private */
    public void setIndexes(List<Index> list) {
        this.indexes = list;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public boolean isKeyAutoIncrement() {
        return this.isKeyAutoIncrement;
    }

    /* access modifiers changed from: package-private */
    public void setKeyAutoIncrement(boolean z) {
        this.isKeyAutoIncrement = z;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public String getEntityVersion() {
        return this.entityVersion;
    }

    /* access modifiers changed from: package-private */
    public void setEntityVersion(String str) {
        this.entityVersion = str;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public int getEntityVersionCode() {
        return this.entityVersionCode;
    }

    public void setEntityVersionCode(int i) {
        this.entityVersionCode = i;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public String getEntityName() {
        return this.entityName;
    }

    /* access modifiers changed from: package-private */
    public void setEntityName(String str) {
        this.entityName = str;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public String getTableName() {
        return this.tableName;
    }

    /* access modifiers changed from: package-private */
    public void setTableName(String str) {
        this.tableName = str;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public List<AEntityId> getEntityIds() {
        return this.entityIds;
    }

    /* access modifiers changed from: package-private */
    public void setEntityIds(List<AEntityId> list) {
        this.entityIds = list;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public List<AAttribute> getAttributes() {
        return this.attributes;
    }

    /* access modifiers changed from: package-private */
    public void setAttributes(List<AAttribute> list) {
        this.attributes = list;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public List<ARelationship> getRelationships() {
        return this.relationships;
    }

    /* access modifiers changed from: package-private */
    public void setRelationships(List<ARelationship> list) {
        this.relationships = list;
    }

    @Override // com.huawei.odmf.model.api.Entity
    public List<String> getIdName() {
        if (this.entityIds != null) {
            ArrayList arrayList = new ArrayList();
            int size = this.entityIds.size();
            for (int i = 0; i < size; i++) {
                arrayList.add(this.entityIds.get(i).getIdName());
            }
            return arrayList;
        }
        throw new ODMFIllegalStateException("The entityIds has not been initialized.");
    }

    static Class getClassType(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Class.forName(str);
            } catch (ClassNotFoundException unused) {
                throw new ODMFIllegalArgumentException(str + "is not a valid entity name.");
            }
        } else {
            throw new ODMFIllegalArgumentException("The entityName is empty.");
        }
    }

    @Override // com.huawei.odmf.model.api.Entity
    public boolean isAttribute(String str) {
        if (!TextUtils.isEmpty(str)) {
            List<AAttribute> list = this.attributes;
            if (list != null) {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    if (this.attributes.get(i).getFieldName().equals(str)) {
                        return true;
                    }
                }
                return false;
            }
            throw new ODMFIllegalStateException("The attributes has not been initialized.");
        }
        throw new ODMFIllegalArgumentException("The propertyName is empty.");
    }

    @Override // com.huawei.odmf.model.api.Entity
    public boolean isRelationship(String str) {
        if (!TextUtils.isEmpty(str)) {
            List<ARelationship> list = this.relationships;
            if (list != null) {
                for (ARelationship aRelationship : list) {
                    if (aRelationship.getFieldName().equals(str)) {
                        return true;
                    }
                }
                return false;
            }
            throw new ODMFIllegalStateException("The relationships has not been initialized.");
        }
        throw new ODMFIllegalArgumentException("The propertyName is empty.");
    }

    @Override // com.huawei.odmf.model.api.Entity
    public AAttribute getAttribute(String str) {
        if (!TextUtils.isEmpty(str)) {
            List<AAttribute> list = this.attributes;
            if (list != null) {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    AAttribute aAttribute = this.attributes.get(i);
                    if (aAttribute.getFieldName().equals(str)) {
                        return aAttribute;
                    }
                }
                return null;
            }
            throw new ODMFIllegalStateException("The attributes has not been initialized.");
        }
        throw new ODMFIllegalArgumentException("The propertyName is empty.");
    }

    @Override // com.huawei.odmf.model.api.Entity
    public ARelationship getRelationship(String str) {
        if (!TextUtils.isEmpty(str)) {
            List<ARelationship> list = this.relationships;
            if (list != null) {
                for (ARelationship aRelationship : list) {
                    if (aRelationship.getFieldName().equals(str)) {
                        return aRelationship;
                    }
                }
                return null;
            }
            throw new ODMFIllegalStateException("The relationships has not been initialized.");
        }
        throw new ODMFIllegalArgumentException("The propertyName is empty.");
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AEntity aEntity = (AEntity) obj;
        String str = this.entityName;
        if (str == null ? aEntity.entityName != null : !str.equals(aEntity.entityName)) {
            return false;
        }
        String str2 = this.tableName;
        if (str2 == null ? aEntity.tableName != null : !str2.equals(aEntity.tableName)) {
            return false;
        }
        if (!this.entityIds.equals(aEntity.entityIds)) {
            return false;
        }
        List<AAttribute> list = this.attributes;
        if (list == null ? aEntity.attributes != null : !list.equals(aEntity.attributes)) {
            return false;
        }
        List<ARelationship> list2 = this.relationships;
        if (list2 != null) {
            return list2.equals(aEntity.relationships);
        }
        return aEntity.relationships == null;
    }

    public int hashCode() {
        String str = this.entityName;
        int i = 0;
        int hashCode = (str != null ? str.hashCode() : 0) * MULTIPLIER;
        String str2 = this.tableName;
        int hashCode2 = (((hashCode + (str2 != null ? str2.hashCode() : 0)) * MULTIPLIER) + this.entityIds.hashCode()) * MULTIPLIER;
        List<AAttribute> list = this.attributes;
        int hashCode3 = (hashCode2 + (list != null ? list.hashCode() : 0)) * MULTIPLIER;
        List<ARelationship> list2 = this.relationships;
        if (list2 != null) {
            i = list2.hashCode();
        }
        return hashCode3 + i;
    }

    public String toString() {
        return "AEntity{entityName='" + this.entityName + "', tableName='" + this.tableName + "', entityIds=" + this.entityIds + ", attributes=" + this.attributes + '}';
    }
}
