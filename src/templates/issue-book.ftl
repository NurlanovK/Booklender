<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Выдача книги</title>
</head>
<body>
    <h1>Выдача книги</h1>
    <#if error?has_content>
        <p style="color:red;">${error}</p>
    </#if>

    <#if books??>
        <form method="post">
            <label for="bookTitle">Выберите книгу:</label>
            <select name="bookTitle" id="bookTitle">
                <#list books as book>
                    <option value="${book.title}">${book.title}</option>
                </#list>
            </select>
            <button type="submit">Выдать</button>
        </form>
    </#if>
</body>
</html>
