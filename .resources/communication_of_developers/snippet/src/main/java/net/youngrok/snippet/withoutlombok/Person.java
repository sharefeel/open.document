package net.youngrok.snippet.withoutlombok;

@SuppressWarnings("all")
public class Person {
    private String name;
    private String birth;
    private String address;

    public String getName() {
        return name;
    }

    public String getBirth() {
        return birth;
    }

    public String getAddress() {
        return address;
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public Person setBirth(String birth) {
        this.birth = birth;
        return this;
    }

    public Person setAddress(String address) {
        this.address = address;
        return this;
    }

    @Override
    public String toString() {
        return "PersionWithourLombok(name=" + name + ", birth=" + birth + ", address=" + address;
    }

    public static Person newPerson() {
        return new Person().setName("IU").setBirth("1993.05.16").setAddress("ASIA/SEOUL");
    }
}
