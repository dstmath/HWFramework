package android.util.proto;

import java.io.IOException;

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

    public static String currentFieldToString(ProtoInputStream proto) throws IOException {
        StringBuilder sb = new StringBuilder();
        int fieldNumber = proto.getFieldNumber();
        int wireType = proto.getWireType();
        sb.append("Offset : 0x" + Integer.toHexString(proto.getOffset()));
        sb.append("\nField Number : 0x" + Integer.toHexString(proto.getFieldNumber()));
        sb.append("\nWire Type : ");
        if (wireType == 0) {
            sb.append("varint");
            long fieldConstant = ProtoStream.makeFieldId(fieldNumber, 1112396529664L);
            sb.append("\nField Value : 0x" + Long.toHexString(proto.readLong(fieldConstant)));
        } else if (wireType == 1) {
            sb.append("fixed64");
            long fieldConstant2 = ProtoStream.makeFieldId(fieldNumber, 1125281431552L);
            sb.append("\nField Value : 0x" + Long.toHexString(proto.readLong(fieldConstant2)));
        } else if (wireType == 2) {
            sb.append("length delimited");
            long fieldConstant3 = ProtoStream.makeFieldId(fieldNumber, 1151051235328L);
            sb.append("\nField Bytes : " + proto.readBytes(fieldConstant3));
        } else if (wireType == 3) {
            sb.append("start group");
        } else if (wireType == 4) {
            sb.append("end group");
        } else if (wireType != 5) {
            sb.append("unknown(" + proto.getWireType() + ")");
        } else {
            sb.append("fixed32");
            long fieldConstant4 = ProtoStream.makeFieldId(fieldNumber, 1129576398848L);
            sb.append("\nField Value : 0x" + Integer.toHexString(proto.readInt(fieldConstant4)));
        }
        return sb.toString();
    }
}
