import json

class jsonDataReader:

    wetness = None
    rain = None
    temperatur = None
    plant1 = None
    plant2 = None
    plant3 = None
    plant4 = None
    plant5 = None
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
                self.plant2 = p['plant2']
                self.plant3 = p['plant3']
                self.plant4 = p['plant4']
                self.plant5 = p['plant5']
                self.timestamp = p['timestamp']
                self.valve = p['valve']