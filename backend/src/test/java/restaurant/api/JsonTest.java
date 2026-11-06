package restaurant.api;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    @Test
    void parsesObjectsArraysAndValues() {
        Object o = Json.parse("{\"table\": 5, \"comment\": \"no \\\"onion\\\"\", \"ok\": true, "
                + "\"items\": [{\"type\": \"pizza\", \"toppings\": [\"cheese\"]}], \"x\": null}");
        Map<?, ?> m = (Map<?, ?>) o;
        assertEquals(5.0, m.get("table"));
        assertEquals("no \"onion\"", m.get("comment"));
        assertEquals(Boolean.TRUE, m.get("ok"));
        assertTrue(m.containsKey("x") && m.get("x") == null);
        Map<?, ?> item = (Map<?, ?>) ((List<?>) m.get("items")).get(0);
        assertEquals(List.of("cheese"), item.get("toppings"));
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> Json.parse("{\"a\": }"));
        assertThrows(IllegalArgumentException.class, () -> Json.parse("[1, 2"));
        assertThrows(IllegalArgumentException.class, () -> Json.parse(""));
    }

    @Test
    void writesNumbersStringsAndNesting() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", 1);
        m.put("price", 2500.0);
        m.put("half", 7777.5);
        m.put("name", "a\"b");
        m.put("list", List.of(1, "x"));
        assertEquals("{\"id\":1,\"price\":2500,\"half\":7777.5,\"name\":\"a\\\"b\",\"list\":[1,\"x\"]}", Json.write(m));
    }
}
