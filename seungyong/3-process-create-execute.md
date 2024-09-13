# 섹션 3. 스레드 생성과 실행

## 스레드 시작

### 자바 메모리 구조

**메서드 영역** : 프로그램을 실행하는데 필요한 공통 데이터 관리

→ 이 영역은 프로그램의 모든 영역에서 공유

- 클래스 정보, static, 런타임 상수 풀

**스택 영역** : 자바 실행 시, 하나의 실행 스택 생성. 각 스택 프레임은 지역 변수, 중간 연산 결과, 메서드 호출 정보 포함

- 스택 프레임 : 스택 영역에 쌓이는 네모 박스가 하나의 스택 프레임

**힙 영역** : 객체(인스턴스)와 배열이 생성되는 영역. 가비지 컬렉션이 이루어지는 주요 영역으로 더 이상 참조되지 않는 객체는 GC에 의해 제거

> 스택 영역은 각 스레드별로 하나의 실행 스택이 생성됨 → 스레드 수 만큼 스택 생성

## 스레드 생성

### 스레드 생성 : Thread 클래스 상속 vs Runnable 인터페이스 구현

### Thread 클래스 상속

```java
public class HelloThread extends Thread{
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + ": run()");
    }
}
```

```java
public class HelloThreadMain {
    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + ": main() start");

        HelloThread helloThread = new HelloThread();
        System.out.println(Thread.currentThread().getName() + ": start() 호출 전");

        helloThread.start();
        System.out.println(Thread.currentThread().getName() + ": start() 호출 후");

        System.out.println(Thread.currentThread().getName() + ": main() end");    }
}
```

주의! run()이 아닌 start() 메서드를 호출해야함! → 그래야 별도의 스레드에서 run() 코드 실행

run()을 직접 호출하게 되면 run()을 호출한 스레드의 스택에 메서드가 올라가 해당 스레드에서 실행됨


1. main 스레드가 HelloThread 인스턴스를 생성 → 이 때 Thread-0 생성됨
2. start() 메서드를 호출하면 Thread-0가 시작되며 run() 메서드 호출

> 즉, main 스레드는 run()을 실행한게 아닌 Thread-0가 run()을 실행하게 함

→ main 스레드는 start()만 실행하고 이후 작업을 실행하므로 이 두 스레드는 동시에 실행됨

**스레드 간 실행 순서 보장 X**

## 데몬 스레드

스레드는 사용자 스레드와 데몬 스레드 2가지로 구분

### 사용자 스레드

- 프로그램의 주요 작업 수행
- 작업이 완료 될 때 까지 실행
- 모든 사용자 스레드가 종료되면 JVM 종료

### 데몬 스레드

- 백그라운드에서 보조 작업 수행
- 모든 사용자 스레드가 종료되면 데몬 스레드는 자동 종료

JVM은 데몬 스레드의 실행 완료를 기다리지 않고 종료됨

```java
public class DaemonThreadMain {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + ": main() start");
        DaemonThread daemonThread = new DaemonThread();
        daemonThread.setDaemon(true); // 데몬 스레드 여부
        daemonThread.start(); System.out.println(Thread.currentThread().getName() + ": main() end");
    }
    static class DaemonThread extends Thread {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + ": run() start");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName() + ": run() end");
        }
    }
}
```

사용자 스레드인 main 스레드가 DaemonThread의 인스턴시를 생성한 후 종료가 되어버리기 때문에 데몬 스레드를 대기하지 않고 그대로 자바가 끝나버림

## 스레드 생성 - Runnable

Runnable 인터페이스를 통해 스레드를 만드는 방법

```java
public class HelloRunnable implements Runnable{
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + ": run()");
    }
}
```

```java
public class HelloRunnableMain {
    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + ": main() start");
        HelloRunnable runnable = new HelloRunnable();
        Thread thread = new Thread(runnable);
        thread.start();
        System.out.println(Thread.currentThread().getName() + ": main() end");
    }
}
```

[앞서 Thread 클래스를 상속 받았던 코드](#thread-클래스-상속)와 동일한 결과가 나오게 된다.

스레드와 실행할 작업이 서로 분리되어있을 뿐, 스레드 객체를 생성할 때 실행할 작업을 생성자로 전달하면 끝!

### Thread 상속 vs Runnable 구현

스레드 사용할 때는 Runnable 인터페이스를 구현하는 방식을 사용하자!

**Thread 상속**

장점

- 간단한 구현

단점

- 상속의 제한 : 자바는 단일 상속만을 허용, 이미 다른 클래스 상속 시 Thread 클래스 상속 불가
- 유연성 부족 : 인터페이스를 사용하는 방식에 비해 유연성 떨어짐

**Runnable 인터페이스 구현**

장점

- 상속의 자유로움
- 코드의 분리
- 효율적인 자원 관리(여러 스레드가 동일한 Runnable 객체 공유 가능)

단점

- 코드가 약간 복잡해짐

## 여러 스레드 만들기

```java
public class ManyThreadMainV1 {
    public static void main(String[] args) {
        log("main() start");
        HelloRunnable runnable = new HelloRunnable();
        Thread thread1 = new Thread(runnable);
        thread1.start();
        Thread thread2 = new Thread(runnable);
        thread2.start();
        Thread thread3 = new Thread(runnable);
        thread3.start();
        log("main() end");
    }
}
```

같은 작업을 서로 다른 스레드에 전달하여 실행 → 각 스레드들은 받은 작업의 run() 메서드 실행

스레드의 실행 순서는 보장되지 않는다.