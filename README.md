# Outbound Voice Campaign Manager

An enterprise-grade Spring Boot application designed to manage, schedule, and monitor outbound voice campaigns with high reliability, per-campaign concurrency limits, and intelligent retry mechanisms.

-----

## 🚀 Quick Start

### 1\. Prerequisites

  * **Java 17** (or higher)
  * **Maven 3.8+**
  * **Docker & Docker Compose** (for PostgreSQL, Prometheus, and Grafana)

### 2\. Infrastructure Setup

Spin up the required infrastructure (Database and Monitoring) using Docker:

```bash
docker-compose up -d
```

  * **PostgreSQL:** `localhost:5432` (user: `postgres`, pass: `postgres`)
  * **Prometheus:** `localhost:9090`
  * **Grafana:** `localhost:3000` (default login: `admin`/`admin`)

### 3\. Run the Application

```bash
./mvnw clean spring-boot:run
```

The application will start on port `8080`. Access the **Swagger UI** at: [http://localhost:8080/swagger-ui/index.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui/index.html)

-----

## 🛠 Execution & Verification Scenarios

### Scenario 1: Bulk Campaign Creation via CSV

**Goal:** Verify that a campaign can be created with bulk numbers and specific business hours.

1.  **Action:** Use the `POST /api/campaigns` endpoint.
2.  **Request:**
      * **Part `campaign` (JSON):**
        ```json
        {
          "name": "Morning Promotional Drive",
          "maxConcurrency": 5,
          "maxRetries": 2,
          "retryDelaySeconds": 10,
          "timeZone": "Asia/Kolkata",
          "businessHours": [
            { "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "18:00" },
            { "dayOfWeek": "TUESDAY", "startTime": "09:00", "endTime": "18:00" }
          ]
        }
        ```
      * **Part `file` (CSV):** Upload a file with one phone number per line.
3.  **Verification:**
      * **API:** Expect a `201 Created` with a campaign ID and the list of associated call records.
      * **DB Query:** `SELECT count(*) FROM call_records WHERE campaign_id = [ID];` should match CSV row count.
      * **Dashboard:** Total Dispatched count in Grafana will increment when the scheduler picks up the records.

### Scenario 2: Concurrency Enforcement

**Goal:** Prove the system respects `maxConcurrency` limits even when multiple campaigns are running.

1.  **Action:** Create two campaigns:
      * **Campaign A:** 50 numbers, `maxConcurrency: 1`
      * **Campaign B:** 10 numbers, `maxConcurrency: 5`
2.  **Verification:**
      * **Logs:** You will see `Campaign B` finishing its calls much faster than `Campaign A`.
      * **DB Query:** `SELECT campaign_id, status, count(*) FROM call_records GROUP BY campaign_id, status;`
      * **Grafana:** The **"Hikari Active Connections"** graph will show a steady pool usage corresponding to the sum of active calls.

### Scenario 3: Fault Tolerance & Intelligent Retries

**Goal:** Verify that failed calls are rescheduled based on the `retryDelaySeconds` and prioritized correctly.

1.  **Setup:** In `application.properties`, set `campaign.telephony.success-rate=0.5`.
2.  **Action:** Run a campaign.
3.  **Verification:**
      * **API:** Call `GET /api/campaigns/{id}`. Observe the `retryCount` incrementing for failed calls.
      * **DB Query:** `SELECT phone_number, retry_count, next_retry_at FROM call_records WHERE status = 'PENDING' AND retry_count > 0;`
      * **Grafana:** The **"Total Retries Attempted"** stat panel will spike, and **"Retry Pressure"** will show the delta between attempts and completions.

### Scenario 4: Business Hour & Timezone Compliance

**Goal:** Ensure calls are not triggered outside of the configured windows.

1.  **Action:** Create a campaign with a `startTime` in the future relative to the provided `timeZone`.
2.  **Verification:**
      * **Logs:** The dispatcher will log: `Campaign 'X' is currently outside business hours. Skipping.`
      * **DB Query:** `SELECT status FROM call_records WHERE campaign_id = [ID];` All records should remain `PENDING`.

-----

## 📊 Monitoring Dashboard

Import the provided `Campaign Dashboard.json` into Grafana to view:

  * **Overall Success Rate:** Percentage of total attempts resulting in completion.
  * **Call Outcome Distribution:** Pie chart of terminal success vs. terminal failure.
  * **Live Threads:** Monitoring the health of the background worker pool.
  * **Retry Pressure:** The volume of calls currently in the retry loop.

-----

## 🧪 Testing

Run the full test suite to ensure system integrity:

```bash
./mvnw test
```

  * **Integration Tests:** Verify Multipart file handling and DB persistence.
  * **Unit Tests:** Validate business hour logic and mock telephony behavior.

-----

[View Design Documentation](DESIGN.md)
