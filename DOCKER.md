# Docker Guide

This project was dockerised to run as a lightweight Spring Boot + PostgreSQL stack.

## What Was Added

- `Dockerfile`
  Uses a multi-stage build with Alpine-based Java images:
  - `eclipse-temurin:21-jdk-alpine` for building
  - `eclipse-temurin:21-jre-alpine` for runtime
- `docker-compose.yml`
  Starts:
  - the Spring Boot API container
  - a PostgreSQL 16 Alpine container
- `.dockerignore`
  Keeps the Docker build context small
- `.env.example`
  Provides the environment variables needed to run the app

## How It Works

### 1. Multi-stage build

The Docker image is built in two stages:

1. Build stage
   Maven packages the Spring Boot application into a jar.
2. Runtime stage
   Only the packaged jar is copied into a smaller Alpine runtime image.

This keeps the final image lighter because Maven, source files, and build tools are not included in the runtime image.

### 2. Alpine-based image

The image uses Alpine-based Eclipse Temurin Java 21 images to reduce size while still matching the project's Java version.

### 3. Non-root runtime

The application runs as a non-root user inside the container for safer defaults.

### 4. Port mapping

The Spring Boot app runs inside the container on port `8081`, but Docker publishes it on host port `80` by default.

That means:

- inside container: `8081`
- on your machine/server: `80`

So you can open:

```text
http://localhost
```

without adding a port.

### 5. Environment-driven config

The application is configured to read important settings from environment variables, including:

- server port
- database connection
- JWT settings
- mail settings
- storage settings

This makes the same image usable across local, test, and production environments.

## Files Used

- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`
- `.env`
- `.env.example`
- `src/main/resources/application.properties`

## Setup Steps

### 1. Copy the env file

In PowerShell:

```powershell
Copy-Item .env.example .env
```

### 2. Edit `.env`

Set the values you need, especially:

- `JWT_SECRET`
- `POSTGRES_PASSWORD`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SUPABASE_STORAGE_ACCESS_KEY`
- `SUPABASE_STORAGE_SECRET_KEY`

Important defaults:

- `HOST_PORT=80`
- `APP_PORT=8081`
- `POSTGRES_PORT=5000`

## Run The Project

### Build and start everything

```powershell
docker compose up -d --build
```

This will:

- build the Spring Boot image
- pull PostgreSQL if needed
- create the Docker network
- create the database volume
- start both containers

### Check running containers

```powershell
docker ps
```

### View app logs

```powershell
docker compose logs -f app
```

### View database logs

```powershell
docker compose logs -f db
```

## Access The App

API:

```text
http://localhost
```

Database from host machine:

```text
localhost:5000
```

Container names:

- `connect-api`
- `connect-db`

## Stop The Project

### Stop containers

```powershell
docker compose down
```

### Stop containers and remove database volume

Warning: this deletes the stored Postgres data.

```powershell
docker compose down -v
```

## Rebuild After Changes

If you change Java code, config, or Docker files:

```powershell
docker compose up -d --build
```

## Build Only The Image

If you only want to build the API image:

```powershell
docker build -t connect-api:alpine .
```

## Deploy On A Server

### Option 1: Build on the server

```bash
git clone <your-repo-url>
cd connect
cp .env.example .env
docker compose up -d --build
```

Then edit `.env` with production values before or after the copy step.

### Option 2: Push image to a registry

Build locally:

```powershell
docker build -t your-dockerhub-user/connect-api:latest .
docker push your-dockerhub-user/connect-api:latest
```

Then update compose on the server to use the pushed image.

## Healthcheck

The API container includes a Docker healthcheck.

Because the root endpoint is protected by Spring Security and returns `401`, the healthcheck treats `200` or `401` as a healthy response once the app is ready.

## Troubleshooting

### Port 80 is already in use

Change this in `.env`:

```env
HOST_PORT=8080
```

Then restart:

```powershell
docker compose up -d --build
```

### App is running but returns `401`

That is expected on protected endpoints if authentication is required.

### Check container health

```powershell
docker inspect --format "{{json .State.Health }}" connect-api
docker inspect --format "{{json .State.Health }}" connect-db
```

### App logs

```powershell
docker logs connect-api
```

### Database logs

```powershell
docker logs connect-db
```

## Summary

This Docker setup makes the project:

- lightweight with Alpine images
- easier to run locally
- easier to deploy
- configurable through environment variables
- accessible on default HTTP port `80`
