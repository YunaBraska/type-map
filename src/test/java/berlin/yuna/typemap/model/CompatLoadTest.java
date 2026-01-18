package berlin.yuna.typemap.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "perf", matches = "true")
class CompatLoadTest {

    @Test
    void benchmarkTypeMapConstructor() {
        final String payload = largeObjectJson(4000);
        final long memBefore = usedMemoryKb();
        final long start = System.nanoTime();

        final TypeMap map = new TypeMap(payload);

        final long durationMs = (System.nanoTime() - start) / 1_000_000;
        final long memDelta = usedMemoryKb() - memBefore;

        assertThat(map).hasSize(4000);
        assertThat(map.get("k0")).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) map.get("k0")).get("idx")).isEqualTo(0L);
        System.out.printf("compatTypeMapConstructor entries=%d took=%dms memDelta=%dKB%n", map.size(), durationMs, memDelta);
    }

    @Test
    void benchmarkTypeListConstructor() {
        final String payload = largeArrayJson(5000);
        final long memBefore = usedMemoryKb();
        final long start = System.nanoTime();

        final TypeList list = new TypeList(payload);

        final long durationMs = (System.nanoTime() - start) / 1_000_000;
        final long memDelta = usedMemoryKb() - memBefore;

        assertThat(list).hasSize(5000);
        assertThat(list.get(0)).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) list.get(0)).get("id")).isEqualTo(0L);
        System.out.printf("compatTypeListConstructor items=%d took=%dms memDelta=%dKB%n", list.size(), durationMs, memDelta);
    }

    private static String largeArrayJson(final int size) {
        final StringBuilder sb = new StringBuilder(size * 64).append('[');
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(i).append(",\"name\":\"n").append(i).append("\",\"flags\":[true,false,true]}");
        }
        sb.append(']');
        return sb.toString();
    }

    private static String largeObjectJson(final int size) {
        final StringBuilder sb = new StringBuilder(size * 48).append('{');
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\"k").append(i).append("\":{\"idx\":").append(i).append(",\"nested\":{\"v\":").append(i * 2).append("}}");
        }
        sb.append('}');
        return sb.toString();
    }

    private static long usedMemoryKb() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }
}
