.PHONY: build run test dist clean help

help:
	@echo "TranslitUA Plugin - Available targets:"
	@echo "  make build    - Build the plugin"
	@echo "  make run      - Run the plugin in sandbox IntelliJ instance"
	@echo "  make test     - Run tests"
	@echo "  make dist     - Build plugin distribution"
	@echo "  make clean    - Clean build artifacts"
	@echo "  make help     - Show this help message"

build:
	./gradlew build

run:
	./gradlew runIde

test:
	./gradlew test

dist:
	./gradlew buildPlugin

clean:
	./gradlew clean