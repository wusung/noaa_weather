#!/usr/bin/env bash
export WORK_PATH=./data/weather/$1/raw
mkdir -p $WORK_PATH
wget -r -l1 -P$WORK_PATH ftp://ftp.ncdc.noaa.gov/pub/data/noaa/$1
