//In firebase, blind state, state lock, 1 1 means rolled up, 0 0 means rolled down

#include <Firebase_Arduino_WiFiNINA.h>
#include <WiFiNINA.h>
#include <WiFiUdp.h>
#include <RTCZero.h>
#include "DHT.h"
#include<stdio.h>

#define DHTPIN 4
#define DHTTYPE DHT11

#define FIREBASE_HOST "smartyblinds-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "NOz1XtY3HqrdACe7qDnARnkuy8NFnL3N3JY9Vsjl"
// #define WIFI_SSID "HoneyPot"
// #define WIFI_PASSWORD "12345678"
#define WIFI_SSID "1800Unit213"
#define WIFI_PASSWORD "1800U213"
FirebaseData firebaseData;

//Set the specific serial number of this blind
String SERIAL_NUM = "123";

//Local initialization from cloud database
int action = 2;
int lock = 2;
int Total_Data = 0;
int SetLight = 0;
String Operation = "";
int iteration = 0;

int lightPin = A4;

DHT dht(DHTPIN, DHTTYPE);

// char *ssid = "HoneyPot";  //  WiFi credentials and setup
// char *password = "12345678";
char *ssid = "1800Unit213";  //  WiFi credentials and setup
char *password = "1800U213";
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

//Always ran once on boot
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
 
  //Print connection status
  //printWiFiStatus();
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH, WIFI_SSID, WIFI_PASSWORD);
  
  // Start Real Time Clock
  rtc_wifi.begin();

  pinMode(lightPin,INPUT);

  pinMode(5,OUTPUT);
  digitalWrite(5,HIGH);
  
  dht.begin(); // initialize the sensor

  //Register these specific blinds to the database
  if (Firebase.getInt(firebaseData,"/Blinds/" + SERIAL_NUM + "/Blind_State")) {
    //do nothing, do not overwrite database vaules
  } else {
    //initialize values to database
    Firebase.setString(firebaseData, "/Blinds/" + SERIAL_NUM + "/User", "NULL");
    Firebase.setInt(firebaseData, "/Blinds/" + SERIAL_NUM + "/Blind_State", 1);
    Firebase.setInt(firebaseData, "/Blinds/" + SERIAL_NUM + "/State_Lock", 1);
    Firebase.setInt(firebaseData, "/Blinds/" + SERIAL_NUM + "/Total_Data", 0);
    Firebase.setString(firebaseData, "/Blinds/" + SERIAL_NUM + "/Operation", "Manual");
    Firebase.setString(firebaseData, "/Blinds/" + SERIAL_NUM + "/auto", "ON");
  }

}

//Code in here gets constantly executed 
void loop() {

  unsigned long epoch;

  float tempC = dht.readTemperature();
  int light = analogRead(lightPin);

  epoch = WiFi.getTime();
  rtc_wifi.setEpoch(epoch + GMT * 60 *60);
  initRealTime();
  String date = ("20" + String(set_year) + "-" + String(set_month) + "-" + String(set_date) + " " + String(set_hour) + ":" + String(set_minute) + ":" + String(set_second));
  
  Serial.print("RTC: ");
  Serial.println(date);
  Serial.print("Temperature: ");
  Serial.println(tempC);
  Serial.print("Light: ");
  Serial.println(light);
  
  //State of the blinds currently
  if (Firebase.getInt(firebaseData,"/Blinds/" + SERIAL_NUM + "/Blind_State")) {

    if (firebaseData.dataType() == "int") {
      action = firebaseData.intData();
      Serial.print("State: ");
      Serial.println(action);
    }
  }

  //Do not want to close when already closed
  if (Firebase.getInt(firebaseData,"/Blinds/" + SERIAL_NUM + "/State_Lock")) {

    if (firebaseData.dataType() == "int") {
      lock = firebaseData.intData();
      Serial.print("Lock: ");
      Serial.println(lock);
    }
}

  //Do not want to overide existing data upon shutdown
  if (Firebase.getInt(firebaseData,"/Blinds/" + SERIAL_NUM + "/Total_Data")) {

    if (firebaseData.dataType() == "int") {
      Total_Data = firebaseData.intData();
      Serial.print("Total Data: ");
      Serial.println(Total_Data);
    }
}

  //Get type of operation
  if (Firebase.getString(firebaseData,"/Blinds/" + SERIAL_NUM + "/Operation")) {

    if (firebaseData.dataType() == "string") {
      Operation = firebaseData.stringData();
      Serial.print("Operation: ");
      Serial.println(Operation);
    }
}

  //1 for blinds rolled up, 0 for blinds rolled down

  //Roll up
  if((action == 1 ) && (lock == 0)){
    rollUp();
    //Do not want to close when already closed
    lock = 1;
    Firebase.setInt(firebaseData,"/Blinds/" + SERIAL_NUM + "/State_Lock", lock);

    //only record sensor data on a manual operation
    if (Operation == "Manual"){

      Total_Data++;
      String newPath = "/Blinds/" + SERIAL_NUM + "/Data/" + String(Total_Data);
      Firebase.setInt(firebaseData,"/Blinds/" + SERIAL_NUM + "/Total_Data", Total_Data);

      //record values on blind event
      Firebase.setInt(firebaseData, newPath + "/Event", action);
      Firebase.setString(firebaseData, newPath + "/RTC", date);
      Firebase.setFloat(firebaseData, newPath + "/Temp", tempC);
      Firebase.setInt(firebaseData, newPath + "/Light", light);
    }
  }
  
  //Roll Down
  if((action == 0) && (lock == 1)){
    rollDown();
    //Do not want to close when already closed
    lock = 0;
    Firebase.setInt(firebaseData,"/Blinds/" + SERIAL_NUM + "/State_Lock", lock);

    //only record sensor data on a manual operation
    if (Operation == "Manual"){
    
      Total_Data++;
      String newPath = "/Blinds/" + SERIAL_NUM + "/Data/" + String(Total_Data);
      Firebase.setInt(firebaseData,"/Blinds/" + SERIAL_NUM + "/Total_Data", Total_Data);
      
      //record values on blind event
      Firebase.setInt(firebaseData, newPath + "/Event", action);
      Firebase.setString(firebaseData, newPath + "/RTC", date);
      Firebase.setFloat(firebaseData, newPath + "/Temp", tempC);
      Firebase.setInt(firebaseData, newPath + "/Light", light);  
    }
  }

  //delay the loop to one second, 600 seconds equals 10 min, every 10 minutes upload current sensor values to database
  iteration++;
  delay(1000);
  if (iteration == 600){
    Firebase.setString(firebaseData, "/Blinds/" + SERIAL_NUM + "/Current/RTC", date);
    Firebase.setFloat(firebaseData, "/Blinds/" + SERIAL_NUM + "/Current/Temp", tempC);
    Firebase.setInt(firebaseData, "/Blinds/" + SERIAL_NUM + "/Current/Light", light); 

    iteration = 0;
  }
  
}