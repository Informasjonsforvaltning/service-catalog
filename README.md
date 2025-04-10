# Service Catalog

This application provides an API for the management of services.

A service is defined according to the [CPSV-AP-NO](https://data.norge.no/specification/cpsv-ap-no#_abstract_in_english) specification.

## Getting Started

These instructions will give you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

Ensure you have the following installed:

- Java 21
- Maven
- Docker

### Running locally

1. Clone the repository
```sh
git clone https://github.com/Informasjonsforvaltning/service-catalog.git
cd service-catalog
```

2. Start MongoDB
```sh
docker compose up
```

3.	Start the application (either through your IDE using the dev profile, or via CLI):
```sh
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Running tests

```sh
mvn verify
```