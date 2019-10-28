package io.apiman.manager.api.rest.contract.exceptions;

/**
 * Thrown when trying to get, update, or delete a developer that does not exist.
 */
public class DeveloperNotFoundException extends AbstractNotFoundException {
    private static final long serialVersionUID = -1864829072317282670L;

    /**
     * Constructor
     */
    public DeveloperNotFoundException() {
    }

    /**
     * Constructor
     *
     * @param message the exception message
     */
    public DeveloperNotFoundException(String message) {
        super(message);
    }

    /**
     * @return the errorCode
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.DEVELOPER_NOT_FOUND;
    }

    /**
     * @return the moreInfo
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.DEVELOPER_NOT_FOUND_INFO;
    }
}
