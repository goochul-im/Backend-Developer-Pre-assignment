# 백엔드 개발자 사전과제

## 기술 스택
- kotlin  1.9.25
- spring boot 3.5.9
- postgresql
- h2 (test)
- spring ai (openai)
- spring security

## 실행 방법
application.yml 파일의 추가가 필요합니다. 다음 파일을 따라 추가하세요.

## 구현 완료 기능
챗봇 스트림 기능을 제외한 모두 구현 완료하였습니다.

```markdown
spring:
  datasource:
    url: 
    username: 
    password: 
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: 
    show-sql: 
    properties:
      hibernate:
        format_sql: 
        dialect: org.hibernate.dialect.PostgreSQLDialect
    open-in-view: false
  ai:
    openai:
      api-key: 
      chat:
        options:
          model: gpt-4o

jwt:
  secret: 
  expiration: 3600000
```

src/test/resources 에 application-test.yml을 추가하세요.
```markdown
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: 
    properties:
      hibernate:
        format_sql: 
    open-in-view: false

  h2:
    console:
      enabled: true

  ai:
    openai:
      api-key: 
      chat:
        options:
          model: gpt-4o

jwt:
  secret: 
  expiration: 3600000
```

## 과제 채점
### 과제 분석 방법
claude code를 이용해 plan을 세워서 ERD와 구현계획을 세워달라고 요청하였고, 이 순서에 따라 구현을 진행했습니다.

### AI 사용법
spring security를 사용한 인증/인가 방식같이 도메인과 연관되지 않은 부분은 모두 AI에게 구현을 맡겼습니다.
또한 구현한 기능에 대한 단위 테스트/통합 테스트 또한 구현을 맡겼습니다.

챗봇 기능을 구현할 때, 버전에 맞지 않는 코드를 답변한 적이 많아 직접 공식문서를 참고하여 리팩토링하였습니다.

### 구현이 어려웠던 기능
구현에 실패한 챗봇 스트림입니다. 처음 구현하는 기능이기도 하고 AI에게 질문을 해도 답변을 제대로 얻지 못했고, 
공식 문서를 참고해서 구현하려 해도 헥사고날 아키텍처에는 어떻게 적용하면 될지 갈피가 잡히지 않아 구현에 실패하였습니다.
