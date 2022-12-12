FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.8.0_2.13.10

ENV SBT_OPTS="-Xms512M -Xmx4096M -Xss2M -XX:MaxMetaspaceSize=2048M"
WORKDIR /src
COPY . /src
RUN apt-get update
RUN apt-get install unzip
RUN wget https://www.wroclaw.pl/open-data/6db186a9-9b94-4ed4-8d63-f3fa6d38dc5f/XML-rozkladyjazdy.zip
RUN mkdir ./data
RUN unzip ./XML-rozkladyjazdy.zip -d ./data
RUN find ./data/ -mindepth 2 -type f -exec mv '{}' ./data \;
RUN find ./data -type d -empty -delete
CMD sbt 'run ./data 0.0.0.0 80'
EXPOSE 80