# 🚀 StackTrace

> A production-oriented developer learning platform built using a Microservices Architecture.

StackTrace is a full-stack developer platform where users can publish technical articles, follow developers, earn badges, participate in quizzes, and grow through a community-driven learning experience.

This project is being built to explore real-world backend engineering concepts such as Microservices, Authentication, System Design, Scalability, Event-Driven Architecture, Caching, and Production Engineering.

---

## ✨ Features

### ✅ User Service
- JWT Authentication
- User Registration & Login
- Profile Management
- Follow / Unfollow Users
- Skills Management
- Badge System
- Leaderboard
- User Search
- Pagination

### 🚧 Post Service
- Create, Update & Delete Posts
- Draft & Publish Workflow
- Tags
- Markdown Support
- Post Search
- Pagination

### 📅 Planned Features
- Nested Comments
- AI Article Summaries
- Quiz System
- Bookmark Posts
- Like Posts
- Notifications
- Trending Posts
- Code Execution
- Outdated Content Detection

---

# 🛠 Tech Stack

## Backend
- Java 21
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- Hibernate
- Maven
- Lombok
- Bean Validation
- JWT Authentication

## Database
- PostgreSQL

## Frontend
- Next.js
- React
- TypeScript
- Tailwind CSS
- shadcn/ui

## Future Integrations
- Redis
- Apache Kafka
- Docker
- OpenAPI (Swagger)
- GitHub Actions
- Prometheus & Grafana

---

# 🏗 Architecture

```
                        Client
                           │
                           ▼
                  API Gateway (Future)
                           │
 ┌───────────────┬──────────┴──────────┬───────────────┐
 │               │                     │               │
 ▼               ▼                     ▼               ▼
User Service   Post Service     Comment Service   Search Service
```

Each service has its own:
- Database
- Business Logic
- REST APIs
- Independent Deployment

---

# 📂 Project Structure

```
StackTrace
│
├── user-service
├── post-service
├── comment-service
├── search-service
├── frontend
└── docs
```

---

# 🔧 Microservices

| Service | Responsibility | Status |
|----------|---------------|--------|
| User Service | Authentication, Profile, Follow System, Skills, Badges | ✅ Completed |
| Post Service | Article Management | 🚧 In Progress |
| Comment Service | Comments & Replies | 📅 Planned |
| Search Service | Search Users & Posts | 📅 Planned |

---

# 🚀 Getting Started

Clone the repository

```bash
git clone https://github.com/<your-username>/StackTrace.git
```

Go to the project directory

```bash
cd StackTrace
```

Each microservice contains its own setup instructions inside its respective `README.md`.

---

# 📈 Current Progress

| Module | Status |
|----------|--------|
| User Service | ✅ Completed |
| Post Service | 🚧 In Progress |
| Comment Service | 📅 Planned |
| Search Service | 📅 Planned |
| Frontend | 🚧 In Progress |

---

# 🎯 Learning Goals

StackTrace is more than a blogging platform.

The project is being built to gain hands-on experience with:

- Microservices Architecture
- Database Design
- System Design
- Spring Security & JWT
- REST API Design
- Scalable Backend Development
- Production Engineering
- Distributed Systems
- Event-Driven Architecture
- Caching
- CI/CD & DevOps

---

# 🗺 Roadmap

### Phase 1
- ✅ User Service
- ✅ Authentication
- ✅ Follow System
- ✅ Skills
- ✅ Badge System

### Phase 2
- 🚧 Post Service
- 📅 Comment Service
- 📅 Search Service

### Phase 3
- 📅 Redis Integration
- 📅 Apache Kafka
- 📅 Docker
- 📅 API Gateway
- 📅 Notification Service

### Phase 4
- 📅 Monitoring
- 📅 CI/CD
- 📅 Production Deployment

---

# 🤝 Contributing

Contributions, suggestions, and discussions are always welcome.

Feel free to open an Issue or submit a Pull Request.

---

# 📜 License

This project is licensed under the MIT License.

---

⭐ If you find this project interesting, consider giving it a star!
