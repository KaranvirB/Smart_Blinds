#include <Firebase_Arduino_WiFiNINA.h>
#include <WiFiNINA.h>
#include <WiFiUdp.h>
#include <RTCZero.h>
#include "DHT.h"

#define DHTPIN 4
#define DHTTYPE DHT11

#define FIREBASE_HOST "smartyblinds-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "NOz1XtY3HqrdACe7qDnARnkuy8NFnL3N3JY9Vsjl"
#define WIFI_SSID "HoneyPot"
#define WIFI_PASSWORD "12345678"
FirebaseData firebaseData;

String path = "/Value";
int action = 2;
int SetLight = 0;
//0 for blinds rolled up, 1 for blinds rolled down
int blindState = 0; //Do not want to close when already closed

int lightPin = A4;

DHT dht(DHTPIN, DHTTYPE);

char *ssid = "HoneyPot";  //  WiFi credentials and setup
char *password = "12345678";
const int GMT = -5; // Time zone constant - change as required for your location

RTCZero rtc_wifi;
int status = WL_IDLE_STATUS;
byte set_year = 0; // can only be 2-digit year
byte set_month = 0;
byte set_date = 0;
byte set_hour = 0;
byte set_minute = 0;
byte set_second = 0;

void rollUp(){
  //Triggers on a falling edge, a click
  digitalWrite(3, LOW);
  delay(10000);
  digitalWrite(3, HIGH);
  Serial.println("Opened!");
}

void rollDown(){
  //Triggers on a falling edge, a click
  digitalWrite(2, LOW);
  delay(10000);
  digitalWrite(2, HIGH);
  Serial.println("Closed!");
}

void initRealTime() {
  set_year = rtc_wifi.getYear(); // can only be 2-digit year
  set_month = rtc_wifi.getMonth();
  set_date = rtc_wifi.getDay();
  set_hour = rtc_wifi.getHours();
  set_minute = rtc_wifi.getMinutes();
  set_second = rtc_wifi.getSeconds();
}


void setup() {

    //WHITE WIRE Blind Down Trigger
  pinMode(2, OUTPUT);
  digitalWrite(2, HIGH);

  //YELLOW WIRE Blind Up Trigger
  pinMode(3, OUTPUT);
  digitalWrite(3, HIGH);

  Serial.begin(9600);
  Serial.println("Serial begins!");

      // Establish a WiFi connection
  while ( status != WL_CONNECTED) {
 
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    status = WiFi.begin(ssid, password);
 
    // Wait 10 seconds for connection:
    delay(10000);
 }
 
//  // Print connection status
//  printWiFiStatus();
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH, WIFI_SSID, WIFI_PASSWORD);
  
  // Start Real Time Clock
  rtc_wifi.begin();

  pinMode(lightPin,INPUT);

  pinMode(5,OUTPUT);
  digitalWrite(5,HIGH);
  
  dht.begin(); // initialize the sensor
  
}

void loop() {
  // put your main code here, to run repeatedly:
  unsigned long epoch;

  float tempC = dht.readTemperature();
  int val = analogRead(lightPin);

  epoch = WiFi.getTime();
  rtc_wifi.setEpoch(epoch + GMT * 60 *60);
  initRealTime();
  String date = (String(set_year) + "-" + String(set_month) + "-" + String(set_date) + " " + String(set_hour) + ":" + String(set_minute) + ":" + String(set_second));
  
  Serial.print("RTC: ");
  Serial.println(date);
  Serial.print("Temperature: ");
  Serial.println(tempC);
  Serial.print("Light: ");
  Serial.println(val);

  Firebase.setString(firebaseData, path + "/RTC:", date);
  Firebase.setFloat(firebaseData, path + "/Temperature:", tempC);
  Firebase.setInt(firebaseData, path + "/Light:", val);



  if (Firebase.getInt(firebaseData, "Blinds")) {

    if (firebaseData.dataType() == "int") {
      action = firebaseData.intData();
      Serial.println(action);
    }
  }

  if((action == 1 )&& (blindState == 1)){
    rollUp();
    blindState = 0;
    Firebase.setInt(firebaseData, "Blinds", 2);
  }
  
  if((action == 0) && (blindState == 0)){
    rollDown();
    blindState = 1;
    Firebase.setInt(firebaseData, "Blinds", 2);  
  }

    if (Firebase.getInt(firebaseData, "SetLight")) {

    if (firebaseData.dataType() == "int") {
      SetLight = firebaseData.intData();
      Serial.println(action);
    }
  }

  if(SetLight != 0 && blindState == 0){
    if (val > SetLight){
      rollDown();
      blindState = 1;
      Firebase.setInt(firebaseData, "Blinds", 2);
    }
  }
if(SetLight != 0 && blindState == 1){
  if (val < SetLight){
      rollUp();
      blindState = 0;
      Firebase.setInt(firebaseData, "Blinds", 2);
    }
}
  
  // delay(10 * 60 * 1000);
  delay(2000);
}