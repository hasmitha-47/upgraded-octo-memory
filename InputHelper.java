import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Reads and validates console input, handling bad entries with Exception Handling. */
public class InputHelper {
    private final Scanner scanner;

    public InputHelper(Scanner scanner) { this.scanner = scanner; }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) throw new NoSuchElementException("Input ended.");
        return scanner.nextLine().trim();
    }

    public String readRequired(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) return s;
            System.out.println("  ! This field cannot be empty.");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(readLine(prompt));
                if (n >= min && n <= max) return n;
                System.out.println("  ! Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ! That is not a valid number.");
            }
        }
    }

    /** Blank input returns -1 (meaning "keep current value"). */
    public int readOptionalPositiveInt(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) return -1;
            try {
                int n = Integer.parseInt(s);
                if (n > 0) return n;
                System.out.println("  ! Enter a positive number or leave blank.");
            } catch (NumberFormatException e) {
                System.out.println("  ! That is not a valid number.");
            }
        }
    }

    /** Blank input returns today's date. */
    public LocalDate readDate(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) return LocalDate.now();
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("  ! Use the format yyyy-MM-dd (e.g. 2026-11-05).");
            }
        }
    }
}
