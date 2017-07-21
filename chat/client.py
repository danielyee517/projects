import socket
import sys
import select
import utils

def chat_client():
  BUFF = ''
  args = sys.argv
  if len(args) < 4:
    print "Please supply a name, server address and port."
    sys.exit()
  name = args[1]
  host = args[2]
  port = int(args[3])
  s = socket.socket()
  s.settimeout(2)
  try :
    s.connect((host, port))
    s.send(name.ljust(utils.MESSAGE_LENGTH, ' '))
  except :
    sys.stdout.write(utils.CLIENT_CANNOT_CONNECT.format(host, port))
    sys.stdout.flush()
    sys.exit()
  sys.stdout.write(utils.CLIENT_MESSAGE_PREFIX); sys.stdout.flush()
  while 1:
    socket_list = [sys.stdin, s]
    ready_to_read,ready_to_write,in_error = select.select(socket_list, [], [])
    for sock in ready_to_read:
      if sock == s:
        # Incoming message from remote server, s
        data = sock.recv(utils.MESSAGE_LENGTH - len(BUFF))
        BUFF += data
        if data:
          if len(BUFF) >= utils.MESSAGE_LENGTH:
            BUFF = BUFF.rstrip()
            sys.stdout.write(utils.CLIENT_WIPE_ME + '\r')
            sys.stdout.write(BUFF + '\n')
            sys.stdout.write(utils.CLIENT_MESSAGE_PREFIX)
            sys.stdout.flush()
            BUFF = BUFF[utils.MESSAGE_LENGTH: ]
        else:
          print utils.CLIENT_SERVER_DISCONNECTED.format(host, port)
          sys.exit()
      else:
        # User entered a message
        msg = sys.stdin.readline().ljust(utils.MESSAGE_LENGTH, ' ')
        s.send(msg)
        sys.stdout.write(utils.CLIENT_MESSAGE_PREFIX); sys.stdout.flush()

if __name__ == "__main__":
  sys.exit(chat_client())
