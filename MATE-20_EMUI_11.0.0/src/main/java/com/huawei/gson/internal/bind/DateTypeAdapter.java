package com.huawei.gson.internal.bind;

import com.huawei.gson.Gson;
import com.huawei.gson.JsonSyntaxException;
import com.huawei.gson.TypeAdapter;
import com.huawei.gson.TypeAdapterFactory;
import com.huawei.gson.internal.JavaVersion;
import com.huawei.gson.internal.PreJava9DateFormatProvider;
import com.huawei.gson.internal.bind.util.ISO8601Utils;
import com.huawei.gson.reflect.TypeToken;
import com.huawei.gson.stream.JsonReader;
import com.huawei.gson.stream.JsonToken;
import com.huawei.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class DateTypeAdapter extends TypeAdapter<Date> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        /* class com.huawei.gson.internal.bind.DateTypeAdapter.AnonymousClass1 */

        @Override // com.huawei.gson.TypeAdapterFactory
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            if (typeToken.getRawType() == Date.class) {
                return new DateTypeAdapter();
            }
            return null;
        }
    };
    private final List<DateFormat> dateFormats = new ArrayList();

    public DateTypeAdapter() {
        this.dateFormats.add(DateFormat.getDateTimeInstance(2, 2, Locale.US));
        if (!Locale.getDefault().equals(Locale.US)) {
            this.dateFormats.add(DateFormat.getDateTimeInstance(2, 2));
        }
        if (JavaVersion.isJava9OrLater()) {
            this.dateFormats.add(PreJava9DateFormatProvider.getUSDateTimeFormat(2, 2));
        }
    }

    @Override // com.huawei.gson.TypeAdapter
    public Date read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.NULL) {
            return deserializeToDate(in.nextString());
        }
        in.nextNull();
        return null;
    }

    private synchronized Date deserializeToDate(String json) {
        Iterator<DateFormat> it = this.dateFormats.iterator();
        while (it.hasNext()) {
            try {
                return it.next().parse(json);
            } catch (ParseException e) {
            }
        }
        try {
            return ISO8601Utils.parse(json, new ParsePosition(0));
        } catch (ParseException e2) {
            throw new JsonSyntaxException(json, e2);
        }
    }

    public synchronized void write(JsonWriter out, Date value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(this.dateFormats.get(0).format(value));
        }
    }
}
