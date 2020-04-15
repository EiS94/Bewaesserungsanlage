# -*- coding: UTF-8 -*-
import json
import random
import DHT as dht
import Adafruit_GPIO.SPI as SPI
import Adafruit_MCP3008
import RPi.GPIO as GPIO
import time
import calendar
import smbus


#sets the wiring from the pi
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)
GPIO.setup(12, GPIO.IN)
GPIO.setup(11, GPIO.IN)
GPIO.setup(22, GPIO.OUT)

#setup for the MCP3008 Analog digital converter
SPI_PORT = 0
SPI_DEVICE = 0
mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))

#safes the platform for temperatur sensor
pf = dht.common.get_platform()

#setup for the light sensor
DEVICE = 0x23
POWER_DOWN = 0x00
POWER_ON = 0x01
RESET = 0x07

# Start measurement at 4lx resolution. Time typically 16ms.
CONTINUOUS_LOW_RES_MODE = 0x13
# Start measurement at 1lx resolution. Time typically 120ms
CONTINUOUS_HIGH_RES_MODE_1 = 0x10
# Start measurement at 0.5lx resolution. Time typically 120ms
CONTINUOUS_HIGH_RES_MODE_2 = 0x11
# Start measurement at 1lx resolution. Time typically 120ms
# Device is automatically set to Power Down after measurement.
ONE_TIME_HIGH_RES_MODE_1 = 0x20
# Start measurement at 0.5lx resolution. Time typically 120ms
# Device is automatically set to Power Down after measurement.
ONE_TIME_HIGH_RES_MODE_2 = 0x21
# Start measurement at 1lx resolution. Time typically 120ms
# Device is automatically set to Power Down after measurement.
ONE_TIME_LOW_RES_MODE = 0x23

bus = smbus.SMBus(1)

def convertToNumber(data):
    result = (data[1] + (256 * data[0])) / 1.2
    return result

def readLight(addr=DEVICE):
    data = bus.read_i2c_block_data(addr, ONE_TIME_HIGH_RES_MODE_1)
    return convertToNumber(data)

#writes the sensor data to data.json file
def run():
    data = {}
    data['data'] = []
    data['data'].append({
        'wetness': getHumidityAir(),
        'rain': getAnalogRainStatus(),
        
        'temperatur': getTemperatur(),
        'plant1': getAnalogPlantStatus(1),
        'plant2': getAnalogPlantStatus(2),
        'plant3': getAnalogPlantStatus(3),
        'plant4': getAnalogPlantStatus(4),
        'plant5': getAnalogPlantStatus(5),
        'illuminance' : getIlluminance(),
        'p1Value' : getAnalogPlantValue(1),
        'p2Value' : getAnalogPlantValue(2),
        'p3Value' : getAnalogPlantValue(3),
        'p4Value' : getAnalogPlantValue(4),
        'p5Value' : getAnalogPlantValue(5),
        'timestamp': getTimestamp(),
        'valve': getValveStatus()
    })

    with open('/home/pi/Bewaesserung/data.json', 'w') as outfile:
        json.dump(data, outfile)

def getAnalogPlantStatus(i):
    plant1Status = getAnalogSignal(i)
    if (plant1Status < 500):
        return "ausgezeichnet bewässert"
    elif (plant1Status < 750):
        return "ausreichend bewässert"
    else:
        return "braucht Wasser"

def getAnalogRainStatus():
    rainStatus = getAnalogSignal(0)
    if (rainStatus < 600):
        return "starker Regen"
    elif (rainStatus < 800):
        return "Regen"
    elif (rainStatus < 950):
        return "leichter Regen"
    else:
        return "kein Regen"

def getAnalogBrightnessValue():
    return getAnalogSignal(7)

def getAnalogRainValue():
    return getAnalogSignal(0)

def getAnalogPlantValue(i):
    return getAnalogSignal(i)    

def getTimestamp():
    return calendar.timegm(time.gmtime())

def getTemperatur():
    h, t = dht.read_retry(dht.DHT22, 4, 15, 2, pf)
    if (t != None):
        return float("{0:.1f}".format(t))
    else: return 199.9

def getHumidityAir():
    h, t = dht.read_retry(dht.DHT22, 4, 15, 2, pf)
    if (h != None):
        return float("{0:.1f}".format(h))
    else: return 199.9

def getRain():
    rain = GPIO.input(12)
    rain = ("%ld" % rain)
    if (rain == "1"):
        return("Nein")
    else:
        return("Ja")

def getHumidityPlant1():
    wet = GPIO.input(11)
    wet = ("%ld" % wet)
    if (wet == "1"):
        return("braucht Wasser")
    else:
        return("ausreichend bewässert")

def openValve():
    GPIO.output(22, GPIO.LOW)

def closeValve():
    GPIO.output(22, GPIO.HIGH)

def getValveStatus():
    if GPIO.input(22) == GPIO.HIGH:
        return "aus"
    else:
        return "an"

def getAnalogSignal(i):
    if (i < 8 and i >= 0):
        return mcp.read_adc(i)
    else: return "select Channel-number between 0 and 7"

def getAllAnalogSignals():
    values = [0]*8
    for i in range(8):
        values[i] = mcp.read_adc(i)
    return values

def getIlluminance():
    return format(readLight(),'.2f')
    

def cleanExit():
    GPIO.cleanup()

def writeTemperatur():
    while (True):
        print("bla")