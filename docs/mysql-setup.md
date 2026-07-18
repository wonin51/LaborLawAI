# MySQL Setup

## Current Backend Defaults

The backend reads MySQL settings from environment variables and falls back to:

```text
DB_URL=jdbc:mysql://localhost:3306/legal_contract_assistant?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=
```

Port `3306` is currently planned for MySQL. Do not run another database on this port.

## Initialize Database

After MySQL is installed and running:

```powershell
mysql -u root -p < backend/src/main/resources/db/init.sql
```

If your root account has no password:

```powershell
mysql -u root < backend/src/main/resources/db/init.sql
```

## Verify Connection

```powershell
mysql -u root -p -e "SHOW DATABASES LIKE 'legal_contract_assistant';"
```

Then start backend:

```powershell
cd backend
mvn spring-boot:run
```

## Notes For Later Data Import

- Put legal documents into `kb_document`.
- Put split text fragments into `kb_chunk`.
- Keep source title, source URL, publication date, jurisdiction, and effective status for citation traceability.
- Do not store personal sensitive data in the public knowledge base.
