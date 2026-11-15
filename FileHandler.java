import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** File Handling: stores books, members and transactions in pipe-separated text files. */
public class FileHandler {
    private static final String SEP = "|";
    private static final String NONE = "-";

    private final Path dir;
    private final Path booksFile;
    private final Path membersFile;
    private final Path transactionsFile;

    public FileHandler(String directory) {
        this.dir = Paths.get(directory);
        this.booksFile = dir.resolve("books.txt");
        this.membersFile = dir.resolve("members.txt");
        this.transactionsFile = dir.resolve("transactions.txt");
    }

    // ---------- saving ----------
    public void saveBooks(Collection<Book> books) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Book b : books) {
            lines.add(String.join(SEP, b.getId(), b.getTitle(), b.getAuthor(), b.getCategory(),
                    String.valueOf(b.getTotalCopies()), String.valueOf(b.getAvailableCopies())));
        }
        write(booksFile, lines);
    }

    public void saveMembers(Collection<Member> members) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Member m : members) {
            lines.add(String.join(SEP, m.getType(), m.getId(), m.getName(),
                    m.getContact(), m.getAffiliation()));
        }
        write(membersFile, lines);
    }

    public void saveTransactions(Collection<Transaction> txs) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Transaction t : txs) {
            lines.add(String.join(SEP, String.valueOf(t.getId()), t.getBookId(), t.getMemberId(),
                    t.getIssueDate().toString(), t.getDueDate().toString(),
                    t.isReturned() ? t.getReturnDate().toString() : NONE,
                    String.valueOf(t.getFine())));
        }
        write(transactionsFile, lines);
    }

    public void saveReport(String fileName, String text) throws IOException {
        Path reports = dir.resolve("reports");
        Files.createDirectories(reports);
        Files.writeString(reports.resolve(fileName), text);
    }

    // ---------- loading ----------
    public List<Book> loadBooks() throws IOException {
        List<Book> list = new ArrayList<>();
        for (String[] p : read(booksFile, 6)) {
            try {
                list.add(new Book(p[0], p[1], p[2], p[3],
                        Integer.parseInt(p[4]), Integer.parseInt(p[5])));
            } catch (NumberFormatException e) {
                System.err.println("Skipping corrupt book record: " + String.join(SEP, p));
            }
        }
        return list;
    }

    public List<Member> loadMembers() throws IOException {
        List<Member> list = new ArrayList<>();
        for (String[] p : read(membersFile, 5)) {
            if (p[0].equals("FACULTY")) list.add(new Faculty(p[1], p[2], p[3], p[4]));
            else                        list.add(new Student(p[1], p[2], p[3], p[4]));
        }
        return list;
    }

    public List<Transaction> loadTransactions() throws IOException {
        List<Transaction> list = new ArrayList<>();
        for (String[] p : read(transactionsFile, 7)) {
            try {
                LocalDate returned = p[5].equals(NONE) ? null : LocalDate.parse(p[5]);
                list.add(new Transaction(Integer.parseInt(p[0]), p[1], p[2],
                        LocalDate.parse(p[3]), LocalDate.parse(p[4]),
                        returned, Integer.parseInt(p[6])));
            } catch (RuntimeException e) {
                System.err.println("Skipping corrupt transaction record: " + String.join(SEP, p));
            }
        }
        return list;
    }

    // ---------- helpers ----------
    private void write(Path file, List<String> lines) throws IOException {
        Files.createDirectories(dir);
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            for (String line : lines) {
                w.write(line);
                w.newLine();
            }
        }
    }

    private List<String[]> read(Path file, int expectedFields) throws IOException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(file)) return rows;          // first run: nothing saved yet
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length == expectedFields) rows.add(parts);
                else System.err.println("Skipping malformed line in " + file.getFileName() + ": " + line);
            }
        }
        return rows;
    }
}
