import java.time.LocalDate;
import java.util.*;

/** Console entry point: login, Librarian menu and Student menu. */
public class Main {

    private static Library library;
    private static InputHelper in;
    // Default admin account (change as needed)
    private static final Librarian ADMIN =
            new Librarian("L001", "Head Librarian", "library@example.com", "admin123");

    public static void main(String[] args) {
        in = new InputHelper(new Scanner(System.in));
        library = new Library(new FileHandler("data"));

        try {
            library.load();
            if (library.isEmpty()) {
                library.seedSampleData();
                System.out.println("(First run: sample books and members were added.)");
            }
        } catch (LibraryException e) {
            System.out.println("Startup error: " + e.getMessage());
            return;
        }

        System.out.println("==========================================");
        System.out.println("      LIBRARY MANAGEMENT SYSTEM (Java)");
        System.out.println("==========================================");

        try {
            boolean running = true;
            while (running) {
                System.out.println("\n1. Librarian login");
                System.out.println("2. Student / Member login");
                System.out.println("0. Exit");
                switch (in.readInt("Choose: ", 0, 2)) {
                    case 1 -> librarianLogin();
                    case 2 -> memberLogin();
                    default -> running = false;
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nInput closed.");
        }
        System.out.println("Goodbye!");
    }

    // ================= login =================
    private static void librarianLogin() {
        String pw = in.readLine("Librarian password: ");
        if (ADMIN.authenticate(pw)) {
            System.out.println("Welcome, " + ADMIN.getName() + " (" + ADMIN.getRole() + ")");
            librarianMenu();
        } else {
            System.out.println("Incorrect password.");
        }
    }

    private static void memberLogin() {
        try {
            Member m = library.findMember(in.readRequired("Enter your Member ID: "));
            System.out.println("Welcome, " + m.getName() + "!");
            memberMenu(m);
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================= librarian menu =================
    private static void librarianMenu() {
        while (true) {
            System.out.println("""

                    ---------- LIBRARIAN MENU ----------
                     1. Add book            5. Register member
                     2. Update book         6. Update member
                     3. Delete book         7. Delete member
                     4. View all books      8. View all members
                     9. Issue book         10. Return book
                    11. Search books       12. Overdue books & fines
                    13. Activity report     0. Logout""");
            int choice = in.readInt("Choose: ", 0, 13);
            if (choice == 0) return;
            try {                                   // one handler covers every operation
                switch (choice) {
                    case 1 -> addBook();
                    case 2 -> updateBook();
                    case 3 -> deleteBook();
                    case 4 -> viewAllBooks();
                    case 5 -> registerMember();
                    case 6 -> updateMember();
                    case 7 -> deleteMember();
                    case 8 -> viewAllMembers();
                    case 9 -> issueBook();
                    case 10 -> returnBook();
                    case 11 -> searchBooks();
                    case 12 -> showOverdue();
                    case 13 -> showReport();
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void addBook() throws LibraryException {
        String id = in.readRequired("Book ID: ");
        String title = in.readRequired("Title: ");
        String author = in.readRequired("Author: ");
        String category = in.readRequired("Category: ");
        int copies = in.readInt("Number of copies: ", 1, 1000);
        Book b = library.addBook(id, title, author, category, copies);
        System.out.println("Added: " + b);
    }

    private static void updateBook() throws LibraryException {
        Book b = library.findBook(in.readRequired("Book ID to update: "));
        System.out.println("Leave a field blank to keep the current value.");
        String title = in.readLine("Title [" + b.getTitle() + "]: ");
        String author = in.readLine("Author [" + b.getAuthor() + "]: ");
        String category = in.readLine("Category [" + b.getCategory() + "]: ");
        int copies = in.readOptionalPositiveInt("Total copies [" + b.getTotalCopies() + "]: ");
        library.updateBook(b.getId(), title, author, category, copies);
        System.out.println("Book updated.");
    }

    private static void deleteBook() throws LibraryException {
        String id = in.readRequired("Book ID to delete: ");
        library.deleteBook(id);
        System.out.println("Book deleted.");
    }

    private static void viewAllBooks() {
        if (library.getBooks().isEmpty()) { System.out.println("No books in the library."); return; }
        Book.printHeader();
        library.getBooks().stream()
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .forEach(Book::display);
    }

    private static void registerMember() throws LibraryException {
        System.out.println("Member type: 1. Student   2. Faculty");
        String type = in.readInt("Choose: ", 1, 2) == 1 ? "STUDENT" : "FACULTY";
        String id = in.readRequired("Member ID: ");
        String name = in.readRequired("Name: ");
        String contact = in.readRequired("Contact (email/phone): ");
        String aff = in.readRequired(type.equals("STUDENT") ? "Course: " : "Department: ");
        System.out.println("Registered: " + library.registerMember(type, id, name, contact, aff));
    }

    private static void updateMember() throws LibraryException {
        Member m = library.findMember(in.readRequired("Member ID to update: "));
        System.out.println("Leave a field blank to keep the current value.");
        String name = in.readLine("Name [" + m.getName() + "]: ");
        String contact = in.readLine("Contact [" + m.getContact() + "]: ");
        String aff = in.readLine("Course/Department [" + m.getAffiliation() + "]: ");
        library.updateMember(m.getId(), name, contact, aff);
        System.out.println("Member updated.");
    }

    private static void deleteMember() throws LibraryException {
        library.deleteMember(in.readRequired("Member ID to delete: "));
        System.out.println("Member deleted.");
    }

    private static void viewAllMembers() {
        if (library.getMembers().isEmpty()) { System.out.println("No members registered."); return; }
        for (Member m : library.getMembers()) m.displayInfo();   // polymorphic call
    }

    private static void issueBook() throws LibraryException {
        String bookId = in.readRequired("Book ID: ");
        String memberId = in.readRequired("Member ID: ");
        LocalDate date = in.readDate("Issue date yyyy-MM-dd (blank = today): ");
        Transaction t = library.issueBook(bookId, memberId, date);
        System.out.println("Issued. Transaction #" + t.getId() + " | Due date: " + t.getDueDate());
    }

    private static void returnBook() throws LibraryException {
        String bookId = in.readRequired("Book ID: ");
        String memberId = in.readRequired("Member ID: ");
        LocalDate date = in.readDate("Return date yyyy-MM-dd (blank = today): ");
        Transaction t = library.returnBook(bookId, memberId, date);
        System.out.println("Returned on " + t.getReturnDate() + ".");
        if (t.getFine() > 0)
            System.out.println("Overdue by " + t.daysOverdue(date) + " day(s). FINE TO COLLECT: Rs." + t.getFine());
        else
            System.out.println("Returned on time. No fine.");
    }

    private static void showOverdue() {
        LocalDate today = LocalDate.now();
        List<Transaction> overdue = library.getOverdueTransactions(today);
        if (overdue.isEmpty()) { System.out.println("No overdue books."); return; }
        System.out.printf("%-6s %-28s %-16s %-12s %-6s %s%n", "Tx#", "Book", "Member", "Due", "Days", "Fine");
        System.out.println("-".repeat(80));
        int total = 0;
        for (Transaction t : overdue) {
            Book b = library.getBookOrNull(t.getBookId());
            Member m = library.getMemberOrNull(t.getMemberId());
            int fine = library.currentFine(t, today);
            total += fine;
            System.out.printf("%-6d %-28.28s %-16.16s %-12s %-6d Rs.%d%n", t.getId(),
                    b == null ? t.getBookId() : b.getTitle(),
                    m == null ? t.getMemberId() : m.getName(),
                    t.getDueDate(), t.daysOverdue(today), fine);
        }
        System.out.println("Total pending fines: Rs." + total);
    }

    private static void showReport() throws LibraryException {
        LocalDate today = LocalDate.now();
        String report = ReportGenerator.generate(library, today);
        System.out.println(report);
        if (in.readLine("Save report to file? (y/n): ").equalsIgnoreCase("y")) {
            String name = "report_" + today + ".txt";
            library.saveReport(name, report);
            System.out.println("Saved to data/reports/" + name);
        }
    }

    // ================= search (shared) =================
    private static void searchBooks() {
        System.out.println("Search by: 1. ID  2. Title  3. Author  4. Category  5. Any keyword");
        int c = in.readInt("Choose: ", 1, 5);
        String keyword = in.readRequired("Search for: ");
        Library.SearchField field = Library.SearchField.values()[c - 1];
        List<Book> results = library.searchBooks(field, keyword);
        if (results.isEmpty()) { System.out.println("No matching books found."); return; }
        Book.printHeader();
        results.forEach(Book::display);
    }

    // ================= student / member menu =================
    private static void memberMenu(Member m) {
        while (true) {
            System.out.println("""

                    ---------- MEMBER MENU ----------
                    1. Search books
                    2. View all books & availability
                    3. My borrowing details
                    0. Logout""");
            int choice = in.readInt("Choose: ", 0, 3);
            if (choice == 0) return;
            switch (choice) {
                case 1 -> searchBooks();
                case 2 -> viewAllBooks();
                case 3 -> showMyDetails(m);
            }
        }
    }

    private static void showMyDetails(Member m) {
        LocalDate today = LocalDate.now();
        m.displayInfo();
        List<Transaction> active = library.activeLoansOf(m.getId());
        System.out.println("\nCurrently borrowed (" + active.size() + "/" + m.getBorrowLimit() + "):");
        if (active.isEmpty()) System.out.println("  none");
        int pending = 0;
        for (Transaction t : active) {
            Book b = library.getBookOrNull(t.getBookId());
            int fine = library.currentFine(t, today);
            pending += fine;
            System.out.printf("  %-30.30s issued %s | due %s %s%n",
                    b == null ? t.getBookId() : b.getTitle(), t.getIssueDate(), t.getDueDate(),
                    t.isOverdue(today) ? "** OVERDUE " + t.daysOverdue(today) + " day(s), fine Rs." + fine : "");
        }
        System.out.println("Fine accrued on current books: Rs." + pending);

        System.out.println("\nHistory:");
        List<Transaction> history = library.historyOf(m.getId());
        if (history.isEmpty()) System.out.println("  none");
        for (Transaction t : history) {
            Book b = library.getBookOrNull(t.getBookId());
            System.out.printf("  #%-3d %-30.30s %s -> %s  fine Rs.%d%n", t.getId(),
                    b == null ? t.getBookId() : b.getTitle(), t.getIssueDate(),
                    t.isReturned() ? t.getReturnDate() : "not returned", library.currentFine(t, today));
        }
    }
}
