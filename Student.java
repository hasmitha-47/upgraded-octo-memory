/** Student member: small borrowing limit, shorter loan period, higher fine. */
public class Student extends Member {
    public Student(String id, String name, String contact, String course) {
        super(id, name, contact, course);
    }
    @Override public String getType()     { return "STUDENT"; }
    @Override public int getBorrowLimit() { return 3; }
    @Override public int getLoanDays()    { return 14; }
    @Override public int getFinePerDay()  { return 2; }
}
