export CP_PATH=
export CP_PATH=$CP_PATH:./lib/weather-spider-0.9-jar-with-dependencies.jar

if [ ! -d "./logs" ]; then mkdir -p ./logs; fi
/usr/bin/java -server -cp $CP_PATH com.jfetek.demo.weather.tasks.weather.MassInsertTask $1 $1 2>&1 | tee ./logs/weather-`date +"%Y%m%d"`.log
