package java.util;

public final class StringJoiner {
    private final String delimiter;
    private String emptyValue;
    private final String prefix;
    private final String suffix;
    private StringBuilder value;

    public StringJoiner(CharSequence delimiter) {
        this(delimiter, "", "");
    }

    public StringJoiner(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        Objects.requireNonNull((Object) prefix, "The prefix must not be null");
        Objects.requireNonNull((Object) delimiter, "The delimiter must not be null");
        Objects.requireNonNull((Object) suffix, "The suffix must not be null");
        this.prefix = prefix.toString();
        this.delimiter = delimiter.toString();
        this.suffix = suffix.toString();
        this.emptyValue = this.prefix + this.suffix;
    }

    public StringJoiner setEmptyValue(CharSequence emptyValue) {
        this.emptyValue = ((CharSequence) Objects.requireNonNull((Object) emptyValue, "The empty value must not be null")).toString();
        return this;
    }

    public String toString() {
        if (this.value == null) {
            return this.emptyValue;
        }
        if (this.suffix.equals("")) {
            return this.value.toString();
        }
        int initialLength = this.value.length();
        String result = this.value.append(this.suffix).toString();
        this.value.setLength(initialLength);
        return result;
    }

    public StringJoiner add(CharSequence newElement) {
        prepareBuilder().append(newElement);
        return this;
    }

    public StringJoiner merge(StringJoiner other) {
        Objects.requireNonNull(other);
        if (other.value != null) {
            prepareBuilder().append(other.value, other.prefix.length(), other.value.length());
        }
        return this;
    }

    private StringBuilder prepareBuilder() {
        if (this.value != null) {
            this.value.append(this.delimiter);
        } else {
            this.value = new StringBuilder().append(this.prefix);
        }
        return this.value;
    }

    public int length() {
        if (this.value != null) {
            return this.value.length() + this.suffix.length();
        }
        return this.emptyValue.length();
    }
}
