package net.youngrok.snippet.withlombok;

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
        return new Person().setName("IU").setBirth("1993.05.16").setAddress("ASIA/SEOUL");
    }
}
