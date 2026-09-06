## 1.2.0

Move the session onto the shared auth libraries. Five views each decided
separately whether you were signed in; they now read one shared session, so the
page follows a login or a logout without a reload.

## 1.1.0

Add the ability to add choices when answering a question. Update dependencies.

## 1.0.0

Update UI dependencies; this is releasable now.

## 0.9.0

Upgraded to Micronaut 5.0.2, JDK 25, and latest dependencies:
- Micronaut: 4.5.1 → 5.0.2
- Shadow plugin: 8.1.1 → 9.4.1 (com.gradleup.shadow)
- micronaut-application plugin: 4.3.8 → 5.0.0
- appengine-gradle-plugin: 2.8.0 → 2.8.7
- gradle-acceptance-plugin: 2.7.2 → 2.8.2
- Reactor: 3.6.7 → 3.8.5
- Gson: 2.11.0 → 2.14.0
- micronaut-utility-beans: 1.6.0 → 2.0.0
- JDK and App Engine runtime updated from 17/java17 to 25/java25
- Added PR build workflow (build.yml)

## 0.8.0

Update dependencies.

## 0.7.0

Backend support for private questions and due dates.

## 0.6.0

Enable requests to a specific user.

## 0.5.0

Update dependencies.

## 0.4.0

Add ability to auto-answer with Chat GPT.

## 0.3.0

Ask and answer questions. Categorize questions as 'mine', 'unanswered', 'pending', and 'all'