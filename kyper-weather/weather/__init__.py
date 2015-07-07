# -*- coding: utf8 -*-
import sys
import pandas as pd
import requests
import json
import ast

try:
    from ..utils import get_data
except Exception:
    def _parse_response(response, version):
        """
          Parses response into Python dict and checks for exceptions
        """
        js = response.json()

        if js.get('status') == "ok":
            if js.get('version') == version:
                return js
            else:
                raise BaseException("error version")
        elif js.get('status') == "error":
            raise BaseException("error status")
        else:
            raise BaseException("error others")

    # Write Development Wrapper
    def get_data(service, version, method, **kwargs):
        end_point = "http://weather.kyper.co"
        # end_point = "http://192.168.64.1:8080/weather-demo"
        req_url = "{SERVER}/{ACTION}".format(SERVER=end_point, ACTION=method)
        return _parse_response(requests.get(req_url, params=kwargs),
                               version).get("data")
        # return json.loads(resp.text)["data"]

VERSION = "0"
SERVICE = "weather"
FIELDS = ["date", "time", "direction", "speed", "temperature", "dewp", "min", "max"]


def weather_address(address, start_dt, end_dt, sample_rate="r", fields=FIELDS):
    lat, lng = _find_station(address)
    params = dict(
        lat=lat,
        lng=lng,
        begin_time=start_dt,
        end_time=end_dt,
        fields=",".join(fields),
        sample_rate=sample_rate
    )

    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    return pd.read_json(data, orient="split")


def weather_station(station, start_dt, end_dt, sample_rate="r", fields=FIELDS):
    params = dict(
        station=station,
        begin_time=start_dt,
        end_time=end_dt,
        fields=",".join(fields),
        sample_rate=sample_rate
    )

    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    return pd.read_json(data, orient="split")


def weather_geo(lat, lng, start_dt, end_dt, sample_rate="R", fields=FIELDS):
    params = dict(
        lat=lat,
        lng=lng,
        begin_time=start_dt,
        end_time=end_dt,
        fields=",".join(fields),
        sample_rate=sample_rate
    )

    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    return pd.read_json(data, orient="split")


def geo_api(address):
    city = ""
    state = ""
    country = ""
    addresses = address.split(",")
    if addresses:
        if addresses.__len__() == 3:
            city = addresses[0]
            state = addresses[1]
            country = addresses[2]
        elif addresses.__len__() == 2:
            city = addresses[0]
            country = addresses[1]
        else:
            city = addresses[0]

    params = dict(
        city=city,
        state=state,
        country=country
    )

    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    json_obj = pd.read_json(data, orient="records")
    ll = ast.literal_eval(json_obj["data"][0]["geo"])
    return ll[1], ll[0]


def station_list(country, lat=-999, lng=-999):
    """
    Find Station by Count Name or Latitude and Longtitude
    :param country:
    :param lat:
    :param lng:
    :return:
    """
    params = dict(
        country=country,
        lat=lat,
        lng=lng
    )
    return get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)


def historical(address, start_dt, end_dt, fields, sample_rate):
    station = _find_station(address=address)
    return weather_station(station=station, start_dt=start_dt, end_dt=end_dt, sample_rate=sample_rate, fields=fields)


def _find_station(address):
    return address
