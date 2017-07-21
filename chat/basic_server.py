import socket
import sys

class BasicServer(object):

    def __init__(self, port):
        self.port = int(port)
        self.socket = socket.socket()
        self.socket.bind(("127.0.0.1", self.port))
        self.socket.listen(5)

args = sys.argv
if len(args) != 2:
    print "Please supply a port."
    sys.exit()
server = BasicServer(args[1])
while 1:
  (new_sock, address) = server.socket.accept()
  message = new_sock.recv(200)
  print(message)
