import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Builds the basic library activity report. */
public class ReportGenerator {

    public static String generate(Library lib, LocalDate today) {
        StringBuilder sb = new StringBuilder();
        List<Transaction> all = lib.getTransactions();

        int totalCopies = lib.getBooks().stream().mapToInt(Book::getTotalCopies).sum();
        int issuedCopies = lib.getBooks().stream().mapToInt(Book::getIssuedCopies).sum();
        List<Transaction> overdue = lib.getOverdueTransactions(today);
        int finesCollected = all.stream().filter(Transaction::isReturned).mapToInt(Transaction::getFine).sum();
        int finesPending = overdue.stream().mapToInt(t -> lib.currentFine(t, today)).sum();

        sb.append("==================================================\n");
        sb.append("        LIBRARY ACTIVITY REPORT  (").append(today).append(")\n");
        sb.append("==================================================\n");
        sb.append(String.format("Book titles          : %d%n", lib.getBooks().size()));
        sb.append(String.format("Total copies         : %d%n", totalCopies));
        sb.append(String.format("Copies issued out    : %d%n", issuedCopies));
        sb.append(String.format("Copies on shelf      : %d%n", totalCopies - issuedCopies));
        sb.append(String.format("Registered members   : %d%n", lib.getMembers().size()));
        sb.append(String.format("Total transactions   : %d%n", all.size()));
        sb.append(String.format("Overdue books        : %d%n", overdue.size()));
        sb.append(String.format("Fines collected      : Rs.%d%n", finesCollected));
        sb.append(String.format("Fines pending        : Rs.%d%n", finesPending));

        sb.append("\n--- Books by category ---\n");
        Map<String, Integer> byCategory = new TreeMap<>();
        for (Book b : lib.getBooks()) byCategory.merge(b.getCategory(), 1, Integer::sum);
        byCategory.forEach((cat, n) -> sb.append(String.format("  %-20s %d%n", cat, n)));

        sb.append("\n--- Most borrowed books ---\n");
        Map<String, Long> counts = all.stream()
                .collect(Collectors.groupingBy(Transaction::getBookId, Collectors.counting()));
        if (counts.isEmpty()) sb.append("  (no borrowing activity yet)\n");
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> {
                    Book b = lib.getBookOrNull(e.getKey());
                    sb.append(String.format("  %-30.30s %d time(s)%n",
                            b == null ? e.getKey() + " (deleted)" : b.getTitle(), e.getValue()));
                });

        sb.append("\n--- Overdue books ---\n");
        if (overdue.isEmpty()) sb.append("  None\n");
        for (Transaction t : overdue) {
            Book b = lib.getBookOrNull(t.getBookId());
            Member m = lib.getMemberOrNull(t.getMemberId());
            sb.append(String.format("  %-25.25s | %-15.15s | due %s | %d day(s) late | Rs.%d%n",
                    b == null ? t.getBookId() : b.getTitle(),
                    m == null ? t.getMemberId() : m.getName(),
                    t.getDueDate(), t.daysOverdue(today), lib.currentFine(t, today)));
        }
        return sb.toString();
    }
}
