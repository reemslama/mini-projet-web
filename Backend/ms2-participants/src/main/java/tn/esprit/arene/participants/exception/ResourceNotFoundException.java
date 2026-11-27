package tn.esprit.arene.participants.exception;

/** -> HTTP 404 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
