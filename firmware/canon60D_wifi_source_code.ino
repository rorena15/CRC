#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

const char* ssid = "Canon60D";
const char* password = "15158660";

ESP8266WebServer server(80);
const int relayPin = 0; 

int mode = 0; 
unsigned long actionTime = 0;
int targetCount = 0;
int currentCount = 0;
int intervalMs = 1000;

void triggerShutter() {
  digitalWrite(relayPin, LOW);
  delay(400); 
  digitalWrite(relayPin, HIGH);
  Serial.println("Shutter Triggered!");
}

void handleShoot() {
  triggerShutter();
  server.send(200, "text/plain", "OK");
}

void handleTimer() {
  int sec = server.arg("sec").toInt();
  if (sec == 0) sec = 5; 
  mode = 1;
  actionTime = millis() + (sec * 1000);
  Serial.print("Timer Set: "); Serial.print(sec); Serial.println("s");
  server.send(200, "text/plain", "OK");
}

void handleBurst() {
  targetCount = server.arg("cnt").toInt();
  int intervalSec = server.arg("int").toInt();
  if (targetCount == 0) targetCount = 5;
  if (intervalSec == 0) intervalSec = 3;
  
  intervalMs = intervalSec * 1000;
  mode = 2;
  currentCount = 0;
  actionTime = millis();
  Serial.print("Burst Set: "); Serial.print(targetCount);
  Serial.print("x ("); Serial.print(intervalSec); Serial.println("s int)");
  server.send(200, "text/plain", "OK");
}

void handleStop() {
  mode = 0;
  currentCount = 0;
  targetCount = 0;
  digitalWrite(relayPin, HIGH);
  Serial.println("Command: STOP");
  server.send(200, "text/plain", "OK");
}

void setup() {
  Serial.begin(115200);
  pinMode(relayPin, OUTPUT);
  digitalWrite(relayPin, HIGH); 
  WiFi.softAP(ssid, password);
  
  server.on("/shoot", handleShoot);
  server.on("/timer", handleTimer);
  server.on("/burst", handleBurst);
  server.on("/stop", handleStop);
  server.begin();
  Serial.println("\n--- System Ready ---");
}

void loop() {
  server.handleClient();
  
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();
    char op = cmd[0];
    
    if (op == 's') { triggerShutter(); } 
    else if (op == 'x') { handleStop(); }
    else if (op == 't') {
      int sec = 5; sscanf(cmd.c_str(), "t %d", &sec);
      mode = 1; actionTime = millis() + (sec * 1000);
      Serial.print("Command: Timer "); Serial.print(sec); Serial.println("s");
    } 
    else if (op == 'b') {
      int c = 5, i = 3; sscanf(cmd.c_str(), "b %d %d", &c, &i);
      targetCount = c; intervalMs = i * 1000;
      mode = 2; currentCount = 0; actionTime = millis();
      Serial.print("Command: Burst "); Serial.print(c);
      Serial.print("x ("); Serial.print(i); Serial.println("s int)");
    }
  }
  
  if (mode == 1 && millis() >= actionTime) {
    triggerShutter();
    mode = 0;
  } else if (mode == 2 && millis() >= actionTime) {
    if (currentCount < targetCount) {
      triggerShutter();
      currentCount++;
      actionTime = millis() + intervalMs;
    } else {
      mode = 0;
      Serial.println("Burst Done");
    }
  }
}
