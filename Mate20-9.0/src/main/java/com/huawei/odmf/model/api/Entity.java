package com.huawei.odmf.model.api;

import com.huawei.odmf.core.StatementsLoader;
import java.util.List;

public interface Entity {
    Attribute getAttribute(String str);

    List<? extends Attribute> getAttributes();

    List<? extends EntityId> getEntityId();

    String getEntityName();

    String getEntityVersion();

    int getEntityVersionCode();

    List<String> getIdName();

    List<Index> getIndexes();

    ObjectModel getModel();

    Relationship getRelationship(String str);

    List<? extends Relationship> getRelationships();

    StatementsLoader getStatements();

    String getTableName();

    boolean isAttribute(String str);

    boolean isKeyAutoIncrement();

    boolean isRelationship(String str);
}
