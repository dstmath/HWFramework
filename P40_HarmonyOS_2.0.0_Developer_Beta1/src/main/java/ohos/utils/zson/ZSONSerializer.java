package ohos.utils.zson;

import java.util.List;
import java.util.Map;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.serializer.JSONSerializer;
import ohos.utils.fastjson.serializer.NameFilter;
import ohos.utils.fastjson.serializer.SerializeConfig;
import ohos.utils.fastjson.serializer.SerializeWriter;

public class ZSONSerializer extends JSONSerializer {
    public ZSONSerializer(SerializeWriter serializeWriter, SerializeConfig serializeConfig) {
        super(serializeWriter, serializeConfig);
    }

    @Override // ohos.utils.fastjson.serializer.JSONSerializer
    public Object processKey(Object obj, Object obj2, Object obj3) {
        List<NameFilter> list = this.nameFilters;
        if (list != null && !(obj instanceof Map)) {
            if (obj2 != null && !(obj2 instanceof String)) {
                obj2 = JSON.toJSONString(obj2);
            }
            for (NameFilter nameFilter : list) {
                obj2 = nameFilter.process(obj, (String) obj2, obj3);
            }
        }
        return obj2;
    }
}
