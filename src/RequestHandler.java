
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import java.util.UUID;

public class RequestHandler {
    private HttpServer server;
    private List<Books> books;
    private List<Employees> employees;
    Map<String, Employees> sessions = new HashMap<>();

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
        server.createContext("/register", this::registerHandler);
        server.createContext("/images", this::serveImage);
        server.createContext("/login", this::loginHandler);
        server.createContext("/issue-book", this::issueBookHandler);
        server.createContext("/return-book", this::returnBookHandler);
        server.createContext("/select-action", this::selectActionHandler);
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

    private void indexHtmlHandler(HttpExchange exchange) {
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

    private void registerHandler(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("GET".equalsIgnoreCase(method)) {
            renderTemplate(exchange, "register.ftl", new HashMap<>());
        } else if ("POST".equalsIgnoreCase(method)) {
            Map<String, String> formData = Utils.parseFormData(exchange);
            String fullName = formData.get("fullName");
            String email = formData.get("email");
            String password = formData.get("password");

            boolean exists = employees.stream().anyMatch(emp -> emp.getEmail().equalsIgnoreCase(email));

            if (exists) {
                renderTemplate(exchange, "register.ftl", Map.of("error", "Пользователь с таким email уже зарегистрирован"));
            } else {
                int newId = employees.size() + 1;
                Employees newEmployee = new Employees(newId, fullName, email, password, List.of(), List.of());
                employees.add(newEmployee);

                try {
                    Utils.saveToFile("src/data/employees.json", employees);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                renderTemplate(exchange, "success.ftl", Map.of("name", fullName));
            }
        }
    }



    public void loginHandler(HttpExchange exchange) throws IOException {
        System.out.println("Запрос на /login");

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            renderTemplate(exchange, "login.ftl", new HashMap<>());
        } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> parameters = getParams(exchange);
            String email = parameters.get("email");
            email = URLDecoder.decode(email, StandardCharsets.UTF_8);
            System.out.println("Декодированный email: " + email);
            String password = parameters.get("password");
            System.out.println("Входные данные: email=" + email + ", password=" + password);
            Employees employee = authenticateUser(email, password);
            System.out.println("Результат аутентификации: " + (employee != null ? "Успешно" : "Ошибка"));


            if (employee != null) {
                String sessionId = UUID.randomUUID().toString();
                sessions.put(sessionId, employee);
                System.out.println("Установлен session_id: " + sessionId);
                exchange.getResponseHeaders().add("Set-Cookie", "session_id=" + sessionId + "; Path=/; HttpOnly");

                exchange.getResponseHeaders().add("Set-Cookie", "session_id=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax");

                exchange.getResponseHeaders().add("Location", "/select-action");
                exchange.sendResponseHeaders(302, -1);
            } else {
                exchange.getResponseHeaders().add("Location", "/login?error=Неверные данные");
                exchange.sendResponseHeaders(302, -1);
            }
        }
    }


    public void selectActionHandler(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        Employees employee = sessions.get(sessionId);
        System.out.println("Полученный session_id: " + sessionId);
        System.out.println("session_id из Cookie: " + sessionId);
        System.out.println("session в памяти: " + sessions.get(sessionId));



        if (employee == null) {
            exchange.getResponseHeaders().add("Location", "/login?error=Необходимо авторизоваться");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("employee", employee);

        renderTemplate(exchange, "select-action.ftl", data);
    }

    private String getSessionId(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                String[] pairs = cookie.split("; ");
                for (String pair : pairs) {
                    if (pair.startsWith("session_id=")) {
                        return pair.substring("session_id=".length());
                    }
                }
            }
        }
        return null;
    }


    private void issueBookHandler(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        Employees employee = sessions.get(sessionId);

        if (employee == null) {
            exchange.sendResponseHeaders(302, -1);
            exchange.getResponseHeaders().add("Location", "/login");
            return;
        }

        if (employee.getCurrentBooks().size() >= 2) {
            renderTemplate(exchange, "error.ftl", Map.of("error", "Вы не можете взять больше двух книг"));
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            renderTemplate(exchange, "issue-book.ftl", getBooksDataModel());
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> formData = Utils.parseFormData(exchange);
            String bookTitle = formData.get("bookTitle");

            if (bookTitle != null && books.stream().anyMatch(book -> book.getTitle().equals(bookTitle))) {
                employee.getCurrentBooks().add(bookTitle);
                Utils.saveToFile("src/data/employees.json", employees);
                renderTemplate(exchange, "noerror.ftl", Map.of("message", "Книга успешно выдана"));
            } else {
                renderTemplate(exchange, "error.ftl", Map.of("error", "Книга не найдена"));
            }
        }
    }

    private void returnBookHandler(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        Employees employee = sessions.get(sessionId);

        if (employee == null) {
            exchange.sendResponseHeaders(302, -1);
            exchange.getResponseHeaders().add("Location", "/login");
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<String> availableBooks = getBooksThatCanBeReturned(employee);
            renderTemplate(exchange, "return-book.ftl", Map.of(
                    "books", availableBooks,
                    "employee", employee
            ));
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> formData = Utils.parseFormData(exchange);
            String bookTitle = formData.get("bookTitle");

            if (bookTitle != null && employee.getCurrentBooks().remove(bookTitle)) {
                employee.getPastBooks().add(bookTitle);
                Utils.saveToFile("src/data/employees.json", employees);
                renderTemplate(exchange, "noerror.ftl", Map.of("message", "Книга успешно возвращена"));
            } else {
                renderTemplate(exchange, "error.ftl", Map.of("error", "Книга не найдена в вашем списке"));
            }
        }
    }

    private List<String> getBooksThatCanBeReturned(Employees employee) {
        return employee.getCurrentBooks();
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

    private Map<String, String> getParams(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        String query = reader.readLine();

        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] param = pair.split("=");
                if (param.length == 2) {
                    params.put(param[0], param[1]);
                }
            }
        }

        return params;
    }

    public Employees authenticateUser(String email, String password) {
        System.out.println("Проверка аутентификации: email=" + email);

        for (Employees employee : employees) {
            if (employee.getEmail().equals(email)) {
                System.out.println("Найден пользователь: " + employee.getEmail());

                if (employee.getPassword().equals(password)) {
                    System.out.println("Аутентификация успешна");
                    return employee;
                } else {
                    System.out.println("Неверный пароль: " + password);
                    System.out.println("Ожидалось: " + employee.getPassword());
                    return null;
                }
            }
        }

        System.out.println("Пользователь не найден: " + email);
        return null;
    }



    private void renderTemplate(HttpExchange exchange, String template, Map<String, Object> data) {
        Utils.renderTemplate(exchange, template, data);
    }
}

