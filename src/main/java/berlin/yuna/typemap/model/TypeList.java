package berlin.yuna.typemap.model;


import berlin.yuna.typemap.logic.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

/**
 * {@link TypeList} is a specialized implementation of {@link ArrayList} that offers enhanced
 * functionality for type-safe data retrieval and manipulation. It is designed for
 * high-performance type conversion while being native-ready for GraalVM. The {@link TypeList}
 * class provides methods to retrieve data in various forms (single objects, collections,
 * arrays, or maps) while ensuring type safety without the need for reflection.
 */
public class TypeList extends ArrayList<Object> implements TypeListI<TypeList> {

    /**
     * Default constructor for creating an empty {@link TypeList}.
     */
    public TypeList() {
        this((Collection<?>) null);
    }

    /**
     * Constructs a new {@link TypeList} of the specified json.
     *
     * @deprecated use {@link #listOf(String)} for JSON/XML input
     */
    @Deprecated(forRemoval = true)
    public TypeList(final String jsonOrXml) {
        this(JsonDecoder.listOf(jsonOrXml));
    }

    /**
     * Constructs a new {@link TypeList} with the same mappings as the specified map.
     *
     * @param map The initial map to copy mappings from, can be null.
     */
    public TypeList(final Collection<?> map) {
        ofNullable(map).ifPresent(super::addAll);
    }

    /**
     * Parses JSON or XML into a {@link TypeList}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null, blank, or cannot be parsed.
     */
    public static TypeList listOf(final String jsonOrXml) {
        return JsonDecoder.listOf(jsonOrXml);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from a {@link CharSequence}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null, blank, or cannot be parsed.
     */
    public static TypeList listOf(final CharSequence jsonOrXml) {
        return JsonDecoder.listOf(jsonOrXml);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from an {@link InputStream}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final InputStream jsonOrXml) {
        return JsonDecoder.listOf(jsonOrXml);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from an {@link InputStream}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Uses the provided charset for JSON content. Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final InputStream jsonOrXml, final Charset charset) {
        return JsonDecoder.listOf(jsonOrXml, charset);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from a {@link Path}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final Path jsonOrXml) {
        return JsonDecoder.listOf(jsonOrXml);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from a {@link Path}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Uses the provided charset for JSON content. Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final Path jsonOrXml, final Charset charset) {
        return JsonDecoder.listOf(jsonOrXml, charset);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from a {@link File}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final File jsonOrXml) {
        return JsonDecoder.listOf(jsonOrXml);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from a {@link File}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Uses the provided charset for JSON content. Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final File jsonOrXml, final Charset charset) {
        return JsonDecoder.listOf(jsonOrXml, charset);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from a {@link URI}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final URI jsonOrXml) {
        return JsonDecoder.listOf(jsonOrXml);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from a {@link URI}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Uses the provided charset for JSON content. Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final URI jsonOrXml, final Charset charset) {
        return JsonDecoder.listOf(jsonOrXml, charset);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from a {@link URL}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final URL jsonOrXml) {
        return JsonDecoder.listOf(jsonOrXml);
    }

    /**
     * Parses JSON or XML into a {@link TypeList} from a {@link URL}.
     * JSON arrays and XML return the parsed list; objects and primitive values are wrapped into a single-item list.
     * Uses the provided charset for JSON content. Returns an empty list when input is null or cannot be parsed.
     */
    public static TypeList listOf(final URL jsonOrXml, final Charset charset) {
        return JsonDecoder.listOf(jsonOrXml, charset);
    }

    /**
     * Parses JSON or XML string into a {@link TypeList}.
     *
     * @deprecated use {@link #listOf(String)} to accept JSON or XML
     */
    @Deprecated(forRemoval = true)
    public static TypeList fromJson(final String jsonOrXml) {
        return JsonDecoder.listOf(jsonOrXml);
    }

    /**
     * Parses JSON or XML CharSequence into a {@link TypeList}.
     *
     * @deprecated use {@link #listOf(CharSequence)} to accept JSON or XML
     */
    @Deprecated(forRemoval = true)
    public static TypeList fromJson(final CharSequence jsonOrXml) {
        return jsonOrXml == null
            ? new TypeList()
            : fromJson(jsonOrXml.toString());
    }

    /**
     * Parses JSON or XML stream into a {@link TypeList}.
     *
     * @deprecated use {@link #listOf(InputStream)} to accept JSON or XML
     */
    @Deprecated(forRemoval = true)
    public static TypeList fromJson(final InputStream jsonOrXml) {
        return fromJson(readStream(jsonOrXml));
    }

    /**
     * Parses JSON or XML stream into a {@link TypeList}.
     *
     * @deprecated use {@link #listOf(InputStream)} or {@link #listOf(InputStream, Charset)}
     */
    @Deprecated(forRemoval = true)
    public static TypeList from(final InputStream input) {
        return from(input, UTF_8);
    }

    /**
     * Parses JSON or XML stream into a {@link TypeList} with the given charset for JSON content.
     *
     * @deprecated use {@link #listOf(InputStream, Charset)}
     */
    @Deprecated(forRemoval = true)
    public static TypeList from(final InputStream input, final Charset charset) {
        return input == null ? new TypeList() : JsonDecoder.listOf(input, charset);
    }


    /**
     * Parses XML string into a {@link TypeList}.
     *
     * @deprecated use {@link #listOf(String)} to accept JSON or XML
     */
    @Deprecated(forRemoval = true)
    public static TypeList fromXml(final String xml) {
        return new TypeList(XmlDecoder.xmlTypeOf(xml));
    }

    /**
     * Parses XML CharSequence into a {@link TypeList}.
     *
     * @deprecated use {@link #listOf(CharSequence)} to accept JSON or XML
     */
    @Deprecated(forRemoval = true)
    public static TypeList fromXml(final CharSequence xml) {
        return xml == null
            ? new TypeList()
            : fromXml(xml.toString());
    }

    /**
     * Parses XML file into a {@link TypeList}.
     *
     * @deprecated use {@link #listOf(Path)} to accept JSON or XML
     */
    @Deprecated(forRemoval = true)
    public static TypeList fromXml(final Path xml) {
        return fromXml(readPath(xml));
    }

    /**
     * Parses XML stream into a {@link TypeList}.
     *
     * @deprecated use {@link #listOf(InputStream)} to accept JSON or XML
     */
    @Deprecated(forRemoval = true)
    public static TypeList fromXml(final InputStream xml) {
        return fromXml(readStream(xml));
    }

    /**
     * Parses CLI args string into a {@link TypeList}.
     */
    public static TypeList fromArgs(final String args) {
        if (args == null || args.isBlank())
            return new TypeList();
        return new TypeList(Collections.singletonList(ArgsDecoder.argsOf(args)));
    }

    /**
     * Parses CLI args array into a {@link TypeList}.
     */
    public static TypeList fromArgs(final String[] args) {
        if (args == null || args.length == 0)
            return new TypeList();
        return new TypeList(Collections.singletonList(ArgsDecoder.argsOf(String.join(" ", args))));
    }

    public static TypeList fromArgs(final CharSequence args) {
        return args == null ? new TypeList() : fromArgs(args.toString());
    }

    public static TypeList fromArgs(final Path args) {
        try (InputStream in = Files.newInputStream(args)) {
            return fromArgs(in);
        } catch (final IOException ignored) {
            return new TypeList();
        }
    }

    public static TypeList fromArgs(final InputStream args) {
        return fromArgs(readStream(args));
    }

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     * @return the updated {@link TypeList} instance for chaining.
     */
    @Override
    public TypeList addR(final int index, final Object value) {
        if (index >= 0 && index < this.size()) {
            super.add(index, value);
        } else {
            super.add(value);
        }
        return this;
    }

    /**
     * Adds the specified value
     *
     * @param index the index whose associated value is to be returned.
     * @param value the value to be added
     * @return the updated {@link TypeList} instance for chaining.
     */
    public TypeList addR(final Object index, final Object value) {
        if (index == null) {
            super.add(value);
        } else if (index instanceof Number) {
            this.addR(((Number) index).intValue(), value);
        }
        return this;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     */
    @Override
    public Object get(final int index) {
        return index >= 0 && index < this.size() ? super.get(index) : null;
    }

    public String toJson() {
        return JsonEncoder.toJson(this);
    }

    public String toXML() {
        return XmlEncoder.toXml(this);
    }

    private static String readPath(final Path path) {
        if (path == null) {
            return "";
        }
        try {
            return Files.readString(path, UTF_8);
        } catch (final IOException ignored) {
            return "";
        }
    }

    private static String readStream(final InputStream stream) {
        if (stream == null) {
            return "";
        }
        try (InputStream in = stream) {
            return new String(in.readAllBytes(), UTF_8);
        } catch (final IOException ignored) {
            return "";
        }
    }
}
