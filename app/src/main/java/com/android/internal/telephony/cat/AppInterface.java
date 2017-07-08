package com.android.internal.telephony.cat;

public interface AppInterface {
    public static final String ALPHA_STRING = "alpha_string";
    public static final String CARD_STATUS = "card_status";
    public static final String CAT_ALPHA_NOTIFY_ACTION = "android.intent.action.stk.alpha_notify";
    public static final String CAT_CMD_ACTION = "android.intent.action.stk.command";
    public static final String CAT_ICC_STATUS_CHANGE = "android.intent.action.stk.icc_status_change";
    public static final String CAT_SESSION_END_ACTION = "android.intent.action.stk.session_end";
    public static final String REFRESH_RESULT = "refresh_result";
    public static final String STK_PERMISSION = "android.permission.RECEIVE_STK_COMMANDS";

    public enum CommandType {
        ;
        
        private int mValue;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cat.AppInterface.CommandType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cat.AppInterface.CommandType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.AppInterface.CommandType.<clinit>():void");
        }

        private CommandType(int value) {
            this.mValue = value;
        }

        public int value() {
            return this.mValue;
        }

        public static CommandType fromInt(int value) {
            for (CommandType e : values()) {
                if (e.mValue == value) {
                    return e;
                }
            }
            return null;
        }
    }

    String getLanguageNotificationCode();

    void onCmdResponse(CatResponseMessage catResponseMessage);

    void onEventDownload(CatEventMessage catEventMessage);
}
