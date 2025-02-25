import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import Models.Books;
import Models.Employees;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class RequestHandler {
    private HttpServer server;
    private List<Books> books;
    private List<Employees> employees;

    public RequestHandler(String host, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);

        Type booksListType = new TypeToken<List<Books>>() {}.getType();
        books = Utils.readFile("src/data/books.json", booksListType);

        Type employeesListType = new TypeToken<List<Employees>>() {}.getType();
        employees = Utils.readFile("src/data/employees.json", employeesListType);

        server.createContext("/", this::indexHtmlHandler);
        server.createContext("/books", this::freemarkerBooksHandler);
        server.createContext("/employees", this::freemarkerEmployeesHandler);
        server.createContext("/book-details", this::singleBookHandler);
        server.createContext("/employees-details", this::singleEmployeeHandler);

        server.createContext("/images", this::serveImage);
    }

    private void serveImage(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String imagePath = "src" + path;

        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(imageFile)) {
                String contentType = Files.probeContentType(imageFile.toPath());
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, imageFile.length());
                fileInputStream.transferTo(exchange.getResponseBody());
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    exchange.sendResponseHeaders(500, -1);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        } else {
            try {
                exchange.sendResponseHeaders(404, -1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен на http://localhost:9889/");
    }

    private void indexHtmlHandler(HttpExchange exchange)  {
        Map<String, Object> dataModel = new HashMap<>();
        renderTemplate(exchange, "index.ftl", dataModel);
    }

    private void singleBookHandler(HttpExchange exchange) {
        renderTemplate(exchange, "book-details.ftl", getSingleBookDataModel(books.get(0)));
    }

    private void singleEmployeeHandler(HttpExchange exchange) {
        renderTemplate(exchange, "employees-details.ftl", getSingleEmployeeDataModel(employees.get(0)));
    }

    private void freemarkerBooksHandler(HttpExchange exchange) {
        renderTemplate(exchange, "books.ftl", getBooksDataModel());
    }

    private void freemarkerEmployeesHandler(HttpExchange exchange) {
        renderTemplate(exchange, "employees.ftl", getEmployeesDataModel());
    }

    private Map<String, Object> getBooksDataModel() {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("books", books);
        return dataModel;
    }

    private Map<String, Object> getEmployeesDataModel() {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("employees", employees);
        return dataModel;
    }

    private Map<String, Object> getSingleBookDataModel(Books book) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("book", book);
        return dataModel;
    }

    private Map<String, Object> getSingleEmployeeDataModel(Employees employee) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("employee", employee);
        return dataModel;
    }

    private void renderTemplate(HttpExchange exchange, String template, Map<String, Object> data) {
        Utils.renderTemplate(exchange, template, data);
    }
}
