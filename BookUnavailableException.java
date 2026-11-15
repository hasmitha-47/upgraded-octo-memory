/** Thrown when every copy of a book is already issued. */
public class BookUnavailableException extends LibraryException {
    public BookUnavailableException(String message) { super(message); }
}
