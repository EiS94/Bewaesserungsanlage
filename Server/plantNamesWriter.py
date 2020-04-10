import json

def run(i=int, name=str):
    with open('/home/pi/Bewaesserung/plantNames.json') as infile:
        dataOld = json.load(infile)

    plants = []
    for j in range(1,8):
        if (j != i):
            plant = "plant{}".format(j)
            plants.append(dataOld[0][plant])
        else:
            plants.append(name)
    
    data = []
    data.append({
        'plant1': plants[0],
        'plant2': plants[1],
        'plant3': plants[2],
        'plant4': plants[3],
        'plant5': plants[4],
        'plant6': plants[5],
        'plant7': plants[6]
    })

    with open('/home/pi/Bewaesserung/plantNames.json', 'w') as outfile:
        json.dump(data, outfile)

def default():
    data = []
    data.append({
        'plant1': "Blumen",
        'plant2': "Lavendel",
        'plant3': "Knoblauch",
        'plant4': "Chillis",
        'plant5': "Erdbeeren",
        'plant6': "Birnbaum",
        'plant7': "Kraeuterbeet"
    })

    with open('/home/pi/Bewaesserung/plantNames.json', 'w') as outfile:
        json.dump(data, outfile)