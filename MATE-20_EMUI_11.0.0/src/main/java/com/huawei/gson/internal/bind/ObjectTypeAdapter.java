package com.huawei.gson.internal.bind;

import com.huawei.gson.Gson;
import com.huawei.gson.TypeAdapter;
import com.huawei.gson.TypeAdapterFactory;
import com.huawei.gson.internal.LinkedTreeMap;
import com.huawei.gson.reflect.TypeToken;
import com.huawei.gson.stream.JsonReader;
import com.huawei.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ObjectTypeAdapter extends TypeAdapter<Object> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        /* class com.huawei.gson.internal.bind.ObjectTypeAdapter.AnonymousClass1 */

        @Override // com.huawei.gson.TypeAdapterFactory
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() == Object.class) {
                return new ObjectTypeAdapter(gson);
            }
            return null;
        }
    };
    private final Gson gson;

    ObjectTypeAdapter(Gson gson2) {
        this.gson = gson2;
    }

    @Override // com.huawei.gson.TypeAdapter
    public Object read(JsonReader in) throws IOException {
        switch (in.peek()) {
            case BEGIN_ARRAY:
                List<Object> list = new ArrayList<>();
                in.beginArray();
                while (in.hasNext()) {
                    list.add(read(in));
                }
                in.endArray();
                return list;
            case BEGIN_OBJECT:
                Map<String, Object> map = new LinkedTreeMap<>();
                in.beginObject();
                while (in.hasNext()) {
                    map.put(in.nextName(), read(in));
                }
                in.endObject();
                return map;
            case STRING:
                return in.nextString();
            case NUMBER:
                return Double.valueOf(in.nextDouble());
            case BOOLEAN:
                return Boolean.valueOf(in.nextBoolean());
            case NULL:
                in.nextNull();
                return null;
            default:
                throw new IllegalStateException();
        }
    }

    @Override // com.huawei.gson.TypeAdapter
    public void write(JsonWriter out, Object value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        TypeAdapter<Object> typeAdapter = this.gson.getAdapter(value.getClass());
        if (typeAdapter instanceof ObjectTypeAdapter) {
            out.beginObject();
            out.endObject();
            return;
        }
        typeAdapter.write(out, value);
    }
}
