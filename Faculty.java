/** Faculty member: bigger limit, longer loan period, lower fine. */
public class Faculty extends Member {
    public Faculty(String id, String name, String contact, String department) {
        super(id, name, contact, department);
    }
    @Override public String getType()     { return "FACULTY"; }
    @Override public int getBorrowLimit() { return 5; }
    @Override public int getLoanDays()    { return 30; }
    @Override public int getFinePerDay()  { return 1; }
}
