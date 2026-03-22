# 🎓 Campus Event Management System

A full-stack event management system where students can create events, register for them, and administrators can manage participation.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)
![JUnit](https://img.shields.io/badge/JUnit-5-red)

---

## 📌 Features

- User registration and login (student / admin roles)
- Create and manage campus events
- Register for events with capacity enforcement
- Duplicate registration prevention
- Real-time participant list
- Full CRUD for events
- 22 JUnit unit tests across all service layers

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot 3.5.11 |
| Database | PostgreSQL 17, Spring Data JPA, Hibernate |
| Frontend | HTML, CSS, Vanilla JavaScript |
| Testing | JUnit 5, Mockito |
| Build Tool | Maven |

---

## 📂 Project Structure
```
src/
├── main/
│   ├── java/com/example/campus_events/
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── Event.java
│   │   │   └── Registration.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── EventRepository.java
│   │   │   └── RegistrationRepository.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   ├── EventService.java
│   │   │   └── RegistrationService.java
│   │   ├── controller/
│   │   │   ├── UserController.java
│   │   │   ├── EventController.java
│   │   │   └── RegistrationController.java
│   │   └── CampusEventsApplication.java
│   └── resources/
│       ├── application.properties
│       └── static/
│           └── index.html
└── test/
    └── java/com/example/campus_events/
        ├── UserServiceTest.java
        ├── EventServiceTest.java
        └── RegistrationServiceTest.java
```

---

## 🗄️ Database Schema
```sql
users
-----
id, name, email (unique), password, role, created_at

events
------
id, title, description, event_date, location, max_participants, organizer_id (FK), created_at

registrations
-------------
id, user_id (FK), event_id (FK), registered_at
UNIQUE(user_id, event_id)
```

---

## 🔌 API Endpoints

### Users
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/users/register | Register new user |
| POST | /api/users/login | Login |
| GET | /api/users/{id} | Get user by ID |

### Events
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/events | Create event |
| GET | /api/events | Get all events |
| GET | /api/events/{id} | Get event by ID |
| PUT | /api/events/{id} | Update event |
| DELETE | /api/events/{id} | Delete event |

### Registrations
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/events/{id}/register | Register for event |
| DELETE | /api/events/{id}/register | Cancel registration |
| GET | /api/events/{id}/participants | Get participants |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven
- PostgreSQL 17

### Setup
```bash
# Clone the repo
git clone https://github.com/vishnu062006/campus-event-management-system.git
cd campus-event-management-system/campus-events

# Create PostgreSQL database
psql postgres
CREATE DATABASE campus_events;
GRANT ALL PRIVILEGES ON DATABASE campus_events TO your_username;
\q

# Update application.properties
spring.datasource.username=your_username
spring.datasource.password=your_password

# Run the app
./mvnw spring-boot:run
```

Open `http://localhost:8080` in your browser.

---

## 🧪 Running Tests
```bash
./mvnw test
```

**Test Coverage:**
- `UserServiceTest` — 7 tests (register, login, validation)
- `RegistrationServiceTest` — 7 tests (capacity, duplicate, cancel)
- `EventServiceTest` — 8 tests (create, validate, delete)

---

## 📋 Business Rules

- User cannot register for the same event twice
- Event capacity cannot exceed `max_participants`
- Event date must be in the future
- Password must be at least 6 characters
- Email must be unique across all users

---

## 👨‍💻 Author

**Vishnu Mashalkar**
BMS College of Engineering — CSE 2028
Internship @ Euphoric Thought Technologies
