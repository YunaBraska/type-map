package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.TypeList;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static berlin.yuna.typemap.logic.ArgsDecoder.hasText;

/**
 * The {@code XmlDecoder} class provides functionality to parse XML content from various sources and
 * convert them into a {@code TypeList}, which is a custom structure for handling typed XML data.
 */
public class XmlDecoder {

    /**
     * Parses an XML file into a {@code TypeList}.
     *
     * @param xml the XML file to parse.
     * @return a {@code TypeList} representing the XML structure, or an empty {@code TypeList} if parsing fails.
     */
    public static TypeList xmlTypeOf(final File xml) {
        if (xml == null)
            return new TypeList();
        try (final InputStream fileStream = Files.newInputStream(xml.toPath())) {
            return xmlTypeOf(fileStream);
        } catch (final Exception ignored) {
            return new TypeList();
        }
    }

    /**
     * Parses an XML input stream into a {@code TypeList}.
     *
     * @param xml the input stream containing XML data.
     * @return a {@code TypeList} representing the XML structure, or an empty {@code TypeList} if parsing fails.
     */
    public static TypeList xmlTypeOf(final InputStream xml) {
        if (xml == null)
            return new TypeList();
        try (final Stream<Pair<String, Object>> stream = streamXmlObject(xml)) {
            final TypeList result = new TypeList();
            stream.forEach(result::add);
            return result;
        } catch (final Exception ignored) {
            return new TypeList();
        }
    }

    /**
     * Parses a string containing XML data into a {@code TypeList}.
     *
     * @param xml the string containing XML data.
     * @return a {@code TypeList} representing the XML structure, or an empty {@code TypeList} if parsing fails.
     */
    public static TypeList xmlTypeOf(final String xml) {
        if (xml == null)
            return new TypeList();
        return xmlTypeOf(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset())));
    }

    /**
     * Parses an XML node and extracts its children and attributes into a {@code TypeList}.
     *
     * @param node the XML node to parse.
     * @return an object representing the node's data, which could be text, a {@code Pair}, or a {@code TypeList}.
     */
    public static Object parseXmlNode(final Node node) {
        // Document node as well?
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            final TypeList result = new TypeList();
            extractChildren(result, node);
            extractAttributes(result, node);
            return result;
        }
        return null;
    }

    /**
     * Creates a {@code DocumentBuilder} instance with security configurations to prevent XXE attacks.
     *
     * @return a secured {@code DocumentBuilder}.
     * @throws IllegalStateException if a configuration error occurs.
     */
    public static DocumentBuilder documentBuilder() {
        try {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            // Prevent XXE
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setXIncludeAware(false);
            dbFactory.setExpandEntityReferences(false);

            final DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(final SAXParseException exception) {
                    // ignored
                }

                @Override
                public void error(final SAXParseException exception) {
                    // ignored
                }

                @Override
                public void fatalError(final SAXParseException exception) {
                    // ignored
                }
            });
            return documentBuilder;
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts a {@code Document} to a {@code TypeList} by normalizing the root element and parsing it.
     *
     * @param document the XML {@code Document} to convert.
     * @return a {@code TypeList} representing the root element and its hierarchy.
     */
    public static TypeList toTypeMap(final Document document) {
        final Element node = document.getDocumentElement();
        node.normalize();

        return new TypeList().addR(new Pair<>(node.getNodeName(), parseXmlNode(node)));
    }

    /**
     * Extracts attributes from an XML node and adds them to a {@code TypeList} as {@code Pair} objects.
     *
     * @param result the {@code TypeList} to which attributes are added.
     * @param node   the XML node from which attributes are extracted.
     */
    private static void extractAttributes(final TypeList result, final Node node) {
        if (node.hasAttributes()) {
            final NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                final Node attribute = attributes.item(i);
                result.add(new Pair<>("@" + attribute.getNodeName(), attribute.getNodeValue()));
            }
        }
    }

    /**
     * Recursively extracts child nodes from an XML node and adds them to a {@code TypeList}.
     *
     * @param result the {@code TypeList} to which child nodes are added.
     * @param node   the XML node from which children are extracted.
     */
    private static void extractChildren(final TypeList result, final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node item = children.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                result.add(new Pair<>(item.getNodeName(), parseXmlNode(item)));
            } else if (item.getNodeType() == Node.TEXT_NODE) {
                // Process value
                final String text = item.getTextContent();
                if (hasText(text))
                    result.add(text.strip());
            }
        }
    }

    /**
     * Streams top-level XML elements as key/value pairs without materializing a DOM.
     *
     * @param xml     input stream containing XML
     * @param charset optional charset override (otherwise XML prolog/UTF-8 is used)
     * @return stream of top-level element pairs
     */
    public static Stream<Pair<String, Object>> streamXmlObject(final InputStream xml, final Charset charset) {
        if (xml == null)
            return Stream.empty();
        final var reader = charset == null
            ? XmlStreams.reader(xml)
            : XmlStreams.reader(new InputStreamReader(xml, charset));

        final IteratorIterator iterator = new IteratorIterator(reader);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL), false)
            .onClose(() -> XmlStreams.closeQuietly(reader));
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final InputStream xml) {
        return streamXmlObject(xml, null);
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final String xml) {
        return streamXmlObject(xml, Charset.defaultCharset());
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final CharSequence xml, final Charset charset) {
        if (xml == null || xml.toString().isBlank())
            return Stream.empty();
        return streamXmlObject(new ByteArrayInputStream(xml.toString().getBytes(charset)), charset);
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final CharSequence xml) {
        return streamXmlObject(xml, Charset.defaultCharset());
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final File xml, final Charset charset) {
        try {
            return streamXmlObject(Files.newInputStream(xml.toPath()), charset);
        } catch (final Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final File xml) {
        return streamXmlObject(xml, Charset.defaultCharset());
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final java.nio.file.Path xml, final Charset charset) {
        try {
            return streamXmlObject(Files.newInputStream(xml), charset);
        } catch (final Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final java.nio.file.Path xml) {
        return streamXmlObject(xml, Charset.defaultCharset());
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final java.net.URI xml, final Charset charset) {
        try {
            return streamXmlObject(xml.toURL(), charset);
        } catch (final Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final java.net.URI xml) {
        return streamXmlObject(xml, Charset.defaultCharset());
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final java.net.URL xml, final Charset charset) {
        try {
            return streamXmlObject(xml.openStream(), charset);
        } catch (final Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static Stream<Pair<String, Object>> streamXmlObject(final java.net.URL xml) {
        return streamXmlObject(xml, Charset.defaultCharset());
    }

    private static class IteratorIterator implements java.util.Iterator<Pair<String, Object>> {
        private final XMLStreamReader reader;
        private final Deque<ElementFrame> stack = new ArrayDeque<>();
        private Pair<String, Object> next;
        private boolean finished;

        IteratorIterator(final XMLStreamReader reader) {
            this.reader = reader;
            advance();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Pair<String, Object> next() {
            final Pair<String, Object> current = next;
            advance();
            return current;
        }

        private void advance() {
            if (finished) {
                next = null;
                return;
            }
            try {
                while (reader.hasNext()) {
                    final int event = reader.next();
                    if (event == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
                        final ElementFrame frame = new ElementFrame(reader.getLocalName());
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            frame.content.add(new Pair<>("@" + reader.getAttributeLocalName(i), reader.getAttributeValue(i)));
                        }
                        stack.push(frame);
                    } else if (event == javax.xml.stream.XMLStreamConstants.CHARACTERS || event == javax.xml.stream.XMLStreamConstants.CDATA) {
                        if (!stack.isEmpty()) {
                            stack.peek().text.append(reader.getText());
                        }
                    } else if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
                        final ElementFrame frame = stack.pop();
                        final String text = frame.text.toString().trim();
                        if (hasText(text))
                            frame.content.add(text);

                        final Pair<String, Object> completed = new Pair<>(frame.name, frame.content);
                        if (stack.isEmpty()) {
                            next = completed;
                            return;
                        }
                        stack.peek().content.add(completed);
                    }
                }
                finished = true;
                next = null;
            } catch (final Exception e) {
                finished = true;
                throw new UncheckedIOException(new IOException(e));
            }
        }
    }

    private static class ElementFrame {
        final String name;
        final TypeList content = new TypeList();
        final StringBuilder text = new StringBuilder();

        ElementFrame(final String name) {
            this.name = name;
        }
    }

    private static final class XmlStreams {
        private static XMLStreamReader reader(final InputStream in) {
            return createReader(factory(), in);
        }

        private static XMLStreamReader reader(final InputStreamReader in) {
            return createReader(factory(), in);
        }

        private static XMLInputFactory factory() {
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            return factory;
        }

        private static XMLStreamReader createReader(final XMLInputFactory factory, final Object source) {
            try {
                return source instanceof final InputStream input
                    ? factory.createXMLStreamReader(input)
                    : factory.createXMLStreamReader((InputStreamReader) source);
            } catch (final Exception e) {
                throw new UncheckedIOException(new IOException(e));
            }
        }

        private static void closeQuietly(final XMLStreamReader reader) {
            try {
                reader.close();
            } catch (final Exception ignored) {
                // ignored
            }
        }
    }

    private XmlDecoder() {
        // static util class
    }
}
