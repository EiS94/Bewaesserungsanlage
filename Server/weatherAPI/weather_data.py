import json
import os
from datetime import datetime, timezone, timedelta
import pytz

import dateutil.parser
import requests

from forecast.authentication import get_auth_key
from forecast.forecast import Forecast
from past.authentication import get_oauth_key
from rain_intensity import RainIntensity


def get_rain_intensity(precip):
    if precip == 0.0:
        return RainIntensity.NONE
    elif precip <= 0.5:
        return RainIntensity.DRIZZLE
    elif precip <= 2.5:
        return RainIntensity.LIGHT_RAIN
    elif precip <= 10.0:
        return RainIntensity.MODERATE_RAIN
    elif precip <= 50.0:
        return RainIntensity.HEAVY_RAIN
    else:
        return RainIntensity.VERY_HEAVY_RAIN


def utc_to_local(utc_dt):
    return utc_dt.replace(tzinfo=timezone.utc).astimezone(tz=None)


def format_datetime(datetime_to_format):
    return datetime_to_format.strftime("%d.%m.%Y, %H:%M:%S")


def get_weather_data(time=datetime.now()):
    """
    use this to get hourly info from the last 24 hours

    datetime_yesterday = (time - timedelta(days=1)).replace(hour=0, minute=0, second=0, microsecond=0)
    url = "https://api.meteomatics.com/" + datetime_yesterday.isoformat() + "Z--" + datetime_now.isoformat() + \
          "Z:PT1H/t_2m:C,precip_1h:mm,precip_24h:mm,sunrise:sql,sunset:sql,uv:idx,wind_speed_10m:kmh" + \
          "/49.995934,8.230993/csv?access_token=" + token
    """
    # use this url to get info of the last hour and last 24 hours
    url = "https://api.meteomatics.com/" + time.isoformat() + "Z/t_2m:C,precip_1h:mm,precip_24h:mm," + \
          "sunrise:sql,sunset:sql,uv:idx,wind_speed_10m:kmh" + "/49.995934,8.230993/csv?access_token=" + \
          get_oauth_key(os.getcwd() + "\\past\\access_data.txt")

    request = requests.get(url)
    if request.status_code == 200:
        past_data = request.text.splitlines()[1].split(";")
    else:
        raise Exception("Error while getting past weather data")

    # use this url to get forecast info (next 3 hours)
    url = "https://api.openweathermap.org/data/2.5/forecast?lat=49.995934&lon=8.230993&units=metric&cnt=10" + \
          "&appid=" + get_auth_key(os.getcwd() + "\\forecast\\api_key.txt")
    request = requests.get(url)
    if request.status_code == 200:
        forecast_data = json.loads(request.text)
    else:
        raise Exception("Error while getting forecast weather data")

    return WeatherData(past_data, forecast_data)


class WeatherData:

    def __init__(self, past_data, forecast_data):
        self.datetime = dateutil.parser.isoparse(past_data[0])
        self.temperature = float(past_data[1])
        self.precip_past_hour = get_rain_intensity(float(past_data[2]))
        self.precip_past_24_hours = get_rain_intensity(float(past_data[3]))
        self.sunrise = dateutil.parser.isoparse(past_data[4])
        self.sunset = dateutil.parser.isoparse(past_data[5])
        self.uv_index = int(past_data[6])
        self.wind_speed_kmh = float(past_data[7])
        self.description = forecast_data["list"][0]["weather"][0]["description"]
        self.min_temp_forecast = 0
        self.max_temp_forecast = 0
        self.description_forecast = ""
        self.precip_forecast = RainIntensity.NONE
        self.forecast_summary = self.process_forecast_data(forecast_data)

    def process_forecast_data(self, forecast_data):
        forecasts = []
        for forecast in forecast_data["list"]:
            forecasts.append(Forecast(forecast))

        precip = 0
        max_temp = -100
        min_temp = 100
        description_dict = {}
        for forecast in forecasts:
            if forecast.time - timedelta(hours=6) < datetime.now():
                if forecast.rain != "None":
                    precip += forecast.rain
                if forecast.temperature > max_temp:
                    max_temp = forecast.temperature
                if forecast.temperature < min_temp:
                    min_temp = forecast.temperature
                if forecast.description not in description_dict:
                    description_dict[forecast.description] = 0
                description_dict[forecast.description] += 1
                description_dict = dict(sorted(description_dict.items(), key=lambda item: item[1], reverse=True))
        self.precip_forecast = get_rain_intensity(precip)
        self.min_temp_forecast = min_temp
        self.max_temp_forecast = max_temp
        self.description_forecast = list(description_dict.keys())[0]
        return "\n  - Forecast:\n    - max. Temp: " + str(max_temp) + "°C\n    - min. Temp: " + str(min_temp) + \
            "°C\n    - Rain: " + self.precip_forecast.name + "\n    - Description: " + self.description_forecast

    def is_after_sunset(self):
        # transformation is needed to compare times
        utc = pytz.UTC
        now = utc.localize(datetime.now())
        return self.sunset < now

    def is_before_sunset(self):
        # transformation is needed to compare times
        utc = pytz.UTC
        now = utc.localize(datetime.now())
        return now < self.sunset

    def is_before_sunrise(self):
        # transformation is needed to compare times
        utc = pytz.UTC
        now = utc.localize(datetime.now())
        return self.sunrise > now

    def is_after_sunrise(self):
        # transformation is needed to compare times
        utc = pytz.UTC
        now = utc.localize(datetime.now())
        return now > self.sunrise

    def is_watering_necessary(self):
        """
        check sun position. Watering is not necessary if
        - is after sunrise
        - is before sunset
        """
        if self.is_after_sunrise() and self.is_before_sunset():
            return False
        """
        check precip past hour. Watering is not necessary if
        - very heavy rain
        - heavy rain
        - moderate rain
        """
        if self.precip_past_hour == RainIntensity.HEAVY_RAIN or self.precip_past_hour == RainIntensity.VERY_HEAVY_RAIN \
                or self.precip_past_hour == RainIntensity.MODERATE_RAIN:
            return False
        """
        check precip past 24 hours. Watering is not necessary if
        - very heavy rain
        - heavy rain and light rain oder drizzle in past hour
        """
        if self.precip_past_24_hours == RainIntensity.VERY_HEAVY_RAIN:
            return False
        if self.precip_past_24_hours == RainIntensity.HEAVY_RAIN and self.precip_past_hour == RainIntensity.LIGHT_RAIN \
                or self.precip_past_hour == RainIntensity.DRIZZLE:
            return False
        """
        include forecast to check. Watering is not necessary if
        - light rain in past hour and light rain in next 3 hours
        - moderate or heavy rain or very heavy rain in next 3 hours
        """
        if self.precip_past_hour == RainIntensity.LIGHT_RAIN and self.precip_forecast == RainIntensity.LIGHT_RAIN:
            return False
        if self.precip_forecast == RainIntensity.MODERATE_RAIN or self.precip_forecast == RainIntensity.HEAVY_RAIN \
                or self.precip_forecast == RainIntensity.VERY_HEAVY_RAIN:
            return False
        """
        otherwise watering is necessary
        """
        return True

    def __str__(self):
        return format_datetime(self.datetime) + "\n  - Temperature: " + str(self.temperature) + \
            "°C\n  - Description: " + self.description + "\n  - Rain past Hour: " + \
            self.precip_past_hour.name + "\n  - Rain past 24 hours: " + \
            self.precip_past_24_hours.name + "\n  - Wind Speed: " + str(self.wind_speed_kmh) + \
            " km/h\n  - UV-Index: " + str(self.uv_index) + "\n  - Sunrise: " + \
            format_datetime(utc_to_local(self.sunrise)) + "\n  - Sunset:  " + \
            format_datetime(utc_to_local(self.sunset)) + self.forecast_summary


if __name__ == "__main__":
    wd = get_weather_data()
    print(wd.is_watering_necessary())
