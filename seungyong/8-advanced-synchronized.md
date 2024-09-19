# 섹션 8. 고급 동기화 - concurrent.Lock

## LockSupport

### synchronized 단점

단점

- 무한 대기 : `BLOCKED` 상태의 스레드는 락이 풀릴 때 까지 무한 대기
    - 타임 아웃, 인터럽트 모두 불가능
- 공정성 : 락이 반환된 후, 어떤 스레드가 락을 획득할 지 알 수 없음 → 기아 상태(starvation)에 빠질 수 있음

이러한 동시성 문제를 해결하기 위해 자바 1.5 부터 `java.util.concurrent` 라이브러리 패키지를 추가함

### LockSupport 기능

스레드를 `WAITING` 상태로 변경한다.

`WAITING` 상태는 누가 깨워주기 전까지 계속 대기 → CPU 스케줄링X

- park() : 스레드를 `WAITING` 상태로 변경
- parkNanos(nanos) : 스레드를 나노초 동안만 `TIMED_WAITING` 상태로 변경
    - 지정한 나노초가 지나면 `RUNNABLE` 로 상태 변경
- unpark() : `WAITING` 상태의 대상 스레드를 `RUNNABLE` 상태로 변경

unpark()는 특정 스레드를 지정하는 매개변수가 있다

→ 실행중인 스레드가 스스로 park()를 호출해 대기상태에 빠질 수 있지만, 대기상태에서는 자신의 코드를 실행할 수 없으므로 스스로 깰 수 없다 → 외부 스레드의 도움으로 깨어나야 하기 때문

### 인터럽트 사용

`WAITING` 상태의 스레드에 인터럽트가 발생하면 `WAITING` 상태에서 `RUNNABLE` 상태로 변하면서 깨어남

### 시간 대기

parkNanos(nanos)를 통해 특정 시간만 대기 가능

이 경우 스레드가 나노초 동안만 `TIMED_WAITING` 상태에 빠지고, 이 후 `RUNNABLE` 로 다시 변경된다.

### BLOCKED vs WAITING

|  | 인터럽트 | 용도 |
| --- | --- | --- |
| BLOCKED | 변화 없음 | synchronized에서 (모니터)락을 얻기 위해 사용 |
| WAITING | RUNNABLE로 상태 변경 | 특정 조건이나 시간 동안 대기할 때 발생 (범용적 대기) |

Thread.join(), LockSupport.park(), Object.wait()과 같은 메서드 호출 시 `WAITING` 상태가 된다.

→ 매개변수를 넣으면 `TIMED_WAITING`

`BLOCKED` , `WAITING` , `TIMED_WAITING` 모두 스레드가 대기, 실행 스케줄링에 들어가지 않는다

→ CPU를 실행하지 않음

LockSupport는 특정 스레드만 락을 가질 수 있도록 하고, 어떤 스레드가 대기중인지 알 수 있는 자료구조도 구현을 직접 해야한다. → 너무 저수준이다.

이를 위해 자바는 Lock 인터페이스와 ReentrantLock이라는 구현체로 미리 다 구현해놓았다.

## Lock 인터페이스

`synchronized` 와 `BLOCKED` 상태를 통한 임계 영역 관리의 한계를 극복하기 위해 사용됨

```java
// 안전한 임계 영역을 위한 Lock 인터페이스
// 무한 대기 문제 해결 가능
// 내부에서 LockSupport 사용
public interface Lock {
     void lock();
     void lockInterruptibly() throws InterruptedException;
     boolean tryLock();
     boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
     void unlock();
     Condition newCondition();
}
```

**void lock()**

- 락을 획득한다. 이미 다른 스레드가 획득했다면, 반환될 때 까지 현재 스레드는 `WAITING` 상태에 빠진다.
    - 인터럽트에 응답X
- lock() 메서드 안이기 때문에 인터럽트가 발생하는 순간 `RUNNABLE` 로 되었다가 강제로 다시 `WAITING` 상태로 변경된다.

> 여기서 락은 객체 내부의 모니터 락이 아님. 기능일 뿐!

**void lockInterruptibly()**

- void lock()과 동일하지만 인터럽트를 허용함
- 대기중 인터럽트 발생 시 `InterruptedException` 이 발생하며 락 획득 포기

**boolean tryLock()**

- 락 획득을 시도하고, 성공 여부 반환 (대기하지 않음)
- 락 획득 성공 시 `true` 실패 시 `false` 반환

**boolean tryLock(long time, TimeUnit unit)**

- 주어진 시간 동안 락 획득 시도 후 성공 여부 반환
- 주어진 시간동안 락 획득 성공시 `true` 실패 시 `false` 반환
- 대기중 인터럽트 발생 시 `InterruptedException` 이 발생하며 락 획득 포기

**void unlock()**

- 락 해제, 락 획득을 대기 중인 스레드 중 하나가 락 획득
- 락을 획득한 스레드가 호출하는 메서드, 그렇지 않으면 `IllegalMonitorStateException` 이 발생

**Condition newCondition()**

- Condition 객체를 생성하여 반환
- Condition 객체는 락과 결합되어 사용, 스레드가 특정 조건을 대기하거나 신호를 받을 수 있게함
- ≒ Object 클래스의 wait, notify, notifyAll

### 공정성

Lock 인터페이스를 통해 synchronized의 무한 대기 문제를 해결했으므로

공정성의 문제만 남아있다.

## ReentrantLock

Lock 인터페이스의 대표적인 구현체로 스레드가 공정하게 락을 얻을 수 있는 모드 제공

```java
public class ReentrantLockEx { 
	// 비공정 모드 락
	private final Lock nonFairLock = new ReentrantLock(); 
	// 공정 모드 락
	private final Lock fairLock = new ReentrantLock(true);
	
  public void nonFairLockTest() {
         nonFairLock.lock();
         try {
					// 임계 영역 
					} finally {
             nonFairLock.unlock();
         }
	}
	
  public void fairLockTest() {
         fairLock.lock();
					try {
						// 임계 영역
	         } finally {
             fairLock.unlock();
					} 
	}
}
```

### 비공정 모드 (Non-fair mode)

ReentrantLock의 기본 모드

락을 풀었을 때, 대기 중인 스레드 중 아무나 락을 획득

**비공정 모드 특징**

- 성능 우선 : 락을 획득하는 속도가 빠름
- 선점 가능 : 새로운 스레드가 기존 스레드 보다 락을 먼저 획득 가능
- 기아 현상 가능성 : 특정 스레드가 계속해서 락을 획득 하지 못할 가능성

### 공정 모드 (Fair mode)

락을 요청한 순서대로 스레드가 락을 획득

**공정모드 특징**

- 공정성 보장 : 대기 큐에 따라 스레드가 락을 획득
- 기아 현상 방지 : 모든 스레드가 언젠간 락 획득
- 성능 저하 : 락을 획득하는 속도가 다소 저하

## 대기 중단

ReentrantLock은 Lock 인터페이스의 구현체이므로 위에서 언급한

- boolean tryLock()
- boolean tryLock(long time, TimeUnit unit)

을 활용하여 락을 무한 대기하지 않고, 중간에 빠져나오는 것이 가능하다.