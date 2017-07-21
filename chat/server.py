import socket
import sys
import select
import utils

HOST = ''
SOCKET_LIST = []
CHANNEL_CLIENTS = {} # channel to list of clients
SOCKET_CLIENT = {}

class Client(object):
  def __init__(self, socket):
    self.buff = ''
    self.channel = None
    self.name = None
    self.socket = socket

def chat_server():
  args = sys.argv
  if len(args) != 2:
    print "Please supply a port."
    sys.exit()
  server_socket = socket.socket()
  server_socket.bind((HOST, int(args[1])))
  server_socket.listen(5)
  SOCKET_LIST.append(server_socket)
  while 1:
    ready_to_read, ready_to_write, in_error = select.select(SOCKET_LIST,[],[])
    for sock in ready_to_read:
      if sock == server_socket:
        (clientSocket, addr) = server_socket.accept()
        newClient(clientSocket) # a new client connected
      # a client is sending a message
      else:
        receive(sock)
  server_socket.close()

# broadcast message to everyone in the socket's channel
def broadcast (client, message, notification=True):
  channel = client.channel
  if not notification:
    message = '[' + client.name + '] ' + message
  for c in CHANNEL_CLIENTS[channel]:
    # send the message only to peers
    if client != c :
      try :
        message = message.ljust(utils.MESSAGE_LENGTH, ' ')
        c.socket.send(message)
      except :
        remove(c)

def receive(socket):
  client = SOCKET_CLIENT[socket]
  data = socket.recv(utils.MESSAGE_LENGTH - len(client.buff))
  client.buff += data
  if data:
    if len(client.buff) >= utils.MESSAGE_LENGTH:
      data = client.buff
      client.buff = client.buff[utils.MESSAGE_LENGTH:]
      process(client, data) # full MSGLEN has been received
  else:
    # Remove the broken socket
    remove(client)

def process(client, data):
  message = ''
  data = data.rstrip()
  if client.name == None:
    client.name = data
  elif data.startswith('/'):
    data = data[1:].split()
    if data[0] == 'create':
      # leave channel and join the one you just created
      if len(data) == 1:
        message = utils.SERVER_CREATE_REQUIRES_ARGUMENT
      elif data[1] in CHANNEL_CLIENTS:
        message = utils.SERVER_CHANNEL_EXISTS.format(data[1])
      else:
        if client.channel:
          CHANNEL_CLIENTS[client.channel].remove(client)
          broadcast(client, utils.SERVER_CLIENT_LEFT_CHANNEL.format(client.name))
        CHANNEL_CLIENTS[data[1]] = [client]
        client.channel = data[1]

    elif data[0] == 'join':
      # leave channel and join new one
      if len(data) == 1:
        message = utils.SERVER_JOIN_REQUIRES_ARGUMENT
      elif data[1] in CHANNEL_CLIENTS:
        if client.channel != None:
          CHANNEL_CLIENTS[client.channel].remove(client)
          broadcast(client, utils.SERVER_CLIENT_LEFT_CHANNEL.format(client.name))
        CHANNEL_CLIENTS[data[1]] += [client]
        client.channel = data[1]
        broadcast(client, utils.SERVER_CLIENT_JOINED_CHANNEL.format(client.name))
      else:
        message = utils.SERVER_NO_CHANNEL_EXISTS.format(data[1])
    elif data[0] == 'list':
      for channel in CHANNEL_CLIENTS.keys():
        message += (channel + '\n')
    else:
      message = utils.SERVER_INVALID_CONTROL_MESSAGE.format('/' + data[0])
  else:
    if client.channel != None:
      broadcast(client, data, False)
    else:
      message = utils.SERVER_CLIENT_NOT_IN_CHANNEL
  if message:
    try :
      message = message.ljust(utils.MESSAGE_LENGTH, ' ')
      client.socket.send(message)
    except :
      remove(client)

def newClient(clientSocket):
  SOCKET_LIST.append(clientSocket)
  client = Client(clientSocket)
  SOCKET_CLIENT[clientSocket] = client

def remove(client):
  socket = client.socket
  broadcast(client, utils.SERVER_CLIENT_LEFT_CHANNEL.format(client.name))
  socket.close()
  if socket in SOCKET_LIST:
    SOCKET_LIST.remove(socket)
  CHANNEL_CLIENTS[client.channel].remove(client)
  del SOCKET_CLIENT[socket]

if __name__ == "__main__":
  sys.exit(chat_server())
