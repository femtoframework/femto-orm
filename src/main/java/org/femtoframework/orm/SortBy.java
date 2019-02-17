package org.femtoframework.orm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * SortBy
 */
public class SortBy implements Externalizable {

    private String column;
    private boolean ascending = true;

    public SortBy() {
    }

    public SortBy(String column) {
        this(column, true);
    }

    public SortBy(String column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(getColumn());
        out.writeBoolean(isAscending());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setColumn(in.readUTF());
        setAscending(in.readBoolean());
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
}
