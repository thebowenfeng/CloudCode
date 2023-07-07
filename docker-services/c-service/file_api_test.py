import requests
import base64
import os

with open("client.py", "rb") as file:
    res = requests.post("http://localhost:8080/api/files/add/test", files={"file": file}, headers={'Authorization': f"Bearer {os.getenv('CODECLOUD_API_KEY')}"})
    print(res.content)
