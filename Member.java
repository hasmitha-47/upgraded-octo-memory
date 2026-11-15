/**
 * A registered library member. Borrowing rules differ per member type,
 * so subclasses override the abstract methods (Polymorphism).
 */
public abstract class Member extends Person {
    private String affiliation;   // course for students, department for faculty

    protected Member(String id, String name, String contact, String affiliation) {
        super(id, name, contact);
        this.affiliation = affiliation;
    }

    public String getAffiliation()                  { return affiliation; }
    public void setAffiliation(String affiliation)  { this.affiliation = affiliation; }

    public abstract String getType();      // STUDENT / FACULTY (used in file storage)
    public abstract int getBorrowLimit();  // max books at one time
    public abstract int getLoanDays();     // days before a book is due
    public abstract int getFinePerDay();   // fine per overdue day (Rs.)

    @Override
    public String getRole() { return "Member (" + getType() + ")"; }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.printf("      Affiliation: %s | Limit: %d books | Loan period: %d days | Fine: Rs.%d/day%n",
                affiliation, getBorrowLimit(), getLoanDays(), getFinePerDay());
    }
}
