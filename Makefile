
weather:
	cd "weather demo v.1.0" && mvn package
spider:
	cd "proj. bigdata spider" && mvn package
	cd "proj. bigdata spider" && mkdir -p target/package/lib target/package/logs
	cd "proj. bigdata spider" && cp bin/*.sh ./target/package
	cd "proj. bigdata spider" && cp target/weather-spider-0.9-jar-with-dependencies.jar target/package/lib
clean:
	rm -rf ./target

