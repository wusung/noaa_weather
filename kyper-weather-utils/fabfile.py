# -*- coding: utf-8 -*-
from __future__ import with_statement

import os
import re

import pymongo
from fabric.api import local

weather_file = re.compile(r'[0-9]+-[0-9]+-[0-9]+\.gz')

DB_FIELDS = {
    #field: (db field,      start, length)
    "USAF": (None,          0,  6),
    "WBAN": (None,          7,  5),
    "YR--MODAHRMN": (None,  13, 12),
    "DIR":  ("direction",   26, 3),
    "SPD":  ("speed",       30, 3),
    "GUS":  ("gus",         34, 3),
    "CLG":  ("clg",         38, 3),
    "SKC":  ("skc",         42, 3),
    "L":    ("l",           46, 1),
    "M":    ("m",           48, 1),
    "H":    ("h",           50, 1),
    "VSB":  ("vsb",         52, 4),
    "MW1":  ("mw",          57, 2),
    "MW2":  ("mw",          60, 2),
    "MW3":  ("mw",          63, 2),
    "MW4":  ("mw",          66, 2),
    "AW1":  ("aw",          69, 2),
    "AW2":  ("aw",          72, 2),
    "AW3":  ("aw",          75, 2),
    "AW4":  ("aw",          78, 2),
    "W":    ("w",           81, 1),
    "TEMP": ("temperature", 83, 4),
    "DEWP": ("dewp",        88, 4),
    "SLP":  ("slp",         93, 6),
    "ALT":  ("alt",         100, 5),
    "STP":  ("stp",         106, 6),
    "MAX":  ("max",         113, 3),
    "MIN":  ("min",         117, 3),
    "PCP01": ("pcp01",      121, 5),
    "PCP06": ("pcp06",      127, 5),
    "PCP24": ("pcp24",      133, 5),
    "PCPXX": ("pcpxx",      139, 5),
    "SD":   ("sd",          145, 2),
}

SSV_FIELD = "USAF  WBAN YR--MODAHRMN DIR SPD GUS CLG SKC L M H  VSB MW1 MW2 MW3 MW4 AW1 AW2 AW3 AW4 W TEMP DEWP    SLP   ALT    STP MAX MIN PCP01 PCP06 PCP24 PCPXX SD"

mongo_db_name="weather_yk"

class MongoCollection(object):
    connection = pymongo.MongoClient("localhost", 27017)
    def __init__(self, collection="", db_name=mongo_db_name):
        if not collection:
            raise TypeError("no collection name.")
        self.collection_name = collection
        self.db_connection = self.connection[db_name]

    @property
    def collection(self):
        return self.db_connection[self.collection_name]


def insert_data(filename):
    from os.path import basename
    s = basename(filename).split('-')
    station = "{}-{}".format(s[0], s[1])

    year = s[2].replace(".ssv", "")
    db = MongoCollection("record.y{}".format(year))

    with open(filename) as file_:
        file_.next()    #YK: skip header
        for line in file_:
            w_data = {k: str(line[ v[1] : v[1]+v[2] ]).strip() for k, v in DB_FIELDS.items()}

            dt_str = w_data["YR--MODAHRMN"]
            date = "{}-{}-{}".format(dt_str[0:4], dt_str[4:6], dt_str[6:8])
            time = "{}:{}:00".format(dt_str[8:10], dt_str[10:12])

            mw = [w_data[k] if "*" not in w_data[k] else None for k in ["MW1", "MW2", "MW3", "MW4"]]
            aw = [w_data[k] if "*" not in w_data[k] else None for k in ["AW1", "AW2", "AW3", "AW4"]]

            def _clean_data(key, value):
                try:
                    key = DB_FIELDS[key][0]
                    if not key:
                        raise KeyError
                except KeyError:
                    return (None, None)
                else:
                    return key, value
            w_data = dict((_clean_data(k, v) for k, v in w_data.items() if "*" not in v))
            try:
                del w_data[None]
            except:
                pass

            w_data["station"] = station
            w_data["date"] = date
            w_data["time"] = time
            if any(mw):
                w_data["mw"] = mw
            if any(aw):
                w_data["aw"] = aw

            db.collection.insert(w_data)

    #YK: TODO: create index



def handle_raw_file(filename):
    print "Process %s." % filename
    local("gunzip -f -k %s" % filename)
    local("java ishJava %s %s" % (filename.replace('.gz', ''), filename.replace('.gz', '.ssv')))
    insert_data(filename.replace('.gz', '.ssv'))
    local("rm -rf %s %s" % (filename.replace('.gz', ''), filename.replace('.gz', '.ssv')))

def process(path="./"):
    for root, dirs, files in os.walk(path):
        for f_name in files:
             match = weather_file.match(f_name)
             if match:
                 handle_raw_file(os.path.join(root, f_name))


if __name__ == "__main__":
    process()
    #insert_data("test/010000-99999-2001.ssv")

