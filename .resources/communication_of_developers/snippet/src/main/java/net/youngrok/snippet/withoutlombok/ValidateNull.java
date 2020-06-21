package net.youngrok.snippet.withoutlombok;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@SuppressWarnings("unused")
public class ValidateNull {
    public void logName(String firstName, String lastName) {
        if (Objects.isNull(firstName)) {
            throw new NullPointerException("firstName is null");
        }
        if (Objects.isNull(lastName)) {
            throw new NullPointerException("lastName is null");
        }
        logger.info("Name is {} {}", firstName, lastName);
    }
}
