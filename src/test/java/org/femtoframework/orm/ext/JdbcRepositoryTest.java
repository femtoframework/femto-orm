package org.femtoframework.orm.ext;

import org.femtoframework.orm.Repository;
import org.femtoframework.parameters.Parameters;
import org.femtoframework.parameters.ParametersMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class JdbcRepositoryTest {

    @Test
    public void toIndexedQuery() {
        Parameters<Integer> index = new ParametersMap<>();
        String sql = Repository.toIndexedQuery("id = :foo_id AND name = :foo_name", index);
        assertEquals("id = ? AND name = ?", sql);
        assertEquals(index.getInt("foo_id"), 0);
        assertEquals(index.getInt("foo_name"), 1);

        index.clear();
        sql = Repository.toIndexedQuery("id =:foo_id AND name =:foo_name OR id=1", index);
        assertEquals("id =? AND name =? OR id=1", sql);
        assertEquals(index.getInt("foo_id"), 0);
        assertEquals(index.getInt("foo_name"), 1);


        index.clear();
        sql = Repository.toIndexedQuery("id=1", index);
        assertEquals("id=1", sql);
        assertTrue(index.isEmpty());

        index.clear();
        sql = Repository.toIndexedQuery("", index);
        assertEquals("", sql);
        assertTrue(index.isEmpty());
    }
}