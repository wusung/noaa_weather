
weather:
	cd "weather demo v.1.0" && mvn package
spider:
	cd "proj. bigdata spider" && mvn package
	mkdir -p target/lib target/logs
	cp bin/*.sh ./target
	cp "proj. bigdata spider"/target/weather-spider-0.9-jar-with-dependencies.jar ./target/lib

clean:
	rm -rf ./target

