package tmsdkobf;

/* compiled from: Unknown */
public final class bu extends fs {
    public int uiRuleType;
    public int uiRuleTypeId;

    public bu() {
        this.uiRuleType = 0;
        this.uiRuleTypeId = 0;
    }

    public fs newInit() {
        return new bu();
    }

    public void readFrom(fq fqVar) {
        this.uiRuleType = fqVar.a(this.uiRuleType, 0, true);
        this.uiRuleTypeId = fqVar.a(this.uiRuleTypeId, 1, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.uiRuleType, 0);
        frVar.write(this.uiRuleTypeId, 1);
    }
}
