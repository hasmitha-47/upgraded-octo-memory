/** Thrown when user input fails validation (blank, malformed, out of range...). */
public class InvalidInputException extends LibraryException {
    public InvalidInputException(String message) { super(message); }
}
