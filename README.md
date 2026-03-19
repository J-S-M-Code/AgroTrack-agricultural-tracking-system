# AgroTrack: agricultural tracking system
Welcome to my final project for my Systems Engineering degree. Here is an introduction and description of the project.
### What is this?
AgroTrack is a system that unifies and solves the problems we currently face in the agricultural sector. 
This system features real-time visualization of animals and continuous monitoring of crops and fields.

### The reason for the project
It arises from solving the communication and monitoring problems in the sector, since today this depends on 
human activity for monitoring and the lack of generating a record of what happens on the farms.

### Structure
```text
src/main/java/com/agrotrack/
├── domain/                  # The application core
│   ├── model/               # Pure domain entities 
│   ├── exception/           # Business exceptions
│   └── port/                # Ports (Interfaces)
│       ├── in/              # Use cases offered by the application (Interfaces for services)
│       └── out/             # Interfaces that the infrastructure must implement
│
├── application/             # Use case orchestration layer
│   └── service/             # Implementation of incoming ports
│
└── infrastructure/          # Technical details and frameworks (Adapters)
    ├── adapter/             # Port Implementations
    │   ├── in/              # Input Adapters (REST Controllers)
    │   │   ├── web/         # Endpoints (Controllers, DTOs, Mappers)
    │   │   └── messaging/   # RabbitMQ Listeners 
    │   └── out/             # Output Adapters
    │       ├── persistence/ # DB Repository Implementations (JPA Entities, Spring Data Repositories, Mappers)
    │       ├── messaging/   # RabbitMQ Publishers
    │       └── storage/     # MinIO Client Implementation
    │
    └── config/              # Spring configurations (Security, CORS, Beans, Swagger/OpenAPI)
```