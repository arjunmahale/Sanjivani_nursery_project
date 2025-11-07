````markdown
# Plant Nursery Management (Complete Project)

This repository is a complete Plant Nursery Management System built with:
- Spring Boot (Web, Data JPA, Security, Mail)
- Hibernate + MySQL
- Thymeleaf UI (modernized)
- OpenPDF + ZXing for invoice PDF generation (rich invoice PDF)
- Twilio for SMS notifications
- Docker + docker-compose for easy local deployment

Features:
- Single admin user (configurable via env var)
- Manage Customers, Products, Orders
- Order creation updates product stock
- Bill (PDF) automatically generated on order creation and when regenerating
- Bill sent to customers via SMS & email (if configured)
- Delivery reminders scheduled daily, configurable days-before
- REST APIs for Customers, Products, Orders
- Reports: daily/monthly / top-selling
- Dockerized for local and EC2 deployments

Quick start (Docker):
1. Copy .env or set environment variables (see docker-compose.yml)
2. Build & run:
   docker-compose up --build
3. Visit: http://localhost:8080
   Default credentials (for local dev): username=admin password=admin123

Run locally (Maven):
1. Configure env vars or copy `src/main/resources/application.properties.template` to `src/main/resources/application.properties` and set values.
2. mvn clean package
3. java -jar target/nursery-0.0.1-SNAPSHOT.jar

Running in Eclipse:
- Import Maven project > Run as Spring Boot App.
- Set environment variables in Run Configurations (VM or environment).
- Ensure MySQL DB is accessible and properties are correct.

Deploy to AWS EC2:
- Build a JAR (mvn clean package).
- Provision EC2 instance (Amazon Linux/Ubuntu), install JDK 17, configure env vars (or use systemd service), open port 8080.
- Copy jar to instance, run: java -jar nursery-0.0.1-SNAPSHOT.jar
- For production: use RDS for MySQL, use S3 for invoice storage (recommended), configure proper secrets management.

Notes:
- For production, secure credentials using environment variables, AWS Secrets Manager, or a vault.
- If you want public access to invoice PDFs without requiring admin auth, we can implement time-limited signed links or upload to S3 and send presigned URLs.
- Add PNG logo to `src/main/resources/static/images/leaf-logo.png` for PDF header and `/static/fonts/CustomFont.ttf` to embed custom font.
