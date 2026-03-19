# 🏥 MediVerse — Hospital Management System

A comprehensive hospital management system built with **Spring Boot**, designed to streamline healthcare operations including patient management, appointment scheduling, prescriptions, billing, and more.

## ✨ Features

- **Authentication & Authorization** — Secure login with JWT-based authentication and role-based access control (Admin, Doctor, Patient)
- **Patient Management** — Register and manage patient profiles and health metrics
- **Doctor Management** — Manage doctor profiles, departments, and availability slots
- **Appointments** — Schedule, update, and track appointments between patients and doctors
- **Medical Records** — Create and access patient medical histories
- **Prescriptions** — Issue and manage prescriptions
- **Payments & Billing** — Process and track payments for services
- **Notifications** — Email notifications for appointments, updates, and alerts
- **Dashboard & Analytics** — Overview of hospital operations and statistics
- **Smart Features** — AI-powered insights and recommendations

## 🛠️ Tech Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Framework    | Spring Boot 3.4                   |
| Language     | Java 17                           |
| Database     | PostgreSQL                        |
| Security     | Spring Security + JWT (jjwt)      |
| ORM          | Spring Data JPA / Hibernate       |
| Email        | Spring Mail (SMTP)                |
| Validation   | Spring Validation                 |
| Build Tool   | Maven                             |
| Other        | Lombok, Spring Actuator           |

## 📁 Project Structure

```
src/main/java/com/mediverse/
├── config/          # App configuration (CORS, beans, etc.)
├── controller/      # REST API controllers
├── exception/       # Custom exceptions & global error handling
├── model/           # JPA entity classes
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, auth entry point, security config
└── service/         # Business logic layer
```


### Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/RUKUNDO7/MediVerse.git
   cd MediVerse
   ```

2. **Create a `.env` file** in the project root with the following variables:

   ```env
   DB_URL=jdbc:postgresql://localhost:5432/mediverse
   DB_USERNAME=your_db_username
   DB_PASSWORD=your_db_password
   MAIL_USERNAME=your_email@gmail.com
   MAIL_PASSWORD=your_email_app_password
   JWT_SECRET=your_jwt_secret_key
   ```

3. **Run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

   The server will start at `http://localhost:8080`.

## 📡 API Overview

| Module             | Base Path                |
|--------------------|--------------------------|
| Authentication     | `/api/auth`              |
| Patients           | `/api/patients`          |
| Doctors            | `/api/doctors`           |
| Appointments       | `/api/appointments`      |
| Availability Slots | `/api/availability-slots`|
| Medical Records    | `/api/medical-records`   |
| Prescriptions      | `/api/prescriptions`     |
| Payments           | `/api/payments`          |
| Departments        | `/api/departments`       |
| Health Metrics     | `/api/health-metrics`    |
| Notifications      | `/api/notifications`     |
| Dashboard          | `/api/dashboard`         |
| Smart Features     | `/api/smart-features`    |

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
