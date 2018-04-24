import socket
import os

s = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
try:
    os.remove("/tmp/socketname")
except OSError:
    pass
s.bind("/tmp/socketname")
s.listen(1)
conn, addr = s.accept()


def wrap_msg(msg):
    return "<msg>"+msg+"</msg>"

while 1:
    msg = input("msg to send:$ ")
    conn.sendall(wrap_msg(msg).encode())

conn.close()