/** The admin user who manages books, members, issues and returns. */
public class Librarian extends Person {
    private final String password;

    public Librarian(String id, String name, String contact, String password) {
        super(id, name, contact);
        this.password = password;
    }

    public boolean authenticate(String attempt) { return password.equals(attempt); }

    @Override public String getRole() { return "Librarian"; }
}
