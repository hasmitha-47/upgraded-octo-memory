import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** One issue/return record. */
public class Transaction {
    private final int id;
    private final String bookId;
    private final String memberId;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;   // null while the book is still out
    private int fine;

    public Transaction(int id, String bookId, String memberId,
                       LocalDate issueDate, LocalDate dueDate,
                       LocalDate returnDate, int fine) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fine = fine;
    }

    public int getId()              { return id; }
    public String getBookId()       { return bookId; }
    public String getMemberId()     { return memberId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate()   { return dueDate; }
    public LocalDate getReturnDate(){ return returnDate; }
    public int getFine()            { return fine; }
    public boolean isReturned()     { return returnDate != null; }

    public boolean isOverdue(LocalDate asOf) {
        return !isReturned() && asOf.isAfter(dueDate);
    }

    public long daysOverdue(LocalDate asOf) {
        LocalDate end = isReturned() ? returnDate : asOf;
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
    }

    public void markReturned(LocalDate date, int fine) {
        this.returnDate = date;
        this.fine = fine;
    }
}
