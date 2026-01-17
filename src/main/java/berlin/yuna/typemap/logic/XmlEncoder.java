package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.TypeList;
import berlin.yuna.typemap.model.TypeMapI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;

import static berlin.yuna.typemap.logic.ArgsDecoder.hasText;
import static berlin.yuna.typemap.logic.XmlDecoder.documentBuilder;

/**
 * The {@code XmlEncoder} class is responsible for converting collections of {@code Pair} objects
 * into XML strings. The class utilizes the DOM API to build and output XML.
 */
public class XmlEncoder {

    /**
     * Converts a collection of {@link Pair} objects into an XML string.
     *
     * @param collection the collection of {@link Pair} objects representing the XML structure.
     * @return a formatted XML string representing the content of the collection.
     * @throws IllegalStateException if an error occurs during XML generation.
     */
    public static String toXml(final Collection<?> collection) {
        if (collection == null || collection.isEmpty() || (!(collection.iterator().next() instanceof Pair))) {
            return "";
        }
        try {
            final Document doc = documentBuilder().newDocument();

            final Pair<?, ?> rootInfo = (Pair<?, ?>) collection.iterator().next();
            final Element rootElement = doc.createElement(rootInfo.key(String.class));
            doc.appendChild(rootElement);
            addNodes(doc, rootElement, rootInfo.value());
            return toXml(doc);
        } catch (final Exception e) {
            throw new IllegalStateException("Error generating XML", e);
        }
    }

    public static String toXmlMap(final TypeMapI<?> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        final java.util.Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();
        final Map.Entry<Object, Object> first = iterator.hasNext() ? iterator.next() : null;
        if (first != null && map.size() == 1 && first.getKey() instanceof String && first.getValue() instanceof Collection<?>) {
            final TypeList content = new TypeList((Collection<?>) first.getValue());
            return toXml(java.util.Collections.singletonList(new Pair<>(first.getKey(), content)));
        }
        final TypeList entries = new TypeList();
        map.forEach((key, value) -> entries.add(new Pair<>(String.valueOf(key), value)));
        return toXml(java.util.Collections.singletonList(new Pair<>("root", entries)));
    }

    public static String toXml(final TypeList list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        if (list.get(0) instanceof Pair<?, ?>) {
            return toXml((Collection<?>) list);
        }
        final TypeList entries = new TypeList();
        for (final Object item : list) {
            entries.add(new Pair<>("item", item));
        }
        return toXml(java.util.Collections.singletonList(new Pair<>("root", entries)));
    }

    /**
     * Helper method to recursively add nodes to the document.
     *
     * @param doc           the document to which nodes are added.
     * @param parentElement the parent element to which child nodes are added.
     * @param value         the value to process, which can be a {@code Collection} or a {@code String}.
     */
    private static void addNodes(final Document doc, final Element parentElement, final Object value) {
        if (value instanceof Collection) {
            final Collection<?> list = (Collection<?>) value;
            for (final Object item : list) {
                if (item instanceof Pair) {
                    final Pair<?, ?> pair = (Pair<?, ?>) item;
                    final String tag = pair.key(String.class);
                    if (tag != null && tag.startsWith("@")) {
                        // It's an attribute
                        parentElement.setAttribute(tag.substring(1), pair.value().toString());
                    } else if (tag != null) {
                        // It's a nested element
                        final Element child = doc.createElement(tag);
                        addNodes(doc, child, pair.value());
                        parentElement.appendChild(child);
                    }
                } else {
                    // It's text content
                    final String text = TypeConverter.convertObj(item, String.class);
                    if (hasText(text)) {
                        parentElement.setTextContent(text);
                    }
                }
            }
        }
    }

    /**
     * Converts the DOM document into an XML string.
     *
     * @param doc the DOM document to convert.
     * @return the XML string.
     * @throws TransformerException if an error occurs during the transformation.
     */
    private static String toXml(final Document doc) throws TransformerException {
        final Transformer transformer = transformerFactory();
        final DOMSource source = new DOMSource(doc);
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }

    /**
     * Creates and configures a new XML transformer.
     *
     * @return a configured {@link Transformer}.
     */
    private static Transformer transformerFactory() {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            // Disable external entity processing
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
            transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");

            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            return transformer;
        } catch (final TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private XmlEncoder() {
        // static util class
    }
}
