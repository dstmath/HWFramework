package com.android.internal.telephony.cat;

import com.android.internal.telephony.cat.AbstractCommandParamsFactory.CommandParamsFactoryReference;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.List;

public class HwCommandParamsFactoryReference implements CommandParamsFactoryReference {
    private static CommandParamsFactoryUtils commandParamsFactoryUtils;
    private CommandParamsFactory mCommandParamsFactory;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cat.HwCommandParamsFactoryReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cat.HwCommandParamsFactoryReference.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.HwCommandParamsFactoryReference.<clinit>():void");
    }

    public HwCommandParamsFactoryReference(CommandParamsFactory commandParamsFactory) {
        CatLog.d(this, "construct HwCommandParamsFactoryReference ");
        this.mCommandParamsFactory = commandParamsFactory;
    }

    public boolean processLanguageNotification(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d(this, "processLanguageNotification");
        String Language = null;
        switch (cmdDet.commandQualifier) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                CatLog.d(this, "commandQualifier 0x01");
                ComprehensionTlv ctlv = commandParamsFactoryUtils.searchForTag(this.mCommandParamsFactory, ComprehensionTlvTag.LANGUAGE, ctlvs);
                if (ctlv != null) {
                    Language = ValueParser.retrieveTextString(ctlv);
                    break;
                }
                throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
            default:
                CatLog.d(this, "commandQualifier 0x00");
                break;
        }
        commandParamsFactoryUtils.setCmdParams(this.mCommandParamsFactory, new HwCommandParams(cmdDet, Language));
        return false;
    }
}
