package ohos.data.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ohos.data.search.model.ChangedIndexContent;
import ohos.data.search.model.IndexData;
import ohos.data.search.model.IndexForm;
import ohos.data.search.model.Recommendation;
import ohos.data.search.model.SearchableEntity;
import ohos.data.searchimpl.model.InnerChangedIndexContent;
import ohos.data.searchimpl.model.InnerIndexData;
import ohos.data.searchimpl.model.InnerIndexForm;
import ohos.data.searchimpl.model.InnerRecommendation;
import ohos.data.searchimpl.model.InnerSearchableEntity;

class ConvertUtils {
    ConvertUtils() {
    }

    static List<InnerIndexForm> indexForms2InnerIndexForms(List<IndexForm> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(list.size());
        for (IndexForm indexForm : list) {
            InnerIndexForm innerIndexForm = new InnerIndexForm();
            innerIndexForm.setIndexFieldName(indexForm.getIndexFieldName());
            innerIndexForm.setIndexType(indexForm.getIndexType());
            innerIndexForm.setPrimaryKey(indexForm.isPrimaryKey());
            innerIndexForm.setSearch(indexForm.isSearch());
            innerIndexForm.setStore(indexForm.isStore());
            arrayList.add(innerIndexForm);
        }
        return arrayList;
    }

    static List<IndexForm> innerIndexForms2IndexForms(List<InnerIndexForm> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(list.size());
        for (InnerIndexForm innerIndexForm : list) {
            arrayList.add(new IndexForm(innerIndexForm.getIndexFieldName(), innerIndexForm.getIndexType(), innerIndexForm.isPrimaryKey(), innerIndexForm.isStore(), innerIndexForm.isSearch()));
        }
        return arrayList;
    }

    static List<InnerIndexData> indexDatas2InnerIndexDatas(List<IndexData> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(list.size());
        for (IndexData indexData : list) {
            InnerIndexData innerIndexData = new InnerIndexData();
            innerIndexData.setValues(indexData.getValues());
            arrayList.add(innerIndexData);
        }
        return arrayList;
    }

    static List<IndexData> innerIndexDatas2IndexDatas(List<InnerIndexData> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(list.size());
        for (InnerIndexData innerIndexData : list) {
            IndexData indexData = new IndexData();
            indexData.setValues(innerIndexData.getValues());
            arrayList.add(indexData);
        }
        return arrayList;
    }

    static List<Recommendation> innerRecommendations2Recommendations(List<InnerRecommendation> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(list.size());
        for (InnerRecommendation innerRecommendation : list) {
            arrayList.add(new Recommendation(innerRecommendation.getField(), innerRecommendation.getValue(), innerIndexDatas2IndexDatas(innerRecommendation.getIndexDataList()), innerRecommendation.getCount()));
        }
        return arrayList;
    }

    static ChangedIndexContent innerIndexContent2IndexContent(InnerChangedIndexContent innerChangedIndexContent) {
        if (innerChangedIndexContent == null) {
            return null;
        }
        return new ChangedIndexContent(innerIndexDatas2IndexDatas(innerChangedIndexContent.getInsertedItems()), innerIndexDatas2IndexDatas(innerChangedIndexContent.getUpdatedItems()), innerIndexDatas2IndexDatas(innerChangedIndexContent.getDeletedItems()));
    }

    static SearchableEntity innerSearchableEntity2SearchableEntity(InnerSearchableEntity innerSearchableEntity) {
        return new SearchableEntity(innerSearchableEntity.getBundleName(), innerSearchableEntity.getAppId(), innerSearchableEntity.getPermission(), innerSearchableEntity.getIntentAction(), innerSearchableEntity.getComponentName(), innerSearchableEntity.isAllowGlobalSearch(), innerSearchableEntity.getVersionCode(), innerSearchableEntity.getVersionName());
    }

    static InnerSearchableEntity searchableEntity2InnerSearchableEntity(SearchableEntity searchableEntity) {
        return new InnerSearchableEntity(searchableEntity.getBundleName(), searchableEntity.getAppId(), searchableEntity.getPermission(), searchableEntity.getIntentAction(), searchableEntity.getComponentName(), searchableEntity.isAllowGlobalSearch(), searchableEntity.getVersionCode(), searchableEntity.getVersionName());
    }
}
