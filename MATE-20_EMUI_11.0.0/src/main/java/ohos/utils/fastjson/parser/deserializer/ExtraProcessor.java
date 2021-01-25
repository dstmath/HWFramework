package ohos.utils.fastjson.parser.deserializer;

public interface ExtraProcessor extends ParseProcess {
    void processExtra(Object obj, String str, Object obj2);
}
