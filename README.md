# Авторизация (Java Swing + JDBC)

Запуск: `run.bat` (Windows) или `./run.sh` (Linux/macOS). Нужен JDK 17+.
В IntelliJ IDEA: открыть папку, `src` — Sources Root, `resources` — Resources Root,
`lib/sqlite-jdbc.jar` — добавить как библиотеку, запускать `auth.Main`.

Тестовые учётки (создаются при первом запуске): `admin / admin123`, `user / user123`.

БД по умолчанию — SQLite (файл app.db). Для PostgreSQL/MySQL: добавить драйвер в classpath и
запустить с `-Ddb.url=jdbc:postgresql://localhost:5432/db -Ddb.user=... -Ddb.password=...`
(в `Database.initialize()` заменить `INTEGER PRIMARY KEY AUTOINCREMENT` на `SERIAL`/`AUTO_INCREMENT`).
Если таблица пользователей уже есть в вашей БД — поправьте имена столбцов в `UserDao`.
