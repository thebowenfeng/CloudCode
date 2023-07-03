import socket
import threading

HOST = "172.25.39.216"
PORT = 10000

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORT))

    def recv_thread():
        while True:
            in_data = s.recv(1024).decode('utf-8')
            if in_data != '':
                print(in_data)

    child = threading.Thread(target=recv_thread)
    child.start()

    while True:
        data = input()
        if data == "ENTER":
            data = "\n"
        s.sendall(data.encode())