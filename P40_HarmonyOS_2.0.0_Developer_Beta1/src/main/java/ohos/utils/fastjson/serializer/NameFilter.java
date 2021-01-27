package ohos.utils.fastjson.serializer;

public interface NameFilter extends SerializeFilter {
    String process(Object obj, String str, Object obj2);
}
