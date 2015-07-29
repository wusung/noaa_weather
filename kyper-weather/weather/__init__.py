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
RAW_FIELDS = ["date", "time", "direction", "speed", "gus", "clg", "skc", "l", 
              "m", "h", "vsb", "mw", "aw", "w", "slp", "alt", "stp", "pcp01", "pcp06",
              "pcp24", "pcpxx", "sd"]
SUM_FIELDS = ["speed", "gus", "vsb", "temerature", "dewp", 
              "slp", "stp", "pcpxx", "sd"]


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


def weather_station(station, start_dt, end_dt, sample_rate="d", fields=FIELDS):
    params = dict(
        station=station,
        begin_time=start_dt,
        end_time=end_dt,
        fields=",".join(fields),
        sample_rate=sample_rate
    )

    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    return pd.read_json(data, orient="split")


def weather_stations(stations, start_dt, end_dt, sample_rate="d", fields=SUM_FIELDS):
    params = dict(
        stations=",".join(stations),
        begin_time=start_dt,
        end_time=end_dt,
        fields=",".join(fields),
        sample_rate=sample_rate
    )

    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    return pd.read_json(data, orient="split")

def station_list(country=None, lat=-999, lng=-999):
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
    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    return pd.read_json(data, orient="split")


def weather_country(country, start_dt=None, end_dt=None, sample_rate="r", fields=SUM_FIELDS):
    params = dict(
        country=country,
        begin_time=start_dt,
        end_time=end_dt,
        sample_rate=sample_rate,
        fields=",".join(fields)
    )
    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    return pd.read_json(data, orient="split")


