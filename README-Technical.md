# APEIRO NHIS Fraud Analyzer - Technical Documentation

## Overview

The NHIS (National Health Insurance Scheme) Fraud Analyzer is a comprehensive full-stack application designed to detect and analyze potentially fraudulent health insurance claims using rule-based scoring algorithms and data analytics.

## Project Information

- **Project Name**: NHIS Fraud Auditor Dashboard
- **Version**: 0.0.1-SNAPSHOT
- **Organization**: com.nhis
- **Architecture**: Microservices-style monolith with separate frontend and backend

## Technology Stack

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Gradle
- **API Documentation**: Springdoc OpenAPI 3 (Swagger)
- **Key Libraries**: Lombok, PostgreSQL JDBC Driver

### Frontend
- **Language**: TypeScript
- **Framework**: React 19.2.0
- **Routing**: TanStack Router 1.132.0
- **Build Tool**: Vite 7.1.7
- **UI Library**: Radix UI Components
- **Styling**: Tailwind CSS 4.0.6
- **Charts**: Recharts 2.15.4, AmCharts5 5.14.3
- **Icons**: Tabler Icons, Lucide React
- **State Management**: React Hooks + TanStack Router state
- **Data Tables**: TanStack Table 8.21.3

### Infrastructure
- **Deployment**: Docker containers
- **Database Hosting**: Render PostgreSQL
- **Application Hosting**: Configurable (PORT environment variable)

## Documentation Structure

This technical documentation is organized into the following sections:

### 1. [High-Level Design (HLD)](./HLD.md)
- System architecture overview
- Component relationships
- Data flow diagrams
- Integration points
- Deployment architecture

### 2. [Low-Level Design (LLD)](./LLD.md)
- Detailed class diagrams
- Database schema design
- Algorithm implementations
- Component interactions
- Code structure

### 3. [Technology Trade-offs](./Technology-Tradeoffs.md)
- Framework selection rationale
- Database choice justification
- Frontend library decisions
- Alternative approaches considered
- Cost-benefit analysis

### 4. [Testing Strategy](./Testing-Strategy.md)
- Unit testing approach
- Integration testing
- End-to-end testing
- Test coverage goals
- Testing tools and frameworks

### 5. [Performance & Scalability](./Performance-Scalability.md)
- Performance optimization strategies
- Bottleneck identification
- Scaling approaches
- Caching strategies
- Load handling

### 6. [API Documentation](./API-Documentation.md)
- Complete REST API reference
- Request/response examples
- Error handling
- Authentication (future)
- Rate limiting considerations

### 7. [Security & Compliance](./Security-Compliance.md)
- HIPAA compliance considerations
- Data protection strategies
- Access control
- Audit logging
- Security best practices

### 8. [Flow Diagrams](./Flow-Diagrams.md)
- User workflows
- Data ingestion flow
- Fraud scoring process
- Authentication flow (future)
- Error handling flows

### 9. [Deployment Guide](./Deployment-Guide.md)
- Infrastructure requirements
- Environment configuration
- CI/CD pipeline
- Monitoring and logging
- Backup and recovery

## Key Features

### 1. Data Ingestion
- CSV file upload support (up to 25MB)
- Automatic data validation
- Duplicate detection and handling
- Bulk import processing

### 2. Fraud Detection
- Rule-based scoring algorithm (0-100 scale)
- Configurable scoring weights
- Multiple fraud indicators:
  - Zero amount billing
  - Amount vs diagnosis median ratio
  - High-risk diagnoses (infertility, dental, osteoporosis, etc.)
  - Date inconsistencies
  - Pediatric anomalies
- Three risk categories: LOW (0-25), MEDIUM (26-75), HIGH (76-100)

### 3. Analytics Dashboard
- Real-time metrics display
- Interactive charts and visualizations
- Time-series analysis
- Category distribution
- Trend analysis

### 4. Claims Management
- Advanced filtering capabilities
- Pagination and sorting
- Search functionality
- Detailed claim view
- Bulk operations support

### 5. Admin Panel
- Fraud scoring configuration
- Data upload interface
- System settings management

## Core Business Logic

### Fraud Scoring Algorithm

The system uses a weighted scoring approach:

```
Base Score = 0

+ Zero Amount Weight (if amount = 0)
+ Ratio Weight (if amount > threshold * median)
+ Diagnosis Weights (pattern matching)
+ Date Anomaly Weights
+ Pediatric Anomaly Weight

Final Score = clamp(Base Score, 0, 100)
```

### Score Categories
- **LOW**: Score 0-25 (Green indicator)
- **MEDIUM**: Score 26-75 (Yellow indicator)
- **HIGH**: Score 76-100 (Red indicator)

## Data Model

### Core Entity: Claim

```
- id: UUID (Primary Key)
- patientId: String (Indexed)
- age: Double
- gender: String
- encounterDate: LocalDate
- dischargeDate: LocalDate
- amountBilled: BigDecimal(15,2)
- diagnosis: String (Indexed, max 512 chars)
- fraudType: String
- fraudScore: Integer (Indexed)
- scoreCategory: Enum(LOW, MEDIUM, HIGH)
- scoreReasons: Text
- createdAt: OffsetDateTime
```

## API Endpoints Overview

### Claims API (`/api/v1/claims`)
- `GET /` - List claims with filters and pagination
- `GET /{id}` - Get single claim details

### Metrics API (`/api/v1/metrics`)
- `GET /` - Get overall metrics
- `GET /chart-data` - Get time-series data

### Admin API (`/api/v1/admin`)
- `POST /ingest` - Upload CSV file
- `GET /scoring-config` - Get scoring configuration
- `PUT /scoring-config` - Update scoring configuration

## Getting Started

### Prerequisites
- Java 21 or higher
- PostgreSQL 12 or higher
- Node.js 18 or higher
- pnpm package manager

### Backend Setup
```bash
cd apeiro-nhis-fraud-analyzer
./gradlew bootRun
```

### Frontend Setup
```bash
cd apeiro-nhis-fraud-analyzer-front
pnpm install
pnpm dev
```

### Environment Variables

**Backend:**
```
DATABASE_URL=jdbc:postgresql://host:port/database
DATABASE_USERNAME=username
DATABASE_PASSWORD=password
PORT=8080
```

**Frontend:**
```
VITE_BACKEND_URL=http://localhost:8080
```

## Development Workflow

1. Backend changes trigger hot reload via Spring Boot DevTools
2. Frontend changes trigger hot reload via Vite HMR
3. Database migrations handled by Hibernate DDL auto-update
4. API documentation available at `/swagger-ui`

## Project Structure

### Backend
```
src/main/java/com/nhis/fraud/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── exception/      # Custom exceptions
├── repository/     # Data access layer
├── service/        # Business logic
├── spec/           # JPA Specifications
└── util/           # Utility classes
```

### Frontend
```
src/
├── components/     # Reusable UI components
│   ├── ui/        # Base UI components (Radix)
│   ├── charts/    # Chart components
│   └── auth/      # Authentication components
├── hooks/         # Custom React hooks
├── lib/           # Utility libraries
├── pages/         # Page components
└── routes/        # TanStack Router routes
```

## Performance Considerations

- Database indexes on frequently queried fields
- Pagination for large datasets
- Efficient CSV parsing with streaming
- Frontend code splitting via Vite
- Lazy loading of chart libraries
- Optimized database queries with JPA Specifications

## Security Considerations

- CORS configuration for cross-origin requests
- Input validation on all endpoints
- SQL injection prevention via JPA
- File upload size limits
- Data sanitization
- Future: Authentication and authorization

## Future Enhancements

1. User authentication and authorization
2. Role-based access control (RBAC)
3. Machine learning-based fraud detection
4. Advanced reporting and exports
5. Real-time notifications
6. Audit logging
7. Data anonymization
8. API rate limiting
9. Comprehensive test coverage
10. Performance monitoring

