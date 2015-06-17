export CP_PATH=
export CP_PATH=$CP_PATH:./lib/weather-spider-0.9-jar-with-dependencies.jar
export JAVA_BIN=$(which java)

if [ ! -d "./logs" ]; then mkdir -p ./logs; fi
rm -f data/weather/isd-history.csv
wget -Pdata/weather ftp://ftp.ncdc.noaa.gov/pub/data/noaa/isd-history.csv
$JAVA_BIN -server -cp $CP_PATH com.jfetek.demo.weather.tasks.weather.StationInsertTask 2>&1 | tee ./logs/station-`date +"%Y%m%d"`.log
