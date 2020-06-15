package net.youngrok.snippet.lombok;

@SuppressWarnings("unused")
public class SingletonWithoutLombok {
    private SingletonWithoutLombok() {
    }

    private static class LazyHolder {
        private static final SingletonWithoutLombok instance = new SingletonWithoutLombok();
    }

    public static SingletonWithoutLombok getInstance() {
        return SingletonWithoutLombok.LazyHolder.instance;
    }
}
