<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Вход</title>
</head>
<body>
    <h1>Вход в систему</h1>
    <#if error??>
        <p style="color: red;">${error}</p>
    </#if>
    <form action="/login" method="post">
        <label for="email">Email:</label>
        <input type="text" id="email" name="email" required><br><br>

        <label for="password">Пароль:</label>
        <input type="password" id="password" name="password" required><br><br>

        <button type="submit">Войти</button>
    </form>
    <br>
    <a href="/register">Регистрация</a>
</body>
</html>
