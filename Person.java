/** Abstract base class for everyone who uses the system. */
public abstract class Person {
    private final String id;      // private fields = Encapsulation
    private String name;
    private String contact;

    protected Person(String id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public String getId()      { return id; }
    public String getName()    { return name; }
    public String getContact() { return contact; }
    public void setName(String name)       { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }

    /** Each subclass describes its own role (Polymorphism). */
    public abstract String getRole();

    /** Can be overridden by subclasses to print extra details. */
    public void displayInfo() {
        System.out.printf("ID: %s | Name: %s | Contact: %s | Role: %s%n",
                id, name, contact, getRole());
    }

    @Override
    public String toString() { return getRole() + " " + id + " (" + name + ")"; }
}
