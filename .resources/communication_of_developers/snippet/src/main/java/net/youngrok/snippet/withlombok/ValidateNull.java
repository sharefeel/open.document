package net.youngrok.snippet.withlombok;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@SuppressWarnings("unused")
public class ValidateNull {
    public void logName(@NonNull String firstName, @NonNull String lastName) {
        logger.info("Name is {} {}", firstName, lastName);
    }
}
