package ohos.utils.fastjson.serializer;

public interface ValueFilter extends SerializeFilter {
    Object process(Object obj, String str, Object obj2);
}
