# 아두이노로 수집한 정보를 firebase firestore 데이터베이스에 실시간으로 전송하는 라즈베리 파이 코드
import serial
import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime

# Firebase 초기화
cred =
 credentials.Certificate("parking-f2b0d-firebase-adminsdk-neko8-48f78d3fd5.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# 시리얼 포트 설정 (라즈베리파이에 연결된 아두이노 포트)
ser = serial.Serial('/dev/ttyUSB0', 9600)
ser.flush()

while True:
    if ser.in_waiting > 0:
        data = ser.readline().decode('utf-8').rstrip()
        print("Received data:", data)  # 데이터 출력

        # 문자열을 쉼표로 분리하여 리스트로 변환
        data_list = data.split(", ")
        
        # 데이터가 올바르게 전송되었는지 확인
        if len(data_list) == 2:
            try:
                distance1 = int(data_list[0])
                distance2 = int(data_list[1])

                # 각 센서의 점유 상태 결정
                is_occupied1 = distance1 < 18  # 17cm 미만일 경우 점유된 것으로 간주
                is_occupied2 = distance2 < 18  # 17cm 미만일 경우 점유된 것으로 간주

                # 현재 시각을 타임스탬프로 변환
                timestamp = datetime.now()

                # Firebase Firestore에 각 센서의 데이터 저장
                # 센서 ID를 'sensor1', 'sensor2'로 가정합니다.
                doc_ref1 = db.collection(u'park').document(u'sensor1')
                doc_ref1.set({
                    u'is_occupied': is_occupied1,
                    u'distance': distance1,
                    u'timestamp': timestamp
                })

                doc_ref2 = db.collection(u'park').document(u'sensor2')
                doc_ref2.set({
                    u'is_occupied': is_occupied2,
                    u'distance': distance2,
                    u'timestamp': timestamp
                })
            except ValueError:
                # data_list의 요소가 정수로 변환될 수 없는 경우
                print("Error: Non-integer value received")
