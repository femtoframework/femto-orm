package org.femtoframework.orm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Limit for LIST
 */
public class Limit implements Externalizable {

    public static final Limit UNLIMITED = new Limit(0, Integer.MAX_VALUE);

    private int offset = 0;
    private int limit = 100;

    public Limit(){
    }

    public Limit(int limit) {
        this(0, limit);
    }

    public Limit(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(offset);
        out.writeInt(limit);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        offset = in.readInt();
        limit = in.readInt();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
