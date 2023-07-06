import requests
import base64

with open("client.py", "rb") as file:
    res = requests.post("http://170.64.181.88:8080/api/files/add/test", files={"file": file})
    print(res)