/opt/jdk/bin/java -server -cp ./Spider.jar com.jfetek.demo.weather.spider.Spiders wiki 2>&1 | tee wiki-log-`date +"%Y%m%d%H%M%S"`.txt

