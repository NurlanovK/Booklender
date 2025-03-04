<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ошибка</title>
</head>
<body>
    <h1>Произошла ошибка</h1>
    <#if error??>
        <p style="color: red; font-weight: bold;">${error}</p>
    </#if>
    <a href="/return-book">Назад к возврату книги</a>
</body>
</html>

