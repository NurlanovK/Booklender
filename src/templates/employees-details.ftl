<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Подробности сотрудника</title>
</head>
<body>
    <h1>Подробности сотрудника</h1>
    <p><strong>ID:</strong> ${employee.id}</p>
    <p><strong>ФИО:</strong> ${employee.fullName}</p>

    <h2>Текущие книги</h2>
    <ul>
        <#list employee.currentBooks as book>
            <li>${book}</li>  <!-- Отображаем название каждой текущей книги -->
        </#list>
    </ul>

    <h2>Прошлые книги</h2>
    <ul>
        <#list employee.pastBooks as book>
            <li>${book}</li>  <!-- Отображаем название каждой прочитанной книги -->
        </#list>
    </ul>

    <br>
    <a href="employees">Назад к списку сотрудников</a>
</body>
</html>

