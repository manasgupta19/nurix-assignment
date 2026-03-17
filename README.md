# Outbound Voice Campaign Microservice

## Overview
A production-quality microservice built with Spring Boot to manage and execute automated outbound voice campaigns. It features timezone-aware business hour scheduling, concurrency control, and automated retry logic.

## Tech Stack
- **Java 21+** (Tested on Java 25 with Byte Buddy experimental flags)
- **Spring Boot 3.x** (Web, Data JPA, Actuator)
- **PostgreSQL** (Source of Truth)
- **Prometheus & Grafana** (Observability)
- **Docker & Docker Compose** (Infrastructure)

## Setup Instructions
1. **Start Infrastructure:**
   ```bash
   docker-compose up -d
   ```

2. **Build and Run:**
```bash
./mvnw spring-boot:run

```


3. **Run Tests:**
```bash
./mvnw clean test

```



## Example API Usage

### 1. Create a Campaign

```bash
curl.exe -X POST http://localhost:8080/api/campaigns \
-H "Content-Type: application/json" \
-d '{
  "name": "Morning Blast",
  "maxConcurrency": 2,
  "maxRetries": 2,
  "retryDelaySeconds": 30,
  "timeZone": "Asia/Kolkata",
  "phoneNumbers": ["9876543210", "9123456789"],
  "businessHours": [
    {"dayOfWeek": "TUESDAY", "startTime": "09:00", "endTime": "18:00"}
  ]
}'

```

### 2. Get Campaign Summary

```bash
curl -X GET http://localhost:8080/api/campaigns/1/summary

```

## System Design Explanation

### Architecture

The system utilizes a **Polling Consumer** pattern. A background `CallDispatcher` scans the database every 5 seconds for "Eligible" calls.

* **Eligibility** is defined as: `Status = PENDING` AND (`nextRetryAt` is NULL OR in the past) AND (Current Time is within `BusinessHours`).
* **Concurrency** is enforced at the campaign level by limiting the number of records picked in a single polling cycle.

### Fault Tolerance

By maintaining all state in **PostgreSQL**, the system ensures that no call is lost during a service restart. The use of **UTC Normalization** for all timestamps prevents "Time-Shift" bugs when running in distributed environments across different regions.

### Observability

Real-time metrics are exposed via `/actuator/prometheus`. Custom counters track `campaign.calls.dispatched` to monitor throughput and failure rates in Grafana.

---

### **Project Handover Checklist**
1.  [x] **Logic:** Concurrency defaults and Retry priority updated.
2.  [x] **Tests:** All integration and unit tests are green.
3.  [x] **Monitoring:** Prometheus configuration and Grafana steps provided.
4.  [x] **Logging:** Segregated logs for Hibernate, Spring, and App logic.
5.  [x] **Documentation:** Comprehensive README with Design and Setup info.
