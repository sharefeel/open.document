package net.youngrok.snippet.withoutlombok;

@SuppressWarnings("unused")
public class Singleton {
    private Singleton() {
    }

    private static class LazyHolder {
        private static final Singleton instance = new Singleton();
    }

    public static Singleton getInstance() {
        return Singleton.LazyHolder.instance;
    }
}
