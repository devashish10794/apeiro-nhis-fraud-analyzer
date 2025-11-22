# High-Level Design (HLD)

## NHIS Fraud Analyzer System

---

## 1. System Overview

The NHIS Fraud Analyzer is a full-stack web application designed to detect and analyze potentially fraudulent health insurance claims using rule-based algorithms and data analytics. The system processes CSV files containing claims data, applies configurable fraud detection rules, and presents insights through an interactive dashboard.

### 1.1 System Goals

- **Fraud Detection**: Automatically identify suspicious claims using configurable rules
- **Data Processing**: Handle large CSV files with validation and deduplication
- **Analytics**: Provide real-time insights and trends
- **Configurability**: Allow administrators to tune fraud detection parameters
- **Scalability**: Support growing datasets and user base

### 1.2 Key Stakeholders

- **Healthcare Auditors**: Primary users who review flagged claims
- **System Administrators**: Configure scoring rules and manage data
- **Data Analysts**: Analyze trends and patterns
- **Compliance Officers**: Ensure regulatory compliance

---

## 2. System Architecture

### 2.1 Architecture Style

**Layered Architecture with Clean Separation**

```mermaid
graph TB
    subgraph "Client Layer"
        UI[Web Browser]
    end
    
    subgraph "Presentation Layer"
        FE[React Frontend<br/>Vite + TypeScript]
    end
    
    subgraph "API Gateway Layer"
        API[REST API<br/>Spring Boot]
    end
    
    subgraph "Business Logic Layer"
        FS[Fraud Scoring Service]
        IS[Ingestion Service]
        VS[Validation Service]
        MS[Metrics Service]
        CS[Chart Service]
    end
    
    subgraph "Data Access Layer"
        REPO[JPA Repository]
    end
    
    subgraph "Data Layer"
        DB[(PostgreSQL Database)]
    end
    
    UI --> FE
    FE -->|HTTP/REST| API
    API --> FS
    API --> IS
    API --> VS
    API --> MS
    API --> CS
    FS --> REPO
    IS --> REPO
    MS --> REPO
    CS --> REPO
    REPO --> DB
```

### 2.2 Component Architecture

```mermaid
graph LR
    subgraph "Frontend Application"
        Pages[Pages/Routes]
        Components[UI Components]
        Charts[Chart Components]
        API_Client[API Client]
    end
    
    subgraph "Backend Application"
        Controllers[Controllers]
        Services[Services]
        Repositories[Repositories]
        Config[Configuration]
    end
    
    subgraph "External Systems"
        CSV[CSV Files]
        DB[(Database)]
    end
    
    Pages --> Components
    Pages --> Charts
    Pages --> API_Client
    API_Client -->|REST API| Controllers
    Controllers --> Services
    Services --> Repositories
    Services --> Config
    Repositories --> DB
    CSV --> Controllers
```

---

## 3. System Components

### 3.1 Frontend Components

#### 3.1.1 Core Pages

| Component | Purpose | Key Features |
|-----------|---------|-------------|
| **Dashboard Page** | Overview analytics | Metrics cards, charts, date filtering |
| **Claims Page** | Claims management | Pagination, filtering, sorting, search |
| **Admin Page** | System configuration | CSV upload, scoring config |
| **Login Page** | User authentication | Login form (future implementation) |

#### 3.1.2 Shared Components

- **App Sidebar**: Navigation menu
- **Site Header**: Top navigation bar
- **Data Table**: Reusable table with pagination
- **Charts**: Fraud category, score distribution, trends
- **Filters**: Advanced filtering UI
- **UI Components**: Radix-based design system

#### 3.1.3 API Client

```typescript
// Centralized API communication
- getJson<T>(): GET requests
- postMultipart<T>(): File uploads
- putJson<T>(): Update operations
```

### 3.2 Backend Components

#### 3.2.1 Controllers Layer

| Controller | Endpoint Base | Responsibility |
|------------|---------------|----------------|
| **ClaimsController** | `/api/v1/claims` | Claims CRUD and queries |
| **MetricsController** | `/api/v1/metrics` | Analytics and aggregations |
| **AdminController** | `/api/v1/admin` | Data ingestion and config |

#### 3.2.2 Service Layer

| Service | Responsibility |
|---------|----------------|
| **FraudScoringService** | Implements fraud detection algorithm |
| **IngestionService** | CSV parsing, validation, processing |
| **ValidationService** | Data validation rules |
| **MetricsService** | Calculate aggregate metrics |
| **ChartService** | Prepare time-series data |

#### 3.2.3 Repository Layer

```java
// ClaimRepository extends JpaRepository
- Spring Data JPA for CRUD
- JPA Specifications for dynamic queries
- Custom queries for complex aggregations
```

#### 3.2.4 Configuration Components

- **ScoringConfig**: In-memory fraud scoring parameters
- **CorsConfig**: Cross-origin resource sharing
- **Application Properties**: Database, server, multipart config

---

## 4. Data Flow Diagrams

### 4.1 Claims Data Ingestion Flow

```mermaid
sequenceDiagram
    participant Admin
    participant Frontend
    participant Controller
    participant Ingestion
    participant Validation
    participant Scoring
    participant Repository
    participant Database
    
    Admin->>Frontend: Upload CSV File
    Frontend->>Controller: POST /api/v1/admin/ingest
    Controller->>Ingestion: Process file
    Ingestion->>Validation: Validate headers
    Ingestion->>Validation: Validate each row
    Ingestion->>Ingestion: Parse CSV rows
    Ingestion->>Ingestion: Calculate diagnosis medians
    Ingestion->>Scoring: Score all claims
    Scoring-->>Ingestion: Scored claims
    Ingestion->>Ingestion: Deduplicate claims
    Ingestion->>Repository: Save unique claims
    Repository->>Database: Bulk insert
    Database-->>Repository: Confirmation
    Repository-->>Ingestion: Result count
    Ingestion-->>Controller: IngestResult
    Controller-->>Frontend: {total, inserted, skipped}
    Frontend-->>Admin: Success message
```

### 4.2 Claims Query Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Controller
    participant Repository
    participant Database
    
    User->>Frontend: Apply filters
    Frontend->>Controller: GET /api/v1/claims?filters
    Controller->>Controller: Build JPA Specification
    Controller->>Repository: findAll(spec, pageable)
    Repository->>Database: Dynamic SQL query
    Database-->>Repository: Result set
    Repository-->>Controller: Page<Claim>
    Controller->>Controller: Map to DTO
    Controller-->>Frontend: Page<ClaimResponseDto>
    Frontend-->>User: Display paginated results
```

### 4.3 Dashboard Metrics Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant MetricsController
    participant MetricsService
    participant ChartService
    participant Database
    
    User->>Frontend: Load dashboard
    Frontend->>MetricsController: GET /api/v1/metrics
    MetricsController->>MetricsService: calculateMetrics()
    MetricsService->>Database: Aggregate queries
    Database-->>MetricsService: Metrics data
    MetricsService-->>MetricsController: MetricsDto
    MetricsController-->>Frontend: Metrics
    
    Frontend->>MetricsController: GET /api/v1/metrics/chart-data
    MetricsController->>ChartService: getChartData()
    ChartService->>Database: Time-series query
    Database-->>ChartService: Chart data
    ChartService-->>MetricsController: List<ChartDataDto>
    MetricsController-->>Frontend: Chart data
    Frontend-->>User: Render charts
```

---

## 5. Database Architecture

### 5.1 Database Schema

```mermaid
erDiagram
    CLAIMS {
        uuid id PK
        varchar patient_id
        double age
        varchar gender
        date encounter_date
        date discharge_date
        decimal amount_billed
        varchar diagnosis
        varchar fraud_type
        integer fraud_score
        varchar score_category
        text score_reasons
        timestamp created_at
    }
```

### 5.2 Indexes

| Index Name | Columns | Purpose |
|------------|---------|----------|
| `idx_claims_patient` | patient_id | Patient lookup |
| `idx_claims_diagnosis` | diagnosis | Diagnosis filtering |
| `idx_claims_fraudScore` | fraud_score | Score-based queries |
| `pk_claims` | id | Primary key |

### 5.3 Data Volume Considerations

- **Estimated Claims/Year**: 100,000 - 1,000,000
- **Storage per Claim**: ~500 bytes
- **Total Storage (1M claims)**: ~500 MB
- **Growth Rate**: 10-20% annually

---

## 6. Integration Points

### 6.1 External Integrations

```mermaid
graph LR
    App[NHIS Fraud Analyzer]
    DB[(PostgreSQL<br/>Render)]
    CSV[CSV Files]
    Future1[NHIS Database<br/>Future]
    Future2[Email Service<br/>Future]
    Future3[SSO Provider<br/>Future]
    
    App --> DB
    CSV --> App
    App -.->|Future| Future1
    App -.->|Future| Future2
    App -.->|Future| Future3
```

### 6.2 API Contract

- **Protocol**: HTTP/REST
- **Format**: JSON
- **Authentication**: None (future: JWT)
- **CORS**: Enabled for development
- **API Versioning**: URL-based (`/api/v1/`)

---

## 7. Deployment Architecture

### 7.1 Current Deployment

```mermaid
graph TB
    subgraph "Client Browser"
        Browser[Web Browser]
    end
    
    subgraph "Frontend Container"
        FE[React App<br/>Port 3000]
    end
    
    subgraph "Backend Container"
        BE[Spring Boot<br/>Port 8080]
    end
    
    subgraph "Database Server"
        DB[(PostgreSQL<br/>Render Cloud)]
    end
    
    Browser --> FE
    FE --> BE
    BE --> DB
```

### 7.2 Recommended Production Architecture

```mermaid
graph TB
    subgraph "Edge Layer"
        CDN[CDN/CloudFlare]
        LB[Load Balancer]
    end
    
    subgraph "Application Layer"
        FE1[Frontend Instance 1]
        FE2[Frontend Instance 2]
        BE1[Backend Instance 1]
        BE2[Backend Instance 2]
    end
    
    subgraph "Data Layer"
        Primary[(Primary DB)]
        Replica[(Read Replica)]
        Cache[Redis Cache]
    end
    
    subgraph "Storage Layer"
        S3[S3/Object Storage]
    end
    
    CDN --> LB
    LB --> FE1
    LB --> FE2
    FE1 --> BE1
    FE1 --> BE2
    FE2 --> BE1
    FE2 --> BE2
    BE1 --> Cache
    BE2 --> Cache
    BE1 --> Primary
    BE2 --> Replica
    BE1 --> S3
    BE2 --> S3
    Primary --> Replica
```

---

## 8. Security Architecture

### 8.1 Security Layers

```mermaid
graph TB
    subgraph "Network Security"
        HTTPS[HTTPS/TLS]
        CORS[CORS Policy]
    end
    
    subgraph "Application Security"
        Auth[Authentication<br/>Future]
        Authz[Authorization<br/>Future]
        Valid[Input Validation]
    end
    
    subgraph "Data Security"
        Encrypt[Encryption at Rest]
        Sanitize[Data Sanitization]
        Backup[Backups]
    end
    
    HTTPS --> Auth
    CORS --> Valid
    Auth --> Authz
    Authz --> Sanitize
    Valid --> Sanitize
    Sanitize --> Encrypt
    Encrypt --> Backup
```

### 8.2 Data Protection

- **PHI/PII Handling**: Claims contain sensitive health information
- **Compliance**: HIPAA considerations (future implementation)
- **Data Retention**: Configurable retention policies needed
- **Audit Logging**: Track all data access (future)

---

## 9. Scalability Considerations

### 9.1 Horizontal Scaling

- **Stateless Backend**: Can scale horizontally with load balancer
- **Database**: Read replicas for query distribution
- **Frontend**: CDN distribution for static assets

### 9.2 Vertical Scaling

- **Database**: Increase instance size for growing data
- **Backend**: Increase JVM heap for larger batch processing

### 9.3 Performance Optimizations

- Database connection pooling
- Query optimization with indexes
- Pagination for large result sets
- Lazy loading in frontend
- API response caching (future)

---

## 10. Monitoring & Observability

### 10.1 Monitoring Strategy

```mermaid
graph LR
    App[Application]
    Metrics[Metrics Collector]
    Logs[Log Aggregator]
    Traces[Distributed Tracing]
    APM[APM Dashboard]
    Alerts[Alert Manager]
    
    App --> Metrics
    App --> Logs
    App --> Traces
    Metrics --> APM
    Logs --> APM
    Traces --> APM
    APM --> Alerts
```

### 10.2 Key Metrics to Monitor

- **Application**: Response time, error rate, throughput
- **Database**: Connection pool, query time, deadlocks
- **Business**: Claims processed, fraud detection rate
- **Infrastructure**: CPU, memory, disk, network

---

## 11. Disaster Recovery

### 11.1 Backup Strategy

- **Database Backups**: Daily full, hourly incremental
- **Configuration Backups**: Version controlled
- **Data Retention**: 90 days minimum

### 11.2 Recovery Objectives

- **RTO (Recovery Time Objective)**: 4 hours
- **RPO (Recovery Point Objective)**: 1 hour

---

## 12. Technology Decisions Summary

| Decision | Choice | Rationale |
|----------|--------|----------|
| **Backend Language** | Java 21 | Enterprise stability, type safety, performance |
| **Backend Framework** | Spring Boot | Rapid development, extensive ecosystem |
| **Database** | PostgreSQL | ACID compliance, JSON support, open source |
| **Frontend Framework** | React | Component reusability, large ecosystem |
| **UI Library** | Radix UI + Tailwind | Accessibility, customization |
| **Build Tool (Frontend)** | Vite | Fast HMR, modern tooling |
| **Routing** | TanStack Router | Type-safe, file-based routing |
| **API Documentation** | Swagger/OpenAPI | Interactive docs, standardized |

---

## 13. Future Architecture Enhancements

### 13.1 Microservices Migration

- Extract fraud scoring to separate service
- Separate analytics service
- Event-driven architecture with message queues

### 13.2 Machine Learning Integration

- ML-based fraud detection models
- Real-time scoring pipeline
- Model training and deployment

### 13.3 Real-time Processing

- Stream processing for live data
- WebSocket for real-time updates
- Event sourcing for audit trail

