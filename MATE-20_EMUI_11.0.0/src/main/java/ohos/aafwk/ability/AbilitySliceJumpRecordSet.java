package ohos.aafwk.ability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;

public class AbilitySliceJumpRecordSet {
    private static final LogLabel LABEL = LogLabel.create();
    private final List<JumpRecord> jumpRecords = new ArrayList();

    /* access modifiers changed from: package-private */
    public synchronized List<AbilitySliceResultInfo> getResultInfoByDst(AbilitySlice abilitySlice) {
        AbilitySliceResultInfo resultInfo;
        ArrayList arrayList = new ArrayList();
        if (abilitySlice == null) {
            Log.error(LABEL, "dst slice is null, can not find specific record.", new Object[0]);
            return arrayList;
        }
        for (JumpRecord jumpRecord : this.jumpRecords) {
            if (jumpRecord.dstSlice == abilitySlice && (resultInfo = jumpRecord.getResultInfo()) != null) {
                arrayList.add(resultInfo);
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean checkForwardCaller(AbilitySlice abilitySlice) {
        if (abilitySlice == null) {
            Log.error(LABEL, "dst slice is null, can not find specific record.", new Object[0]);
            return false;
        }
        for (JumpRecord jumpRecord : this.jumpRecords) {
            if (jumpRecord.srcSlice == abilitySlice) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public synchronized void updateForwardCaller(AbilitySlice abilitySlice, AbilitySlice abilitySlice2) {
        if (abilitySlice2 == null) {
            Log.error(LABEL, "src slice is null, can not find specific record.", new Object[0]);
        } else if (abilitySlice == null) {
            Log.error(LABEL, "dst slice is null, can not find specific record.", new Object[0]);
        } else {
            JumpRecord jumpRecord = null;
            Iterator<JumpRecord> it = this.jumpRecords.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                JumpRecord next = it.next();
                if (next.srcSlice == abilitySlice) {
                    jumpRecord = next;
                    break;
                }
            }
            if (jumpRecord != null) {
                jumpRecord.srcSlice = abilitySlice2;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void saveResultInfo(AbilitySlice abilitySlice, Intent intent) {
        if (abilitySlice == null) {
            Log.error(LABEL, "src slice is null, can not find specific record.", new Object[0]);
            return;
        }
        JumpRecord jumpRecord = null;
        Iterator<JumpRecord> it = this.jumpRecords.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            JumpRecord next = it.next();
            if (next.srcSlice == abilitySlice) {
                jumpRecord = next;
                break;
            }
        }
        if (jumpRecord != null && jumpRecord.requestCode >= 0) {
            jumpRecord.clearResultInfo();
            jumpRecord.addResultInfo(abilitySlice.getClass().getName(), jumpRecord.dstSlice, intent, jumpRecord.requestCode);
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void cleanResultInfoByDst(AbilitySlice abilitySlice) {
        if (abilitySlice == null) {
            Log.error(LABEL, "dst slice is null, can not find specific record.", new Object[0]);
            return;
        }
        for (JumpRecord jumpRecord : this.jumpRecords) {
            if (jumpRecord.dstSlice == abilitySlice) {
                jumpRecord.clearResultInfo();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void removeRecordByDst(AbilitySlice abilitySlice) {
        if (abilitySlice == null) {
            Log.error(LABEL, "target slice is null, can not find specific record.", new Object[0]);
            return;
        }
        Iterator<JumpRecord> it = this.jumpRecords.iterator();
        while (it.hasNext()) {
            JumpRecord next = it.next();
            if (next.dstSlice == abilitySlice) {
                if (Log.isDebuggable()) {
                    Log.debug(LABEL, "clear jump record. src: %{public}s, dst: %{public}s, requestCode: %{public}d.", next.srcSlice.getClass().getName(), next.dstSlice.getClass().getName(), Integer.valueOf(next.requestCode));
                }
                it.remove();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void prepareEmptyRecord(AbilitySlice abilitySlice, AbilitySlice abilitySlice2, int i) {
        if (abilitySlice == null) {
            Log.error(LABEL, "caller slice is null, can not ensure specific record.", new Object[0]);
        } else if (abilitySlice2 == null) {
            Log.error(LABEL, "target slice is null, can not ensure specific record.", new Object[0]);
        } else {
            JumpRecord jumpRecord = null;
            for (JumpRecord jumpRecord2 : this.jumpRecords) {
                if (jumpRecord2.srcSlice == abilitySlice2 && jumpRecord2.dstSlice == abilitySlice) {
                    jumpRecord = jumpRecord2;
                }
            }
            if (jumpRecord != null) {
                jumpRecord.clearResultInfo();
                jumpRecord.requestCode = i;
            } else {
                JumpRecord jumpRecord3 = new JumpRecord(abilitySlice, abilitySlice2, i);
                if (Log.isDebuggable()) {
                    Log.debug(LABEL, "create jump record. src: %{public}s, dst: %{public}s, requestCode: %{public}d", jumpRecord3.srcSlice.getClass().getName(), jumpRecord3.dstSlice.getClass().getName(), Integer.valueOf(jumpRecord3.requestCode));
                }
                this.jumpRecords.add(jumpRecord3);
            }
        }
    }

    private static class JumpRecord {
        private AbilitySlice dstSlice;
        private int requestCode;
        private AbilitySliceResultInfo sliceResultWaitToReturn;
        private AbilitySlice srcSlice;

        JumpRecord(AbilitySlice abilitySlice, AbilitySlice abilitySlice2, int i) {
            this.srcSlice = abilitySlice2;
            this.dstSlice = abilitySlice;
            this.requestCode = i;
        }

        /* access modifiers changed from: package-private */
        public void addResultInfo(String str, AbilitySlice abilitySlice, Intent intent, int i) {
            if (this.sliceResultWaitToReturn != null) {
                Log.error(AbilitySliceJumpRecordSet.LABEL, "sliceResultWaitToReturn has been set before. fromName: %{public}s", this.sliceResultWaitToReturn.fromName);
                return;
            }
            this.sliceResultWaitToReturn = new AbilitySliceResultInfo(str, abilitySlice, intent, i);
            if (Log.isDebuggable()) {
                Log.debug(AbilitySliceJumpRecordSet.LABEL, "add resultInfo. from: %{public}s, to: %{public}s, requestCode: %{public}d", this.sliceResultWaitToReturn.fromName, this.sliceResultWaitToReturn.to.getClass().getName(), Integer.valueOf(this.sliceResultWaitToReturn.requestCode));
            }
        }

        /* access modifiers changed from: package-private */
        public void clearResultInfo() {
            if (this.sliceResultWaitToReturn != null && Log.isDebuggable()) {
                Log.debug(AbilitySliceJumpRecordSet.LABEL, "clear resultInfo. from: %{public}s, to: %{public}s, requestCode: %{public}d", this.sliceResultWaitToReturn.fromName, this.sliceResultWaitToReturn.to.getClass().getName(), Integer.valueOf(this.sliceResultWaitToReturn.requestCode));
            }
            this.sliceResultWaitToReturn = null;
        }

        /* access modifiers changed from: package-private */
        public AbilitySliceResultInfo getResultInfo() {
            return this.sliceResultWaitToReturn;
        }
    }
}
