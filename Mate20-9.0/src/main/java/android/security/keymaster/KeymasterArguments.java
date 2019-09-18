package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KeymasterArguments implements Parcelable {
    public static final Parcelable.Creator<KeymasterArguments> CREATOR = new Parcelable.Creator<KeymasterArguments>() {
        public KeymasterArguments createFromParcel(Parcel in) {
            return new KeymasterArguments(in);
        }

        public KeymasterArguments[] newArray(int size) {
            return new KeymasterArguments[size];
        }
    };
    public static final long UINT32_MAX_VALUE = 4294967295L;
    private static final long UINT32_RANGE = 4294967296L;
    public static final BigInteger UINT64_MAX_VALUE = UINT64_RANGE.subtract(BigInteger.ONE);
    private static final BigInteger UINT64_RANGE = BigInteger.ONE.shiftLeft(64);
    private List<KeymasterArgument> mArguments;

    public KeymasterArguments() {
        this.mArguments = new ArrayList();
    }

    private KeymasterArguments(Parcel in) {
        this.mArguments = in.createTypedArrayList(KeymasterArgument.CREATOR);
    }

    public void addEnum(int tag, int value) {
        int tagType = KeymasterDefs.getTagType(tag);
        if (tagType == 268435456 || tagType == 536870912) {
            addEnumTag(tag, value);
            return;
        }
        throw new IllegalArgumentException("Not an enum or repeating enum tag: " + tag);
    }

    public void addEnums(int tag, int... values) {
        if (KeymasterDefs.getTagType(tag) == 536870912) {
            for (int value : values) {
                addEnumTag(tag, value);
            }
            return;
        }
        throw new IllegalArgumentException("Not a repeating enum tag: " + tag);
    }

    public int getEnum(int tag, int defaultValue) {
        if (KeymasterDefs.getTagType(tag) == 268435456) {
            KeymasterArgument arg = getArgumentByTag(tag);
            if (arg == null) {
                return defaultValue;
            }
            return getEnumTagValue(arg);
        }
        throw new IllegalArgumentException("Not an enum tag: " + tag);
    }

    public List<Integer> getEnums(int tag) {
        if (KeymasterDefs.getTagType(tag) == 536870912) {
            List<Integer> values = new ArrayList<>();
            for (KeymasterArgument arg : this.mArguments) {
                if (arg.tag == tag) {
                    values.add(Integer.valueOf(getEnumTagValue(arg)));
                }
            }
            return values;
        }
        throw new IllegalArgumentException("Not a repeating enum tag: " + tag);
    }

    private void addEnumTag(int tag, int value) {
        this.mArguments.add(new KeymasterIntArgument(tag, value));
    }

    private int getEnumTagValue(KeymasterArgument arg) {
        return ((KeymasterIntArgument) arg).value;
    }

    public void addUnsignedInt(int tag, long value) {
        int tagType = KeymasterDefs.getTagType(tag);
        if (tagType != 805306368 && tagType != 1073741824) {
            throw new IllegalArgumentException("Not an int or repeating int tag: " + tag);
        } else if (value < 0 || value > 4294967295L) {
            throw new IllegalArgumentException("Int tag value out of range: " + value);
        } else {
            this.mArguments.add(new KeymasterIntArgument(tag, (int) value));
        }
    }

    public long getUnsignedInt(int tag, long defaultValue) {
        if (KeymasterDefs.getTagType(tag) == 805306368) {
            KeymasterArgument arg = getArgumentByTag(tag);
            if (arg == null) {
                return defaultValue;
            }
            return ((long) ((KeymasterIntArgument) arg).value) & 4294967295L;
        }
        throw new IllegalArgumentException("Not an int tag: " + tag);
    }

    public void addUnsignedLong(int tag, BigInteger value) {
        int tagType = KeymasterDefs.getTagType(tag);
        if (tagType == 1342177280 || tagType == -1610612736) {
            addLongTag(tag, value);
            return;
        }
        throw new IllegalArgumentException("Not a long or repeating long tag: " + tag);
    }

    public List<BigInteger> getUnsignedLongs(int tag) {
        if (KeymasterDefs.getTagType(tag) == -1610612736) {
            List<BigInteger> values = new ArrayList<>();
            for (KeymasterArgument arg : this.mArguments) {
                if (arg.tag == tag) {
                    values.add(getLongTagValue(arg));
                }
            }
            return values;
        }
        throw new IllegalArgumentException("Tag is not a repeating long: " + tag);
    }

    private void addLongTag(int tag, BigInteger value) {
        if (value.signum() == -1 || value.compareTo(UINT64_MAX_VALUE) > 0) {
            throw new IllegalArgumentException("Long tag value out of range: " + value);
        }
        this.mArguments.add(new KeymasterLongArgument(tag, value.longValue()));
    }

    private BigInteger getLongTagValue(KeymasterArgument arg) {
        return toUint64(((KeymasterLongArgument) arg).value);
    }

    public void addBoolean(int tag) {
        if (KeymasterDefs.getTagType(tag) == 1879048192) {
            this.mArguments.add(new KeymasterBooleanArgument(tag));
            return;
        }
        throw new IllegalArgumentException("Not a boolean tag: " + tag);
    }

    public boolean getBoolean(int tag) {
        if (KeymasterDefs.getTagType(tag) != 1879048192) {
            throw new IllegalArgumentException("Not a boolean tag: " + tag);
        } else if (getArgumentByTag(tag) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void addBytes(int tag, byte[] value) {
        if (KeymasterDefs.getTagType(tag) != -1879048192) {
            throw new IllegalArgumentException("Not a bytes tag: " + tag);
        } else if (value != null) {
            this.mArguments.add(new KeymasterBlobArgument(tag, value));
        } else {
            throw new NullPointerException("value == nulll");
        }
    }

    public byte[] getBytes(int tag, byte[] defaultValue) {
        if (KeymasterDefs.getTagType(tag) == -1879048192) {
            KeymasterArgument arg = getArgumentByTag(tag);
            if (arg == null) {
                return defaultValue;
            }
            return ((KeymasterBlobArgument) arg).blob;
        }
        throw new IllegalArgumentException("Not a bytes tag: " + tag);
    }

    public void addDate(int tag, Date value) {
        if (KeymasterDefs.getTagType(tag) != 1610612736) {
            throw new IllegalArgumentException("Not a date tag: " + tag);
        } else if (value == null) {
            throw new NullPointerException("value == nulll");
        } else if (value.getTime() >= 0) {
            this.mArguments.add(new KeymasterDateArgument(tag, value));
        } else {
            throw new IllegalArgumentException("Date tag value out of range: " + value);
        }
    }

    public void addDateIfNotNull(int tag, Date value) {
        if (KeymasterDefs.getTagType(tag) != 1610612736) {
            throw new IllegalArgumentException("Not a date tag: " + tag);
        } else if (value != null) {
            addDate(tag, value);
        }
    }

    public Date getDate(int tag, Date defaultValue) {
        if (KeymasterDefs.getTagType(tag) == 1610612736) {
            KeymasterArgument arg = getArgumentByTag(tag);
            if (arg == null) {
                return defaultValue;
            }
            Date result = ((KeymasterDateArgument) arg).date;
            if (result.getTime() >= 0) {
                return result;
            }
            throw new IllegalArgumentException("Tag value too large. Tag: " + tag);
        }
        throw new IllegalArgumentException("Tag is not a date type: " + tag);
    }

    private KeymasterArgument getArgumentByTag(int tag) {
        for (KeymasterArgument arg : this.mArguments) {
            if (arg.tag == tag) {
                return arg;
            }
        }
        return null;
    }

    public boolean containsTag(int tag) {
        return getArgumentByTag(tag) != null;
    }

    public int size() {
        return this.mArguments.size();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(this.mArguments);
    }

    public void readFromParcel(Parcel in) {
        in.readTypedList(this.mArguments, KeymasterArgument.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public static BigInteger toUint64(long value) {
        if (value >= 0) {
            return BigInteger.valueOf(value);
        }
        return BigInteger.valueOf(value).add(UINT64_RANGE);
    }
}
