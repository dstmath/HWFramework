package android.util;

import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import com.android.internal.telephony.RILConstants;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;
import libcore.internal.StringPool;

public final class JsonReader implements Closeable {
    private static final /* synthetic */ int[] -android-util-JsonScopeSwitchesValues = null;
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private final char[] buffer;
    private int bufferStartColumn;
    private int bufferStartLine;
    private final Reader in;
    private boolean lenient;
    private int limit;
    private String name;
    private int pos;
    private boolean skipping;
    private final List<JsonScope> stack;
    private final StringPool stringPool;
    private JsonToken token;
    private String value;
    private int valueLength;
    private int valuePos;

    private static /* synthetic */ int[] -getandroid-util-JsonScopeSwitchesValues() {
        if (-android-util-JsonScopeSwitchesValues != null) {
            return -android-util-JsonScopeSwitchesValues;
        }
        int[] iArr = new int[JsonScope.values().length];
        try {
            iArr[JsonScope.CLOSED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[JsonScope.DANGLING_NAME.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[JsonScope.EMPTY_ARRAY.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[JsonScope.EMPTY_DOCUMENT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[JsonScope.EMPTY_OBJECT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[JsonScope.NONEMPTY_ARRAY.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[JsonScope.NONEMPTY_DOCUMENT.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[JsonScope.NONEMPTY_OBJECT.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        -android-util-JsonScopeSwitchesValues = iArr;
        return iArr;
    }

    public JsonReader(Reader in) {
        this.stringPool = new StringPool();
        this.lenient = false;
        this.buffer = new char[GL10.GL_STENCIL_BUFFER_BIT];
        this.pos = 0;
        this.limit = 0;
        this.bufferStartLine = 1;
        this.bufferStartColumn = 1;
        this.stack = new ArrayList();
        push(JsonScope.EMPTY_DOCUMENT);
        this.skipping = false;
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        this.in = in;
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public void beginArray() throws IOException {
        expect(JsonToken.BEGIN_ARRAY);
    }

    public void endArray() throws IOException {
        expect(JsonToken.END_ARRAY);
    }

    public void beginObject() throws IOException {
        expect(JsonToken.BEGIN_OBJECT);
    }

    public void endObject() throws IOException {
        expect(JsonToken.END_OBJECT);
    }

    private void expect(JsonToken expected) throws IOException {
        peek();
        if (this.token != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + peek());
        }
        advance();
    }

    public boolean hasNext() throws IOException {
        peek();
        if (this.token == JsonToken.END_OBJECT || this.token == JsonToken.END_ARRAY) {
            return false;
        }
        return true;
    }

    public JsonToken peek() throws IOException {
        if (this.token != null) {
            return this.token;
        }
        switch (-getandroid-util-JsonScopeSwitchesValues()[peekStack().ordinal()]) {
            case HwCfgFilePolicy.EMUI /*1*/:
                throw new IllegalStateException("JsonReader is closed");
            case HwCfgFilePolicy.PC /*2*/:
                return objectValue();
            case HwCfgFilePolicy.BASE /*3*/:
                return nextInArray(true);
            case HwCfgFilePolicy.CUST /*4*/:
                replaceTop(JsonScope.NONEMPTY_DOCUMENT);
                JsonToken firstToken = nextValue();
                if (this.lenient || this.token == JsonToken.BEGIN_ARRAY || this.token == JsonToken.BEGIN_OBJECT) {
                    return firstToken;
                }
                throw new IOException("Expected JSON document to start with '[' or '{' but was " + this.token);
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                return nextInObject(true);
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                return nextInArray(false);
            case HwCfgFilePolicy.CLOUD_APN /*7*/:
                try {
                    JsonToken token = nextValue();
                    if (this.lenient) {
                        return token;
                    }
                    throw syntaxError("Expected EOF");
                } catch (EOFException e) {
                    JsonToken jsonToken = JsonToken.END_DOCUMENT;
                    this.token = jsonToken;
                    return jsonToken;
                }
            case PGSdk.TYPE_VIDEO /*8*/:
                return nextInObject(false);
            default:
                throw new AssertionError();
        }
    }

    private JsonToken advance() throws IOException {
        peek();
        JsonToken result = this.token;
        this.token = null;
        this.value = null;
        this.name = null;
        return result;
    }

    public String nextName() throws IOException {
        peek();
        if (this.token != JsonToken.NAME) {
            throw new IllegalStateException("Expected a name but was " + peek());
        }
        String result = this.name;
        advance();
        return result;
    }

    public String nextString() throws IOException {
        peek();
        if (this.token == JsonToken.STRING || this.token == JsonToken.NUMBER) {
            String result = this.value;
            advance();
            return result;
        }
        throw new IllegalStateException("Expected a string but was " + peek());
    }

    public boolean nextBoolean() throws IOException {
        peek();
        if (this.token != JsonToken.BOOLEAN) {
            throw new IllegalStateException("Expected a boolean but was " + this.token);
        }
        boolean result = this.value == TRUE;
        advance();
        return result;
    }

    public void nextNull() throws IOException {
        peek();
        if (this.token != JsonToken.NULL) {
            throw new IllegalStateException("Expected null but was " + this.token);
        }
        advance();
    }

    public double nextDouble() throws IOException {
        peek();
        if (this.token == JsonToken.STRING || this.token == JsonToken.NUMBER) {
            double result = Double.parseDouble(this.value);
            advance();
            return result;
        }
        throw new IllegalStateException("Expected a double but was " + this.token);
    }

    public long nextLong() throws IOException {
        peek();
        if (this.token == JsonToken.STRING || this.token == JsonToken.NUMBER) {
            long result;
            try {
                result = Long.parseLong(this.value);
            } catch (NumberFormatException e) {
                double asDouble = Double.parseDouble(this.value);
                result = (long) asDouble;
                if (((double) result) != asDouble) {
                    throw new NumberFormatException(this.value);
                }
            }
            advance();
            return result;
        }
        throw new IllegalStateException("Expected a long but was " + this.token);
    }

    public int nextInt() throws IOException {
        peek();
        if (this.token == JsonToken.STRING || this.token == JsonToken.NUMBER) {
            int result;
            try {
                result = Integer.parseInt(this.value);
            } catch (NumberFormatException e) {
                double asDouble = Double.parseDouble(this.value);
                result = (int) asDouble;
                if (((double) result) != asDouble) {
                    throw new NumberFormatException(this.value);
                }
            }
            advance();
            return result;
        }
        throw new IllegalStateException("Expected an int but was " + this.token);
    }

    public void close() throws IOException {
        this.value = null;
        this.token = null;
        this.stack.clear();
        this.stack.add(JsonScope.CLOSED);
        this.in.close();
    }

    public void skipValue() throws IOException {
        this.skipping = true;
        if (!hasNext() || peek() == JsonToken.END_DOCUMENT) {
            throw new IllegalStateException("No element left to skip");
        }
        int count = 0;
        while (true) {
            try {
                JsonToken token = advance();
                if (token == JsonToken.BEGIN_ARRAY || token == JsonToken.BEGIN_OBJECT) {
                    count++;
                    continue;
                } else if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
                    count--;
                    continue;
                }
                if (count == 0) {
                    break;
                }
            } finally {
                this.skipping = false;
            }
        }
    }

    private JsonScope peekStack() {
        return (JsonScope) this.stack.get(this.stack.size() - 1);
    }

    private JsonScope pop() {
        return (JsonScope) this.stack.remove(this.stack.size() - 1);
    }

    private void push(JsonScope newTop) {
        this.stack.add(newTop);
    }

    private void replaceTop(JsonScope newTop) {
        this.stack.set(this.stack.size() - 1, newTop);
    }

    private JsonToken nextInArray(boolean firstElement) throws IOException {
        JsonToken jsonToken;
        if (firstElement) {
            replaceTop(JsonScope.NONEMPTY_ARRAY);
        } else {
            switch (nextNonWhitespace()) {
                case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
                    break;
                case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
                    checkLenient();
                    break;
                case RILConstants.RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG /*93*/:
                    pop();
                    jsonToken = JsonToken.END_ARRAY;
                    this.token = jsonToken;
                    return jsonToken;
                default:
                    throw syntaxError("Unterminated array");
            }
        }
        switch (nextNonWhitespace()) {
            case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
            case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
                break;
            case RILConstants.RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG /*93*/:
                if (firstElement) {
                    pop();
                    jsonToken = JsonToken.END_ARRAY;
                    this.token = jsonToken;
                    return jsonToken;
                }
                break;
            default:
                this.pos--;
                return nextValue();
        }
        checkLenient();
        this.pos--;
        this.value = "null";
        jsonToken = JsonToken.NULL;
        this.token = jsonToken;
        return jsonToken;
    }

    private JsonToken nextInObject(boolean firstElement) throws IOException {
        JsonToken jsonToken;
        if (firstElement) {
            switch (nextNonWhitespace()) {
                case LogPower.TOUCH_DOWN /*125*/:
                    pop();
                    jsonToken = JsonToken.END_OBJECT;
                    this.token = jsonToken;
                    return jsonToken;
                default:
                    this.pos--;
                    break;
            }
        }
        switch (nextNonWhitespace()) {
            case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
            case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
                break;
            case LogPower.TOUCH_DOWN /*125*/:
                pop();
                jsonToken = JsonToken.END_OBJECT;
                this.token = jsonToken;
                return jsonToken;
            default:
                throw syntaxError("Unterminated object");
        }
        int quote = nextNonWhitespace();
        switch (quote) {
            case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                break;
            case PerfHub.PERF_TAG_MAX /*39*/:
                checkLenient();
                break;
            default:
                checkLenient();
                this.pos--;
                this.name = nextLiteral(false);
                if (this.name.isEmpty()) {
                    throw syntaxError("Expected name");
                }
                break;
        }
        this.name = nextString((char) quote);
        replaceTop(JsonScope.DANGLING_NAME);
        jsonToken = JsonToken.NAME;
        this.token = jsonToken;
        return jsonToken;
    }

    private JsonToken objectValue() throws IOException {
        switch (nextNonWhitespace()) {
            case RILConstants.RIL_REQUEST_RESET_RADIO /*58*/:
                break;
            case StatisticalConstant.TYPE_WIFI_END /*61*/:
                checkLenient();
                if ((this.pos < this.limit || fillBuffer(1)) && this.buffer[this.pos] == '>') {
                    this.pos++;
                    break;
                }
            default:
                throw syntaxError("Expected ':'");
        }
        replaceTop(JsonScope.NONEMPTY_OBJECT);
        return nextValue();
    }

    private JsonToken nextValue() throws IOException {
        JsonToken jsonToken;
        int c = nextNonWhitespace();
        switch (c) {
            case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                break;
            case PerfHub.PERF_TAG_MAX /*39*/:
                checkLenient();
                break;
            case StatisticalConstant.TYPE_SHARED_TARGET /*91*/:
                push(JsonScope.EMPTY_ARRAY);
                jsonToken = JsonToken.BEGIN_ARRAY;
                this.token = jsonToken;
                return jsonToken;
            case LogPower.NOTIFICATION_CANCEL /*123*/:
                push(JsonScope.EMPTY_OBJECT);
                jsonToken = JsonToken.BEGIN_OBJECT;
                this.token = jsonToken;
                return jsonToken;
            default:
                this.pos--;
                return readLiteral();
        }
        this.value = nextString((char) c);
        jsonToken = JsonToken.STRING;
        this.token = jsonToken;
        return jsonToken;
    }

    private boolean fillBuffer(int minimum) throws IOException {
        for (int i = 0; i < this.pos; i++) {
            if (this.buffer[i] == '\n') {
                this.bufferStartLine++;
                this.bufferStartColumn = 1;
            } else {
                this.bufferStartColumn++;
            }
        }
        if (this.limit != this.pos) {
            this.limit -= this.pos;
            System.arraycopy(this.buffer, this.pos, this.buffer, 0, this.limit);
        } else {
            this.limit = 0;
        }
        this.pos = 0;
        do {
            int total = this.in.read(this.buffer, this.limit, this.buffer.length - this.limit);
            if (total == -1) {
                return false;
            }
            this.limit += total;
            if (this.bufferStartLine == 1 && this.bufferStartColumn == 1 && this.limit > 0 && this.buffer[0] == '\ufeff') {
                this.pos++;
                this.bufferStartColumn--;
            }
        } while (this.limit < minimum);
        return true;
    }

    private int getLineNumber() {
        int result = this.bufferStartLine;
        for (int i = 0; i < this.pos; i++) {
            if (this.buffer[i] == '\n') {
                result++;
            }
        }
        return result;
    }

    private int getColumnNumber() {
        int result = this.bufferStartColumn;
        for (int i = 0; i < this.pos; i++) {
            if (this.buffer[i] == '\n') {
                result = 1;
            } else {
                result++;
            }
        }
        return result;
    }

    private int nextNonWhitespace() throws IOException {
        while (true) {
            if (this.pos < this.limit || fillBuffer(1)) {
                char[] cArr = this.buffer;
                int i = this.pos;
                this.pos = i + 1;
                int c = cArr[i];
                switch (c) {
                    case PGSdk.TYPE_SCRLOCK /*9*/:
                    case PGSdk.TYPE_CLOCK /*10*/:
                    case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
                    case IndexSearchConstants.INDEX_BUILD_FLAG_INTERNAL_FILE /*32*/:
                        break;
                    case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                        checkLenient();
                        skipToEndOfLine();
                        break;
                    case RILConstants.RIL_REQUEST_SET_NETWORK_SELECTION_MANUAL /*47*/:
                        if (this.pos == this.limit && !fillBuffer(1)) {
                            return c;
                        }
                        checkLenient();
                        switch (this.buffer[this.pos]) {
                            case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
                                this.pos++;
                                if (skipTo("*/")) {
                                    this.pos += 2;
                                    break;
                                }
                                throw syntaxError("Unterminated comment");
                            case RILConstants.RIL_REQUEST_SET_NETWORK_SELECTION_MANUAL /*47*/:
                                this.pos++;
                                skipToEndOfLine();
                                break;
                            default:
                                return c;
                        }
                    default:
                        return c;
                }
            }
            throw new EOFException("End of input");
        }
    }

    private void checkLenient() throws IOException {
        if (!this.lenient) {
            throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON");
        }
    }

    private void skipToEndOfLine() throws IOException {
        char c;
        do {
            if (this.pos < this.limit || fillBuffer(1)) {
                char[] cArr = this.buffer;
                int i = this.pos;
                this.pos = i + 1;
                c = cArr[i];
                if (c == '\r') {
                    return;
                }
            } else {
                return;
            }
        } while (c != '\n');
    }

    private boolean skipTo(String toFind) throws IOException {
        while (true) {
            if (this.pos + toFind.length() > this.limit && !fillBuffer(toFind.length())) {
                return false;
            }
            int c = 0;
            while (c < toFind.length()) {
                if (this.buffer[this.pos + c] != toFind.charAt(c)) {
                    this.pos++;
                } else {
                    c++;
                }
            }
            return true;
        }
    }

    private String nextString(char quote) throws IOException {
        StringBuilder stringBuilder = null;
        do {
            int start = this.pos;
            while (this.pos < this.limit) {
                char[] cArr = this.buffer;
                int i = this.pos;
                this.pos = i + 1;
                char c = cArr[i];
                if (c == quote) {
                    if (this.skipping) {
                        return "skipped!";
                    }
                    if (stringBuilder == null) {
                        return this.stringPool.get(this.buffer, start, (this.pos - start) - 1);
                    }
                    stringBuilder.append(this.buffer, start, (this.pos - start) - 1);
                    return stringBuilder.toString();
                } else if (c == '\\') {
                    if (stringBuilder == null) {
                        stringBuilder = new StringBuilder();
                    }
                    stringBuilder.append(this.buffer, start, (this.pos - start) - 1);
                    stringBuilder.append(readEscapeCharacter());
                    start = this.pos;
                }
            }
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder();
            }
            stringBuilder.append(this.buffer, start, this.pos - start);
        } while (fillBuffer(1));
        throw syntaxError("Unterminated string");
    }

    private String nextLiteral(boolean assignOffsetsOnly) throws IOException {
        StringBuilder builder = null;
        this.valuePos = -1;
        this.valueLength = 0;
        int i = 0;
        while (true) {
            String str;
            if (this.pos + i < this.limit) {
                switch (this.buffer[this.pos + i]) {
                    case PGSdk.TYPE_SCRLOCK /*9*/:
                    case PGSdk.TYPE_CLOCK /*10*/:
                    case PGSdk.TYPE_MUSIC /*12*/:
                    case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
                    case IndexSearchConstants.INDEX_BUILD_FLAG_INTERNAL_FILE /*32*/:
                    case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
                    case RILConstants.RIL_REQUEST_RESET_RADIO /*58*/:
                    case StatisticalConstant.TYPE_SHARED_TARGET /*91*/:
                    case RILConstants.RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG /*93*/:
                    case LogPower.NOTIFICATION_CANCEL /*123*/:
                    case LogPower.TOUCH_DOWN /*125*/:
                        break;
                    case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                    case RILConstants.RIL_REQUEST_SET_NETWORK_SELECTION_MANUAL /*47*/:
                    case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
                    case StatisticalConstant.TYPE_WIFI_END /*61*/:
                    case RILConstants.RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG /*92*/:
                        checkLenient();
                        break;
                    default:
                        i++;
                        continue;
                }
            } else if (i >= this.buffer.length) {
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(this.buffer, this.pos, i);
                this.valueLength += i;
                this.pos += i;
                i = 0;
                if (fillBuffer(1)) {
                }
            } else if (!fillBuffer(i + 1)) {
                this.buffer[this.limit] = '\u0000';
            }
            if (assignOffsetsOnly && builder == null) {
                this.valuePos = this.pos;
                str = null;
            } else if (this.skipping) {
                str = "skipped!";
            } else if (builder == null) {
                str = this.stringPool.get(this.buffer, this.pos, i);
            } else {
                builder.append(this.buffer, this.pos, i);
                str = builder.toString();
            }
            this.valueLength += i;
            this.pos += i;
            return str;
        }
    }

    public String toString() {
        return getClass().getSimpleName() + " near " + getSnippet();
    }

    private char readEscapeCharacter() throws IOException {
        if (this.pos != this.limit || fillBuffer(1)) {
            char[] cArr = this.buffer;
            int i = this.pos;
            this.pos = i + 1;
            char escaped = cArr[i];
            switch (escaped) {
                case RILConstants.RIL_REQUEST_DEVICE_IDENTITY /*98*/:
                    return '\b';
                case LogPower.WEBVIEW_RESUMED /*102*/:
                    return '\f';
                case LogPower.ALL_DOWNLOAD_FINISH /*110*/:
                    return '\n';
                case LogPower.APP_RUN_BG /*114*/:
                    return '\r';
                case LogPower.SCREEN_OFF /*116*/:
                    return '\t';
                case LogPower.KEYBOARD_SHOW /*117*/:
                    if (this.pos + 4 <= this.limit || fillBuffer(4)) {
                        String hex = this.stringPool.get(this.buffer, this.pos, 4);
                        this.pos += 4;
                        return (char) Integer.parseInt(hex, 16);
                    }
                    throw syntaxError("Unterminated escape sequence");
                default:
                    return escaped;
            }
        }
        throw syntaxError("Unterminated escape sequence");
    }

    private JsonToken readLiteral() throws IOException {
        this.value = nextLiteral(true);
        if (this.valueLength == 0) {
            throw syntaxError("Expected literal value");
        }
        this.token = decodeLiteral();
        if (this.token == JsonToken.STRING) {
            checkLenient();
        }
        return this.token;
    }

    private JsonToken decodeLiteral() throws IOException {
        if (this.valuePos == -1) {
            return JsonToken.STRING;
        }
        if (this.valueLength == 4 && (('n' == this.buffer[this.valuePos] || PhoneNumberUtils.WILD == this.buffer[this.valuePos]) && (('u' == this.buffer[this.valuePos + 1] || 'U' == this.buffer[this.valuePos + 1]) && (('l' == this.buffer[this.valuePos + 2] || DateFormat.STANDALONE_MONTH == this.buffer[this.valuePos + 2]) && ('l' == this.buffer[this.valuePos + 3] || DateFormat.STANDALONE_MONTH == this.buffer[this.valuePos + 3]))))) {
            this.value = "null";
            return JsonToken.NULL;
        } else if (this.valueLength == 4 && (('t' == this.buffer[this.valuePos] || 'T' == this.buffer[this.valuePos]) && (('r' == this.buffer[this.valuePos + 1] || 'R' == this.buffer[this.valuePos + 1]) && (('u' == this.buffer[this.valuePos + 2] || 'U' == this.buffer[this.valuePos + 2]) && ('e' == this.buffer[this.valuePos + 3] || DateFormat.DAY == this.buffer[this.valuePos + 3]))))) {
            this.value = TRUE;
            return JsonToken.BOOLEAN;
        } else if (this.valueLength == 5 && (('f' == this.buffer[this.valuePos] || 'F' == this.buffer[this.valuePos]) && ((DateFormat.AM_PM == this.buffer[this.valuePos + 1] || DateFormat.CAPITAL_AM_PM == this.buffer[this.valuePos + 1]) && (('l' == this.buffer[this.valuePos + 2] || DateFormat.STANDALONE_MONTH == this.buffer[this.valuePos + 2]) && ((DateFormat.SECONDS == this.buffer[this.valuePos + 3] || 'S' == this.buffer[this.valuePos + 3]) && ('e' == this.buffer[this.valuePos + 4] || DateFormat.DAY == this.buffer[this.valuePos + 4])))))) {
            this.value = FALSE;
            return JsonToken.BOOLEAN;
        } else {
            this.value = this.stringPool.get(this.buffer, this.valuePos, this.valueLength);
            return decodeNumber(this.buffer, this.valuePos, this.valueLength);
        }
    }

    private JsonToken decodeNumber(char[] chars, int offset, int length) {
        int i = offset;
        int c = chars[offset];
        if (c == 45) {
            i = offset + 1;
            c = chars[i];
        }
        if (c == 48) {
            i++;
            c = chars[i];
        } else if (c < 49 || c > 57) {
            return JsonToken.STRING;
        } else {
            i++;
            c = chars[i];
            while (c >= 48 && c <= 57) {
                i++;
                c = chars[i];
            }
        }
        if (c == 46) {
            i++;
            c = chars[i];
            while (c >= 48 && c <= 57) {
                i++;
                c = chars[i];
            }
        }
        if (c == LogPower.ACTIVITY_PAUSED || c == 69) {
            i++;
            c = chars[i];
            if (c == 43 || c == 45) {
                i++;
                c = chars[i];
            }
            if (c < 48 || c > 57) {
                return JsonToken.STRING;
            }
            i++;
            c = chars[i];
            while (c >= 48 && c <= 57) {
                i++;
                c = chars[i];
            }
        }
        if (i == offset + length) {
            return JsonToken.NUMBER;
        }
        return JsonToken.STRING;
    }

    private IOException syntaxError(String message) throws IOException {
        throw new MalformedJsonException(message + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    private CharSequence getSnippet() {
        StringBuilder snippet = new StringBuilder();
        int beforePos = Math.min(this.pos, 20);
        snippet.append(this.buffer, this.pos - beforePos, beforePos);
        snippet.append(this.buffer, this.pos, Math.min(this.limit - this.pos, 20));
        return snippet;
    }
}
