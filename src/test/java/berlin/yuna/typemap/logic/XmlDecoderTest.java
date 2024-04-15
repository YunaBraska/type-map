package berlin.yuna.typemap.logic;

import berlin.yuna.typemap.model.Pair;
import berlin.yuna.typemap.model.TypeList;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


class XmlDecoderTest {

    final Path pomPath = Paths.get(System.getProperty("user.dir"), "pom.xml");

    @Test
    void decodeXml() throws IOException {
        final TypeList result = XmlDecoder.xmlTypeOf(pomPath.toFile());
        result.get(String.class, "project", "description");
        assertThat(result.get(String.class, "project", "groupId")).isEqualTo("berlin.yuna");
        assertThat(result.get(String.class, "project", "artifactId")).isEqualTo("type-map");
        assertThat(result.get(Integer.class, "project", "properties", "java-version")).isEqualTo(8);
        assertThat(result.getList(Pair.class, "project", "dependencies")).hasSize(3);

        assertThat(XmlDecoder.xmlTypeOf(Files.newInputStream(pomPath))).isEqualTo(result);
        assertThat(XmlDecoder.xmlTypeOf(new String(Files.readAllBytes(pomPath)))).isEqualTo(result);

        assertThat(XmlDecoder.xmlTypeOf((File) null)).isEqualTo(new TypeList());
        assertThat(XmlDecoder.xmlTypeOf((String) null)).isEqualTo(new TypeList());
        assertThat(XmlDecoder.xmlTypeOf((InputStream) null)).isEqualTo(new TypeList());

        XmlDecoder.xmlTypeOf(XmlEncoder.toXml(result));
    }

    @Test
    void encodeXml() {
        final TypeList xml1 = XmlDecoder.xmlTypeOf(pomPath.toFile());
        final String encode1 = XmlEncoder.toXml(xml1);

        final TypeList xml2 = XmlDecoder.xmlTypeOf(encode1);
        final String encode2 = XmlEncoder.toXml(xml2);

        assertThat(encode1).isNotNull().isNotBlank().isNotEmpty().isEqualTo(encode2);
        assertThat(xml1).isEqualTo(xml2);
    }
}
