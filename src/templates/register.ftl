<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Регистрация</title>
</head>
<body>
    <h1>Регистрация сотрудника</h1>
    <#if error??>
        <p style="color: red;">${error}</p>
    </#if>
    <form action="/register" method="post">
        <label for="email">Идентификатор (email):</label>
        <input type="text" id="email" name="email" required><br><br>

        <label for="fullName">Имя:</label>
        <input type="text" id="fullName" name="fullName" required><br><br>

        <label for="password">Пароль:</label>
        <input type="password" id="password" name="password" required><br><br>

        <button type="submit">Зарегистрироваться</button>
    </form>
    <br>
    <a href="index">На главную</a>
</body>
</html>