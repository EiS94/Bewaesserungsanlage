from flask import Flask
from flask import request
import jsonWriter
import netifaces as ni

def getIpAddress():
    ni.ifaddresses('wlp1s0')
    return ni.ifaddresses('wlp1s0')[ni.AF_INET][0]['addr']

app = Flask(__name__)

@app.route('/')
def index():
    return "Bewässerungsanlage"

@app.route('/data.json')
def data():
    jsonWriter.run()
    data = open("data.json", "r")
    homepage = data.read()
    return homepage

@app.route('/v')
def getValveStatus():
    valveStatus = request.args.get("valve")
    if (valveStatus == "on"):
        print("Wasser marsch!")
    else: print("Stopp!")
    return valveStatus



if __name__ == '__main__':
    app.run(debug=True, host=getIpAddress())