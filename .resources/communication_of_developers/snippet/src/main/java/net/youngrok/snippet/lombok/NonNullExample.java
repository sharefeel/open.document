package net.youngrok.snippet.lombok;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@SuppressWarnings("unused")
public class NonNullExample {
    public void lombokMethod(@NonNull String firstName, @NonNull String lastName) {
        logger.info("Name is {} {}", firstName, lastName);
    }

    public void oldMethod(String firstName, String lastName) {
        if (Objects.isNull(firstName)) {
            throw new NullPointerException("firstName is null");
        }
        if (Objects.isNull(lastName)) {
            throw new NullPointerException("lastName is null");
        }
        logger.info("Name is {} {}", firstName, lastName);
    }
}
