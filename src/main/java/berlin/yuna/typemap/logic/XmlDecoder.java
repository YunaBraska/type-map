package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.TypeList;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

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
        try {
            return toTypeMap(documentBuilder().parse(xml));
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
        try {
            return toTypeMap(documentBuilder().parse(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()))));
        } catch (final Exception ignored) {
            return new TypeList();
        }
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

        return new TypeList().addReturn(new Pair<>(node.getNodeName(), parseXmlNode(node)));
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
                if (i == 0 || hasText(text))
                    result.add(text != null ? text.trim() : "");
            }
        }
    }

    private XmlDecoder() {
        // static util class
    }
}

