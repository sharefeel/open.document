package net.youngrok.snippet.lombok;

@SuppressWarnings("all")
public class PersonWithoutLombok {
    private String name;
    private String birth;
    private String address;

    public String name() {
        return name;
    }

    public String birth() {
        return birth;
    }

    public String address() {
        return address;
    }

    public PersonWithoutLombok name(String name) {
        this.name = name;
        return this;
    }

    public PersonWithoutLombok birth(String birth) {
        this.birth = birth;
        return this;
    }

    public PersonWithoutLombok address(String address) {
        this.address = address;
        return this;
    }

    @Override
    public String toString() {
        return "PersionWithourLombok(name=" + name + ", birth=" + birth + ", address=" + address;
    }

    public static PersonWithoutLombok newPerson() {
        PersonWithoutLombok personWithoutLombok = new PersonWithoutLombok();
        personWithoutLombok.name("IU");
        personWithoutLombok.birth("1993.05.16");
        personWithoutLombok.address("ASIA/SEOUL");
        return personWithoutLombok;
    }
}
