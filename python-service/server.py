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


def is_socket_closed(sock: socket.socket) -> bool:
    try:
        # this will try to read bytes without blocking and also without removing them from buffer (peek only)
        data = sock.recv(16, socket.MSG_DONTWAIT | socket.MSG_PEEK)
        if len(data) == 0:
            return True
    except BlockingIOError:
        return False  # socket is open and reading from it would block
    except ConnectionResetError:
        return True  # socket was closed for some other reason
    except Exception as e:
        print("unexpected exception when checking if a socket is closed")
        return False
    return False


with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()
    while True:
        conn, addr = s.accept()
        print(f"{addr} connected")

        ps = pexpect.spawn("python3")

        def read_child_stdout():
            global ps
            while True:
                try:
                    conn.sendall(os.read(ps.child_fd, 1024))
                except:
                    ps = pexpect.spawn("python3")


        child_thread = threading.Thread(target=read_child_stdout)
        child_thread.start()

        while True:
            if is_socket_closed(conn):
                ps.terminate(force=True)
                break
            ps.sendline(conn.recv(1024))
            ps.stdin.flush()


