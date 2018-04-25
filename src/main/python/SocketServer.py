#!/usr/bin/python3.5 -u
# coding=UTF-8

import socket
import os
import time
import sys


s = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
try:
    os.remove("/tmp/socketname")
except OSError:
    pass
s.bind("/tmp/socketname")
s.listen(1)
conn, addr = s.accept()
print("Python --> Client connected:" + str(addr))


def wrap_msg(msg):
    return "<msg>"+msg+"</msg>"

while 1:
    msg = input("msg to send:$ ")
    print("msg retrieved: " + msg)
    conn.sendall(wrap_msg(msg).encode())

conn.close()