package a.b.c.foo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FooTest {

    @Test
    void shouldFooReturnTrue() {
        assertTrue(Foo.foo());
    }
}