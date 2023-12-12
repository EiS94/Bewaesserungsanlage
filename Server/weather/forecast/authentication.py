"""
The API key must be saved in this folder in a file "api_key.txt"
"""


def get_auth_key(file_path="api_key.txt"):
    with open(file_path) as file:
        return file.readline()
