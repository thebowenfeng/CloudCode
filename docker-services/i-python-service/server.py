import socket
import pexpect
import os
import threading
import sys

HOST = "0.0.0.0"
PORT = int(sys.argv[1])

# /mnt/c/Users/85751/Desktop/Projects/socket-python-test
# docker run -p 10000-10005:10000-10005 -it test:latest
print(f"server started. Running on port: {PORT}")


with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()
    conn, addr = s.accept()
    print(f"{addr} connected")

    ps = pexpect.spawn("python3")

    def read_child_stdout():
        while True:
            try:
                conn.sendall(os.read(ps.child_fd, 1024))
            except:
                s.shutdown(socket.SHUT_RDWR)
                s.close()
                os._exit(1)


    child_thread = threading.Thread(target=read_child_stdout)
    child_thread.start()

    while True:
        try:
            ps.sendline(conn.recv(1024))
            ps.stdin.flush()
        except:
            s.shutdown(socket.SHUT_RDWR)
            s.close()
            break


