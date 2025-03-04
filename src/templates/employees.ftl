<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Список сотрудников</title>
</head>
<body>
    <h1>Список сотрудников</h1>
    <table border="1">
        <tr>
            <th>ID</th>
            <th>ФИО</th>
            <th>Текущие книги</th>
            <th>Прочитанные книги</th>
            <th>Действие</th>
        </tr>
        <#list employees as emp>
        <tr>
            <td>${emp.id!}</td>
            <td>${emp.fullName!}</td>
            <td>${emp.currentBooks?size}</td>
            <td>${emp.pastBooks?size}</td>
            <td><a href="/employees-details?id=${emp.id}">Подробнее</a></td>
        </tr>
        </#list>
    </table>
    <br>
    <a href="index">На главную</a><br>
    <a href="register">Зарегистрировать нового сотрудника</a><br>
    <a href="login">Зайти в профиль сотрудника</a>

</body>
</html>
