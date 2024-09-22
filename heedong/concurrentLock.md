# concurrent.Lock

## synchronized 단점
무한 대기: `BLOCKED` 상태의 스레드는 락이 풀릴 때 까지 무한 대기함
- 타임아웃 또는 인터럽트가 없음

공정성: 락이 돌아왔을 때 `BLOCKED` 상태의 여러 스레드 중에 어떤 스레드가 락을 획득할지 알 수 없음
- 최악의 경우 특정 스레드가 너무 오랜기간 락을 획득하지 못할 수 있음

<br>

## LockSupport
`LockSupport`를 사용하면 `synchronized`의 가장 큰 단점인 무한 대기 문제를 해결할 수 있음
- `LockSupport` 는 스레드를 `WAITING` 상태로 변경 (`park()`)
- `WAITING` 상태는 누가 깨워주기 전까지는 계속 대기하고 CPU 실행 스케줄링에 들어가지 않는다.

LockSupport` 제공 기능
- `park()`: 스레드를 `WAITING` 상태로 변경
- `parkNanos(nanos)`: 스레드를 나노초 동안만 `TIMED_WAITING` 상태로 변경
  - 지정한 나노초가 지나면 `TIMED_WAITING` 상태에서 빠져나오고 `RUNNABLE` 상태로 변경
- `unpark(thread)` : `WAITING` 상태의 대상 스레드를 `RUNNABLE` 상태로 변경

`unpark()` 대신 인터럽트(`interrupt()`)을 통해 스레드를 깨울 수도 있음
- `WAITING` 상태의 스레드에 인터럽트가 발생하면 `WAITING` 상태에서 `RUNNABLE` 상태로 변하면서 깨어남

<br>

## BLOCKED vs WAITING
|   구분    |                    `BLOCKED`                    | `WAITING`, `TIMED_WAITING` |
|:-------:|:-----------------------------------------------:|:--------------------------:|
| 인터럽트 관점 | 인터럽트가 걸려도 대기 상태를 빠져나오지 못함<br>(여전히 `BLOCKED` 상태) |   인터럽트가 걸리면 대기 상태를 빠져나옴<br>(`RUNNABLE` 상태로 변함)  |
|   용도    | `synchronized`에서 락을 획득하기 위해 대기할 때 사용 | 스레드가 특정 조건이나 시간 동안 대기할 때 발생하는 상태 |
| CPU 관점  | 스레드 대기 / 실행 스케줄링 포함 X | 스레드 대기 / 실행 스케줄링 포함 X |

- `BLOCKED` 상태는 `synchronized`에서만 사용하는 특별한 대기 상태
- `WAITING`, `TIMED_WAITING` 상태는 범용적으로 활용할 수 있는 대기 상태


대기(`WAITING`) 상태와 시간 대기 상태(`TIMED_WAITING`)는 관련 메서드가 서로 짝이 있음
- `Thread.join()` - `Thread.join(long millis)`
- `Thread.park()` - `Thread.parkNanos(long millis)`
- `Object.wait()` - `Object.wait(long timeout)`

<br>

## LockSupport 정리
`LockSupport` 를 사용하면 스레드를 `WAITING` , `TIMED_WAITING` 상태로 변경할 수 있고, 스레드를 깨울 수 있음
- `synchronized` 의 단점인 무한 대기 문제를 해결할 수 있음

다만 낮은 수준의 인터페이스를 제공하기 때문에 사용하는데 편리하지는 않음
- 다수의 스레드를 실행시켰을 때 특정 스레드만 깨우는 작업
- 대기 중인 스레드 중에 어떤 스레드를 깨울지에 대한 우선순위 설정

그래서 `concurrent` 패키지 내에는  `Lock` 인터페이스와 `ReentrantLock`같은 구현체로 이런 기능들을 제공하고 있음

<br><br>

## ReentrantLock
### Lock 인터페이스
동시성 프로그래밍에서 쓰이는 안전한 임계 영역을 위한 락을 구현하는데 사용

여기서 사용하는 락은 객체 내부에 있는 모니터 락이 아님

```java
public interface Lock {
     void lock();
     void lockInterruptibly() throws InterruptedException;
     boolean tryLock();
     boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
     void unlock();
     Condition newCondition();
}
```

| 메서드 | 설명                                                                                                                                                        |
|:------:|:----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `lock()` | - 락을 획득하고 락을 획득할 때까지 무한 대기(`WAITING`)<br>- 인터럽트에 응하지 않음                                                                                                   |
| `lockInterruptibly()` | - 락 획득을 시도하되 다른 스레드가 인터럽트할 수 있도록 함<br>- 다른 스레드에서 이미 락 획득 락을 획득할 때까지 무한 대기(`WAITING`)<br>- 대기 중에 인터럽트가 발생하면 `InterruptedException`이 발생하며 락 획득 포기           |
| `tryLock()` | - 락 획득을 시도하고, 즉시 성공 여부를 반환<br>- 다른 스레드가 이미 락을 획득했다면 `false` 를 반환하고, 그렇지 않으면 락을 획득하고 `true` 를 반환                                                           |
| `tryLock(long time, TimeUnit unit)` | - 주어진 시간 동안 락 획득을 시도, 주어진 시간 안에 락을 획득하면 `true` 를 반환<br>- 주어진 시간이 지나도 락을 획득하지 못한 경우 `false` 를 반환<br>- 대기 중 인터럽트가 발생하면 `InterruptedException`이 발생하며 락 획득 포기 |
| `unlock()` | - 락을 해제하고, 락 획득을 대기 중인 스레드 중 하나가 락을 획득할 수 있게 됨<br>- 락을 획득한 스레드가 호출해야 하며, 그렇지 않으면 `IllegalMonitorStateException` 이 발생할 수 있음                                |
| `newCondition()` | - 락과 결합되어 사용하는 `Condition` 객체를 생성하여 반환<br>- 스레드가 특정 조건을 기다리거나 신호를 받을 수 있도록 함<br>- `Object` 클래스의 `wait` , `notify` , `notifyAll` 메서드와 유사한 역할을 수행           |

`lock()` 메서드는 대기(`WAITING`) 상태인데 인터럽트에 응하지 않는다
- 인터럽트가 발생하면 순간 대기 상태를 빠져나오는 것은 맞음 (순간적으로 `WAITING`에서 `RUNNABLE` 상태로 전이)
- 다만 `lock()` 메서드 안에서 해당 스레드를 다시 `WAITING` 상태로 강제로 변경

<br>

> `Lock` 인터페이스를 통해 `synchronized`의 단점 중 하나인 무한 대기 문제를 해결할 수 있음
> 
> 그렇다면 공정성에 대한 문제는 어떻게 해결할까? → `ReentrantLock` 클래스를 통해 해결 가능
> 
> - 공정성: 락이 돌아왔을 때 `BLOCKED` 상태의 여러 스레드 중에 어떤 스레드가 락을 획득할 지 알 수 없음. 최악의 경우 특정 스레드가 너무 오랜기간 락을 획득하지 못할 수 있음

<br>

### ReentrantLock 클래스
`Lock` 인터페이스의 대표적인 구현체로, 스레드가 공정하게 락을 얻을 수 있는 모드를 제공

```java
public class ReentrantLock implements Lock, java.io.Serializable {
    
  ...
  
  public ReentrantLock() {
    sync = new NonfairSync();
  }

  public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
  }
  
  ...
}
```

<br>

#### 비공정 모드 (Non-fair mode)
```java
private final Lock lock = new ReentrantLock();
```
비공정 모드는 `ReentrantLock` 의 기본 모드

이 모드에서는 락을 먼저 요청한 스레드가 락을 먼저 획득한다는 보장이 없음

락을 풀었을 때, 대기 중인 스레드 중 아무나 락을 획득할 수 있음

이는 락을 빨리 획득할 수 있지만, 특정 스레드가 장기간 락을 획득하지 못할 가능성도 있음

<br>

#### 공정 모드 (Fair mode)
```java
private final Lock lock = new ReentrantLock(true);
```
공정 모드는 락을 요청한 순서대로 스레드가 락을 획득할 수 있게 함

먼저 대기한 스레드가 먼저 락을 획득하게 되어 스레드 간의 공정성을 보장

그러나 이로 인해 성능이 저하될 수 있음

<br>

#### ReentrantLock 정리
|  구분   |                 비공정 모드                 |              공정 모드              |
|:-----:|:--------------------------------------:|:-------------------------------:|
|  선점성  |  새로운 스레드가 기존 대기 스레드보다 먼저 락을 획득할 수 있음   |   대기 큐에서 먼저 대기한 스레드가 락을 먼저 획득   |
| 기아 현상 | 발생할 수 있음(특정 스레드가 계속해서 락을 획득하지 못할 수 있음) | 방지<br>(모든 스레드는 언젠가 락을 획득할 수 있음) |
|  성능   |         빠름<br>(락을 획득하는 속도가 빠름)         |     느림<br>(락을 획득하는 속도가 느림)      |

