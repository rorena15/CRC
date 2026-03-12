# 📸 IoT Wireless Shutter Release for Canon 60D

ESP-01S Wi-Fi 모듈과 1채널 릴레이를 활용하여 캐논(Canon) EOS 60D 카메라를 스마트폰으로 원격 제어하는 IoT 무선 릴리즈 프로젝트입니다.

---

## 1. 프로젝트 개요 (Overview)
기존의 유선 릴리즈를 하드웨어 해킹하여 Wi-Fi 제어가 가능한 스마트 릴리즈로 개조했습니다. 저비용으로 높은 신뢰성을 확보하기 위해 릴레이를 통한 물리적 절연 설계를 채택하였습니다.

- **핵심 목표:** 저비용 무선 촬영 시스템 구축 및 카메라 본체 보호
- **주요 기능:** Wi-Fi 기반 원격 셔터 제어, One-Shot AF 연동 지능형 촬영

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
