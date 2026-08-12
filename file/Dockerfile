# 基础镜像
FROM openjdk:8

#设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#拷贝jar包
COPY hakira-core-0.1.2-SNAPSHOT.jar /hakira-core.jar

#入口
ENTRYPOINT [ "java", "-jar", "/hakira-core.jar" ]