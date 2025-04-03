FROM openjdk:latest
ADD publiccms-parent/publiccms/target/publiccms.war /opt/publiccms.war
ADD data /data
ENV PORT=8080
ENV CONTEXTPATH=""
ENV FILEPATH="/data/publiccms"
ENV TZ=Asia/Shanghai
VOLUME $FILEPATH
ENTRYPOINT ["/bin/sh" "-c" "java -jar -Dcms.port=$PORT -Dcms.contextPath=$CONTEXTPATH -Dcms.filePath=$FILEPATH /opt/publiccms.war > /var/log/publiccms.log"]
EXPOSE $PORT