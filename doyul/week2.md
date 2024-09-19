### Volatile 키워드의 동작 원리와 한계점

> 가시성(visibility) 문제를 해결

Volatile 변수에 대한 읽기와 쓰기는 항상 메인 메모리에서 직접 이루어지기 때문에, CPU 캐시를 거치지 않고 직접 메인 메모리와 통신하여 최신 값 보장.

Volatile 변수 접근 전후로 메모리 장벽(memory barrier) 생성으로 명령어 재배치를 방지한다.

하지만, 단일 변수 read/write 작업에 대해서만 원자성을 보장하기 때문에, 복합 연산에 대해서는 원자성을 보장하지 않는다. 또한 여러 스레드가 동시에 값을 변경한다면 race condition이 발생할 수 있다.

### Synchronized 키워드와 ReentrantLock의 차이점

| 특징 | Synchronized | ReentrantLock |
|------|--------------|---------------|
| 유연성 | 블록 또는 메소드에 적용되는 선언적 방식 | lock()과 unlock() 메소드를 통해 명시적 제어 가능 |
| 공정성 | 공정성 보장하지 않음 | 생성자 파라미터를 통해 공정한 Lock 생성 가능 |
| 인터럽트 처리 | 인터럽트에 반응하지 않음 | lockInterruptibly() 메소드로 인터럽트에 반응 가능 |
| 타임아웃 | 락 획득 시도에 대한 타임아웃 미지원 | tryLock(long time, TimeUnit unit) 메소드로 타임아웃 지원 |
| 성능 | 일반적인 상황에서 ReentrantLock과 비슷 | 경쟁이 심한 상황에서 더 나은 성능 가능 |
| 조건 변수 | Object의 wait(), notify() 메소드 사용 | Condition 객체를 통해 더 세밀한 조건 제어 가능 |

### LockSupport 클래스의 park()와 unpark() 메소드

> LockSupport는 저수준 동기화

- `park()`: 현재 스레드를 WAITING 상태로 만들어 CPU 자원을 사용하지 않게 한다.
- `unpark(Thread t)`: 지정된 스레드를 깨워 RUNNABLE 상태로 만든다.

- 특징
	- permit 개념을 사용하기 때문에 순서 독립성을 갖는다.
	- `park()`는 `thread.interrupt()`에 의해 중단 될 수 있다.
	- `parkNanos()`를 통해 타임아웃

- 주의점
	- Spurious Wakeup 가능성으로 인해 조건 검사 추가가 권장된다.
	- 데드락
	- volatile이나 동기화 없이는 가시성 문제도 발생 가능

고수준 `concurrent` 패키지를 이용하는 것이 권장된다.

### Java Memory Model(JMM)과 happens-before 관계

> JMM은 멀티스레드 환경에서 변수의 가시성, 원자성, 순서성을 보장하기 위한 규칙을 정의한다.

- JMM 특징
	- 공유 변수는 메인 메모리에 저장
	- 각 스레드는 CPU 캐시를 이용하여 작업에 속한 변수의 복사본을 가질 수 있다.
	- 변수 값이 언제 메인 메모리로 read/write 되는지 정한다.

>  `A happens-before B`는, A의 메모리 작업 결과가 B에서 보이는 것을 보장한다.
