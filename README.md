# Team25_BE

<img src="https://github.com/user-attachments/assets/8fcad6d6-4464-40f5-b97f-048d49786a5d" width="300" height="150"/>
<br>
🏆 카카오 테크 캠퍼스 최종 산출물 29팀 중 우수상

### ❗ 테스트 안내

> 테스트 시 관리자 페이지에서 매니저 승인이 가능합니다. <br>
[admin 페이지 주소](https://meditogetherapp.com/admin) <br>
비밀번호: admin (평가 기간 후 변경 예정입니다) <br>
매니저 등록 후 매니저 앱의 프로필 페이지에서 근무 지역과 근무 시간을 설정해야 그에 맞게 이용자 앱에서 검색됩니다.
자세한 앱 사용법은 [테스트 시나리오](https://quickest-asterisk-75d.notion.site/bf50e3dcdb444b298734142ab6bcde29)에서 확인 가능합니다.

<br>

![메디투게더 소개](https://github.com/user-attachments/assets/121ff619-5adf-49ca-8128-5d677da23bc6)

> 목차
> - [📌 프로젝트 소개](#프로젝트-소개)
> - [👩‍👩‍👧‍👧 팀원 소개](#팀원-소개)
> - [✏️ 주요 기능](#주요-기능)
> - [🔗 링크 모음](#링크-모음)
> - [📜 ERD](#erd)
> - [📄 API 모아보기](#api-모아보기)
> - [🚩 시작 가이드](#시작-가이드)
> - [🖥️ 서비스 아키텍처](#서비스-아키텍처)

<br>

## 프로젝트 소개

### 개발 동기 및 목적

저희 칠전팔기 팀은 **“병원 동행 서비스 매칭 플랫폼”** 을 주제로 선정했습니다. 고령화 사회로 접어들면서 의료 이용에 어려움을 겪는 고령층이 늘어나고 있습니다. 특히 무인 접수기 등 디지털 도구의 도입으로 인해 익숙하지 않은 어르신들은 병원 이용에 불편함을 겪고 있습니다.

이 문제를 해결하기 위해 저희는 **도움이 필요한 환자와 전문 매니저를 매칭**하여 환자가 안전하게 의료 서비스를 이용할 수 있는 플랫폼을 제안합니다. 병원 동행 서비스의 수요는 고령화와 1인 가구 증가로 인해 지속적으로 증가하고 있으며, 병원 동행 매니저 업무는 비교적 낮은 노동 강도로 중장년층의 새로운 직업으로 주목받고 있습니다.

저희 플랫폼은 **환자와 전문 매니저 간의 매칭**을 통해 병원 이용을 지원하며, **실시간 조회 및 리포트 기능**을 통해 자녀들도 안심하고 서비스를 이용할 수 있도록 돕겠습니다.

<br>

### 서비스 소개

> 투명한 서비스로 환자와 병원 동행 매니저를 매칭하다, '메디투게더'

1. ✏️ **로그인 및 회원가입**

   - SNS 간편 로그인 (카카오)으로 회원가입이 가능해요.
   - 로그아웃 및 탈퇴 기능도 제공하고 있어요.

2. 🗂️ 환자의 **프로필 관리**

   - 환자 개인 정보를 입력하고 의료 관련 정보를 관리할 수 있어요 (이름, 나이, 성별, 병력 등).
   - 주치의 정보도 등록해두면 서비스 이용에 도움이 돼요.

3. 📅 **서비스 예약**으로 병원 동행을 미리 준비해요

   - 동행 서비스 예약을 통해 병원 정보와 예약 시간을 쉽게 설정할 수 있어요.
   - 예약 후에도 수정이나 관리가 가능해요.

4. 📍 실시간 동행 현황 알림

   - 동행 서비스 이용 중 실시간으로 환자와 동행자의 위치를 확인할 수 있어요.
   - 보호자나 환자 본인이 어플에서 이동 경로와 위치를 실시간으로 확인할 수 있어요.

5. 📝 **진료 리포트 제공**으로 진료 정보를 한눈에

   - 진료가 끝나면 리포트를 자동으로 생성해줘요 (진료 정보, 처방 내역 등 포함).

<br>

### 개발 기간

2024.09 ~ 2024.11 (카카오 테크 캠퍼스 2기 - STEP3)

<br>

## 팀원 소개

|        | **강수민**                                              | **김상해**                                                 | **박민재**                                                   | **이성훈**                                                  |
|--------|------------------------------------------------------|---------------------------------------------------------|-----------------------------------------------------------|----------------------------------------------------------|
| E-Mail | [ramiregi@pusan.ac.kr](mailto:ramiregi@pusan.ac.kr)  | [pelikan@pusan.ac.kr](mailto:pelikan@pusan.ac.kr)       | [minjae4650@gmail.com](mailto:minjae4650@gmail.com)       | [p.plue1881@gmail.com](mailto:p.plue1881@gmail.com)      |
| GitHub | [wvssm](https://github.com/wvssm)                    | [gobad820](https://github.com/gobad820)                 | [minjae4650](https://github.com/minjae4650)               | [NextrPlue](https://github.com/NextrPlue)                |
|        | <img src="https://github.com/wvssm.png" width=100px> | <img src="https://github.com/gobad820.png" width=100px> | <img src="https://github.com/minjae4650.png" width=100px> | <img src="https://github.com/NextrPlue.png" width=100px> |

<br>

## 주요 기능

> - 로그인/회원가입
> - 예약
> - 실시간 동행 현황
> - 예약 현황
> - 매니저 리스트 조회
> - 매니저 프로필 조회
> - 진료 리포트
> - 결제

<table>
  <tr>
    <th>기능</th>
    <th>설명</th>
  </tr>
  <tr>
    <td><b>로그인/회원가입</b></td>
    <td>카카오 OAuth 로그인 기능을 제공하며, 카카오 로그인 후 발급된 access token과 refresh token을 통해 JWT 관리를 수행합니다.<br> 
        refresh token의 만료 기간이 7일 이상 남아 있을 경우 자동 로그인도 지원합니다.</td>
  </tr>
  <tr>
    <td><b>예약</b></td>
    <td>사용자 정보를 입력(이름, 생년월일, 성별, 연락처, 출발지, 도착지 등) 받아 개인정보 수집 동의 후 예약을 완료합니다.</td>
  </tr>
  <tr>
    <td><b>실시간 동행 현황</b></td>
    <td>매니저 앱에서 매니저가 입력한 동행 상황 정보(예: 24.11.20 21:04 부산대 병원 도착)를 실시간으로 확인할 수 있습니다.</td>
  </tr>
  <tr>
    <td><b>예약 현황</b></td>
    <td>현재 예약 상태와 지난 예약 내역을 조회할 수 있으며, 진행 중인 예약에 대해서는 취소 요청이 가능합니다.<br>
        또한, 지난 예약에 대한 리포트도 확인할 수 있습니다.</td>
  </tr>
  <tr>
    <td><b>매니저 리스트 조회</b></td>
    <td>예약 시 출발지를 도로명 주소 검색 API를 통해 선택하면, 해당 출발지의 시/도 정보를 저장하여 해당 지역에 맞는 매니저 리스트를 조회할 수 있습니다.</td>
  </tr>
  <tr>
    <td><b>매니저 프로필 조회</b></td>
    <td>매니저 리스트에서 항목을 선택하면 매니저 앱에 등록된 매니저들의 프로필을 조회할 수 있습니다.<br>
        특히, 매니저 프로필 이미지는 Amazon S3 서비스를 통해 다운로드 받아 표시됩니다.</td>
  </tr>
  <tr>
    <td><b>결제</b></td>
    <td>나이스페이먼츠를 통한 결제를 제공하며, 빌링키 발급을 통한 카드 등록 및 결제를 지원합니다. 빌링키는 삭제가 가능하며, 결제 취소도 지원합니다.</td>
  </tr>
</table>

<br>

## BE 개발 주안점

### 📌 Request와 Response는 반드시 DTO로 사용

> 모든 요청과 응답은 **DTO(Data Transfer Object)** 로 처리하여 데이터 구조의 명확성을 확보하고, **Controller와 Service 간의 데이터 의존성을 줄였습니다**.
>
> 이를 통해 코드 유지보수가 용이하고 각 레이어의 책임을 명확히 분리하였습니다.

### 📌 Controller와 Service의 역할 분리

> **Controller는 정상적인 동작을 가정**하며, 핵심 검증과 예외 처리는 **Service 레이어**에서 담당하도록 설계했습니다.
>
> 이를 통해 Controller는 요청을 전달하고, Service는 실제 로직과 예외 처리를 수행해 **유연성과 일관성을 강화**했습니다.

### 📌 예외 코드 관리: ErrorCode 클래스 사용

> 모든 예외 코드는 **`ErrorCode` 클래스에서 통합 관리**하여, 코드 내에서 **일관된 예외 처리**가 가능하도록 했습니다.
>
> 이를 통해 예외 코드의 재사용성과 유지보수성을 높였습니다.

### 📌 근무시간 테이블 구조 설계

> 초기에는 데이터베이스 Primary Key를 통해 직접 조회하는 방식으로 설계했지만, Android 클라이언트가 근무시간 테이블의 ID를 알 수 없는 상황이 발생하여 설계 방식을 변경했습니다.
>
> - **고려했던 방법**: 요일별로 각기 다른 테이블을 생성하거나, 하나의 테이블에 모든 요일 정보를 통합하는 방식 중 고민했습니다.
> - **최종 구현**: **하나의 테이블에 전체 요일별 근무시간 정보를 통합**하여 관리했습니다. `User` -> `Manager` -> `근무시간 테이블`로 **One-to-One 매핑**을 설정하여, 매니저 프로필 조회 시 **전체 근무시간을 한 번에 조회**할 수 있도록 효율성을 높였습니다.

### 📌 Android와 OAuth 책임 분리 및 보안 강화

> 안드로이드에서 **네이티브 방식으로 OAuth**를 구현할 경우 간편한 로그인 경험을 제공할 수 있지만, 안드로이드에서 제공한 정보를 검증하기 위해 **백엔드와 OAuth 서버 간의 직접적인 통신**이 필요했으며, 이에 OAuth 서버의 Token이 필요했습니다.
>
> 안드로이드와 백엔드 간에 **OAuth 서버의 토큰을 주고 받는다면 보안상 위험한 상황**이므로, 이를 보안하고자 **HTTPS 프로토콜을 사용**해 통신 보안을 강화했습니다.

### 📌 JWT 토큰에 포함할 정보

> **JWT 토큰**에 개인 정보를 포함하는 것은 보안상 위험하다고 판단하여, 사용자 이름 대신 **UUID**를 포함하도록 결정했습니다.
>
> **ulid-creator 라이브러리**를 통해 **UUID 버전 7**을 활용했으며, 이는 **타임스탬프 기반으로 고유성**을 보장하여 중복 가능성을 최소화했습니다.
>
> 이를 통해 사용자마다 고유한 값을 유지하면서도 **개인 정보 보호**를 강화했습니다.

### 📌 민감한 정보 관리: Spring Cloud Config와 Vault

> 시스템의 클라이언트 키, 시크릿 키 등의 민감한 정보를 관리하기 위해 **Spring Cloud Config와 HashiCorp Vault**를 활용하였습니다.
>
> Spring Cloud Config는 **환경별 설정을 중앙에서 관리**하여 코드 수정 없이 구성을 변경할 수 있게 해주며, Vault는 **시크릿 데이터를 안전하게 암호화하고 접근 제어**를 통해 보호합니다.

### 📌 나이스페이먼츠 결제 서비스 구현

> **나이스페이먼츠와 연동하여 빌링키 기반의 결제 시스템**을 구축하였습니다.
>
> 한 번의 카드 등록으로 간편하게 결제가 가능하며, 종량제 및 구독 서비스에도 활용할 수 있습니다.

### 📌 카드 정보 안전 전송

> 빌링키 발급 과정에서 카드 정보를 안드로이드로부터 평문으로 전송받는 것은 보안상 위험이 있습니다.
>
> 안드로이드에서 **카드 정보를 암호화하여 서버로 전송**하고, 서버는 해당 암호화 데이터를 그대로 나이스페이먼츠에 전달하여 **서버 내 카드 정보 처리 및 저장을 방지**하며 보안성을 향상시켰습니다.

### 📌 데이터 암호화 관리

> 빌링키와 같은 민감한 데이터는 **항상 암호화된 상태**로 저장되며, 필요 시에만 복호화하여 사용합니다.
>
> Vault의 `secretKey`를 사용해 암호화하여 **보안성을 극대화**하고 데이터 유출의 위험을 최소화합니다.

<br>

## 링크 모음

|                                               기획                                                |                                                                                          디자인                                                                                           |                                     개발                                     |                                                                      배포                                                                       |
|:-----------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------:|
| [노션](https://www.notion.so/28ef94a6ccd4459280e549b658e3e3ab?v=79a3e414d3ad44a5ac32a21a64aea358) | [와이어프레임](https://www.figma.com/design/MOl5hc5iBjWT8XPTGfsdkm/%EC%99%80%EC%9D%B4%EC%96%B4%ED%94%84%EB%A0%88%EC%9E%84-%EC%B9%A0%EC%A0%84%ED%8C%94%EA%B8%B0?node-id=0-1&node-type=canvas) |    [백엔드 깃허브](https://github.com/kakao-tech-campus-2nd-step3/Team25_BE)     |                                                   [백엔드 배포 주소](https://meditogetherapp.com)                                                    |
|                [최종 기획안](https://www.notion.so/6618de8cc3e14655b28816d3adb80607)                 |                                                                                                                                                                                        | [안드로이드(이용자 앱) 깃허브](https://github.com/kakao-tech-campus-2nd-step3/Team25_Android) |                              [메디투게더 원스토어](https://m.onestore.co.kr/ko-kr/apps/appsDetail.omp?prodId=0000779535)                               |
|                                                                                                 |                                                                                                                                                                                        |          [안드로이드(매니저 앱) 깃허브](https://github.com/kakao-tech-campus-2nd-step3/Team25_Android_2)           |                            [메디투게더 매니저앱 원스토어](https://m.onestore.co.kr/ko-kr/apps/appsDetail.omp?prodId=0000779536)                            |
|                                                                                                 |                                                                                                                                                                                        |    [API 문서](https://www.notion.so/API-5f451248315e4bca9f6de224fa1215a1)    | [Vault 서버 배포 주소](http://ec2-13-125-34-52.ap-northeast-2.compute.amazonaws.com:8200/ui/vault/auth?redirect_to=%2Fvault%2Fdashboard&with=token) |
|                                                                                                 |                                                                                                                                                                                        |          [ERD 명세서](https://www.erdcloud.com/d/uP9dvKQirFwQGzEp6)           |                                              [메디투게더 관리자 페이지](https://meditogetherapp.com/admin)                                               |
|                                                                                                 |                                                                                                                                                                                        |    [테스트 시나리오](https://www.notion.so/bf50e3dcdb444b298734142ab6bcde29)     |                                                                                                                                               |
|                                                                                                 |                                                                                                                                                                                        |    [테스트 결과보고서](https://www.notion.so/4b60db4392514af4ba1d8ddd3970f1fa)     |                                                                                                                                               |


<br>

## ERD

![ERD](https://github.com/user-attachments/assets/2d8a2b28-b6cb-4ffb-8268-247ba9a28b52)

<br>

## API 모아보기

📝 [API 문서](https://quickest-asterisk-75d.notion.site/API-5f451248315e4bca9f6de224fa1215a1)

![API 명세서](https://github.com/user-attachments/assets/8a8f2ae2-5d71-4f5d-83e3-8babdabe84a2)

<br>

## 시작 가이드

> Requirements: Java 21, Spring 3.3.3
>
> 메디투게더 환경 변수 설정이 완료되어야 프로젝트가 실행됩니다.

1. 프로젝트 클론
```
git clone https://github.com/kakao-tech-campus-2nd-step3/Team25_BE.git
cd Team25_BE
```

2. 실행
```
./gradlew build
cd build
cd libs
java -jar backend-0.0.1-SNAPSHOT.jar
```

<br>

## 서비스 아키텍처
![기술 스택](https://github.com/user-attachments/assets/c976c69a-6596-4ec3-870d-2a360b81ec52)
