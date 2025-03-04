<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Возврат книги</title>
</head>
<body>
    <h1>Возврат книги</h1>
    <#if error?has_content>
        <p style="color:red;">${error}</p>
    </#if>

    <#if books??>
        <form method="post">
            <label for="bookTitle">Выберите книгу для возврата:</label>
            <select name="bookTitle" id="bookTitle">
                <#list books as book>
                    <#if employee.currentBooks?seq_contains(book)>
                        <option value="${book}">${book}</option>
                    </#if>
                </#list>
            </select>
            <button type="submit">Возвратить</button>
        </form>
    </#if>

</body>
</html>


