import requests
import json
import base64

"""
The API access data must be saved in this folder in a file "access_data.txt" 
with the format username:password
"""


def get_oauth_key(access_data_path="access_data.txt"):
    with open(access_data_path, "r") as file:
        data = file.readline().split(":")
        user = data[0]
        pwd = data[1]

    # old method
    # b64 = base64.b64encode(str.encode(user + ":" + pwd)).decode()
    # headers = {"Authorization": f"Basic {b64}"}
    # request = requests.get('https://login.meteomatics.com/api/v1/token', headers=headers)

    request = requests.get('https://login.meteomatics.com/api/v1/token', auth=(user, pwd))
    if request.status_code == 200:
        result = json.loads(request.text)
        return result["access_token"]
    else:
        raise Exception("Error while getting meteomatics-API-token.")
