package berlin.yuna.typemap.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static berlin.yuna.typemap.logic.JsonEncoder.toJson;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class JsonEncoderTest {

    @BeforeEach
    void setUp() {
        System.getProperties().setProperty("user.timezone", "UTC");
        TimeZone.setDefault(null);
    }

    @Test
    void toJsonTest() {
        final Map<Integer, Date> inputMap = new HashMap<>();
        inputMap.put(2, new Date(1800000000000L));
        final Map<String, String> outputMap = new HashMap<>();
        outputMap.put("2", String.valueOf(new Date(1800000000000L)));
        String jsonString;

        // NULL
        jsonString = "{}";
        assertThat(toJson(null)).isEqualTo(jsonString);
        assertThat(JsonDecoder.jsonOf(jsonString)).isNull();
        assertThat(JsonDecoder.jsonListOf(jsonString)).isEmpty();
        assertThat(JsonDecoder.jsonMapOf(jsonString)).isEmpty();

        // MAP
        jsonString = "{2:\"Fri Jan 15 08:00:00 UTC 2027\"}";
        assertThat(toJson(inputMap)).isEqualTo(jsonString);
        assertThat((Map<Object, Object>) JsonDecoder.jsonOf(jsonString)).containsExactlyInAnyOrderEntriesOf(outputMap);
        assertThat(JsonDecoder.jsonListOf(jsonString)).containsExactly(outputMap);
        assertThat(JsonDecoder.jsonMapOf(jsonString)).containsExactlyInAnyOrderEntriesOf(outputMap);

        // COLLECTION
        jsonString = "[\"BB\",1,true,null,1.2]";
        final List<Object> expectedCollection = asList("BB", 1L, true, null, 1.2);
        assertThat(toJson(expectedCollection)).isEqualTo(jsonString);
        assertThat((Collection<Object>) JsonDecoder.jsonOf(jsonString)).isEqualTo(expectedCollection);
        assertThat(JsonDecoder.jsonListOf(jsonString)).isEqualTo(expectedCollection);
        assertThat((Collection<Object>) JsonDecoder.jsonMapOf(jsonString).get("")).isEqualTo(expectedCollection);

        // ARRAY
        jsonString = "[\"BB\",1,true]";
        final List<Object> expectedArray = asList("BB", 1L, true);
        assertThat(toJson(new Object[]{"BB", 1, true, null})).isEqualTo(jsonString);
        assertThat((Collection<Object>) JsonDecoder.jsonOf(jsonString)).isEqualTo(expectedArray);
        assertThat(JsonDecoder.jsonListOf(jsonString)).isEqualTo(expectedArray);
        assertThat(JsonDecoder.jsonMapOf(jsonString)).containsEntry("", expectedArray);

        // String
        jsonString = "{\"HH,II,\\n\"}";
        final String expectedString = "HH,II,\n";
        assertThat(toJson(expectedString)).isEqualTo(jsonString);
        assertThat((JsonDecoder.jsonOf(jsonString))).isEqualTo(expectedString);
        assertThat(JsonDecoder.jsonListOf(jsonString)).isEqualTo(singletonList(expectedString));
        assertThat(JsonDecoder.jsonMapOf(jsonString)).containsEntry("", expectedString);
        assertThat(JsonDecoder.jsonTypeOf(jsonString).get(String.class,0)).isEqualTo(expectedString);
    }
}
