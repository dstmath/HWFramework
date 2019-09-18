package com.google.gson;

import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class DefaultDateTypeAdapter extends TypeAdapter<Date> {
    private static final String SIMPLE_NAME = "DefaultDateTypeAdapter";
    private final Class<? extends Date> dateType;
    private final DateFormat enUsFormat;
    private final DateFormat localFormat;

    DefaultDateTypeAdapter(Class<? extends Date> dateType2) {
        this(dateType2, DateFormat.getDateTimeInstance(2, 2, Locale.US), DateFormat.getDateTimeInstance(2, 2));
    }

    DefaultDateTypeAdapter(Class<? extends Date> dateType2, String datePattern) {
        this(dateType2, (DateFormat) new SimpleDateFormat(datePattern, Locale.US), (DateFormat) new SimpleDateFormat(datePattern));
    }

    DefaultDateTypeAdapter(Class<? extends Date> dateType2, int style) {
        this(dateType2, DateFormat.getDateInstance(style, Locale.US), DateFormat.getDateInstance(style));
    }

    public DefaultDateTypeAdapter(int dateStyle, int timeStyle) {
        this((Class<? extends Date>) Date.class, DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US), DateFormat.getDateTimeInstance(dateStyle, timeStyle));
    }

    public DefaultDateTypeAdapter(Class<? extends Date> dateType2, int dateStyle, int timeStyle) {
        this(dateType2, DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US), DateFormat.getDateTimeInstance(dateStyle, timeStyle));
    }

    DefaultDateTypeAdapter(Class<? extends Date> dateType2, DateFormat enUsFormat2, DateFormat localFormat2) {
        if (dateType2 == Date.class || dateType2 == java.sql.Date.class || dateType2 == Timestamp.class) {
            this.dateType = dateType2;
            this.enUsFormat = enUsFormat2;
            this.localFormat = localFormat2;
            return;
        }
        throw new IllegalArgumentException("Date type must be one of " + Date.class + ", " + Timestamp.class + ", or " + java.sql.Date.class + " but was " + dateType2);
    }

    public void write(JsonWriter out, Date value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        synchronized (this.localFormat) {
            out.value(this.enUsFormat.format(value));
        }
    }

    public Date read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        Date date = deserializeToDate(in.nextString());
        if (this.dateType == Date.class) {
            return date;
        }
        if (this.dateType == Timestamp.class) {
            return new Timestamp(date.getTime());
        }
        if (this.dateType == java.sql.Date.class) {
            return new java.sql.Date(date.getTime());
        }
        throw new AssertionError();
    }

    private Date deserializeToDate(String s) {
        Date parse;
        synchronized (this.localFormat) {
            try {
                parse = this.localFormat.parse(s);
            } catch (ParseException e) {
                throw new JsonSyntaxException(s, e);
            } catch (ParseException e2) {
                try {
                    parse = this.enUsFormat.parse(s);
                } catch (ParseException e3) {
                    parse = ISO8601Utils.parse(s, new ParsePosition(0));
                }
            }
        }
        return parse;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SIMPLE_NAME);
        sb.append('(').append(this.localFormat.getClass().getSimpleName()).append(')');
        return sb.toString();
    }
}
