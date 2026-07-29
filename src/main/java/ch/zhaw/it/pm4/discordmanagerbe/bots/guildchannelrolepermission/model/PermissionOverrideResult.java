package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.model;

import java.util.Optional;

/**
 * Represents the result of a permission override operation.
 * @param <T> the type of data that can be returned in the result
 */
public class PermissionOverrideResult<T> {

    /**
     * Indicates whether the operation was successful.
     */
    private final boolean success;

    /**
     * A message providing additional information about the result.
     */
    private final String message;

    /**
     * The data returned by the operation, if any.
     */
    private final T data;

    /**
     * An exception that occurred during the operation, if any.
     */
    private final Exception exception;

    /**
     * Private constructor to create a PermissionOverrideResult instance.
     * @param success indicates if the operation was successful
     * @param message provides additional information about the result
     * @param data the data returned by the operation, if any
     * @param exception the exception that occurred during the operation, if any
     */
    private PermissionOverrideResult(boolean success, String message, T data, Exception exception) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.exception = exception;
    }

    /**
     * Creates a successful PermissionOverrideResult with no data.
     * @return a successful PermissionOverrideResult instance
     * @param <T> the type of data that can be returned in the result
     */
    public static <T> PermissionOverrideResult<T> success() {
        return new PermissionOverrideResult<>(true, "Operation completed successfully", null, null);
    }

    /**
     * Creates a successful PermissionOverrideResult with data.
     * @param data the data to be included in the result
     * @return a successful PermissionOverrideResult instance with data
     * @param <T> the type of data that can be returned in the result
     */
    public static <T> PermissionOverrideResult<T> success(T data) {
        return new PermissionOverrideResult<>(true, "Operation completed successfully", data, null);
    }

    /**
     * Creates a failed PermissionOverrideResult with a message.
     * @param message the message describing the failure
     * @return a failed PermissionOverrideResult instance
     * @param <T> the type of data that can be returned in the result
     */
    public static <T> PermissionOverrideResult<T> failure(String message) {
        return new PermissionOverrideResult<>(false, message, null, null);
    }

    /**
     * Creates a failed PermissionOverrideResult with a message and an exception.
     * @param message the message describing the failure
     * @param exception the exception that occurred during the operation
     * @return a failed PermissionOverrideResult instance with an exception
     * @param <T> the type of data that can be returned in the result
     */
    public static <T> PermissionOverrideResult<T> failure(String message, Exception exception) {
        return new PermissionOverrideResult<>(false, message, null, exception);
    }

    /**
     * Checks if the operation was successful.
     * @return true if the operation was successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the message associated with the result.
     * @return the message providing additional information about the result
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the data returned by the operation, if any.
     * @return an Optional containing the data if present, or an empty Optional if no data was returned
     */
    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    /**
     * Gets the data returned by the operation, or throws an exception if the operation was not successful or no data is available.
     * @return the data if the operation was successful and data is available
     */
    public T getDataOrThrow() {
        if (success && data != null) {
            return data;
        }
        
        String errorMsg = success ? "No data available" : message;
        throw exception instanceof RuntimeException ?
                (RuntimeException) exception : new RuntimeException(errorMsg, exception);
    }

    /**
     * Gets the exception that occurred during the operation, if any.
     * @return an Optional containing the exception if present, or an empty Optional if no exception occurred
     */
    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }
}