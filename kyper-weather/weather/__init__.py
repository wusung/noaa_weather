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
SUM_FIELDS = ["date", "time", "speed", "gus", "vsb", "temperature", "dewp", 
              "slp", "stp", "pcpxx", "sd"]


def weather_stations(stations, start_date, end_date, freq="d", fields=SUM_FIELDS):
    params = dict(
        stations=",".join(stations),
        begin_time=start_date,
        end_time=end_date,
        fields=",".join(fields),
        sample_rate=freq
    )
    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    result = pd.read_json(data, orient="split")                                                                       
    return result.sort_index(by=["date"], ascending=[True])

def station_list(country=None, lat=-999, lng=-999, limit=10):
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
        lng=lng,
        limit=limit
    )
    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    return pd.read_json(data, orient="split")


