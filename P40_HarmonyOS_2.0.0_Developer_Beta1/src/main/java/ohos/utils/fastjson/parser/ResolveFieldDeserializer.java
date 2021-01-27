package ohos.utils.fastjson.parser;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.parser.deserializer.FieldDeserializer;
import ohos.utils.fastjson.util.TypeUtils;

/* access modifiers changed from: package-private */
public final class ResolveFieldDeserializer extends FieldDeserializer {
    private final Collection collection;
    private final int index;
    private final Object key;
    private final List list;
    private final Map map;
    private final DefaultJSONParser parser;

    @Override // ohos.utils.fastjson.parser.deserializer.FieldDeserializer
    public void parseField(DefaultJSONParser defaultJSONParser, Object obj, Type type, Map<String, Object> map2) {
    }

    public ResolveFieldDeserializer(DefaultJSONParser defaultJSONParser, List list2, int i) {
        super(null, null, 0);
        this.parser = defaultJSONParser;
        this.index = i;
        this.list = list2;
        this.key = null;
        this.map = null;
        this.collection = null;
    }

    public ResolveFieldDeserializer(Map map2, Object obj) {
        super(null, null, 0);
        this.parser = null;
        this.index = -1;
        this.list = null;
        this.key = obj;
        this.map = map2;
        this.collection = null;
    }

    public ResolveFieldDeserializer(Collection collection2) {
        super(null, null, 0);
        this.parser = null;
        this.index = -1;
        this.list = null;
        this.key = null;
        this.map = null;
        this.collection = collection2;
    }

    @Override // ohos.utils.fastjson.parser.deserializer.FieldDeserializer
    public void setValue(Object obj, Object obj2) {
        JSONArray jSONArray;
        Object relatedArray;
        Map map2 = this.map;
        if (map2 != null) {
            map2.put(this.key, obj2);
            return;
        }
        Collection collection2 = this.collection;
        if (collection2 != null) {
            collection2.add(obj2);
            return;
        }
        this.list.set(this.index, obj2);
        List list2 = this.list;
        if ((list2 instanceof JSONArray) && (relatedArray = (jSONArray = (JSONArray) list2).getRelatedArray()) != null && Array.getLength(relatedArray) > this.index) {
            if (jSONArray.getComponentType() != null) {
                obj2 = TypeUtils.cast(obj2, jSONArray.getComponentType(), this.parser.config);
            }
            Array.set(relatedArray, this.index, obj2);
        }
    }
}
