<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Подробности книги</title>
</head>
<body>
    <h1>Подробности книги</h1>
    <p><strong>ID:</strong> ${book.id!}</p>
    <p><strong>Название:</strong> ${book.title!}</p>
    <p><strong>Автор:</strong> ${book.author!}</p>
    <p><strong>Описание:</strong> ${book.description!}</p>
    <p><strong>Статус:</strong> ${book.status!}</p>
     <img src="${book.cover}" alt="Обложка книги" width="200">
    <br><br>
    <a href="books">Назад к списку книг</a>
</body>
</html>
