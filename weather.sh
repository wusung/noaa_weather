/opt/jdk/bin/java -server -cp ./Spider.jar com.jfetek.demo.weather.spider.Spiders weather 2>&1 | tee weather-log-`date +"%Y%m%d%H%M%S"`.txt

