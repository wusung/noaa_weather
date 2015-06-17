export CP_PATH=
export CP_PATH=$CP_PATH:./lib/weather-spider-0.9-jar-with-dependencies.jar
export JAVA_BIN=$(which java)

if [ ! -d "./logs" ]; then mkdir -p ./logs; fi
$JAVA_BIN -server -cp $CP_PATH com.jfetek.demo.weather.tasks.weather.MassInsertTask $1 $1 2>&1 | tee ./logs/weather-`date +"%Y%m%d"`.log
