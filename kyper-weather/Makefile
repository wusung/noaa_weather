test:
	python -m unittest discover -v ./tests
green:
	green . -a
clean:

scp-wrapper:
	scp ./weather/__init__.py wusung.peng@weather.kyper.co:/tmp

deploy-data:
	cp ./weather/__init__.py ~/pycharm/kyper-data/kyper-data/weather
