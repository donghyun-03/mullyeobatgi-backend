# 1단계: Gradle 로 실행 파일(jar) 만들기
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 먼저 받아 두면 코드만 바뀔 때 빌드가 빨라진다
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
# 윈도우에서 커밋하면 실행 권한이 빠지므로 여기서 붙인다
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q > /dev/null

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# 2단계: 실행에 필요한 것만 담기
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Seoul
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
# 무료 서버의 작은 메모리에 맞춰 힙을 컨테이너 메모리의 70% 로 제한한다
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=70 $JAVA_OPTS -jar app.jar"]
