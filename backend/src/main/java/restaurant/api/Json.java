package restaurant.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {
    private final String s;
    private int pos;

    private Json(String s) { this.s = s; }

    public static Object parse(String text) {
        Json p = new Json(text);
        p.skip();
        Object v = p.value();
        p.skip();
        if (p.pos != p.s.length()) throw p.error();
        return v;
    }

    private IllegalArgumentException error() { return new IllegalArgumentException("Invalid JSON"); }

    private void skip() {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
    }

    private char peek() {
        if (pos >= s.length()) throw error();
        return s.charAt(pos);
    }

    private Object value() {
        char c = peek();
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (s.startsWith("true", pos))  { pos += 4; return Boolean.TRUE; }
        if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        if (s.startsWith("null", pos))  { pos += 4; return null; }
        return number();
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<>();
        pos++; skip();
        if (peek() == '}') { pos++; return m; }
        while (true) {
            skip();
            if (peek() != '"') throw error();
            String key = string();
            skip();
            if (peek() != ':') throw error();
            pos++; skip();
            m.put(key, value());
            skip();
            char c = peek(); pos++;
            if (c == '}') return m;
            if (c != ',') throw error();
        }
    }

    private List<Object> array() {
        List<Object> l = new ArrayList<>();
        pos++; skip();
        if (peek() == ']') { pos++; return l; }
        while (true) {
            skip();
            l.add(value());
            skip();
            char c = peek(); pos++;
            if (c == ']') return l;
            if (c != ',') throw error();
        }
    }

    private String string() {
        StringBuilder sb = new StringBuilder();
        pos++; // opening quote
        while (true) {
            char c = peek(); pos++;
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }
            char e = peek(); pos++;
            switch (e) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (pos + 4 > s.length()) throw error();
                    try { sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16)); }
                    catch (NumberFormatException ex) { throw error(); }
                    pos += 4;
                    break;
                default: throw error();
            }
        }
    }

    private Double number() {
        int start = pos;
        while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
        if (start == pos) throw error();
        try { return Double.parseDouble(s.substring(start, pos)); }
        catch (NumberFormatException ex) { throw error(); }
    }

    public static String write(Object o) {
        StringBuilder sb = new StringBuilder();
        write(o, sb);
        return sb.toString();
    }

    private static void write(Object o, StringBuilder sb) {
        if (o == null) { sb.append("null"); }
        else if (o instanceof String str) { quote(str, sb); }
        else if (o instanceof Boolean) { sb.append(o); }
        else if (o instanceof Number n) {
            double d = n.doubleValue();
            if (d == Math.rint(d) && Math.abs(d) < 1e15) sb.append((long) d); else sb.append(d);
        } else if (o instanceof Map<?, ?> m) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                quote(String.valueOf(e.getKey()), sb);
                sb.append(':');
                write(e.getValue(), sb);
            }
            sb.append('}');
        } else if (o instanceof Iterable<?> it) {
            sb.append('[');
            boolean first = true;
            for (Object x : it) {
                if (!first) sb.append(',');
                first = false;
                write(x, sb);
            }
            sb.append(']');
        } else {
            quote(String.valueOf(o), sb);
        }
    }

    private static void quote(String str, StringBuilder sb) {
        sb.append('"');
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c);
            }
        }
        sb.append('"');
    }
}
