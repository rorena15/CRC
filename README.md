# 📸 IoT Wireless Shutter Release for Canon 60D
<div align="center">
  <img src="https://img.shields.io/badge/C++-00599C?style=for-the-badge&logo=C%2B%2B&logoColor=white">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white">
  <img src="https://img.shields.io/badge/Arduino-00979D?style=for-the-badge&logo=Arduino&logoColor=white">
</div>

ESP-01S Wi-Fi 모듈과 1채널 릴레이를 활용하여 캐논(Canon) EOS 60D 카메라를 스마트폰으로 원격 제어하는 IoT 무선 릴리즈 프로젝트입니다.

---

## 1. 프로젝트 개요 (Overview)
기존의 유선 릴리즈를 하드웨어 해킹하여 Wi-Fi 제어가 가능한 스마트 릴리즈로 개조했습니다. 저비용으로 높은 신뢰성을 확보하기 위해 릴레이를 통한 물리적 절연 설계를 채택하였습니다.

- **핵심 목표:** 저비용 무선 촬영 시스템 구축 및 카메라 본체 보호
- **주요 기능:** Wi-Fi 기반 원격 셔터 제어, One-Shot AF 연동 지능형 촬영
- **프로젝트 루트**
  ```
  CRC/
  ├── firmware/         # ESP-01S 아두이노 코드 (.ino)
  ├── app/              # 어플리케이션 프로젝트 소스 코드
  ├── img/              # 스플래쉬 이미지
  └── README.md         # 메인 설명서
  ```
---

## 2. 하드웨어 구성 (Hardware)

### 주요 부품 (BOM)
| 부품명 | 역할 | 비고 |
| :--- | :--- | :--- |
| **ESP-01S** | 메인 컨트롤러 (Wi-Fi) | 펌웨어 구동 및 네트워크 통신 |
| **Relay Module V4.0** | 신호 스위칭 및 절연 | Active-LOW 제어 방식 |
| **Canon E3 Terminal** | 카메라 인터페이스 | 2.5mm 3극 잭 규격 |
| **ZJ-18 Paste** | 솔더링 플럭스 | 난납땜 구간(쇠판) 접합용 |

### 회로 설계 (Circuitry)
- **물리적 절연:** 릴레이(Relay)를 사용하여 MCU 전원부와 카메라 제어 회로를 완벽히 분리(Galvanic Isolation).
- **하이브리드 채널:** 단일 릴레이로 초점(Focus)과 셔터(Shutter) 라인을 병렬 제어하여 배선 단순화.

---

## 3. 소프트웨어 로직 (Software Logic)

### 지능형 촬영 알고리즘
카메라의 **One-Shot AF(Focus Priority)** 로직을 역이용하여 단일 채널로도 안정적인 촬영이 가능하도록 설계했습니다.

1. **신호 인가:** 릴레이 작동 시 초점 및 셔터 신호 동시 발생.
2. **카메라 판단:** One-Shot AF 모드에서 초점이 맞을 때까지 셔터 개방 대기.
3. **촬영 수행:** 렌즈 구동 완료 후 초점이 고정되는 찰나에 셔터 유닛 가동.

```cpp
void triggerShutter() {
  digitalWrite(0, LOW);  // 릴레이 ON (Active-LOW)
  delay(1500);           // AF 구동 및 촬영 대기 시간
  digitalWrite(0, HIGH); // 릴레이 OFF
}
```
## 4. 트러블슈팅 (Troubleshooting)
#### 부품 식별 및 분석
- 문제: 외장 플래시 폐기판에서 추출한 3단자 소자 활용 검토.
- 분석: SCR(사이리스터, CR3CM 등) 측정 결과, DC 회로에서의 래칭(Latching) 현상으로 인해 신호 차단이 불가능함을 확인.
- 해결: 안정적인 ON/OFF 제어를 위해 릴레이 기반의 설계를 최종 확정.

#### 난납땜 해결 (Soldering Challenge)
- 문제: 리모컨 내부 쇠판 및 점퍼 와이어 팁의 니켈 도금으로 인해 납이 붙지 않는 현상 발생.
- 해결: 점퍼 팁 제거 후 구리선 직결.
- 칼끝을 이용한 표면 연마(Scraping)로 산화막 제거.
- 로진 계열 페이스트(ZJ-18)를 활용한 입납(Tinning) 공정 수행.

## 5. 결과물 (Result)
- 스마트폰 웹 UI 또는 전용 앱을 통해 60D의 셔터를 지연 없이 제어 성공.
- 핫슈 마운트를 하우징에 통합하여 카메라 상단 장착 가능하도록 마감.

## 6.앱
#### 카메라를 원격으로 제어하기 위한 전용 안드로이드 앱. 
- **IDE:** Android Studio
- **Language:** Java / Kotlin (사용하신 언어에 맞춰 수정하세요)
- **Communication:** HTTP Request via Local Wi-Fi
- **Features:**
  - **Single Shot:** 즉시 촬영 릴레이 트리거
  - **Focus & Release:** 원격 반셔터 및 촬영 시퀀스 제어
  - **IP Configuration:** ESP-01S 모듈과의 통신을 위한 고정 IP 설정 기능
  
#### App UI 및 작동 방식
1. 안드로이드 앱에서 촬영 버튼 클릭 시, 지정된 URL(`http://[ESP_IP]/capture`)로 GET/POST 요청 전송.
2. ESP-01S의 웹 서버가 요청을 수신하여 GPIO 0번(릴레이)을 1.5초간 가동(LOW).
3. 60D 카메라의 One-Shot AF 로직에 의해 초점 확보 후 셔터 동작.

| 로딩 화면 | 메인 화면 |
| :---: | :---: |
| ![App Main UI]() | ![App Setting UI]() |
> *실제 앱 스크린샷으로 교체 예정*

## 7. 향후 로드맵
- WIFI 환경에서의 스니핑 위협 제거를 위한 상호 인증 기능 추가

## 8. 라이선스 및 작성자
- 작성자: rorena15
- 분야: IT Security / IoT Embedded
