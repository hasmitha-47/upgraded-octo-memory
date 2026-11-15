/** Thrown when adding a record whose ID already exists. */
public class DuplicateRecordException extends LibraryException {
    public DuplicateRecordException(String message) { super(message); }
}
