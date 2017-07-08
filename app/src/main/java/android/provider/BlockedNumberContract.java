package android.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

public class BlockedNumberContract {
    public static final String AUTHORITY = "com.android.blockednumber";
    public static final Uri AUTHORITY_URI = null;
    public static final String METHOD_CAN_CURRENT_USER_BLOCK_NUMBERS = "can_current_user_block_numbers";
    public static final String METHOD_IS_BLOCKED = "is_blocked";
    public static final String METHOD_UNBLOCK = "unblock";
    public static final String RES_CAN_BLOCK_NUMBERS = "can_block";
    public static final String RES_NUMBER_IS_BLOCKED = "blocked";
    public static final String RES_NUM_ROWS_DELETED = "num_deleted";

    public static class BlockedNumbers {
        public static final String COLUMN_E164_NUMBER = "e164_number";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_ORIGINAL_NUMBER = "original_number";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/blocked_number";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/blocked_number";
        public static final Uri CONTENT_URI = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BlockedNumberContract.BlockedNumbers.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BlockedNumberContract.BlockedNumbers.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BlockedNumberContract.BlockedNumbers.<clinit>():void");
        }

        private BlockedNumbers() {
        }
    }

    public static class SystemContract {
        public static final String ACTION_BLOCK_SUPPRESSION_STATE_CHANGED = "android.provider.action.BLOCK_SUPPRESSION_STATE_CHANGED";
        public static final String METHOD_END_BLOCK_SUPPRESSION = "end_block_suppression";
        public static final String METHOD_GET_BLOCK_SUPPRESSION_STATUS = "get_block_suppression_status";
        public static final String METHOD_NOTIFY_EMERGENCY_CONTACT = "notify_emergency_contact";
        public static final String METHOD_SHOULD_SYSTEM_BLOCK_NUMBER = "should_system_block_number";
        public static final String RES_BLOCKING_SUPPRESSED_UNTIL_TIMESTAMP = "blocking_suppressed_until_timestamp";
        public static final String RES_IS_BLOCKING_SUPPRESSED = "blocking_suppressed";

        public static class BlockSuppressionStatus {
            public final boolean isSuppressed;
            public final long untilTimestampMillis;

            public BlockSuppressionStatus(boolean isSuppressed, long untilTimestampMillis) {
                this.isSuppressed = isSuppressed;
                this.untilTimestampMillis = untilTimestampMillis;
            }
        }

        public SystemContract() {
        }

        public static void notifyEmergencyContact(Context context) {
            context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_NOTIFY_EMERGENCY_CONTACT, null, null);
        }

        public static void endBlockSuppression(Context context) {
            context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_END_BLOCK_SUPPRESSION, null, null);
        }

        public static boolean shouldSystemBlockNumber(Context context, String phoneNumber) {
            Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_SHOULD_SYSTEM_BLOCK_NUMBER, phoneNumber, null);
            if (res != null) {
                return res.getBoolean(BlockedNumberContract.RES_NUMBER_IS_BLOCKED, false);
            }
            return false;
        }

        public static BlockSuppressionStatus getBlockSuppressionStatus(Context context) {
            Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_GET_BLOCK_SUPPRESSION_STATUS, null, null);
            return new BlockSuppressionStatus(res.getBoolean(RES_IS_BLOCKING_SUPPRESSED, false), res.getLong(RES_BLOCKING_SUPPRESSED_UNTIL_TIMESTAMP, 0));
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BlockedNumberContract.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BlockedNumberContract.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.BlockedNumberContract.<clinit>():void");
    }

    private BlockedNumberContract() {
    }

    public static boolean isBlocked(Context context, String phoneNumber) {
        Bundle res = context.getContentResolver().call(AUTHORITY_URI, METHOD_IS_BLOCKED, phoneNumber, null);
        if (res != null) {
            return res.getBoolean(RES_NUMBER_IS_BLOCKED, false);
        }
        return false;
    }

    public static int unblock(Context context, String phoneNumber) {
        return context.getContentResolver().call(AUTHORITY_URI, METHOD_UNBLOCK, phoneNumber, null).getInt(RES_NUM_ROWS_DELETED, 0);
    }

    public static boolean canCurrentUserBlockNumbers(Context context) {
        Bundle res = context.getContentResolver().call(AUTHORITY_URI, METHOD_CAN_CURRENT_USER_BLOCK_NUMBERS, null, null);
        if (res != null) {
            return res.getBoolean(RES_CAN_BLOCK_NUMBERS, false);
        }
        return false;
    }
}
