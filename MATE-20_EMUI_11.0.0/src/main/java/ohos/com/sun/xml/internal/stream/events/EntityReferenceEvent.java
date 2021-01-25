package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import ohos.javax.xml.stream.events.EntityDeclaration;
import ohos.javax.xml.stream.events.EntityReference;

public class EntityReferenceEvent extends DummyEvent implements EntityReference {
    private EntityDeclaration fEntityDeclaration;
    private String fEntityName;

    public EntityReferenceEvent() {
        init();
    }

    public EntityReferenceEvent(String str, EntityDeclaration entityDeclaration) {
        init();
        this.fEntityName = str;
        this.fEntityDeclaration = entityDeclaration;
    }

    public String getName() {
        return this.fEntityName;
    }

    public String toString() {
        String replacementText = this.fEntityDeclaration.getReplacementText();
        if (replacementText == null) {
            replacementText = "";
        }
        return "&" + getName() + ";='" + replacementText + "'";
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write(38);
        writer.write(getName());
        writer.write(59);
    }

    public EntityDeclaration getDeclaration() {
        return this.fEntityDeclaration;
    }

    /* access modifiers changed from: protected */
    public void init() {
        setEventType(9);
    }
}
