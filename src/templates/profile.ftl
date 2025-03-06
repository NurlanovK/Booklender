<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Профиль сотрудника</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ccc; border-radius: 10px; }
        h2 { text-align: center; }
        .section { margin-bottom: 15px; }
        .section h3 { margin-bottom: 5px; }
        ul { list-style-type: none; padding: 0; }
        li { background: #f3f3f3; padding: 5px; margin: 3px 0; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Профиль сотрудника</h2>
        <div class="section">
            <h3>Имя:</h3>
            <p>${employee.fullName}</p>
        </div>
        <div class="section">
            <h3>Email:</h3>
            <p>${employee.email}</p>
        </div>
        <div class="section">
            <h3>Текущие книги:</h3>
            <ul>
                <#if employee.currentBooks?? && employee.currentBooks?size &gt; 0>
                    <#list employee.currentBooks as book>
                        <li>${book}</li>
                    </#list>
                <#else>
                    <li>Нет текущих книг</li>
                </#if>
            </ul>
        </div>
        <div class="section">
            <h3>Прошлые книги:</h3>
            <ul>
                <#if employee.pastBooks?? && employee.pastBooks?size &gt; 0>
                    <#list employee.pastBooks as book>
                        <li>${book}</li>
                    </#list>
                <#else>
                    <li>Нет прошлых книг</li>
                </#if>
            </ul>
        </div>
          <br>
                <a href="select-action">Выбрать действие</a><br>
    </div>

</body>
</html>