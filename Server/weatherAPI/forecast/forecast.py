from datetime import datetime


def format_datetime(datetime_to_format):
    return datetime_to_format.strftime("%d.%m.%Y, %H:%M:%S")


class Forecast:

    def __init__(self, forecast_json):
        self.time = datetime.fromtimestamp(forecast_json["dt"])
        self.temperature = forecast_json["main"]["temp"]
        self.description = forecast_json["weather"][0]["description"]
        self.rain = "None"
        if "rain" in forecast_json:
            self.rain = forecast_json["rain"]["3h"]

    def __str__(self):
        return format_datetime(self.time) + "\n  - Temperature: " + str(self.temperature) + "°C\n  - Description: " + \
               self.description + "\n  - Rain: " + str(self.rain)
