package com.ticket.god_of_ticketing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.ticket.god_of_ticketing.service.LectureService;

/*
1. 첫 번째 : @Component (명찰)
클래스 위에 붙어있는 **@Component**가 핵심입니다.

기존 방식: 개발자가 필요할 때마다 new DataInitializer()로 객체를 생성해서 썼습니다.

스프링 방식:

서버가 켜지면(Run), 스프링은 프로젝트 안에 있는 모든 파일을 싹 훑습니다(Component Scan).

@Component라는 스티커가 붙은 클래스를 발견하면, "어? 이건 내가 관리해야 할 녀석(Bean)이네?" 하고 알아서 메모리에 객체를 생성(new)해둡니다.

즉, new를 안 해도, 스프링이 몰래 new DataInitializer(...)를 이미 해버린 상태입니다.

2. 두 번째: CommandLineRunner (업무 지시서)
객체만 만들어놓고 가만히 있으면 실행이 안 되겠죠? 여기서 인터페이스 **CommandLineRunner**가 등장합니다.

Java
public class DataInitializer implements CommandLineRunner
의미: "스프링아, 나(DataInitializer)를 만들고 나면, 서버 켜지자마자 내 안에 있는 run() 메서드를 무조건 실행해 줘!" 라는 약속입니다.

스프링은 서버 로딩이 다 끝나면, 자기가 관리하는 객체들 중에서 CommandLineRunner 명찰을 단 녀석들을 찾아서 일제히 run()을 호출합니다.

+++++실행순서+++++
1. Server Start: GodOfTicketingApplication.run() 실행.
2.Scan: 스프링이 @Component가 붙은 DataInitializer 발견.
3.Creation: 스프링이 DataInitializer 객체 생성 (이때 LectureService도 주입해 줌).
4.Execution: "어? 너 CommandLineRunner네? 그럼 바로 일해!" -> run() 자동 호출.
5.Result: 초기 데이터 설정 완료 로그 출력.
 */

@Component
// @RequiredArgsConstructor // <--- 롬복 안 되니까 지우세요.
public class DataInitializer implements CommandLineRunner {

    private final LectureService lectureService; 
    // ❌ 주의: 여기서 절대 = new LectureService(...) 하지 마세요! 변수 선언만 하세요.

    // ✅ [수동 추가] 생성자 주입
    // 스프링이 이미 만들어둔(DB 연결된) LectureService를 여기에 쏙 넣어줍니다.
    public DataInitializer(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 이제 이 lectureService는 스프링이 준 '진짜'라서 에러가 안 납니다.
        lectureService.create("인기 교양: 주식 투자의 기초", 30);
        System.out.println("============== 초기 데이터 설정 완료 ==============");
    }
}