/** Thrown when a book, member or transaction cannot be found. */
public class RecordNotFoundException extends LibraryException {
    public RecordNotFoundException(String message) { super(message); }
}
