package com.huawei.wallet.sdk.common.apdu.contactless;

import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import java.util.List;

public class ExcuteApduContactlessService {
    ExcuteApduContactlessService() {
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    com.huawei.wallet.sdk.common.apdu.TaskResult<com.huawei.wallet.sdk.common.apdu.model.ChannelID> excuteApduList(java.util.List<com.huawei.wallet.sdk.common.apdu.model.ApduCommand> r18, com.huawei.wallet.sdk.common.apdu.model.ChannelID r19) {
        /*
            r17 = this;
            r1 = r17
            r2 = r18
            r3 = r19
            com.huawei.wallet.sdk.common.apdu.TaskResult r0 = new com.huawei.wallet.sdk.common.apdu.TaskResult
            r0.<init>()
            r4 = r0
            r5 = 0
            java.lang.String r6 = "Success"
            if (r2 == 0) goto L_0x0164
            boolean r0 = r18.isEmpty()
            if (r0 == 0) goto L_0x0019
            goto L_0x0164
        L_0x0019:
            if (r3 == 0) goto L_0x015b
            android.nfc.tech.IsoDep r0 = r19.getIsodep()
            if (r0 != 0) goto L_0x0023
            goto L_0x015b
        L_0x0023:
            r17.resetApduCommondStatus(r18)
            android.nfc.tech.IsoDep r0 = r19.getIsodep()
            r7 = r0
            r8 = 0
            r9 = 1
            java.lang.Object r0 = r2.get(r8)     // Catch:{ IOException -> 0x0128 }
            com.huawei.wallet.sdk.common.apdu.model.ApduCommand r0 = (com.huawei.wallet.sdk.common.apdu.model.ApduCommand) r0     // Catch:{ IOException -> 0x0128 }
            r4.setLastExcutedCommand(r0)     // Catch:{ IOException -> 0x0128 }
            boolean r0 = r7.isConnected()     // Catch:{ IOException -> 0x0128 }
            if (r0 != 0) goto L_0x003f
            r7.connect()     // Catch:{ IOException -> 0x0128 }
        L_0x003f:
            java.util.Iterator r0 = r18.iterator()     // Catch:{ IOException -> 0x0128 }
        L_0x0043:
            boolean r10 = r0.hasNext()     // Catch:{ IOException -> 0x0128 }
            if (r10 == 0) goto L_0x0123
            java.lang.Object r10 = r0.next()     // Catch:{ IOException -> 0x0128 }
            com.huawei.wallet.sdk.common.apdu.model.ApduCommand r10 = (com.huawei.wallet.sdk.common.apdu.model.ApduCommand) r10     // Catch:{ IOException -> 0x0128 }
            r4.setLastExcutedCommand(r10)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r11 = r10.getApdu()     // Catch:{ IOException -> 0x0128 }
            boolean r12 = com.huawei.wallet.sdk.common.utils.StringUtil.isEmpty(r11, r9)     // Catch:{ IOException -> 0x0128 }
            if (r12 == 0) goto L_0x0066
            r5 = 1001(0x3e9, float:1.403E-42)
            java.lang.String r0 = "ExcuteApduContactlessService apdu of command is null"
            r6 = r0
            com.huawei.wallet.sdk.common.apdu.TaskResult r0 = r1.setResult(r4, r3, r5, r6)     // Catch:{ IOException -> 0x0128 }
            return r0
        L_0x0066:
            byte[] r12 = com.huawei.wallet.sdk.common.apdu.util.HexByteHelper.hexStringToByteArray(r11)     // Catch:{ IOException -> 0x0128 }
            byte[] r12 = r7.transceive(r12)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r13 = com.huawei.wallet.sdk.common.apdu.util.HexByteHelper.byteArrayToHexString(r12)     // Catch:{ IOException -> 0x0128 }
            boolean r14 = com.huawei.wallet.sdk.common.utils.StringUtil.isEmpty(r13, r9)     // Catch:{ IOException -> 0x0128 }
            if (r14 != 0) goto L_0x00fb
            int r14 = r13.length()     // Catch:{ IOException -> 0x0128 }
            r15 = 4
            if (r14 >= r15) goto L_0x0081
            goto L_0x00fb
        L_0x0081:
            r10.parseRapduAndSw(r13)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r14 = r10.getChecker()     // Catch:{ IOException -> 0x0128 }
            java.lang.String r15 = r10.getSw()     // Catch:{ IOException -> 0x0128 }
            java.util.Locale r9 = java.util.Locale.getDefault()     // Catch:{ IOException -> 0x0128 }
            java.lang.String r9 = r15.toUpperCase(r9)     // Catch:{ IOException -> 0x0128 }
            if (r14 == 0) goto L_0x0098
            r15 = 1
            goto L_0x0099
        L_0x0098:
            r15 = r8
        L_0x0099:
            if (r15 == 0) goto L_0x00f6
            java.lang.String r8 = r10.getChecker()     // Catch:{ IOException -> 0x0128 }
            boolean r8 = r9.matches(r8)     // Catch:{ IOException -> 0x0128 }
            if (r8 != 0) goto L_0x00f6
            r4.setLastExcutedCommand(r10)     // Catch:{ IOException -> 0x0128 }
            r5 = 4002(0xfa2, float:5.608E-42)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0128 }
            r0.<init>()     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = "excuteApduList excuteApdu failed. sw is not matched. rapdu : "
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            r0.append(r13)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = " sw : "
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = r10.getSw()     // Catch:{ IOException -> 0x0128 }
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = " checker : "
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = r10.getChecker()     // Catch:{ IOException -> 0x0128 }
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = " apdu index : "
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            int r8 = r10.getIndex()     // Catch:{ IOException -> 0x0128 }
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = " apdu["
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = com.huawei.wallet.sdk.common.apdu.util.OmaUtil.getLogApdu(r11)     // Catch:{ IOException -> 0x0128 }
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = "]"
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r0 = r0.toString()     // Catch:{ IOException -> 0x0128 }
            r6 = r0
            com.huawei.wallet.sdk.common.apdu.TaskResult r0 = r1.setResult(r4, r3, r5, r6)     // Catch:{ IOException -> 0x0128 }
            return r0
        L_0x00f6:
            r8 = 0
            r9 = 1
            goto L_0x0043
        L_0x00fb:
            r10.setRapdu(r13)     // Catch:{ IOException -> 0x0128 }
            r5 = 4001(0xfa1, float:5.607E-42)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0128 }
            r0.<init>()     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = "excuteApduList excuteApdu["
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = com.huawei.wallet.sdk.common.apdu.util.OmaUtil.getLogApdu(r11)     // Catch:{ IOException -> 0x0128 }
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r8 = "] failed. rapdu is small. resp : "
            r0.append(r8)     // Catch:{ IOException -> 0x0128 }
            r0.append(r13)     // Catch:{ IOException -> 0x0128 }
            java.lang.String r0 = r0.toString()     // Catch:{ IOException -> 0x0128 }
            r6 = r0
            com.huawei.wallet.sdk.common.apdu.TaskResult r0 = r1.setResult(r4, r3, r5, r6)     // Catch:{ IOException -> 0x0128 }
            return r0
        L_0x0123:
            r4.setData(r3)
            return r4
        L_0x0128:
            r0 = move-exception
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "ExcuteApduContactlessService excuteApduList failed. IOException : "
            r8.append(r9)
            java.lang.String r9 = r0.getMessage()
            r8.append(r9)
            java.lang.String r6 = r8.toString()
            r8 = 0
            com.huawei.wallet.sdk.common.log.LogC.e(r6, r8)
            java.lang.String r8 = r0.getMessage()
            r9 = 1
            boolean r8 = com.huawei.wallet.sdk.common.utils.StringUtil.isEmpty(r8, r9)
            if (r8 == 0) goto L_0x0154
            r8 = 6002(0x1772, float:8.41E-42)
            com.huawei.wallet.sdk.common.apdu.TaskResult r8 = r1.setResult(r4, r3, r8, r6)
            return r8
        L_0x0154:
            r8 = 6003(0x1773, float:8.412E-42)
            com.huawei.wallet.sdk.common.apdu.TaskResult r8 = r1.setResult(r4, r3, r8, r6)
            return r8
        L_0x015b:
            r0 = 6001(0x1771, float:8.409E-42)
            java.lang.String r5 = "ExcuteApduContactlessService excuteApduList failed. Tag is empty"
            com.huawei.wallet.sdk.common.apdu.TaskResult r6 = r1.setResult(r4, r3, r0, r5)
            return r6
        L_0x0164:
            r0 = 1004(0x3ec, float:1.407E-42)
            java.lang.String r5 = "ExcuteApduContactlessService excuteApduList failed.apdu is empty"
            com.huawei.wallet.sdk.common.apdu.TaskResult r6 = r1.setResult(r4, r3, r0, r5)
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.wallet.sdk.common.apdu.contactless.ExcuteApduContactlessService.excuteApduList(java.util.List, com.huawei.wallet.sdk.common.apdu.model.ChannelID):com.huawei.wallet.sdk.common.apdu.TaskResult");
    }

    private TaskResult<ChannelID> setResult(TaskResult<ChannelID> result, ChannelID channelID, int resultCode, String msg) {
        result.setData(channelID);
        result.setResultCode(resultCode);
        result.setMsg(msg);
        return result;
    }

    private void resetApduCommondStatus(List<ApduCommand> commands) {
        for (ApduCommand command : commands) {
            command.setRapdu("");
            command.setSw("");
        }
    }
}
