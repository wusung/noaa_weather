Webservice API Project:
	archive: weather-demo_v.1.0_20150421-1.zip

	���ҡG
		Java 6
		Tomcat 6
		Eclipse 4.3 �M��
		�ϥ�Maven�޲zlibrary�̩ۨ�

	�פJ�G
		1. File -> Import -> General: Existing Projects into Workspace
		2. Select archive file: ���w�ɮשҦb
		3. �U���ܮط|�C�X���ɮ׸̪��M��
		4. ����M�� weather demo v.1.0
		5. �פJ�Y�i

	�ץX��webapp�G
		1. File -> Export -> Web: WAR file
		2. Web project ��ܱM��
		3. Destination ��ܶץX�ؿ��P�ɦW
		[4,5�B�J�����]�w server runtime �ɥi�ϥ�]
		4. �Ŀ� Optimize for a specific server runtime
		5. ��ܥؼ� runtime (�p�Gtomcat 6)
		[4,5�B�J�����]�w server runtime �ɥi�ϥ�]
		6. Finish�A�Y�N�M�ץ��]�����@�� webapp ��(war��)

	API�]�w�ɡG res/com.jfetek.demo.weather/system.setup
		�{���ϥΥؿ��P��Ʈw�s�u�]�w�����i�H�b�̭��ק�
		�`�N�I
			�C���Ұʵ{���|��{������ؿ����O�_�� system.setup �ɮ�
			�p�G�S���A�h�|���� system.setup �ɨ����ؿ�
			�@��tomcat����ؿ��Y���w�˥ؿ����U�� bin/ 
			�ק���ɮפ��e�A�A���ҵ{���Y�|Ū�J�s���]�w��


Webservice API webapp:
	file: weather-demo_v.1.0_20150421-1.war

	�ϥΤ�k�G
		���tomcat��webapps�ؿ����U�A�Y�|�۰ʸѶ}�õo����webapp
		webapp�W�٧Ywar�ɪ��W�١A��p aaa.war �Y�|�o��������ڥؿ����U aaa �ؿ�
		�Y /aaa/
		�p�G�Q�n�o��������ڥؿ��Awar�ɦW�אּ ROOT.war �Y�|�o��������ڥؿ�
		�Y /


Spider Project
	file: bigdata-spider_v.1.0_20150421-1.zip

	���ҡG
		Java 6
		Tomcat 6
		Eclipse 4.3 �M��
		�ϥ�Maven�޲zlibrary�̩ۨ�
		MongoDB

	�פJ�G
		1. File -> Import -> General: Existing Projects into Workspace
		2. Select archive file: ���w�ɮשҦb
		3. �U���ܮط|�C�X���ɮ׸̪��M��
		4. ����M�� proj. bigdata spider
		5. �פJ�Y�i

	���]�G
		1. File -> Export -> Java: runnable JAR file
		2. Launch configuration: ���spider�M��
		3. Export destination: ��ܿ�X�ؿ��P�ɦW
		4. Library handling: Extract required libraries into generated JAR
		5. Finish�Y�i

	Spider�]�w�ɡG src/main/resources/com.jfetek.demo.weather/system.setup
		Spider�ϥΥؿ��P��Ʈw�s�u�]�w�����i�H�b�̭��ק�
		�`�N�I
			�C���Ұʵ{���|��{������ؿ����O�_�� system.setup �ɮ�
			�p�G�S���A�h�|���� system.setup �ɨ����ؿ�
			����ؿ��Y������spider���ؿ�
			�ק���ɮפ��e�A�A���ҵ{���Y�|Ū�J�s���]�w��

	Wiki Spider �����k�G
		java -cp ./Spider.jar com.jfetek.demo.weather.spider.Spiders wiki
		�ΰѦ� wiki.sh ���e

	Weather Spider �����k�G
		java -cp ./Spider.jar com.jfetek.demo.weather.spider.Spiders weather
		�ΰѦ� weather.sh ���e

	