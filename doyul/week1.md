1. 프로세스와 스레드 소개

- 프로세스: 실행 중인 프로그램의 인스턴스
- 스레드: 프로세스 내에서 실행되는 작업의 가장 작은 단위
- 멀티태스킹: 여러 프로세스를 동시에 실행하는 것
- 멀티스레딩: 하나의 프로세스 내에서 여러 스레드를 동시에 실행하는 것
- 프로세스 vs 스레드:
  - 프로세스는 독립적인 메모리 공간을 가짐
  - 스레드는 프로세스의 메모리를 공유함
- 스레드의 구성 요소: 스택, 레지스터 집합, 프로그램 카운터

2. 스레드 생성과 실행

- Thread 클래스 상속 방법:

  ```java
  class MyThread extends Thread {
    public void run() {
      // 스레드 실행 코드
    }
  }
  ```

- Runnable 인터페이스 구현 방법:

  ```java
  class MyRunnable implements Runnable {
    public void run() {
      // 스레드 실행 코드
    }
  }
  ```

- 스레드 시작:

  ```java
  Thread thread = new Thread(new MyRunnable());
  thread.start();
  ```

- 주의사항: run() 메서드 직접 호출 X, start() 메서드 사용

3. 스레드 제어와 생명 주기 1

- 스레드 상태: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
- join() 메서드: 다른 스레드의 완료를 기다림
- sleep() 메서드: 지정된 시간 동안 스레드를 일시 정지
- interrupt() 메서드: 스레드의 작업을 취소하도록 요청
- yield() 메서드: 다른 스레드에게 실행 양보

4. 스레드 제어와 생명 주기 2

- 스레드 동기화: 여러 스레드가 공유 자원에 안전하게 접근할 수 있도록 함
- synchronized 키워드: 메서드나 블록에 사용하여 동기화
- wait(), notify(), notifyAll() 메서드: 스레드 간 통신
- volatile 키워드: 변수의 가시성 보장
- 데드락: 둘 이상의 스레드가 서로의 작업이 끝나기를 기다리며 무한정 대기하는 상황
- 스레드 안전성: 여러 스레드에서 동시에 접근해도 문제가 발생하지 않는 코드
