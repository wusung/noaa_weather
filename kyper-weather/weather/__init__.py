# -*- coding: utf8 -*-
import sys
import pandas as pd

from collections import namedtuple
from ..utils import get_data


VERSION = "0"
SERVICE = "weather"
FIELDS = ["date", "time", "direction", "speed", "temperature", "dewp", "min", "max"]
RAW_FIELDS = ["date", "direction", "speed", "gus", "clg", "skc", "l",
              "m", "h", "vsb", "mw", "aw", "w", "slp", "alt", "stp", "pcp01", "pcp06",
              "pcp24", "pcpxx", "sd"]
SUM_FIELDS = ["date", "speed", "gus", "vsb", "temperature", "dewp", 
              "slp", "stp", "pcpxx", "sd"]


def weather_historical(stations, start_date, end_date, fields=RAW_FIELDS, offset=0, limit=1000):
    """
    Args:
        stations: list[string], The id of weather stations. The number of stations should be less than 100.
        start_date: str, The optional start date for the query (optional).
        end_date: str, The optional end date for the query (optional).
        freq: str: The returned frequence, can be one of 'd', 'w', 'm', which stand for daily, weekly and monthly
        fields: list[string], the returned fields, can be the following values. ["date", "speed", "gus", "vsb", "temperature", "dewp", "slp", "stp", "pcpxx", "sd"] which means as the following.
            date: Date
            speed: Wind speed in miles per hour
            gus: Gust in miles per hour
            vsb: Visibility in statute miles to nearest tenth
            temperature: Temperature in fahrenheit
            dewp: Dew point in fahrenheit
            slp: Sea level pressure in millibars to nearest tenth
            stp: Station pressure in millibars to nearest tenth
            pcpxx: Liquid precip report in inches and hundredths, for a period
            sd: Snow depth in inches
        offset: The starting position of returning records. Default: 0
        limit: Limit of returning records. Default: 1000
    Returns:
        pandas.DataFrame: Return a pandas.DataFrame contains weather data in the stations. Returns DataFrame.emtpy if none where found.
    """
    if len(stations) > 100:
        raise Exception("The number of stations should be less than 100")
    params = dict(
        stations=",".join(stations),
        begin_time=start_date,
        end_time=end_date,
        fields=",".join(fields),
        offset=offset,
        limit=limit
    )
    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    result = pd.read_json(data, orient="split")
    return result.sort_index(ascending=[True])


def weather_stations(stations, start_date, end_date, freq="d", fields=SUM_FIELDS, offset=0, limit=1000):
    """
    Args:
        stations: list[string], The id of weather stations. The number of stations should be less than 100.
        start_date: str, The optional start date for the query (optional).
        end_date: str, The optional end date for the query (optional).
        freq: str: The returned frequence, can be one of 'd', 'w', 'm', which stand for daily, weekly and monthly
        fields: list[string], the returned fields, can be the following values. ["date", "speed", "gus", "vsb", "temperature", "dewp", "slp", "stp", "pcpxx", "sd"] which means as the following.
            date: Date
            speed: Wind speed in miles per hour
            gus: Gust in miles per hour
            vsb: Visibility in statute miles to nearest tenth
            temperature: Temperature in fahrenheit
            dewp: Dew point in fahrenheit
            slp: Sea level pressure in millibars to nearest tenth
            stp: Station pressure in millibars to nearest tenth
            pcpxx: Liquid precip report in inches and hundredths, for a period
            sd: Snow depth in inches
        offset: The starting position of returning records. Default: 0
        limit: Limit of returning records. Default: 1000
    Returns:
        pandas.DataFrame: Return a pandas.DataFrame contains weather data in the stations. Returns DataFrame.emtpy if none where found.
    """
    if len(stations) > 100:
        raise Exception("The number of stations should be less than 100")
    params = dict(
        stations=",".join(stations),
        begin_time=start_date,
        end_time=end_date,
        fields=",".join(fields),
        sample_rate=freq,
        offset=offset,
        limit=limit
    )
    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    result = pd.read_json(data, orient="split")                                                                       
    return result.sort_index(ascending=[True])


def station_list(country=None, lat=-999, lng=-999, limit=10):
    """ Find Station by country Name or Latitude and Longtitude.
    Args:
        country: The country name for the query (optional). Refer to ftp://ftp.ncdc.noaa.gov/pub/data/noaa/country-list.txt
        lat: The latitue of the location for the query
        lng: The longitude of the location for the query
        limit: Limit of returning records. Default: 10
    Returns:
        pandas.DataFrame: Return a pandas.DataFrame contains stations in the country or location. Returns DataFrame.emtpy if none where found.
    """
    params = dict(
        country=country,
        lat=lat,
        lng=lng,
        limit=limit
    )
    data = get_data(SERVICE, VERSION, sys._getframe().f_code.co_name, **params)
    return pd.read_json(data, orient="split")


def get_fields():
    """ List all fields provided by weather data API.
    Returns:
        A pandas.DataFrame of all fields provided by weather data API.
    """

    Field = namedtuple('Field','Name Description')
    all_fields = [Field("date","Date"),
                  Field("speed", "Wind speed in miles per hour, accurate to integer"),
                  Field("gus", "Gust in miles per hour, accurate to integer"),
                  Field("vsb", "Visibility in statute miles to nearest tenth, accurate to 1 decimal places"),
                  Field("temperature", "Temperature in fahrenheit, accurate to integer"),
                  Field("dewp", "Dew point in fahrenheit, accurate to integer"),
                  Field("slp", "Sea level pressure in millibars to nearest tenth, accurate to 2 decimal places"),
                  Field("stp", "Station pressure in millibars to nearest tenth, accurate to 2 decimal places"),
                  Field("pcpxx", "Liquid precip report in inches and hundredths, for a period, accurate to 2 decimal places"),
                  Field("sd", "Snow depth in inches in round number")]

    return pd.DataFrame(all_fields, columns=["Name","Description"])

