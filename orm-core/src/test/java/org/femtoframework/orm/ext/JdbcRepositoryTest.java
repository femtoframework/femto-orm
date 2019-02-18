package org.femtoframework.orm.ext;

import org.femtoframework.orm.Repository;
import org.junit.Test;

import static org.junit.Assert.*;

public class JdbcRepositoryTest {

    @Test
    public void toIndexedQuery() {
        JdbcRepository jdbcRepository = new JdbcRepository();
        Repository.IndexedQuery indexedQuery = jdbcRepository.toIndexedQuery("id = :foo_id AND name = :foo_name");
        assertEquals("id = ? AND name = ?", indexedQuery.getQuery());
        assertEquals(indexedQuery.getIndex().getInt("foo_id"), 0);
        assertEquals(indexedQuery.getIndex().getInt("foo_name"), 1);

        indexedQuery = jdbcRepository.toIndexedQuery("id =:foo_id AND name =:foo_name OR id=1");
        assertEquals("id =? AND name =? OR id=1", indexedQuery.getQuery());
        assertEquals(indexedQuery.getIndex().getInt("foo_id"), 0);
        assertEquals(indexedQuery.getIndex().getInt("foo_name"), 1);

        indexedQuery = jdbcRepository.toIndexedQuery("id=1");
        assertEquals("id=1", indexedQuery.getQuery());
        assertTrue(indexedQuery.getIndex().isEmpty());

        indexedQuery = jdbcRepository.toIndexedQuery("");
        assertNull(indexedQuery);
    }
}