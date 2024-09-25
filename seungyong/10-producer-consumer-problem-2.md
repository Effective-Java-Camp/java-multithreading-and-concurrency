# 섹션 10. 생산자 소비자 문제2

## Lock Condition - 예제 4

### 개선 방안

앞선 예제에서는 소비자가 소비자를 깨우고, 생산자가 생산자를 깨워 비효율이 발생함

> 그렇다면 스레드의 대기 집합을 둘로 나누면 비효율 해결!

Lock, ReentrantLock을 사용하면 대기 집합을 분리할 수 있다.

### Condition

- Condition은 ReentrantLock을 사용하는 스레드의 대기 공간 → lock.newCondition()으로 생성 가능
- Object.wait()은 모든 객체가 스레드 대기 공간을 가지고 있기 때문에 상관 없지만,
- Lock(ReentrantLock)은 직접 스레드 대기 공간을 만들어서 사용해야함

### condition.await()

- Object.wait()와 유사한 기능. 지정한 condition에 현재 스레드를 `WAITING` 상태로 보관
- ReentrantLock에서 획득한 락을 반납

### condition.signal()

- Object.notify()와 유사한 기능. 지정한 condition에서 대기중인 스레드 하나를 깨운다.

### 스레드 대기 공간 분리

```java
private final ReentrantLock lock = new ReentrantLock();
private final Condition producerCond = lock.newCondition();
private final Condition consumerCond = lock.newCondition();
```

이렇게 같은 Lock이어도 두개의 condition으로 분리할 수 있다.

- 공간을 분리한 뒤, 생산자와 소비자는 대기해야 할 상황이 오면 각각의 대기 공간에서 대기
- 생산자 → 소비자, 소비자 → 생산자를 깨우게 만들면 해결

### Oject.notify() vs Condition.signal()

- Object.notify()
    - 대기 중인 스레드 중 임의의 하나를 선택해서 깨움 → JVM 구현에 따라 다름
    - `synchronized` 블록 내에서 모니터 락을 가지고 있는 스레드가 호출
- Condition.signal()
    - FIFO 순서로 대기 중인 스레드 하나를 깨움.
        - 자바 버전과 구현에 따라 달라질 수 있지만, 보통 Queue 구조를 사용하므로 FIFO
    - `ReentrantLock`을 가지고 있는 스레드가 호출

## 스레드의 대기

### synchronized 대기

synchronized는 2가지 단계의 대기 상태 존재

- 대기 1: 락 획득 대기
    - `BLOCKED` 상태로 락 획득 대기
    - `synchronized` 를 시작할 때 락이 없으면 대기
    - 다른 스레드가 `synchronized` 를 빠져나갈 때 대기가 풀리며 획득 시도
- 대기 2: wait() 대기
    - `WAITING` 상태로 대기
    - wait() 호출 시 스레드 대기 집합에서 대기
    - 다른 스레드가 notify()를 호출 시 빠져나감

> 락 획득 대기 중인 스레드들 역시 관리가 되어야 추 후에 스레드가 락을 반납하고 나갔을 때 락 획득을 경쟁할 수 있다.
> 

### 락 대기 집합

자바 내부에서 `BLOCKED` 상태의 스레드 들을 ‘락 대기 집합’에서 관리한다.

락을 보유하고 있던 스레드가 락을 반납하면 이 ‘락 대기 집합’에서 관리되고 있던 스레드들 중 하나가 락을 획득한다.

이러한 2단계 대기소 구조는 synchronized와 마찬가지로 ReentrantLock도 구성되어 있다.

ReentrantLock은 대기 큐에서 `WAITING` 상태로 대기한다(Interrupt도 가능) 

### 멀티 스레드와 임계 영역을 다루기 위한 자바 객체 인스턴스 3요소

- 모니터 락
- 락 대기 집합(모니터 락 대기 집합)
- 스레드 대기 집합

> synchronized 영역 들어가기 위한 `모니터 락` → 없으면 `락 대기 집합` 에서 BLOCKED 상태로 대기 → 락을 획득 후 로직 실행 중 wait()를 호출해 `모니터 락` 을 반납하고 `스레드 대기 집합` 에서 대기
> 

이 세가지는 서로 맞물려 돌아간다.

### BlockingQueue

위와 같은 방식으로 생산자 소비자 문제를 해결하기 위해 자바는 BlockingQueue라는 인터페이스와 구현체를 제공한다. → 스레드를 차단(Blocking)할 수 있는 큐

- 데이터 추가 차단 : 큐가 가득 차면 데이터 추가 작업(`put()`)을 시도하는 스레드는 공간이 생길 때 까지 차단됨
- 데이터 획득 차단 : 큐가 비어있으면 데이터 획득 작업(`take()`)을 시도하는 스레드는 데이터가 들어올 때까지 차단됨

```java
public interface BlockingQueue<E> extends Queue<E> {
     boolean add(E e);
     boolean offer(E e);
     void put(E e) throws InterruptedException;
     boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;
     E take() throws InterruptedException;
     E poll(long timeout, TimeUnit unit) throws InterruptedException;
     boolean remove(Object o);
//...
}
```

BlokingQueue 인터페이스의 대표적인 구현체 → BlockingDeque도 존재

- ArrayBlockingQueue : 배열 기반 구현체, 버퍼의 크기 고정
- LinkedBlockingQueue : 링크 기반 구현체, 버퍼의 크기를 고정 or 무한으로 사용 가능

ArrayBlockingQueue는 우리가 앞서 구현했던 예제의 방식과 유사하게 동작한다.

```java
public class ArrayBlockingQueue {
     final Object[] items;
     int count;
     ReentrantLock lock;
     Condition notEmpty; //소비자 스레드가 대기하는 
     condition Condition notFull; //생산자 스레드가 대기하는 condition
     
     public void put(E e) throws InterruptedException {
		     lock.lockInterruptibly();
		     try {
				     while (count == items.length) { // 객체 배열이 꽉차면 생산자 스레드 대기
							   notFull.await();
				     }
						 enqueue(e);
        } finally {
            lock.unlock();
        }
}
    private void enqueue(E e) {
        items[putIndex] = e;
        count++;
        notEmpty.signal();  // 비지 않았으면 소비자 스레드를 깨움
} }
```

## BlockingQueue - 기능 설명

실무에서의 멀티스레드 사용은 응답성이 중요하므로 중지 요청이나 타임아웃을 하는 방법이 필요

큐가 가득 찼을 때의 선택지

- Throws Exception : 예외를 던진다. 예외를 받아서 처리
- Special Value : 대기하지 않는다. 즉시 false(혹은 특정 값) 반환
- Blocks : 대기
- Times Out : 특정 시간 만큼만 대기

BlockingQueue는 이 상황들에 맞는 다양한 메서드를 제공

| **Operation** | **Throws Exception** | **Special Value** | **Blocks** | **Times Out** |
| --- | --- | --- | --- | --- |
| **Insert(추가)** | add(e) | offer(e) | put(e) | offer(e, time, unit) |
| **Remove(제거)** | remove() | poll() | take() | poll(time, unit) |
| **Examine(관찰)** | element() | peek() | not applicable | not applicable |
