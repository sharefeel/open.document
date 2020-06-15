package net.youngrok.snippet.lombok;

import lombok.*;

@Getter
@Setter
@ToString
@SuppressWarnings("unused")
public class Person {
    private String name;
    private String birth;
    private String address;

    public static Person newPerson() {
        return new Person().name("IU").birth("1993.05.16").address("ASIA/SEOUL");
    }
}
