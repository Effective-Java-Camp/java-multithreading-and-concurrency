# 스레드 생성과 실행

## JVM 메모리 영역
![jvm-memory](https://github.com/ruthetum/study/blob/main/.assets/java/jvm-memory.png?raw=true)

#### Method area
- 클래스의 정보, `static` 변수가 저장
- 클래스 로더에 의해 로드된 클래스 파일의 바이트 코드를 저장하는 공간

#### Heap area
- 객체와 배열이 생성되는 공간

#### Stack area
- 스레드 별로 사용하는 공간(스레드 별로 런타임 스택 생성)
- 메서드 콜, 지역 변수, 매개변수, 리턴 값, 연산 중 발생하는 임시 값 등이 저장

#### PC Register
- 스레드의 실행 위치(execution instruction 정보의 위치)를 저장

#### Native Method Stack
- 자바 외의 언어로 작성된 네이티브 코드(메서드)를 위한 스택


## 스레드 생성
- `Thread` 클래스를 상속받아 `run()` 메서드를 오버라이딩하여 스레드를 생성
- `Runnable` 인터페이스를 구현하여 스레드를 생성


### Thread 클래스 상속

스레드 클래스 정의
```java
public class HelloThread extends Thread {
    
    @Override
    public void run() {
        doSomething();
    }
}
```

스레드 생성 및 실행
- `start()` 메서드를 호출하여 스레드를 실행
    - main 스레드와 별도의 스레드를 실행
- `run()` 메서드를 직접 호출하면 main 스레드에서 실행
    - `main` 스레드는 `HelloThread` 인스턴스에 있는 `run()` 메서드를 호출하는 형태

```java
public class HelloThreadMain {

    public static void main(String[] args) {
        HelloThread helloThread = new HelloThread();
        helloThread.start();
    }
}
```

<br>

#### 데몬 스레드
스레드는 사용자(user) 스레드와 데몬(daemon) 스레드로 구분

차이는 JVM 종료 시점
- 모든 user 스레드가 종료되면 JVM도 종료
- 데몬 스레드는 user 스레드가 모두 종료되면 데몬 스레드도 종료 (why? JVM이 종료돼서)


`setDaemon(true)` 메서드를 호출하여 데몬 스레드로 설정
- 데몬 스레드 여부는 `start()` 실행 전에 결정 (이후에 수정 불가)

```java
public class DaemonThreadMain {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + ": main() start");
        DaemonThread daemonThread = new DaemonThread();
        daemonThread.setDaemon(true); // 데몬 스레드 여부
        daemonThread.start();
        System.out.println(Thread.currentThread().getName() + ": main() end");
    }

  static class DaemonThread extends Thread {

    @Override
    public void run() {
      System.out.println(Thread.currentThread().getName() + ": run()");
      try {
        Thread.sleep(10000); // 10초간 실행
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      System.out.println(Thread.currentThread().getName() + ": run() end");
    }
  }
}
```

`run()` 메서드 안에서 `Thread.sleep()` 를 호출할 때 checked exception 인 `InterruptedException` 을 밖으로 던질 수 없고 반드시 잡아야 함
- why? `run()` 메서드는 부모 클래스 `Thread`의 인터페이스인 `Runnable` 인터페이스에서 throws를 선언하지 않고 있음
    - 자바 오버라이드 규칙에 따라 부모 메서드 또는 인터페이스가 체크 예외를 던지지 않는 경우, 재정의된 자식 메서드도 체크 예외를 던질 수 없음
- `InterruptedException`은 checked exception 이기 때문에 `try-catch` 블록으로 예외를 잡아야 함

<br>

### Runnable 인터페이스 구현
`Runnable` 인터페이스를 구현하는 방식으로 스레드를 생성

```java
public interface Runnable {
     void run();
}
```

```java
 public class HelloRunnable implements Runnable {
     @Override
     public void run() {
         System.out.println(Thread.currentThread().getName() + ": run()");
     }
}
```

### Thread 상속 vs Runnable 구현
> 스레드 사용할 때는 `Thread` 를 상속 받는 방법보다 `Runnable` 인터페이스를 구현하는 방식을 사용하자

자바는 다중 상속이 안 된다 -> `Thread` 클래스를 상속받으면 다른 클래스를 상속받을 수 없음

코드의 분리가 안 된다 -> 굳이 `Thread` 클래스를 상속받아서 디펜던시 및 추가적인 메모리를 할당 할 필요가 없음

여러 스레드가 동일한 `Runnable` 객체를 공유할 수 있어 자원 관리를 효율적으로 할 수 있다 -> `Runnable` 인스턴스를 여러 스레드에서 사용할 수 있음 (개별 스레드에서 `run()` 참조 값이 같음)





