/** A book record. Copy counts can only change through controlled methods (Encapsulation). */
public class Book {
    private final String id;
    private String title;
    private String author;
    private String category;
    private int totalCopies;
    private int availableCopies;

    public Book(String id, String title, String author, String category,
                int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public String getId()             { return id; }
    public String getTitle()          { return title; }
    public String getAuthor()         { return author; }
    public String getCategory()       { return category; }
    public int getTotalCopies()       { return totalCopies; }
    public int getAvailableCopies()   { return availableCopies; }
    public int getIssuedCopies()      { return totalCopies - availableCopies; }
    public boolean isAvailable()      { return availableCopies > 0; }

    public void setTitle(String title)       { this.title = title; }
    public void setAuthor(String author)     { this.author = author; }
    public void setCategory(String category) { this.category = category; }

    /** Changes total copies while keeping the number currently issued unchanged. */
    public void setTotalCopies(int newTotal) {
        int issued = getIssuedCopies();
        if (newTotal < issued)
            throw new IllegalArgumentException("Total copies cannot be less than copies currently issued (" + issued + ").");
        this.totalCopies = newTotal;
        this.availableCopies = newTotal - issued;
    }

    public void issueCopy() {
        if (availableCopies <= 0) throw new IllegalStateException("No copies available.");
        availableCopies--;
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) availableCopies++;
    }

    public void display() {
        System.out.printf("%-8s %-30.30s %-20.20s %-14.14s %d/%d  %s%n",
                id, title, author, category, availableCopies, totalCopies,
                isAvailable() ? "Available" : "Issued out");
    }

    public static void printHeader() {
        System.out.printf("%-8s %-30s %-20s %-14s %-5s %s%n",
                "ID", "Title", "Author", "Category", "Avail", "Status");
        System.out.println("-".repeat(88));
    }

    @Override
    public String toString() { return id + " - " + title + " by " + author; }
}
