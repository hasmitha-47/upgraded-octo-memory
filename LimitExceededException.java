/** Thrown when a member tries to borrow more books than their limit allows. */
public class LimitExceededException extends LibraryException {
    public LimitExceededException(String message) { super(message); }
}
