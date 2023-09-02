# 키친포스

## 퀵 스타트

```sh
cd docker
docker compose -p kitchenpos up -d
```

## 요구 사항
### 메뉴 그룹 서비스
1. 등록
- [x] 그룹명이 입력되어야 한다. 
2. 조회
- [ ] 모든 메뉴 그룹을 한번에 조회 가능하다. 
---
### 메뉴 서비스 
1. 등록
- [x] 메뉴 신규시 가격이 입력되어야하고, 0이상이어야 한다.
- [x] 메뉴는 상품이 비어있으면 안된다.
- [x] 상품 수량은 0 이상 이다. 
- [x] (상품 가격 * 수량)을 했을 때, 메뉴 가격보다 비싸야한다.
- [x] 상품명이 입력되어야하며, 비속어를 포함하면 안된다.
2. 가격 변경
- [x] 변경할 가격이 입력되어야하며, 0 이상이어야한다.
- [x] 메뉴 가격은 내부 모든 메뉴 상품의 (가격*수량) 을 합한 값보다 작아야 한다. 
3. 메뉴 표시
- [x] 메뉴를 표시할 때, 메뉴 가격은 내부 모든 메뉴 상품의 (가격*수량) 을 합한 값보다 작아야 한다.
- [x] 메뉴는 표시하거나 숨길 수 있다.(메뉴가 없는 경우 메뉴를 숨길 수 있다.)
4. 조회
- [ ] 가지고 있는 모든 메뉴를 한번에 조회 가능하다.
---
### 주문 서비스
1. 등록
- [x] 신규 주문 시 주문 유형이 입력되어야 한다. 
- [ ] 주문 유형은 매장식사, 배달, 포장이 있다. 
- [ ] 주문 상태는 대기, 승인, 제공, 배달중, 배달완료, 완료가 있다.
- [x] 매장 식사가 아닌 경우 수량이 0 이상이어야 한다. 
- [x] 표시되지 않은 메뉴는 주문할 수 없다. 
- [x] 주문 항목의 가격과 메뉴의 가격이 동일해야한다.
- [x] 배달인 경우 배송지가 비어있으면 안된다. 
- [x] 매장 식사인 경우 주문 테이블이 비어있으면 안된다.
2. 주문 승인
- [x] 주문 승인 시 주문 상태는 대기여야 한다.
- [x] 주문 유형이 배달인 경우 가격은 (수량*메뉴 가격)의 합이다.
3. 주문 제공
- [x] 주문 제공시 주문상태가 수락이어야 한다.
4. 배달 시작
- [x] 배달 시 주문 유형은 배달이어야한다.
- [x] 주문 상태는 제공됨이어야한다. 
5. 배달 완료
- [x] 주문 상태는 배달중이어야한다.
6. 주문 완료
- [x] 주문 유형이 배달이고, 상태가 배달완료여야한다.
- [x] 포장이거나, 매장 식사는 상태가 제공됨 이어야한다.
- [x] 매장 식사 시 완료 처리 되지 않은 주문이 없는 경우 손님수를 0으로 설정하고, 테이블 상태를 비어있음으로 변경한다.
7. 조회
- [ ] 모든 주문을 한번에 조회 가능하다.

### 주문 테이블 서비스
1. 등록
- [x] 새로운 테이블 생성시 테이블 명이 주어져야 함.
- [x] 손님 착석시 테이블이 차지됨으로 설정.
- [x] 주문 완료시 테이블을 비어있는 상태로 변경한다. 
- [x] 주문이 완료되지 않은 경우 테이블을 비어있는 상태로 변경할 수 없다.
2. 손님수 변경
- [x] 손님 수는 0 이상이어야 한다. 
- [x] 테이블에 기존 손님이 있던 경우에만 손님수 변경이 가능하다. 
3. 조회
- [ ] 모든 테이블을 조회할 수 있다.
 
### 제품 서비스
1. 등록
- [x] 신규 제품 생성 시 가격이 입력되어야하며, 음수가 아니어야한다.
- [x] 제품명은 입력되어야하며, 비속어가 없어야한다. 
2. 가격 변경
- [x] 가격 변경시 변경 가격이 입력되어야하며, 음수가 아니어야한다. 
- [x] 상품가격*수량을 한 합이 메뉴 가격보다 작은 경우 메뉴를 숨긴다. 
3. 조회
- [ ] 모든 제품서비스를 조회 가능하다.

## 용어 사전

| 한글명 | 영문명 | 설명 |
| --- | --- | --- |
|  |  |  |

## 모델링
