from socket import *
from time import ctime
#import RPi.GPIO as GPIO

ctrCmd = ['Up','Down']

HOST = ''
PORT = 21567
BUFSIZE = 1024
ADDR = (HOST,PORT)

tcpSerSock = socket(AF_INET, SOCK_STREAM)
tcpSerSock.bind(ADDR)
tcpSerSock.listen(5)

while True:
        print('Waiting for connection')
        tcpCliSock,addr = tcpSerSock.accept()
        print('...connected from :', addr)
        try:
                while True:
                        data = ''
                        data = tcpCliSock.recv(BUFSIZE)
                        if not data:
                                break
                        if data == ctrCmd[0]:
                                print('Increase: ')
                        if data == ctrCmd[1]:
                                print('Decrease: ')
        except KeyboardInterrupt:
            print("Hier Bewässerung stoppen")
tcpSerSock.close();
