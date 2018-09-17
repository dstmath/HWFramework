package android_maps_conflict_avoidance.com.google.common.io.protocol;

import android_maps_conflict_avoidance.com.google.common.io.BoundInputStream;
import android_maps_conflict_avoidance.com.google.common.io.Gunzipper;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;

public final class ProtoBufUtil {
    public static boolean isGzipResponseSeen = false;

    private ProtoBufUtil() {
    }

    public static String getProtoValueOrEmpty(ProtoBuf proto, int tag) {
        String string;
        if (proto != null && proto.has(tag)) {
            string = proto.getString(tag);
        } else {
            try {
                string = "";
            } catch (ClassCastException e) {
                return "";
            }
        }
        return string;
    }

    public static String getSubProtoValueOrEmpty(ProtoBuf proto, int sub, int tag) {
        try {
            return getProtoValueOrEmpty(getSubProtoOrNull(proto, sub), tag);
        } catch (ClassCastException e) {
            return "";
        }
    }

    public static ProtoBuf getSubProtoOrNull(ProtoBuf proto, int sub) {
        return (proto != null && proto.has(sub)) ? proto.getProtoBuf(sub) : null;
    }

    public static int getProtoValueOrDefault(ProtoBuf proto, int tag, int defaultValue) {
        if (proto != null) {
            try {
                if (proto.has(tag)) {
                    defaultValue = proto.getInt(tag);
                }
            } catch (IllegalArgumentException e) {
                return defaultValue;
            } catch (ClassCastException e2) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static int getProtoValueOrZero(ProtoBuf proto, int tag) {
        return getProtoValueOrDefault(proto, tag, 0);
    }

    public static long getProtoLongValueOrZero(ProtoBuf proto, int tag) {
        long j = 0;
        if (proto != null) {
            try {
                if (proto.has(tag)) {
                    j = proto.getLong(tag);
                }
            } catch (IllegalArgumentException e) {
                return 0;
            } catch (ClassCastException e2) {
                return 0;
            }
        }
        return j;
    }

    public static long getProtoValueOrNegativeOne(ProtoBuf proto, int tag) {
        long j = -1;
        if (proto != null) {
            try {
                if (proto.has(tag)) {
                    j = proto.getLong(tag);
                }
            } catch (IllegalArgumentException e) {
                return -1;
            } catch (ClassCastException e2) {
                return -1;
            }
        }
        return j;
    }

    public static boolean getProtoValueOrFalse(ProtoBuf proto, int tag) {
        boolean z = false;
        if (proto != null) {
            try {
                if (proto.has(tag)) {
                    z = proto.getBool(tag);
                }
            } catch (IllegalArgumentException e) {
                return false;
            } catch (ClassCastException e2) {
                return false;
            }
        }
        return z;
    }

    public static InputStream getInputStreamForProtoBufResponse(DataInput dataInput) throws IOException {
        int size = dataInput.readInt();
        InputStream is = new BoundInputStream((InputStream) dataInput, Math.abs(size));
        if (size >= 0) {
            return is;
        }
        isGzipResponseSeen = true;
        return Gunzipper.gunzip(is);
    }

    public static ProtoBuf readProtoBufResponse(ProtoBufType protoBufType, DataInput dataInput) throws IOException {
        ProtoBuf response = new ProtoBuf(protoBufType);
        InputStream is = getInputStreamForProtoBufResponse(dataInput);
        response.parse(is);
        if (is.read() == -1) {
            return response;
        }
        throw new IOException();
    }

    public static void writeProtoBufToOutput(DataOutput output, ProtoBuf protoBuf) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        protoBuf.outputTo(baos);
        byte[] bytes = baos.toByteArray();
        output.writeInt(bytes.length);
        output.write(bytes);
    }
}
