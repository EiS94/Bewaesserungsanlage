import json

class jsonDataReader:

    wetness = None
    rain = None
    temperatur = None
    plant1 = None
    timestamp = None
    valve = None

    def __init__(self):
        with open('data.json') as json_file:
            data = json.load(json_file)
            for p in data['data']:
                self.wetness = p['wetness']
                self.rain = p['rain']
                self.temperatur = p['temperatur']
                self.plant1 = p['plant1']
                self.timestamp = p['timestamp']
                self.valve = p['valve']

    def updateData(self):
        with open('data.json') as json_file:
            data = json.load(json_file)
            for p in data['data']:
                self.wetness = p['wetness']
                self.rain = p['rain']
                self.temperatur = p['temperatur']
                self.plant1 = p['plant1']
                self.timestamp = p['timestamp']
                self.valve = p['valve']