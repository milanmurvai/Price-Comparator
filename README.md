# Price Comparator

A Spring Boot application that imports product prices and discounts from CSV files, stores them in a database, and provides REST endpoints for shopping basket optimization, listing discounts, price alerts, price history, and recommendations.

## 🗂️ Project Structure

- **model/** – JPA entities
- **repository/** – Spring Data JPA repositories
- **service/** – business logic for each functionality 
- **controller/** – REST endpoints for handling client requests 
- **dto/** – request and response objects used for communication between frontend and backend
- **config/** – application configuration 
- **resources/** – CSVs for test data, application config

## ⚙️ Environment Variables

To configure the database connection, the following environment variables must be defined and the database must be created:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/price_comparator
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password
```

To enable email notifications for price alerts, also configure:

```env
SPRING_MAIL_USERNAME=your_email@example.com
SPRING_MAIL_PASSWORD=your_email_password
```
**Note**: This email will be used as a sender for price alerts notifications.

---
## ▶️ How to Run

```bash
# Build
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

After starting the backend, access the Swagger UI at:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


## ✅ Assumptions & Simplifications

- CSV files are named in format `store_YYYY-MM-DD.csv` or `store_discounts_YYYY-MM-DD.csv`
- CSVs are uploaded via `POST /import/csv`
- Discount lines are ignored in the following cases:
   - The product does not exist in the database
   - A discount for the same product/store/date already exists
- Overlapping discounts are automatically handled as follows:
   - If a new discount is entirely inside an existing one, the old one is split into two parts
   - If a new discount fully matches an old one (same interval, different import date), the old one is replaced
   - If the new discount is completely outside and includes one or more old discounts, they are deleted
   - If the new discount overlaps at the beginning, the old one is truncated on the right
   - If the new discount overlaps at the end, the old one is truncated on the left

## 📬 API Usage & Examples

### POST /import/csv
Upload CSV file. Filename must follow the expected format.

### POST /basket/optimize
```json
{
  "productIds": ["P001", "P002"],
  "date": "2025-05-01"
}
```

### POST /prices/alerts
```json
{
  "userEmail": "test@example.com",
  "productName": "lapte",
  "targetPrice": 4.99
}
```

### POST /prices/history
```json
{
  "productName": "lapte",
  "startDate": "2025-05-01",
  "endDate": "2025-05-10",
  "store": null,
  "brand": null,
  "category": null
}
```

### GET /discounts/best?date=2025-05-01
Get best discounts per product for given date.

### GET /discounts/today
List all discounts imported today.

### GET /recommendations
```json
{
  "productName": "lapte",
  "date": "2025-05-01"
}
```

## 🧪 Testing

Run tests with:

```bash
./mvnw test
```

Code coverage includes:
- Controllers
- Services 
- Repositories
---
