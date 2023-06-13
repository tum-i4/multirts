package a.b.c.bar;

import a.b.c.foo.Foo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FooTestCase {
    @Test
    void shouldFooReturnTrue() {
        assertTrue(Foo.foo());
    }
}
