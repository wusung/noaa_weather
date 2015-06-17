export CP_PATH=
export CP_PATH=$CP_PATH:./lib/weather-spider-0.9-jar-with-dependencies.jar

if [ ! -d "./logs" ]; then mkdir -p ./logs; fi
rm -rf data/weather/country-list.txt
wget -Pdata/weather ftp://ftp.ncdc.noaa.gov/pub/data/noaa/country-list.txt
/usr/bin/java -server -cp $CP_PATH com.jfetek.demo.weather.tasks.weather.CountryInsertTask 2>&1 | tee ./logs/country-`date +"%Y%m%d"`.log
