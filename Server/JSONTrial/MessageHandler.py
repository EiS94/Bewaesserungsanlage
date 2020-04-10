import time

def handleMessage(message):
    if message == "Wasser an!":
        print("Wasser wird angeschalten")
        time.sleep(2)
        print("Wasser wieder abgeschalten")
