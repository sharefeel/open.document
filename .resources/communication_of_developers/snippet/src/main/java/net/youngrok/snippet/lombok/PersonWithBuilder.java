package net.youngrok.snippet.lombok;

import lombok.Builder;

@Builder
@SuppressWarnings("unused")
public class PersonWithBuilder {
    private String name;
    private String birth;
    private String address;

    public static PersonWithBuilder newPerson() {
        return PersonWithBuilder.builder().name("IU").birth("1993.05.16").address("ASIA/SEOUL").build();
    }
}
