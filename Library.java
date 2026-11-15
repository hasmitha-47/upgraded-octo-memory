import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Core service class. Holds all data in Collections and applies the library rules. */
public class Library {

    public enum SearchField { ID, TITLE, AUTHOR, CATEGORY, ANY }

    // Collections: Map for fast lookup by ID, List for the ordered transaction log
    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private int nextTransactionId = 1;

    private final FileHandler fileHandler;

    public Library(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    // ================= storage =================
    public void load() throws LibraryException {
        try {
            for (Book b : fileHandler.loadBooks())     books.put(b.getId(), b);
            for (Member m : fileHandler.loadMembers()) members.put(m.getId(), m);
            transactions.addAll(fileHandler.loadTransactions());
            for (Transaction t : transactions)
                nextTransactionId = Math.max(nextTransactionId, t.getId() + 1);
        } catch (IOException e) {
            throw new LibraryException("Could not read saved data: " + e.getMessage(), e);
        }
    }

    private void persist() throws LibraryException {
        try {
            fileHandler.saveBooks(books.values());
            fileHandler.saveMembers(members.values());
            fileHandler.saveTransactions(transactions);
        } catch (IOException e) {
            throw new LibraryException("Could not save data: " + e.getMessage(), e);
        }
    }

    public void saveReport(String fileName, String text) throws LibraryException {
        try {
            fileHandler.saveReport(fileName, text);
        } catch (IOException e) {
            throw new LibraryException("Could not save report: " + e.getMessage(), e);
        }
    }

    // ================= validation helpers =================
    private static String clean(String value, String field) throws InvalidInputException {
        if (value == null || value.trim().isEmpty())
            throw new InvalidInputException(field + " cannot be empty.");
        if (value.contains("|"))
            throw new InvalidInputException(field + " cannot contain the '|' character.");
        return value.trim();
    }

    private static String cleanId(String value, String field) throws InvalidInputException {
        String id = clean(value, field).toUpperCase();
        if (!id.matches("[A-Z0-9_-]+"))
            throw new InvalidInputException(field + " may only contain letters, digits, '-' and '_'.");
        return id;
    }

    // ================= book management =================
    public Book addBook(String id, String title, String author, String category, int copies)
            throws LibraryException {
        String bookId = cleanId(id, "Book ID");
        if (books.containsKey(bookId))
            throw new DuplicateRecordException("A book with ID " + bookId + " already exists.");
        if (copies < 1) throw new InvalidInputException("Number of copies must be at least 1.");
        Book b = new Book(bookId, clean(title, "Title"), clean(author, "Author"),
                clean(category, "Category"), copies, copies);
        books.put(bookId, b);
        persist();
        return b;
    }

    /** Blank / negative arguments mean "keep the existing value". */
    public Book updateBook(String id, String title, String author, String category, int copies)
            throws LibraryException {
        Book b = findBook(id);
        if (title != null && !title.isBlank())       b.setTitle(clean(title, "Title"));
        if (author != null && !author.isBlank())     b.setAuthor(clean(author, "Author"));
        if (category != null && !category.isBlank()) b.setCategory(clean(category, "Category"));
        if (copies > 0) {
            try {
                b.setTotalCopies(copies);
            } catch (IllegalArgumentException e) {
                throw new InvalidInputException(e.getMessage());
            }
        }
        persist();
        return b;
    }

    public void deleteBook(String id) throws LibraryException {
        Book b = findBook(id);
        if (b.getIssuedCopies() > 0)
            throw new InvalidInputException("Cannot delete \"" + b.getTitle() + "\": "
                    + b.getIssuedCopies() + " copy(ies) are currently issued.");
        books.remove(b.getId());
        persist();
    }

    public Book findBook(String id) throws LibraryException {
        Book b = books.get(cleanId(id, "Book ID"));
        if (b == null) throw new RecordNotFoundException("No book found with ID " + id.trim().toUpperCase() + ".");
        return b;
    }

    // ================= member management =================
    public Member registerMember(String type, String id, String name, String contact, String affiliation)
            throws LibraryException {
        String memberId = cleanId(id, "Member ID");
        if (members.containsKey(memberId))
            throw new DuplicateRecordException("A member with ID " + memberId + " already exists.");
        String n = clean(name, "Name"), c = clean(contact, "Contact"), a = clean(affiliation, "Course/Department");
        Member m = type.equalsIgnoreCase("FACULTY")
                ? new Faculty(memberId, n, c, a)
                : new Student(memberId, n, c, a);        // polymorphic reference
        members.put(memberId, m);
        persist();
        return m;
    }

    public Member updateMember(String id, String name, String contact, String affiliation)
            throws LibraryException {
        Member m = findMember(id);
        if (name != null && !name.isBlank())               m.setName(clean(name, "Name"));
        if (contact != null && !contact.isBlank())         m.setContact(clean(contact, "Contact"));
        if (affiliation != null && !affiliation.isBlank()) m.setAffiliation(clean(affiliation, "Course/Department"));
        persist();
        return m;
    }

    public void deleteMember(String id) throws LibraryException {
        Member m = findMember(id);
        if (!activeLoansOf(m.getId()).isEmpty())
            throw new InvalidInputException("Cannot delete " + m.getName() + ": they still have books to return.");
        members.remove(m.getId());
        persist();
    }

    public Member findMember(String id) throws LibraryException {
        Member m = members.get(cleanId(id, "Member ID"));
        if (m == null) throw new RecordNotFoundException("No member found with ID " + id.trim().toUpperCase() + ".");
        return m;
    }

    // ================= issue & return =================
    public Transaction issueBook(String bookId, String memberId, LocalDate date) throws LibraryException {
        Book book = findBook(bookId);
        Member member = findMember(memberId);

        if (!book.isAvailable())
            throw new BookUnavailableException("\"" + book.getTitle() + "\" has no copies available right now.");

        List<Transaction> active = activeLoansOf(member.getId());
        if (active.size() >= member.getBorrowLimit())
            throw new LimitExceededException(member.getName() + " has reached the limit of "
                    + member.getBorrowLimit() + " books.");
        for (Transaction t : active)
            if (t.getBookId().equals(book.getId()))
                throw new InvalidInputException(member.getName() + " already has this book.");

        Transaction t = new Transaction(nextTransactionId++, book.getId(), member.getId(),
                date, date.plusDays(member.getLoanDays()), null, 0);
        book.issueCopy();
        transactions.add(t);
        persist();
        return t;
    }

    public Transaction returnBook(String bookId, String memberId, LocalDate date) throws LibraryException {
        Book book = findBook(bookId);
        Member member = findMember(memberId);
        Transaction target = null;
        for (Transaction t : activeLoansOf(member.getId()))
            if (t.getBookId().equals(book.getId())) { target = t; break; }
        if (target == null)
            throw new RecordNotFoundException(member.getName() + " has not borrowed \"" + book.getTitle() + "\".");
        if (date.isBefore(target.getIssueDate()))
            throw new InvalidInputException("Return date cannot be before the issue date (" + target.getIssueDate() + ").");

        int fine = (int) (target.daysOverdue(date) * member.getFinePerDay());
        target.markReturned(date, fine);
        book.returnCopy();
        persist();
        return target;
    }

    // ================= search =================
    /** Overload 1: search everywhere (title, author, category, ID). */
    public List<Book> searchBooks(String keyword) {
        return searchBooks(SearchField.ANY, keyword);
    }

    /** Overload 2: search a specific field. */
    public List<Book> searchBooks(SearchField field, String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return books.values().stream()
                .filter(b -> switch (field) {
                    case ID       -> b.getId().toLowerCase().equals(k);
                    case TITLE    -> b.getTitle().toLowerCase().contains(k);
                    case AUTHOR   -> b.getAuthor().toLowerCase().contains(k);
                    case CATEGORY -> b.getCategory().toLowerCase().contains(k);
                    case ANY      -> b.getId().toLowerCase().equals(k)
                                  || b.getTitle().toLowerCase().contains(k)
                                  || b.getAuthor().toLowerCase().contains(k)
                                  || b.getCategory().toLowerCase().contains(k);
                })
                .collect(Collectors.toList());
    }

    // ================= overdue & fines =================
    public List<Transaction> activeLoansOf(String memberId) {
        return transactions.stream()
                .filter(t -> t.getMemberId().equals(memberId) && !t.isReturned())
                .collect(Collectors.toList());
    }

    public List<Transaction> historyOf(String memberId) {
        return transactions.stream()
                .filter(t -> t.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    public List<Transaction> getOverdueTransactions(LocalDate asOf) {
        return transactions.stream()
                .filter(t -> t.isOverdue(asOf))
                .sorted(Comparator.comparing(Transaction::getDueDate))
                .collect(Collectors.toList());
    }

    /** Fine that a transaction has built up so far (or its final fine if returned). */
    public int currentFine(Transaction t, LocalDate asOf) {
        if (t.isReturned()) return t.getFine();
        Member m = members.get(t.getMemberId());
        int rate = (m == null) ? 0 : m.getFinePerDay();
        return (int) (t.daysOverdue(asOf) * rate);
    }

    // ================= read-only views =================
    public Collection<Book> getBooks()                { return Collections.unmodifiableCollection(books.values()); }
    public Collection<Member> getMembers()            { return Collections.unmodifiableCollection(members.values()); }
    public List<Transaction> getTransactions()        { return Collections.unmodifiableList(transactions); }
    public Book getBookOrNull(String id)              { return books.get(id); }
    public Member getMemberOrNull(String id)          { return members.get(id); }

    // ================= sample data =================
    public boolean isEmpty() { return books.isEmpty() && members.isEmpty(); }

    public void seedSampleData() throws LibraryException {
        addBook("B001", "Java: The Complete Reference", "Herbert Schildt", "Programming", 3);
        addBook("B002", "Effective Java", "Joshua Bloch", "Programming", 2);
        addBook("B003", "Introduction to Algorithms", "Thomas Cormen", "Computer Science", 2);
        addBook("B004", "Wings of Fire", "A P J Abdul Kalam", "Biography", 2);
        addBook("B005", "The Alchemist", "Paulo Coelho", "Fiction", 1);
        registerMember("STUDENT", "S001", "Ananya S Nair", "ananya@example.com", "B.Tech CSE");
        registerMember("STUDENT", "S002", "Navami CP", "navami@example.com", "B.Tech CSE");
        registerMember("FACULTY", "F001", "Dr. Rao", "rao@example.com", "Computer Science");
    }
}
