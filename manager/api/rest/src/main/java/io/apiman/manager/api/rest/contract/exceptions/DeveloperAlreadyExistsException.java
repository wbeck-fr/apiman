package io.apiman.manager.api.rest.contract.exceptions;

public class DeveloperAlreadyExistsException extends AbstractAlreadyExistsException {

    private static final long serialVersionUID = -272284297907454867L;

    /**
     * Constructor
     */
    public DeveloperAlreadyExistsException() {
    }

    /**
     * Constructor
     *
     * @param message the message
     */
    public DeveloperAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * @return the errorCode
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.DEVELOPER_ALREADY_EXISTS;
    }

    /**
     * @return the moreInfo
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.DEVELOPER_ALREADY_EXISTS_INFO;
    }
}
