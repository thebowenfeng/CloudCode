import time

import requests

HOST = "172.25.39.216"

res = requests.post(f"http://{HOST}:4243/containers/create", json={
    "Image": "ipython:latest",
    "Cmd": ["10000"],
    "ExposedPorts": {
        "10000/tcp": {}
    },
    "HostConfig": {
        "Memory": 536870912,
        "MemorySwap": 536870912,
        "NanoCpus": 1000000000,
        "PortBindings": {
            "10000/tcp": [{"HostIp": "", "HostPort": "10000"}]
        }
    }
})

print(res)
container_id = res.json()["Id"]
print(f"Container ID: {container_id}")

res2 = requests.post(f"http://{HOST}:4243/containers/{container_id}/start")
print(res2)

time.sleep(1)
print(requests.get(f"http://{HOST}:4243/containers/json").json())