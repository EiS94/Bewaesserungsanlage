import json
import random

def run():
    data = {}
    data['data'] = []
    data['data'].append({
        'wetness': '30%',
        'rain': 'no',
        'temperatur': str(random.randint(-10, 40)),
        'description': 'sunny'
    })

    with open('data.json', 'w') as outfile:
        json.dump(data, outfile)
