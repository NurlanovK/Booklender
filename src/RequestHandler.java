
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
import java.util.*;
import java.util.stream.Collectors;

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
        server.createContext("/profile", this::profileHandler);
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
        Map<String, String> params = getQueryParams(exchange);
        String idParam = params.get("id");

        if (idParam != null) {
            try {
                int bookId = Integer.parseInt(idParam);
                Books book = books.stream()
                        .filter(b -> b.getId() == bookId)
                        .findFirst()
                        .orElse(null);

                if (book != null) {
                    renderTemplate(exchange, "book-details.ftl", getSingleBookDataModel(book));
                    return;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        sendNotFoundResponse(exchange);
    }

    private void singleEmployeeHandler(HttpExchange exchange) {
        Map<String, String> params = getQueryParams(exchange);
        String idParam = params.get("id");

        if (idParam != null) {
            try {
                int employeeId = Integer.parseInt(idParam);
                Employees employee = employees.stream()
                        .filter(e -> e.getId() == employeeId)
                        .findFirst()
                        .orElse(null);

                if (employee != null) {
                    renderTemplate(exchange, "employees-details.ftl", getSingleEmployeeDataModel(employee));
                    return;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        sendNotFoundResponse(exchange);
    }


    private Map<String, String> getQueryParams(HttpExchange exchange) {
        Map<String, String> queryParams = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }


    private void sendNotFoundResponse(HttpExchange exchange) {
        try {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

                exchange.getResponseHeaders().add("Location", "/profile");
                exchange.sendResponseHeaders(302, -1);
            } else {
                exchange.getResponseHeaders().add("Location", "/login?error=Неверные данные");
                exchange.sendResponseHeaders(302, -1);
            }
        }
    }
    public void profileHandler(HttpExchange exchange) throws IOException {
        System.out.println("Запрос на /profile");

        String sessionId = getSessionId(exchange);
        Employees employee = sessions.get(sessionId);

        if (employee == null) {
            exchange.getResponseHeaders().add("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        renderTemplate(exchange, "profile.ftl", getSingleEmployeeDataModel(employee));

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


    private void loadBooksFromFile() {
        this.books = Utils.readFile("src/data/books.json", new TypeToken<List<Books>>() {}.getType());
    }

    private void loadEmployeesFromFile() {
        this.employees = Utils.readFile("src/data/employees.json", new TypeToken<List<Employees>>() {}.getType());
    }

    private void saveBooksToFile() {
        try {
            Utils.saveToFile("src/data/books.json", books);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveEmployeesToFile() {
        try {
            Utils.saveToFile("src/data/employees.json", employees);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void issueBookHandler(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        Employees employee = sessions.get(sessionId);

        if (employee == null) {
            exchange.getResponseHeaders().add("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        if (employee.getCurrentBooks().size() >= 2) {
            renderTemplate(exchange, "error.ftl", Map.of("error", "Вы не можете взять больше двух книг"));
            return;
        }

        // Фильтруем книги только со статусом "Free"
        List<Books> availableBooks = books.stream()
                .filter(book -> book.getStatus().equalsIgnoreCase("Free"))
                .collect(Collectors.toList());

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            renderTemplate(exchange, "issue-book.ftl", Map.of("books", availableBooks));
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> formData = Utils.parseFormData(exchange);
            String bookTitle = formData.get("bookTitle");

            Books selectedBook = books.stream()
                    .filter(book -> book.getTitle().equals(bookTitle) && book.getStatus().equalsIgnoreCase("Free"))
                    .findFirst()
                    .orElse(null);

            if (selectedBook != null) {
                employee.getCurrentBooks().add(bookTitle);
                selectedBook.setStatus("Busy");

                // Сохранение книг и сотрудников в файлы
                saveBooksToFile();
                saveEmployeesToFile();

                renderTemplate(exchange, "noerror.ftl", Map.of("message", "Книга успешно выдана"));
            } else {
                renderTemplate(exchange, "error.ftl", Map.of("error", "Книга недоступна"));
            }
        }
    }

    public void returnBookHandler(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        Employees employee = sessions.get(sessionId);

        if (employee == null) {
            exchange.getResponseHeaders().add("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<String> availableBooks = new ArrayList<>(employee.getCurrentBooks());
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

                Books returnedBook = books.stream()
                        .filter(book -> book.getTitle().equals(bookTitle))
                        .findFirst()
                        .orElse(null);

                if (returnedBook != null) {
                    returnedBook.setStatus("Free");
                    saveBooksToFile();
                }

                saveEmployeesToFile();
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

