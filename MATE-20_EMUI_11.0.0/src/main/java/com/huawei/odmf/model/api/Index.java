package com.huawei.odmf.model.api;

import java.util.List;

public interface Index {
    boolean addAttribute(Attribute attribute);

    List<Attribute> getCompositeIndexAttributes();

    String getIndexName();

    void setCompositeIndexAttributes(List<Attribute> list);
}
