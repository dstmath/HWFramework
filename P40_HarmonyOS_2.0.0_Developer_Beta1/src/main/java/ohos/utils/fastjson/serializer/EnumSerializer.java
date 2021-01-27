package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/* access modifiers changed from: package-private */
public class EnumSerializer implements ObjectSerializer {
    EnumSerializer() {
    }

    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        SerializeWriter serializeWriter = jSONSerializer.out;
        if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
            String str = ((Enum) obj).toString();
            if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                serializeWriter.writeStringWithSingleQuote(str);
            } else {
                serializeWriter.writeStringWithDoubleQuote(str, 0, false);
            }
        } else {
            serializeWriter.writeInt(((Enum) obj).ordinal());
        }
    }
}
