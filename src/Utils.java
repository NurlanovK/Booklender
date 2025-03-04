

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
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

    public static <T> void saveToFile(String filePath, T data) throws IOException {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(data);

            System.out.println("Путь к файлу: " + filePath);
            System.out.println("Данные для записи: " + json);

            Files.write(Paths.get(filePath), json.getBytes(StandardCharsets.UTF_8));
            System.out.println("Данные успешно записаны в файл " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл " + filePath);
            e.printStackTrace();
            throw e;
        }
    }


    public static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();
        String formData = sb.toString();
        Map<String, String> params = new HashMap<>();

        for (String pair : formData.split("&")) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            }
        }

        return params;
    }





    public static void renderTemplate(HttpExchange exchange, String template, Map<String, Object> data) {
        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody(), StandardCharsets.UTF_8)) {
            Template tmpl = freemarker.getTemplate(template);
            exchange.sendResponseHeaders(200, 0);
            tmpl.process(data, writer);
            System.out.println("Шаблон " + template + " успешно отрендерен");
        } catch (IOException | TemplateException e) {
            System.err.println("Ошибка при рендеринге шаблона: " + template);
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }



}
