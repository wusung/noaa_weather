/usr/bin/java -server -cp ./weather-spider-0.9.jar com.jfetek.demo.weather.spider.Spiders weather 2>&1 | tee ./logs/weather-`date +"%Y%m%d%H%M%S"`.log

