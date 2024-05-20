// 초음파 센서 값으로 LED 조절하는 아두이노 코드
#define TRIG_PIN1 6
#define ECHO_PIN1 7
#define RED_LED_PIN1 8
#define GREEN_LED_PIN1 9
 
#define TRIG_PIN2 4
#define ECHO_PIN2 5
#define RED_LED_PIN2 11
#define GREEN_LED_PIN2 12

unsigned long startTime1Low = 0;
unsigned long startTime2Low = 0;
unsigned long startTime1High = 0;
unsigned long startTime2High = 0;

int consecutiveSecondsLow1 = 0;
int consecutiveSecondsLow2 = 0;
int consecutiveSecondsHigh1 = 0;
int consecutiveSecondsHigh2 = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  pinMode(TRIG_PIN1, OUTPUT);
  pinMode(ECHO_PIN1, INPUT);
  pinMode(RED_LED_PIN1, OUTPUT);
  pinMode(GREEN_LED_PIN1, OUTPUT);

  pinMode(TRIG_PIN2, OUTPUT);
  pinMode(ECHO_PIN2, INPUT);
  pinMode(RED_LED_PIN2, OUTPUT);
  pinMode(GREEN_LED_PIN2, OUTPUT);

}

void loop() {
  // put your main code here, to run repeatedly:
  long distance1 = measureDistance(TRIG_PIN1, ECHO_PIN1);
  delay(10);
  long distance2 = measureDistance(TRIG_PIN2, ECHO_PIN2);

  Serial.print(distance1);
  Serial.print(", ");
  Serial.println(distance2);

  checkDistanceAndControlLED(distance1, startTime1Low, consecutiveSecondsLow1, startTime1High, consecutiveSecondsHigh1, RED_LED_PIN1, GREEN_LED_PIN1);
  checkDistanceAndControlLED(distance2, startTime2Low, consecutiveSecondsLow2, startTime2High, consecutiveSecondsHigh2, RED_LED_PIN2, GREEN_LED_PIN2);
  
  delay(1000);

}
void checkDistanceAndControlLED(long distance, unsigned long &startTimeLow, int &consecutiveSecondsLow, unsigned long &startTimeHigh, int &consecutiveSecondsHigh, int redPin, int greenPin) {
  if (distance < 18) {
    consecutiveSecondsLow++;
    if (consecutiveSecondsLow == 5) {
      consecutiveSecondsHigh = 0;
    }
    if (consecutiveSecondsLow >= 5 && consecutiveSecondsHigh == 0) {
      controlLED(distance, redPin, greenPin);
    }
    startTimeLow = millis();
  } else {
    consecutiveSecondsHigh++;
    if (consecutiveSecondsHigh == 5) {
      consecutiveSecondsLow = 0;
    }
    if (consecutiveSecondsHigh >= 5 && consecutiveSecondsLow == 0) {
      controlLED(distance, redPin, greenPin);
    }
    startTimeHigh = millis();
  }

  if (millis() - startTimeLow >= 5000) {
    startTimeLow = millis();
  }
  if (millis() - startTimeHigh >= 5000) {
    startTimeHigh = millis();
  }
}

long measureDistance(int trigPin, int echoPin) {
  long duration, distance;
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  distance = (duration/2) / 29.1;
  delayMicroseconds(2000);
  return distance;
}

void controlLED(long distance, int redPin, int greenPin) {
  if (distance < 18) {
    digitalWrite(redPin, HIGH);
    digitalWrite(greenPin, LOW);
  } else {
    digitalWrite(redPin, LOW);
    digitalWrite(greenPin, HIGH);
  }
}
