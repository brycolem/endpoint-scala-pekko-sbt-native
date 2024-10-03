FROM sbtscala/scala-sbt:eclipse-temurin-alpine-22_36_1.10.2_3.4.3 AS builder
WORKDIR /app

COPY project/ ./project/
COPY build.sbt .

RUN sbt update

COPY . .

RUN sbt clean stage

FROM openjdk:17-jdk-alpine
WORKDIR /app

RUN apk update && apk add bash
COPY --from=builder /app/target/universal/stage /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8001

CMD ["/app/bin/scala"]
