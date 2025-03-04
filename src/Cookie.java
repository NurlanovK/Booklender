import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Cookie {
    private final String name;
    private final String value;
    private Integer maxAge;
    private boolean httpOnly;
    private String path = "/"; // Путь по умолчанию
    private String domain = "localhost"; // Домен по умолчанию

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static Cookie make(String name, String value) {
        return new Cookie(name, value);
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }
    public void setPath(String path) {
        this.path = path;
    }

    // Метод для установки домена
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public static Map<String, String> parse(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return new HashMap<>();
        }

        return HttpCookie.parse(cookieHeader).stream()
                .collect(Collectors.toMap(HttpCookie::getName, HttpCookie::getValue));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name + "=" + value);
        if (maxAge != null) {
            sb.append("; Max-Age=").append(maxAge);
        }
        if (httpOnly) {
            sb.append("; HttpOnly");
        }
        return sb.toString();
    }
}

