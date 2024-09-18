# synchronized
특정 메서드 또는 영역에 대해 lock을 통해 한 번에 하나의 스레드만 접근할 수 있도록 설정(임계영역 설정)하고, 임계 영역에 대한 동기화를 제공하는 키워드

이를 통해 여러 스레드가 동시에 해당 메서드 또는 영역에 접근하는 것을 막을 수 있음
- 여러 스레드가 동시에 접근할 수 있는 자원(e.g. 객체, 메서드)에 대한 일관성있는 접근 제공

<br>

### 락(lock)
`synchronized` 블록 영역에 한 스레드가 작업을 하려고 하면 다른 스레드는 접근할 수 없음

모든 객체는 락(lock)을 하나씩 가지고 있고, 모든 객체가 갖고 있으니 고유 락(Intrinsic Lock)이라고 부름
- 또는 모니터 락(Monitor Lock) 또는 모니터(Monitor) 라고 부르기도 함

자바에서는 이러한 고유 lock(Intrinsic lock)을 통해 스레드의 접근을 제어

<br>

#### 락의 단위
> 클래스 레벨(Class level lock)이 객체 레벨(Object level lock)보다 더 범위가 큼


1. **Object level lock**: 객체의 인스턴스에 대한 lock

    ```java
    private void syncWithMethod(String msg) {
        // this: 객체 레벨 lock
        synchronized (this) { doSomething(); }
    }
    ```
    - BankAccount 클래스의 모든 인스턴스가 각자의 개별적인 `lock`을 관리

<br>

2. **Class level lock**: 클래스의 모든 인스턴스에 대한 lock

    ```java
    private void syncWithMethod(String msg) {
        // BankAccount.class: 클래스 레벨 lock
        synchronized (BankAccount.class) { doSomething(); }
    }
   ```
    - BankAccount 클래스의 모든 인스턴스가 하나의 `lock`을 통해 관리
    - `static` 데이터를 thread safe하게 작업하기 위해 사용

<br>

### 임계영역(critical section)
둘 이상의 쓰레드가 공유 자원에 접근할 때 오직 한 쓰레드만 접근을 보장하는 영역

<br>

## 메모리 동기화 및 synchronized
### 배경
멀티스레드 환경에서 공유 자원에 대한 갱신 작업 시 메모리 동기화 문제가 발생할 수 있음

일반적으로 인스턴스의 멤버 변수에 접근하는 경우
- 물론 단순 조회만 한다면 문제가 없음
    ```java
     class Immutable {
        private final int value;
    
        public Immutable(int value) {
            this.value = value;
        }
    
        public int getValue() {
            return value;
        }
    }
    ```
  - 멤버 변수인 `value`를 위해 별도 동기화 처리가 필요하지 않음
- 또한 메서드 내 지역변수는 스레드 별로 별도로 생성되기 때문에 추가적인 동기화 처리가 필요하지 않음

### 해결
`synchronized` 키워드를 이용해서 스레드의 접근을 제한할 수 있음

```java
class Counter {
    private int count = 0;

    public synchronized void increment() {
        count = count + 1;
    }

    public synchronized int getCount() {
        return count;
    }
}
```

<br>

### 장점
멀티스레드 환경에서 동시 접근을 편리하게 제어할 수 있음
- `synchronized` 메서드나 블록이 완료되면 자동으로 락 대기중인 다른 스레드의 잠금이 해제됨
- 개발자가 직접 특정 스레드를 깨우도록 관리해야 할 필요가 없음

### 단점
무한 대기: `BLOCKED` 상태의 스레드는 락이 풀릴 때 까지 무한 대기함
- 타임아웃 또는 인터럽트가 없음

공정성: 락이 돌아왔을 때 `BLOCKED` 상태의 여러 스레드 중에 어떤 스레드가 락을 획득할지 알 수 없음
- 최악의 경우 특정 스레드가 너무 오랜기간 락을 획득하지 못할 수 있음

그 외에 `synchronized`의 문제는 아니지만, Spring에서 DB와 함께 `@Transactional`와 함께 사용했을 때 원하는 결과를 얻지 못할 수 있음 (서버가 1개인 경우에도)
- e.g. decrease 로직(특정 값 DB 조회 -> 차감 -> DB 저장)이 있고, `synchronized` 키워드를 통해 decrease 메서드의 동시 접근을 제한하는 경우
  ```java
  public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
      this.ticketRepository = ticketRepository;
    }

    @Transactional
    public synchronized void decrease(Long id, Long quantity) {
      var ticket = ticketRepository.findById(id).orElseThrow();
      ticket.decrease(quantity);
      ticketRepository.save(ticket);
    }
  }
  ```
  - decrement 로직 자체는 동시 접근을 제어하지만, 실제 DB 반영(커밋) 전에 다른 스레드에서 호출이 가능 (caused by AOP 구현 방법)

### 그러면?
`synchronized` 의 가장 치명적인 단점은 락을 얻기 위해 `BLOCKED` 상태가 되면 락을 얻을 때까지 무한 대기

이런 문제를 해결하기 위해 자바 1.5부터 `java.util.concurrent` 라는 동시성 문제 해결을 위한 패키지가 추가됨
- `Locks` 패키지, `Atomic` 패키지, `Executor`, `Concurrent` 자료구조, `CountDownLatch`, ...
- ref. https://memo-the-day.tistory.com/120

참고로 단순하고 편리하게 사용하기에는 `synchronized` 가 좋으므로, 목적에 부합한다면 `synchronized` 를 사용하면 됨