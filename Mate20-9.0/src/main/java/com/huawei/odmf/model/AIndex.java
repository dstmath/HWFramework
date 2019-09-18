package com.huawei.odmf.model;

import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.model.api.Index;
import java.util.ArrayList;
import java.util.List;

public class AIndex implements Index {
    List<Attribute> compositeIndexAttributes;
    String indexName;

    public AIndex(String indexName2, List<Attribute> compositeIndexAttributes2) {
        this.indexName = indexName2;
        this.compositeIndexAttributes = compositeIndexAttributes2;
    }

    public AIndex(String indexName2) {
        this(indexName2, new ArrayList());
    }

    public List<Attribute> getCompositeIndexAttributes() {
        return this.compositeIndexAttributes;
    }

    public void setCompositeIndexAttributes(List<Attribute> compositeIndexAttributes2) {
        this.compositeIndexAttributes = compositeIndexAttributes2;
    }

    public boolean addAttribute(Attribute attribute) {
        return this.compositeIndexAttributes.add(attribute);
    }

    public String getIndexName() {
        return this.indexName;
    }
}
