package ohos.utils.fastjson.parser;

import java.lang.reflect.Type;
import java.util.Arrays;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;

public class EnumDeserializer implements ObjectDeserializer {
    private final Class<?> enumClass;
    protected long[] enumNameHashCodes;
    protected final Enum[] enums;
    protected final Enum[] ordinalEnums;

    public EnumDeserializer(Class<?> cls) {
        this.enumClass = cls;
        this.ordinalEnums = (Enum[]) cls.getEnumConstants();
        Enum[] enumArr = this.ordinalEnums;
        long[] jArr = new long[enumArr.length];
        this.enumNameHashCodes = new long[enumArr.length];
        int i = 0;
        while (true) {
            Enum[] enumArr2 = this.ordinalEnums;
            if (i >= enumArr2.length) {
                break;
            }
            String name = enumArr2[i].name();
            long j = -3750763034362895579L;
            for (int i2 = 0; i2 < name.length(); i2++) {
                j = (j ^ ((long) name.charAt(i2))) * 1099511628211L;
            }
            jArr[i] = j;
            this.enumNameHashCodes[i] = j;
            i++;
        }
        Arrays.sort(this.enumNameHashCodes);
        this.enums = new Enum[this.ordinalEnums.length];
        for (int i3 = 0; i3 < this.enumNameHashCodes.length; i3++) {
            int i4 = 0;
            while (true) {
                if (i4 >= jArr.length) {
                    break;
                } else if (this.enumNameHashCodes[i3] == jArr[i4]) {
                    this.enums[i3] = this.ordinalEnums[i4];
                    break;
                } else {
                    i4++;
                }
            }
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        try {
            JSONLexer jSONLexer = defaultJSONParser.lexer;
            int i = jSONLexer.token;
            if (i == 2) {
                int intValue = jSONLexer.intValue();
                jSONLexer.nextToken(16);
                if (intValue >= 0 && intValue <= this.ordinalEnums.length) {
                    return (T) this.ordinalEnums[intValue];
                }
                throw new JSONException("parse enum " + this.enumClass.getName() + " error, value : " + intValue);
            } else if (i == 4) {
                String stringVal = jSONLexer.stringVal();
                jSONLexer.nextToken(16);
                if (stringVal.length() == 0) {
                    return null;
                }
                long j = -3750763034362895579L;
                for (int i2 = 0; i2 < stringVal.length(); i2++) {
                    j = (j ^ ((long) stringVal.charAt(i2))) * 1099511628211L;
                }
                int binarySearch = Arrays.binarySearch(this.enumNameHashCodes, j);
                if (binarySearch < 0) {
                    return null;
                }
                return (T) this.enums[binarySearch];
            } else if (i == 8) {
                jSONLexer.nextToken(16);
                return null;
            } else {
                Object parse = defaultJSONParser.parse();
                throw new JSONException("parse enum " + this.enumClass.getName() + " error, value : " + parse);
            }
        } catch (JSONException e) {
            throw e;
        } catch (Exception e2) {
            throw new JSONException(e2.getMessage(), e2);
        }
    }
}
