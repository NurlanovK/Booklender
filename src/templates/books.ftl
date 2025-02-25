<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Список книг</title>
</head>
<body>
    <h1>Список книг</h1>
    <table border="1">
        <tr>
            <th>ID</th>
            <th>Название</th>
            <th>Автор</th>
            <th>Обложка</th>
            <th>Действие</th>
        </tr>
        <#list books as book>
        <tr>
            <td>${book.id!}</td>
            <td>${book.title!}</td>
            <td>${book.author!}</td>


            <td><img src="${book.cover}" alt="Обложка" width="50"></td>
            <td><a href="book-details?id=${book.id}">Подробнее</a></td>
        </tr>
        </#list>
    </table>
    <br>
    <a href="index">На главную</a>
</body>
</html>

