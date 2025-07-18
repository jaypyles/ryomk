# Default target
.DEFAULT_GOAL := help

# Targets
.PHONY: help build up down logs restart backend-clean

help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build Docker images using docker-compose
	docker compose build

up: ## Start containers in detached mode (recreates containers)
	docker compose up -d --force-recreate

down: ## Stop and remove containers, networks, and volumes
	docker compose down

logs: ## Show logs from all containers
	docker compose logs -f

restart: down up ## Restart containers

backend-clean: ## Clean the Spring Boot app target directory
	docker compose exec app ./mvnw clean
