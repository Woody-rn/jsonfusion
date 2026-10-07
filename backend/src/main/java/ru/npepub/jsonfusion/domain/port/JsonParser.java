package ru.npepub.jsonfusion.domain.port;

import ru.npepub.jsonfusion.domain.model.JsonDocument;

import java.io.InputStream;

/**
 * Port for parsing JSON input into a {@link JsonDocument}.
 * Implementations must validate that the input is a JSON array of objects.
 */
public interface JsonParser {

    /**
     * Parses the input stream into a {@link JsonDocument}.
     *
     * @param input JSON input stream
     * @return parsed document
     * @throws ru.npepub.jsonfusion.domain.exception.InvalidJsonException
     *         if the input is not a valid JSON array of objects
     */
    JsonDocument parse(InputStream input);
}