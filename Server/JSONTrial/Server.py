import socket
import jsonWriter
import netifaces as ni
from _thread import start_new_thread
import time
import sys

def test(a):
    try:
        while (True):
            print("bla",a)
            time.sleep(3)
    except KeyboardInterrupt:
        print("Ctrl+C pressed...")
        sys.exit(1)
        

start_new_thread(test,(4,))

def getIpAddress():
    ni.ifaddresses('wlp1s0')
    return ni.ifaddresses('wlp1s0')[ni.AF_INET][0]['addr']


waterValveOpen = False

s = socket.socket()  # Create a socket object
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
host_name = socket.gethostname()
host = getIpAddress() #socket.gethostbyname(host_name)  # Get local machine name
port = 8080
s.bind((host, port))  # Bind to the port
print('Starting server on', host, port)
print('The Web server URL for this would be http://%s:%d/' % (host, port))

s.listen(5)  # Now wait for client connection.

print('Entering infinite loop; hit CTRL-C to exit')

while True:
    # Establish connection with client.
    c, (client_host, client_port) = s.accept()
    print('Got connection from', client_host, client_port)
    action = c.recv(1000).decode()  # should receive request from client. (GET ....)


    # read the data.json-File into the homepage value and send it to the Homepage as html
    jsonWriter.run()
    data = open("data.json", "r")
    homepage = data.read()
    c.send('HTTP/1.0 200 OK\n'.encode())
    c.send('Content-Type: text/html\n'.encode())
    c.send('\n'.encode())  # header and body should be separated by additional newline
    c.send(homepage.encode())  # Use triple-quote string.

    # decide, which action should run
    if (action == "1"):
        # change the status from the waterValue
        if (not waterValveOpen):
            waterValveOpen = True
            print("Wasser marsch!")
        else:
            waterValveOpen = False
            print("Wasser gestoppt!")

    # close the Connection
    c.close()
