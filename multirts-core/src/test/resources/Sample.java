package foo;

@interface Anno {
}

@Anno
public class Sample {
    void foo() {
    }

    interface X {
        void foo();
    }

    static class Foo {
        void bar() {
        }
    }

    static class Y implements X {

        @Override
        public void foo() {

        }
    }

    abstract static class Bar {
    }

}
