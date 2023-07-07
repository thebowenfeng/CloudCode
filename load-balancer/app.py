from flask import Flask, request
import redis
import json
import requests
import time
import threading
import os

MAX_AFK_TIME = 10 # Maximum time a metrics worker can stop emitting heartbeat signals before it is assumed dead (seconds)

app = Flask(__name__)
r = redis.Redis(host="172.25.39.216", port=6379, decode_responses=True)
if r.get("server_list") is None:
    r.set("server_list", json.dumps([]))


def metrics_worker():
    worker_lock = r.lock("metrics_worker", timeout=10)
    worker_lock.acquire()
    while True:
        for server in json.loads(r.get("server_list")):
            count = int(requests.get(f"http://{server}/api/metrics/session/count",
                                 headers={'Authorization': f"Bearer {os.getenv('CODECLOUD_API_KEY')}"}).content)
            r.set(server, count)
            worker_lock.reacquire()
        time.sleep(1)


def add_server(host):
    old_list = json.loads(r.get("server_list"))
    if host not in old_list:
        old_list.append(host)
        r.set("server_list", json.dumps(old_list))
        r.set(host, 0)


def route_request(route, user_id, method, json_body=None, auth_header=None):
    with r.lock(f"lock[{user_id}]"):
        session = r.get(user_id)
        if route == "api/session/create" and session is not None:
            # Check if session mapping should be invalidated
            resp = requests.get(f"http://{session}/api/metrics/session/has?userId={user_id}", headers={'Authorization': auth_header})
            if resp.status_code != 200:
                # Invalidate mapping if server does not contain session anymore
                session = None

        if session is None:
            server_list = json.loads(r.get("server_list"))

            min_load = 99999
            min_server = None
            for server in server_list:
                server_load = int(r.get(server))
                if server_load < min_load:
                    min_server = server
                    min_load = server_load

            r.set(user_id, min_server)
            session = min_server

    if method == "GET":
        return requests.get(f"http://{session}/{route}?userId={user_id}", headers={'Authorization': auth_header})
    elif method == "POST":
        return requests.post(f"http://{session}/{route}", json=json_body, headers={'Authorization': auth_header})


@app.route('/load_balancer/add', methods=['POST'])
def add():
    new_server = request.json.get("server", None)
    if new_server is None:
        return "No server specified", 400

    add_server(new_server)


@app.route('/', defaults={'path': ''})
@app.route('/<path:path>', methods=['GET'])
def route_all_get(path):
    user_id = request.args.get('userId')
    if user_id is None:
        return "user_id not present", 400
    auth_header = request.headers.get('Authorization', None)

    resp = route_request(path, user_id, "GET", None, auth_header)
    return resp.content, resp.status_code, resp.headers.items()


@app.route('/', defaults={'path': ''})
@app.route('/<path:path>', methods=['POST'])
def route_all_post(path):
    user_id = request.json.get('user_id', None)
    if user_id is None:
        return "user_id not present", 400
    auth_header = request.headers.get('Authorization', None)

    resp = route_request(path, user_id, "POST", request.json, auth_header)
    return resp.content, resp.status_code, resp.headers.items()


add_server("localhost:8080")

t = threading.Thread(target=metrics_worker, daemon=True)
t.start()
if __name__ == '__main__':
    app.run()
