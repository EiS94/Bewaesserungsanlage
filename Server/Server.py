# -*- coding: UTF-8 -*-
from flask import Flask, render_template, send_from_directory, send_file
from flask import request
import JsonWriter
import netifaces as ni
import sys
import signal
import wheaterData as wD
from _thread import start_new_thread
import time
from datetime import datetime
from flask import Markup
import calendar
import ValveHandler
import plantNamesWriter as pNW
import hashlib
import logging


#autostarts after network is connected and sleeps 10 seconds, so that the ip-adress is set.
#autostart file: sudo nano /etc/systemd/system/my_server.service
#enable: sudo systemctl enable my_server.service
#status: sudo systemctl status my_server.service
#see log of the server: journalctl -u my_server.service 
time.sleep(10)

#gets the current local ip address from the wlan0 interface
def getIpAddress():
    ni.ifaddresses('wlan0')
    return ni.ifaddresses('wlan0')[ni.AF_INET][0]['addr']

#start new Thread to write the sensor datas to a file in background
start_new_thread(wD.writeData,())
start_new_thread(ValveHandler.calcuteValveStatus,())

app = Flask(__name__)

#startpage
@app.route('/')
def index():
    plant1Status = JsonWriter.getAnalogSignal(1)
    if (plant1Status < 600):
        plant1Status = "ausgezeichnet bewaessert"
    elif (plant1Status < 800):
        plant1Status = "ausreichend bewaessert"
    else:
        plant1Status = "braucht Wasser"

    rainStatus = JsonWriter.getAnalogSignal(0)
    if (rainStatus < 600):
        rainStatus = "starker Regen"
    elif (rainStatus < 800):
        rainStatus = "Regen"
    elif (rainStatus < 950):
        rainStatus = "leichter Regen"
    else:
        rainStatus = "kein Regen"

    return render_template('index.html', plant1=plant1Status, temperatur=JsonWriter.getTemperatur(), humidity=JsonWriter.getHumidityAir(), rain=rainStatus)

#Json file with the information from the sensors, needed for the android app
@app.route('/data.json')
def data():
    JsonWriter.run()
    #data = open("data.json", "r")
    #homepage = data.read()
    #return homepage
    return send_file("data.json", cache_timeout=0)

@app.route('/plantNames.json')
def plantNames():
    pNW.run()
    return send_file("plantNames.json", cache_timeout=0)

@app.route('/dishes.csv')
def dishes():
    return send_file("dishes.csv", cache_timeout=0)

#needed, to controll the watervalve
@app.route('/v')
def getValveStatus():
    valveStatus = request.args.get("valve")
    if (valveStatus == "on"):
        JsonWriter.openValve()
        print("Wasser marsch!")
    else: 
        JsonWriter.closeValve()
        print("Stopp!")
    return valveStatus

#needed, to change plantNames by user
@app.route('/plantNames')
def changePlantNames():
    plant1 = request.args.get("plant1")
    plant2 = request.args.get("plant2")
    plant3 = request.args.get("plant3")
    plant4 = request.args.get("plant4")
    plant5 = request.args.get("plant5")
    plant6 = request.args.get("plant6")
    plant7 = request.args.get("plant7")
    if (plant1 != "defaultt"):
        pNW.run(1,plant1)
        pNW.run(2,plant2)
        pNW.run(3,plant3)
        pNW.run(4,plant4)
        pNW.run(5,plant5)
        pNW.run(6,plant6)
        pNW.run(7,plant7)
    else:
        pNW.default()
    plantNames()
    return "Wird benoetigt, um Pflanzennamen durch den Benutzer zu aendern"

#stops the Server and closes the valve
@app.route("/s")
def shutdown():
    pw = "6941bea706c4231b322a29b91fe6701c4d5e89f3e32f4c84f49b84caf0486b96"
    password = request.args.get("password")
    sig = hashlib.sha256(password.encode()).hexdigest()
    if (sig == pw):
        func = request.environ.get('werkzeug.server.shutdown')
        if func is None:
            print("Error by shutting down server")
        else:
            JsonWriter.closeValve()
            print("Wasser abgeschalten")
            JsonWriter.cleanExit()
            print("Ausgänge freigegeben")
            func()
            print("Server shutted down.")
        return "Server shutted down."
    else:
        return "Wrong password, try again"

@app.route('/wheaterData.txt')
def writeWheaterData():
    return send_file("wheater.txt", cache_timeout=0)

#show log-File
@app.route("/log")
def writeLogFile():
    return send_file("log.txt", cache_timeout=0)

#test-page, no functionality
@app.route('/test')
def test():
    return render_template('test.html')

@app.route('/favicon.png')
def favicon():
    return send_file('favicon.png', cache_timeout=0)

#shows a chart from the last mesuared data from the sensors
@app.route('/wheaterData')
def wheaterData():
    #read wheater.txt file (timestamp, temperatur, humidity)
    file = open('/home/pi/Bewaesserung/wheater.txt', 'r') 
    lines = file.readlines()
    labels = []
    tempData = []
    humData = []
    rainData = []
    plant1Data = []
    illData = []

    #write all entrys to seperate lists
    for item in lines:
        split = item.split(",")
        labels.append(split[0])
        tempData.append(split[1])
        humData.append(split[2])
        rainData.append(split[3])
        plant1Data.append(split[4])
        illData.append(split[5])

    #make String of time: "['MM:hh:ss',...,'MM:hh:ss']"
    labelsString = "["
    for item in labels:
        time = datetime.fromtimestamp(int(item))
        labelsString += '\'{}\''.format(str(time)[11:-3])
        labelsString += ","
    labelsString = labelsString[0:-1]
    labelsString += "]"

    #make String of temperatur: "[temp1,...,tempN]"
    tempString = "["
    for item in tempData:
        tempString += item
        tempString += ","
    tempString = tempString[0:-1]
    tempString += "]" 

    #make String of humidity: "[hum1,...,humN]"
    humString = "["
    for item in humData:
        humString += item
        humString += ","
    humString = humString[0:-1]
    humString += "]"

    #make String of rain: "[rain1,...rainN]"
    rainString = "["
    for item in rainData:
        rainString += item
        rainString += ","
    rainString = rainString[0:-1]
    rainString += "]"

    #make String of plant1: "[plant11,...plant1N"]"
    plant1String = "["
    for item in plant1Data:
        plant1String += item[0:-1]
        plant1String += ","
    plant1String = plant1String[0:-1]
    plant1String += "]"

    #make String for illumincane: "[illuminance1,...,illuminanceN]"
    illString = "["
    for item in illData:
        illString += item
        illString += ","
    illString = illString[0:-1]
    illString += "]"

    #write String to temp-chart
    resultTemp = "new Chart(document.getElementById('temp-chart'), {type: 'line',data: {labels: %s" % labelsString
    resultTemp += ",datasets: [{data: %s,label:'Temperatur',borderColor:'#3e95cd',fill: false}]},options: {title: {display: true,text: 'Temperatur in °C'}\n}\n});" % tempString
    resultTemp = Markup(resultTemp)

    #write String to hum-chart
    resultHum = "new Chart(document.getElementById('hum-chart'), {type: 'line',data: {labels: %s" % labelsString
    resultHum += ",datasets: [{data: %s,label:'Luftfeuchtigkeit',borderColor:'#263e8e',fill: false}]},options: {title: {display: true,text: 'Luftfeuchtigkeit in Prozent'}\n}\n});" % humString
    resultHum = Markup(resultHum)

    #write String to rain-chart
    resultRain = "new Chart(document.getElementById('rain-chart'), {type: 'line',data: {labels: %s" % labelsString
    resultRain += ",datasets: [{data: %s,label:'analoge Sensordaten',borderColor:'#5312ee',fill: false}]},options: {title: {display: true,text: 'Regendaten'}\n}\n});" % rainString
    resultRain = Markup(resultRain)

    #write String to plant1-chart
    resultPlant1 = "new Chart(document.getElementById('plant1-chart'), {type: 'line',data: {labels: %s" % labelsString
    resultPlant1 += ",datasets: [{data: %s,label:'analoge Sensordaten',borderColor:'#e05a66',fill: false}]},options: {title: {display: true,text: 'Planze 1'}\n}\n});" % plant1String
    resultPlant1 = Markup(resultPlant1)
    #resultPlant1 = "var trace = {x: %s," % labelsString
    #resultPlant1 += "y: %s, mode: 'line'};" % plant1String
    #resultPlant1 = Markup(resultPlant1)

    resultIll = "new Chart(document.getElementById('ill-chart'), {type: 'line',data: {labels: %s" % labelsString
    resultIll += ",datasets: [{data: %s,label:'Helligkeit',borderColor:'#e05a66',fill: false}]},options: {title: {display: true,text: 'Helligkeit in Lux'}\n}\n});" % illString
    resultIll = Markup(resultIll)

    file.close()
    return render_template('wheaterData.html', tempchart=resultTemp, humchart=resultHum, rainchart=resultRain, illchart=resultIll)


#starts the server
if __name__ == '__main__':
    app.run(debug=True, host="192.168.178.26")


#stops the valve, when server is stoppped with CTRL+C
def handler(signal, frame):
    print("CTRL-C pressed")
    JsonWriter.closeValve()
    print("Wasser abgeschalten")
    JsonWriter.cleanExit()
    print("Ausgänge freigegeben")
    sys.exit(0)

signal.signal(signal.SIGINT, handler)
signal.pause()