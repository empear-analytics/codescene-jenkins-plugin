package org.jenkinsci.plugins.codescene.Domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class RepositoryTest {

    @Test
    public void testCreateRepositoryWithValidName() {
        assertNotNull(new Repository("Test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRepositoryWithInvalidNameNull() {
        new Repository(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRepositoryWithInvalidNameEmptyString() {
        new Repository("");
    }
}