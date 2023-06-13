package a.b.c.bar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BarTest {

    @Test
    void shouldBarReturnFalse() {
        assertFalse(Bar.bar());
    }
}