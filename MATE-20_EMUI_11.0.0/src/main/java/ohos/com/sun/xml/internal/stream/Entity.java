package ohos.com.sun.xml.internal.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.xml.internal.stream.util.ThreadLocalBufferAllocator;

public abstract class Entity {
    public boolean inExternalSubset;
    public String name;

    public abstract boolean isExternal();

    public abstract boolean isUnparsed();

    public Entity() {
        clear();
    }

    public Entity(String str, boolean z) {
        this.name = str;
        this.inExternalSubset = z;
    }

    public boolean isEntityDeclInExternalSubset() {
        return this.inExternalSubset;
    }

    public void clear() {
        this.name = null;
        this.inExternalSubset = false;
    }

    public void setValues(Entity entity) {
        this.name = entity.name;
        this.inExternalSubset = entity.inExternalSubset;
    }

    public static class InternalEntity extends Entity {
        public String text;

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public final boolean isExternal() {
            return false;
        }

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public final boolean isUnparsed() {
            return false;
        }

        public InternalEntity() {
            clear();
        }

        public InternalEntity(String str, String str2, boolean z) {
            super(str, z);
            this.text = str2;
        }

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public void clear() {
            Entity.super.clear();
            this.text = null;
        }

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public void setValues(Entity entity) {
            Entity.super.setValues(entity);
            this.text = null;
        }

        public void setValues(InternalEntity internalEntity) {
            Entity.super.setValues((Entity) internalEntity);
            this.text = internalEntity.text;
        }
    }

    public static class ExternalEntity extends Entity {
        public XMLResourceIdentifier entityLocation;
        public String notation;

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public final boolean isExternal() {
            return true;
        }

        public ExternalEntity() {
            clear();
        }

        public ExternalEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, boolean z) {
            super(str, z);
            this.entityLocation = xMLResourceIdentifier;
            this.notation = str2;
        }

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public final boolean isUnparsed() {
            return this.notation != null;
        }

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public void clear() {
            Entity.super.clear();
            this.entityLocation = null;
            this.notation = null;
        }

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public void setValues(Entity entity) {
            Entity.super.setValues(entity);
            this.entityLocation = null;
            this.notation = null;
        }

        public void setValues(ExternalEntity externalEntity) {
            Entity.super.setValues((Entity) externalEntity);
            this.entityLocation = externalEntity.entityLocation;
            this.notation = externalEntity.notation;
        }
    }

    public static class ScannedEntity extends Entity {
        public static final int DEFAULT_BUFFER_SIZE = 8192;
        public static final int DEFAULT_INTERNAL_BUFFER_SIZE = 1024;
        public static final int DEFAULT_XMLDECL_BUFFER_SIZE = 28;
        public int baseCharOffset;
        public char[] ch = null;
        public int columnNumber = 1;
        public int count;
        boolean declaredEncoding = false;
        public String encoding;
        public XMLResourceIdentifier entityLocation;
        boolean externallySpecifiedEncoding = false;
        public int fBufferSize = 8192;
        public int fLastCount;
        public int fTotalCountTillLastLoad;
        public boolean isExternal;
        public boolean isGE = false;
        public int lineNumber = 1;
        public boolean literal;
        public boolean mayReadChunks;
        public int position;
        public Reader reader;
        public int startPosition;
        public InputStream stream;
        public String version;
        public boolean xmlDeclChunkRead = false;
        public String xmlVersion = "1.0";

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public final boolean isUnparsed() {
            return false;
        }

        public String getEncodingName() {
            return this.encoding;
        }

        public String getEntityVersion() {
            return this.version;
        }

        public void setEntityVersion(String str) {
            this.version = str;
        }

        public Reader getEntityReader() {
            return this.reader;
        }

        public InputStream getEntityInputStream() {
            return this.stream;
        }

        public ScannedEntity(boolean z, String str, XMLResourceIdentifier xMLResourceIdentifier, InputStream inputStream, Reader reader2, String str2, boolean z2, boolean z3, boolean z4) {
            this.isGE = z;
            this.name = str;
            this.entityLocation = xMLResourceIdentifier;
            this.stream = inputStream;
            this.reader = reader2;
            this.encoding = str2;
            this.literal = z2;
            this.mayReadChunks = z3;
            this.isExternal = z4;
            int i = z4 ? this.fBufferSize : 1024;
            this.ch = ThreadLocalBufferAllocator.getBufferAllocator().getCharBuffer(i);
            if (this.ch == null) {
                this.ch = new char[i];
            }
        }

        public void close() throws IOException {
            ThreadLocalBufferAllocator.getBufferAllocator().returnCharBuffer(this.ch);
            this.ch = null;
            this.reader.close();
        }

        public boolean isEncodingExternallySpecified() {
            return this.externallySpecifiedEncoding;
        }

        public void setEncodingExternallySpecified(boolean z) {
            this.externallySpecifiedEncoding = z;
        }

        public boolean isDeclaredEncoding() {
            return this.declaredEncoding;
        }

        public void setDeclaredEncoding(boolean z) {
            this.declaredEncoding = z;
        }

        @Override // ohos.com.sun.xml.internal.stream.Entity
        public final boolean isExternal() {
            return this.isExternal;
        }

        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("name=\"" + this.name + '\"');
            StringBuilder sb = new StringBuilder();
            sb.append(",ch=");
            sb.append(new String(this.ch));
            stringBuffer.append(sb.toString());
            stringBuffer.append(",position=" + this.position);
            stringBuffer.append(",count=" + this.count);
            return stringBuffer.toString();
        }
    }
}
