package ohos.utils.fastjson.serializer;

import ohos.telephony.TelephoneNumberUtils;

public abstract class BeforeFilter implements SerializeFilter {
    private static final Character COMMA = Character.valueOf(TelephoneNumberUtils.PAUSE);
    private static final ThreadLocal<Character> seperatorLocal = new ThreadLocal<>();
    private static final ThreadLocal<JSONSerializer> serializerLocal = new ThreadLocal<>();

    public abstract void writeBefore(Object obj);

    /* access modifiers changed from: package-private */
    public final char writeBefore(JSONSerializer jSONSerializer, Object obj, char c) {
        serializerLocal.set(jSONSerializer);
        seperatorLocal.set(Character.valueOf(c));
        writeBefore(obj);
        serializerLocal.set(null);
        return seperatorLocal.get().charValue();
    }

    /* access modifiers changed from: protected */
    public final void writeKeyValue(String str, Object obj) {
        char charValue = seperatorLocal.get().charValue();
        serializerLocal.get().writeKeyValue(charValue, str, obj);
        if (charValue != ',') {
            seperatorLocal.set(COMMA);
        }
    }
}
