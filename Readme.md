# 🌍 **Samvaya** - Community Events Platform

> A scalable, full-stack **community events platform** designed to empower social initiatives.
> Connecting communities for meaningful causes: blood donations, NGO activities, hunger relief camps, environmental drives, and more.

## 🎯 **Vision & Mission**

**Samvaya** (Sanskrit: "Connection" or "Community") is a purpose-driven events platform that brings together individuals, organizations, and communities to create positive social impact. Unlike commercial event platforms, Samvaya focuses exclusively on **non-profit and community-driven initiatives** that address real-world challenges.

### **Social Impact Focus**

Samvaya serves as a digital bridge for:

- **Blood Donation Drives**: Connect donors with blood banks and emergency needs
- **NGO Activities**: Organize and promote volunteer opportunities, awareness campaigns, and fundraising events
- **Hunger Relief Camps**: Coordinate food distribution drives and community kitchens
- **Environmental Initiatives**: Tree planting, clean-up drives, sustainability workshops
- **Educational Outreach**: Free workshops, skill-building sessions, literacy programs
- **Healthcare Camps**: Free medical check-ups, vaccination drives, health awareness programs
- **Disaster Relief**: Emergency response coordination and resource mobilization

### **Community-Centric Approach**

- **Zero Transaction Fees**: All events are free to create and attend
- **Volunteer-Driven**: Built by and for the community
- **Transparency**: Open tracking of event participation and impact metrics
- **Accessibility**: Designed to be inclusive and accessible to all communities
- **Trust & Safety**: Verified organizers and community moderation

---

## 🏗️ **Technical Architecture**

Samvaya is built with **enterprise-grade scalability** to handle millions of users and events, ensuring the platform remains responsive and reliable as communities grow.

### **High-Level Architecture**

```
┌─────────────┐
│   Frontend  │ (Next.js - User & Admin Interfaces)
└──────┬──────┘
       │
┌──────▼──────────────────────────────────────┐
│         API Gateway (Spring Cloud)          │
│  - JWT Authentication                       │
│  - Rate Limiting (Redis)                    │
│  - Request Routing                          │
└──────┬──────────────────────────────────────┘
       │
   ┌───┴───────────────────────────────────────┐
   │                                           │
┌──▼──────────┐  ┌──────────┐  ┌──────────┐  │
│ Auth Service│  │  Event   │  │  Ticket  │  │
│             │  │  Service │  │  Service │  │
└─────────────┘  └──────────┘  └──────────┘  │
                                             │
┌─────────────┐  ┌──────────┐  ┌──────────┐  │
│Notification│  │   Chat   │  │  Media   │  │
│  Service   │  │  Service │  │  Service │  │
└─────────────┘  └──────────┘  └──────────┘  │
                                             │
┌─────────────┐  ┌──────────┐               │
│  Analytics  │  │Elasticsearch│            │
│  Service    │  │  (Search) │               │
└─────────────┘  └──────────┘               │
   │                                           │
   └───────────────────────────────────────────┘
         │
    ┌────┴────────────────────┐
    │                         │
┌───▼────┐              ┌─────▼────┐
│  Kafka │              │   Redis  │
│  (MQ)  │              │ (Cache)  │
└────────┘              └──────────┘
```

### **Core Technologies**

#### **Backend (Microservices Architecture)**
- **Spring Boot 3.x**: All microservices built with Spring Boot
- **Spring Cloud Gateway**: API Gateway for routing and authentication
- **Spring Data JPA**: Database access layer
- **Spring WebSocket**: Real-time communication
- **8 Microservices**:
  - `api-gateway`: Central entry point, JWT validation, rate limiting
  - `auth-service`: User authentication and authorization
  - `event-service`: Event CRUD, search, feed generation
  - `ticket-service`: Registration/booking management
  - `notification-service`: Email/SMS notifications, webhooks
  - `chat-service`: Real-time event discussions
  - `media-service`: Image and media uploads
  - `analytics-service`: Batch processing and insights

#### **Frontend**
- **Next.js 16**: React framework with SSR/SSG
- **TypeScript**: Type-safe development
- **Tailwind CSS**: Modern, responsive UI
- **Two Interfaces**:
  - User-facing: Event discovery, registration, chat, feed
  - Admin/Manager: Analytics dashboard, event management, webhook management

#### **Data Layer**
- **PostgreSQL 16**: Primary relational database (ACID transactions)
- **Redis 7**: 
  - Caching (event details, popular events, feed data)
  - Rate limiting (prevent abuse)
  - Session management
  - Real-time presence tracking
  - Chat message storage
- **Elasticsearch**: Full-text search for events

#### **Message Queue & Streaming**
- **Apache Kafka**: 
  - Async event processing (ticket confirmations → notifications)
  - Decoupled service communication
  - Event sourcing for analytics
  - High-throughput message handling

#### **Infrastructure**
- **Docker & Docker Compose**: Containerization and orchestration
- **Nginx**: Reverse proxy, load balancing, CDN simulation
- **Spring Actuator**: Health checks and monitoring

---

## 🚀 **Scalability Features**

### **1. Microservices Architecture**
- **Independent Scaling**: Each service can scale independently based on load
- **Fault Isolation**: Failure in one service doesn't cascade
- **Technology Flexibility**: Services can use different tech stacks if needed
- **Team Autonomy**: Different teams can own different services

### **2. Kafka Message Queues**
- **Async Processing**: Decouples services for better performance
- **Event-Driven Architecture**: Real-time event propagation
- **High Throughput**: Handles millions of messages per second
- **Durability**: Messages persisted for reliability
- **Use Cases**:
  - Ticket confirmations → Notification service
  - Event updates → Analytics service
  - User actions → Feed generation

### **3. Redis Caching & Rate Limiting**
- **Multi-Layer Caching**:
  - Event details (reduce DB load)
  - Popular events list
  - Feed data (sorted sets)
  - User sessions
- **Rate Limiting**:
  - Per-IP limits for public endpoints
  - Per-user limits for authenticated routes
  - Prevents abuse and ensures fair usage
- **Presence Tracking**: Real-time online user counts per event
- **TTL + LRU Eviction**: Automatic cache management

### **4. WebSocket Real-Time Communication**
- **Live Event Chat**: Participants can discuss during events
- **Presence Indicators**: See who's online
- **Low Latency**: Direct bidirectional communication
- **SockJS Fallback**: Ensures compatibility across browsers
- **Scalable**: Can be distributed across multiple instances

### **5. Database Sharding Strategy**
- **City-Based Sharding**: Events partitioned by location
- **Organizer-Based Sharding**: Large organizers get dedicated shards
- **Shard Resolution Logic**: Automatic routing to correct shard
- **Horizontal Scaling**: Add more database nodes as needed

### **6. Advanced Caching Strategies**
- **Cache-Aside Pattern**: Check cache, fallback to DB, update cache
- **Bloom Filters**: Quick existence checks before DB queries
- **Consistent Hashing**: Distributed cache node routing
- **Feed Precomputation**: Popular/trending events pre-calculated

### **7. Search & Discovery**
- **Elasticsearch Integration**: Full-text search across events
- **Filtered Search**: By city, category, date, organizer
- **Feed Algorithm**: Personalized event recommendations
- **Pagination**: Efficient handling of large result sets

---

## 📊 **Implementation Status**

### ✅ **Fully Implemented**

**Core Services:**
- All 8 microservices operational
- Docker containerization and orchestration
- API Gateway with JWT authentication
- PostgreSQL databases (separate schemas per service)

**Features:**
- User authentication and authorization
- Event CRUD operations with search
- Ticket booking with optimistic locking
- Real-time WebSocket chat
- Notification system with Kafka integration
- Media upload and CDN simulation
- Analytics dashboard with batch processing
- Feed/recommendations system
- Webhook system with retry logic
- Rate limiting (Redis-based)
- Health checks (Spring Actuator)

**Frontend:**
- Next.js user interface (all pages)
- Admin/manager dashboard
- Responsive design with Tailwind CSS

**Infrastructure:**
- Docker Compose setup
- Nginx reverse proxy
- Redis caching layer
- Kafka message queue
- Elasticsearch search

### ⚠️ **Partially Implemented**

- **Sharding**: Design and resolution logic implemented, single DB with shard context (not full multi-database)
- **Consistent Hashing**: Conceptual implementation for cache routing
- **CDN**: Simulated via Nginx with caching headers
- **Load Balancing**: Single instance per service (no multi-instance LB tested)
- **Stream Processing**: Batch analytics only (no real-time streaming)

### ❌ **Not Implemented**

- Long polling (went straight to WebSocket)
- External STOMP broker
- Multi-node Redis with consistent hashing
- Full observability dashboards (Grafana/Prometheus)
- gRPC/tRPC/GraphQL (REST APIs only)
- Multi-instance horizontal scaling tests
- Real-time stream processing
- CDC (Change Data Capture)

---

## 🎯 **Key System Design Concepts Covered**

This project demonstrates **production-ready system design patterns**:

✅ **APIs, REST, API Gateways, JWT, Webhooks**  
✅ **Load Balancing, Proxy vs Reverse Proxy, CDN**  
✅ **CAP Theorem, Sharding, Partitioning, Consistent Hashing**  
✅ **Caching Strategies, Eviction Policies (LRU/TTL)**  
✅ **Availability, Services & Databases in System Design**  
✅ **SQL vs NoSQL, ACID Transactions**  
✅ **Sync vs Async, Message Queues (Kafka)**  
✅ **Algorithms in Distributed Systems, Bloom Filters**  
✅ **Rate Limiting, Idempotency, Concurrency vs Parallelism**  
✅ **WebSockets, Stateful vs Stateless**  
✅ **Batch Processing, Analytics**

---

## 🌟 **Social Impact Features**

### **Community Engagement**

1. **Event Discovery**
   - Browse events by cause (blood donation, hunger relief, education, etc.)
   - Filter by location, date, and organizer
   - Personalized feed based on interests and past participation

2. **Real-Time Collaboration**
   - Live chat during events for coordination
   - Presence indicators showing active participants
   - Event updates and announcements

3. **Transparency & Trust**
   - Verified organizer badges
   - Event impact metrics (participants, donations, reach)
   - Community ratings and feedback
   - Public event history

4. **Accessibility**
   - Multi-language support (planned)
   - Mobile-responsive design
   - Low-bandwidth optimizations
   - Free access for all users

### **Organizer Tools**

- **Event Management Dashboard**: Create, edit, and manage events
- **Analytics**: Track participation, engagement, and impact
- **Webhook Integration**: Connect with external systems (donation platforms, CRM)
- **Notification System**: Automated emails/SMS to participants
- **Media Management**: Upload event images and documents

### **Impact Tracking**

- **Participation Metrics**: Number of registrations, attendees
- **Community Growth**: New users, repeat participants
- **Event Success Rates**: Completion vs cancellation
- **Geographic Reach**: Events by city/region
- **Cause Categories**: Distribution of events by type

---

## 🚀 **Getting Started**

### **Prerequisites**

- Docker and Docker Compose
- Java 17+
- Node.js 18+ and npm/yarn
- PostgreSQL 16 (or use Docker)
- Redis 7 (or use Docker)
- Kafka 7.6.0 (or use Docker)

### **Quick Start**

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Springboot
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres redis kafka zookeeper elasticsearch nginx
   ```

3. **Build and start microservices**
   ```bash
   ./gradlew build
   docker-compose up -d
   ```

4. **Start frontend**
   ```bash
   cd eventverse/frontend
   npm install
   npm run dev
   ```

5. **Access the application**
   - User Interface: `http://localhost:3000`
   - Admin Dashboard: `http://localhost:3000/admin`
   - API Gateway: `http://localhost:8080`

### **Configuration**

- Create `application.properties` files for each service (see `.gitignore`)
- Configure database connections, Redis, Kafka endpoints
- Set JWT secrets and other environment variables
- See `Commands.md` for detailed setup instructions

---

## 📁 **Project Structure**

```
Springboot/
├── eventverse/
│   ├── backend/
│   │   ├── api-gateway/
│   │   ├── auth-service/
│   │   ├── event-service/
│   │   ├── ticket-service/
│   │   ├── notification-service/
│   │   ├── chat-service/
│   │   ├── media-service/
│   │   └── analytics-service/
│   └── frontend/
│       └── (Next.js application)
├── docker-compose.yml
├── .gitignore
├── Readme.md
└── Commands.md
```

---

## 🤝 **Contributing**

Samvaya is a community-driven project. Contributions are welcome!

**Areas for Contribution:**
- Feature development
- Bug fixes
- Documentation improvements
- UI/UX enhancements
- Performance optimizations
- Test coverage

---

## 📄 **License**

[Specify your license here]

---

## 🙏 **Acknowledgments**

Built with ❤️ for the community, by the community.

**Samvaya** - Connecting communities, creating impact.

---

*For detailed technical documentation, see `Commands.md` and individual service READMEs.*
