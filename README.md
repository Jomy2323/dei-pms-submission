# DEI-PMS João Silva Project

## 🚀 How to Run the Project

### 1️⃣ Initialize the Database
From the `src/` directory, run:
```sh
cd src
docker-compose up -d
```
This will start the database in the background using Docker.

### 2️⃣ Start the Backend (Java Spring Boot)
Navigate to the backend folder and run:
```sh
cd backend
mvn clean install
mvn spring-boot:run
```
The backend will be available at **http://localhost:8080/**.

### 3️⃣ Start the Frontend (Vue.js)
Navigate to the frontend folder and run:
```sh
cd ../frontend
npm install  # Only needed the first time
npm run dev
```
The frontend will be available at **http://localhost:5173/**.

---

## 🔑 Logging In
You can log in as different roles.  
If you're unsure **which IST ID to use**, check `populate.sql` to see the preloaded users.

---

## 📜 Troubleshooting
- **Port Conflicts?**  
  - Ensure ports **8080 (backend)** and **5173 (frontend)** are available.
  - If needed, modify `application.properties` (backend) or `vite.config.js` (frontend).

- **Database Not Connecting?**  
  - Make sure Docker is running.
  - Run `docker ps` to check if the database container is active.

---
