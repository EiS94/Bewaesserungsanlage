import JsonWriter
import time
from datetime import datetime
import json
import jsonDataReader

def calcuteValveStatus():
    while(True):
        t = getTime(JsonWriter.getTimestamp())
        print(t)
        #Standardfall
        if (t.hour < 8 or t.hour > 18):
            #hier dann noch die Helligkeit abfragen

            #bei starkem Regen nicht bewaessern
            if (JsonWriter.getRain != "starker Regen"):

                #wenn mehr als 1 Pflanze Wasser braucht wird bewaessert
                if (countPlantStatus("braucht Wasser") > 1):
                    JsonWriter.openValve()
                    addLogData("mehr als 1 Pflanze braucht Wasser")

                #wenn mehr als 4 Pflanzen nur ausreichend bewaessert sind wird bewaessert
                elif (countPlantStatus("ausreichend bewässert") > 4):
                    JsonWriter.openValve()
                    addLogData("mehr als 4 nur ausreichend")

                #wenn eine Pflanze Wasser braucht und mehr als 2 Pflanzen nur ausreichend
                #bewaessert sind, wird bewaessert
                elif (countPlantStatus("ausreichend bewässert") > 4 and countPlantStatus("braucht Wasser") == 1):
                    JsonWriter.openValve()
                    addLogData("1 braucht, mehr als 2 ausreichend")

                #ansonsten sicherheitshalber Ventil schliessen
                else: JsonWriter.closeValve()
                addLogData("sicherheitshalber ausmachen")

        #Fall, wenn alle Wasser benoetigen
        elif (countPlantStatus("braucht Wasser") == 5):
            JsonWriter.openValve()
            addLogData("alle brauchen Wasser")

        #ansonsten wird sicherheitshalber geschlossen
        else: 
            JsonWriter.closeValve()
            addLogData("aus")
        time.sleep(5*60)

def getTime(timestamp):
    return datetime.fromtimestamp(timestamp)

def countPlantStatus(i=str) -> int:
    counter = 0
    if (JsonWriter.getAnalogPlantStatus(1) == i):
        counter += 1
    if (JsonWriter.getAnalogPlantStatus(2) == i):
        counter += 1
    if (JsonWriter.getAnalogPlantStatus(3) == i):
        counter += 1
    if (JsonWriter.getAnalogPlantStatus(4) == i):
        counter += 1
    if (JsonWriter.getAnalogPlantStatus(5) == i):
        counter += 1
    return counter

def updateData(jsonData=jsonDataReader.jsonDataReader):
        with open('/home/pi/Bewaesserung/data.json') as json_file:
            data = json.load(json_file)
            for p in data['data']:
                jsonData.wetness = p['wetness']
                jsonData.rain = p['rain']
                jsonData.temperatur = p['temperatur']
                jsonData.plant1 = p['plant1']
                jsonData.timestamp = p['timestamp']
                jsonData.valve = p['valve']

def addLogData(cause=str):
    now = datetime.now()
    date_time = now.strftime("%d/%m/%Y, %H:%M:%S ")
    output = date_time + cause + " Temperatur: " +  str(JsonWriter.getTemperatur()) + " Helligkeit: " + str(JsonWriter.getIlluminance()) + \
            " Feuchtigkeit: " + str(JsonWriter.getHumidityAir()) + " Regen: " + str(JsonWriter.getRain()) + " Planze1: " + \
            str(JsonWriter.getAnalogPlantValue(1)) + " Pflanze2: " + str(JsonWriter.getAnalogPlantValue(2)) + " Pflanze3: " + \
            str(JsonWriter.getAnalogPlantValue(3)) + " Pflanze4: " + str(JsonWriter.getAnalogPlantValue(4)) + " Pflanze5: " + \
            str(JsonWriter.getAnalogPlantValue(5)) + "\n"
    with open('/home/pi/Bewaesserung/log.txt', 'a') as file:
        file.write(output)

