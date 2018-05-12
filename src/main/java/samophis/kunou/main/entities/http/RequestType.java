package samophis.kunou.main.entities.http;

/**
 * Describes the request types/methods that the Discord REST API officially supports.
 *
 * @author SamOphis
 * @since 0.1
 */

public enum RequestType {
    /** A HTTP GET request with no body attached -- used to fetch entities. */
    GET,
    /** A HTTP POST request with a body attached -- used to create entities. */
    POST,
    /** A HTTP PUT request with a body attached -- used to create or update entities at a specific location. */
    PUT,
    /** A HTTP PATCH request with a body attached -- used to modify existing entities. */
    PATCH,
    /** A HTTP DELETE request with no body attached -- used to delete entities. */
    DELETE
}