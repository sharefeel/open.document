package net.youngrok.snippet.lombok;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Singleton {
    private static class LazyHolder {
        private static final Singleton instance = new Singleton();
    }

    public static Singleton getInstance() {
        return LazyHolder.instance;
    }
}
