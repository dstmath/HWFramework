package ohos.event.intentagent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.event.intentagent.IntentAgentConstant;

public final class IntentAgentInfo {
    private IntentParams extraInfo;
    private List<IntentAgentConstant.Flags> flags;
    private List<Intent> intents;
    private int operationType;
    private int requestCode;

    public IntentAgentInfo() {
        this(null);
    }

    public IntentAgentInfo(int i, IntentAgentConstant.OperationType operationType2, IntentAgentConstant.Flags flags2, List<Intent> list, IntentParams intentParams) {
        this(i, operationType2, Collections.singletonList(flags2), list, intentParams);
    }

    public IntentAgentInfo(int i, IntentAgentConstant.OperationType operationType2, List<IntentAgentConstant.Flags> list, List<Intent> list2, IntentParams intentParams) {
        this.flags = new ArrayList();
        this.intents = new ArrayList();
        this.requestCode = i;
        if (operationType2 != null) {
            this.operationType = operationType2.ordinal();
        }
        if (list != null && !list.isEmpty()) {
            this.flags.addAll(list);
        }
        if (list2 != null) {
            for (Intent intent : list2) {
                if (intent != null) {
                    this.intents.add(new Intent(intent));
                }
            }
        }
        if (intentParams != null) {
            this.extraInfo = new IntentParams(intentParams);
        }
    }

    public IntentAgentInfo(IntentAgentInfo intentAgentInfo) {
        this.flags = new ArrayList();
        this.intents = new ArrayList();
        if (intentAgentInfo != null) {
            this.requestCode = intentAgentInfo.requestCode;
            this.operationType = intentAgentInfo.operationType;
            this.flags.addAll(intentAgentInfo.flags);
            List<Intent> list = intentAgentInfo.intents;
            if (list != null) {
                for (Intent intent : list) {
                    this.intents.add(new Intent(intent));
                }
            }
            IntentParams intentParams = intentAgentInfo.extraInfo;
            if (intentParams != null) {
                this.extraInfo = new IntentParams(intentParams);
            }
        }
    }

    public int getRequestCode() {
        return this.requestCode;
    }

    public int getOperationType() {
        return this.operationType;
    }

    public List<IntentAgentConstant.Flags> getFlags() {
        return this.flags;
    }

    public List<Intent> getIntents() {
        return this.intents;
    }

    public IntentParams getExtraInfo() {
        return this.extraInfo;
    }
}
