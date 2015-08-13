from datetime import datetime
from pandas import DataFrame
import unittest
import weather


class TestWeather(unittest.TestCase):
    def test_import(self):
        import weather
        print(weather)

    def test_weather_station(self):
        weather_r = weather.weather_station(station="727930-24233", start_dt="1948-01-01", end_dt="1948-01-01",
                                            fields=["date", "time", "direction", "speed", "temperature"],
                                            sample_rate="r")
        print(weather_r.head())

        weather_d = weather.weather_station(station="727930-24233", start_dt="1948-01-01", end_dt="1948-01-01",
                                            sample_rate="r")
        print(weather_d.head())

    def test_weather_geo_1(self):

        # 702610-26411
        lat, lng = weather.geo_api(address="BOSTON,MA,US")
        weather_r = weather.weather_geo(lat=lat, lng=lng, start_dt="1903-01-01", end_dt="1948-01-01",
                                        sample_rate="r")
        self.assertFalse(weather_r.empty)

    def test_weather_geo_2(self):
        weather_r = weather.weather_geo(lat=42.35, lng=-71.05, start_dt="1903-01-01", end_dt="1948-01-01",
                                        sample_rate="r")
        self.assertFalse(weather_r.empty)

    def test_weather_geo_1(self):
        weather_r = weather.weather_geo(lat=-1.692384, lng=-77.036871, start_dt="1903-01-01", end_dt="1948-01-01",
                                        sample_rate="r")
        self.assertFalse(weather_r.empty)

    # def test_historical(self):
    #     data = weather.historical(address="BOSTON,MA,US", start_dt=datetime(1940, 1, 1), end_dt=datetime(1942, 1, 1),
    #                               fields=["date", "time", "direction", "speed"], sample_rate="d")
    #     print(data)

    def test_geo_api(self):
        x, y = weather.geo_api("Boston, MA, US")
        print(x, y)
        self.assertIsNotNone(x)
        self.assertIsNotNone(y)

        x, y = weather.geo_api("Boston, US")
        print(x, y)
        self.assertIsNotNone(x)
        self.assertIsNotNone(y)

        x, y = weather.geo_api("boston")
        print(x, y)
        self.assertIsNotNone(x)
        self.assertIsNotNone(y)

    def test_station_list(self):
        stations = weather.station_list("US")
        #self.assertIsInstance(stations, dict.__class__)

    def test_get_fields(self):
        weather.get_fields()

    if __name__ == '__main__':
        unittest.main()
