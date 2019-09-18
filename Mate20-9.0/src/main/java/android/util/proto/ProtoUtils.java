package android.util.proto;

public class ProtoUtils {
    public static void toAggStatsProto(ProtoOutputStream proto, long fieldId, long min, long average, long max) {
        long aggStatsToken = proto.start(fieldId);
        proto.write(1112396529665L, min);
        proto.write(1112396529666L, average);
        proto.write(1112396529667L, max);
        proto.end(aggStatsToken);
    }

    public static void toDuration(ProtoOutputStream proto, long fieldId, long startMs, long endMs) {
        long token = proto.start(fieldId);
        proto.write(1112396529665L, startMs);
        proto.write(1112396529666L, endMs);
        proto.end(token);
    }

    public static void writeBitWiseFlagsToProtoEnum(ProtoOutputStream proto, long fieldId, int flags, int[] origEnums, int[] protoEnums) {
        if (protoEnums.length == origEnums.length) {
            int len = origEnums.length;
            for (int i = 0; i < len; i++) {
                if (origEnums[i] == 0 && flags == 0) {
                    proto.write(fieldId, protoEnums[i]);
                    return;
                }
                if ((origEnums[i] & flags) != 0) {
                    proto.write(fieldId, protoEnums[i]);
                }
            }
            return;
        }
        throw new IllegalArgumentException("The length of origEnums must match protoEnums");
    }
}
