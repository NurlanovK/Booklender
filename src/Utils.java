

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Utils {
    private static final Configuration freemarker = new Configuration(Configuration.VERSION_2_3_29);

    static {
        freemarker.setClassForTemplateLoading(Utils.class, "/templates");
    }

    public static <T> T readFile(String path, java.lang.reflect.Type type) {
        try (Reader reader = new FileReader(path)) {
            return new Gson().fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void renderTemplate(HttpExchange exchange, String template, Map<String, Object> data) {
        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody(), StandardCharsets.UTF_8)) {
            Template tmpl = freemarker.getTemplate(template);
            exchange.sendResponseHeaders(200, 0);
            tmpl.process(data, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}
