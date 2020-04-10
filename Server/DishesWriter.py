def addDish(name=str, veggy=str, url=None, ingredients=None):
    output = None
    if (url == None and ingredients == None):
        output = name + ";" + veggy + ";null;null\n"
    elif (url == None and ingredients != None):
        output = name + ";" + veggy + ";null;" + ingredients + "\n"
    elif (url != None and ingredients == None):
        output = name + ";" + veggy + ";" + url + ";null\n"
    else:
        output = name + ";" + veggy + ";" + url + ";" + ingredients + "\n"

    with open("/home/pi/Bewaesserung/dishes.csv", "a") as file:
        file.write(output)

def removeDish(name=str):
    file = open('/home/pi/Bewaesserung/dishes.csv', 'r') 
    lines = file.readlines()
    file.close()
    for line in lines:
        dish = line.split(";")
        if dish[0] == name:
            lines.remove(line)
    with open('/home/pi/Bewaesserung/dishes.csv', 'w') as file:
        for line in lines:
            file.write(line)