import time
from datetime import datetime
import sys
import calendar
import JsonWriter

#def getTemperatur():
#    h, t = dht.read_retry(dht.DHT22, 4)
#    return float("{0:.1f}".format(t))
#
#def getHumidityAir():
#    h, t = dht.read_retry(dht.DHT22, 4)
#    return float("{0:.1f}".format(h))

#write the acutal sensor datas to temperatur.eike
#if there are more than 30 entrys, the oldest are deleted
#Data-Format: timestamp,temperatur,humidity,rain,plant1,plant2,plant3,plant4,plant5
def addData():
    timestamp = calendar.timegm(time.gmtime())
    temperatur = "{},{},{},{},{},{}\n".format(timestamp,JsonWriter.getTemperatur(),JsonWriter.getHumidityAir(),JsonWriter.getAnalogSignal(0),JsonWriter.getAnalogSignal(1),JsonWriter.getIlluminance())
    file = open('/home/pi/Bewaesserung/wheater.txt', 'r') 
    lines = file.readlines()
    file.close()
    while (len(lines) >= 30):
        lines.pop(0)
    lines.append(temperatur)
    with open('/home/pi/Bewaesserung/wheater.txt', 'w') as f:
        for item in lines:
            f.write(item)

def writeData():
    try:
        while(True):
            addData()
            time.sleep(60*60)
    except KeyboardInterrupt:
        print("Ctrl+C pressed...")
        sys.exit(1)
        