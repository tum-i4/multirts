package a.b.c.foobar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FooBarTest {

    @Test
    void shouldReturnTrue() {
        assertTrue(true);
    }

    @Test
    void shouldReturnFalse() {
        assertFalse(false);
    }

    @Test
    void shouldFooBarReturnTrue() {
        assertTrue(FooBar.foo());
    }
}