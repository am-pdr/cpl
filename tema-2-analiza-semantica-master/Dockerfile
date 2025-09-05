FROM jokeswar/base-ctl

RUN echo "Hello from Docker"

RUN apt-get update

# tzdata is required by openjdk-21
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install --fix-missing
RUN	apt-get install -yqq spim openjdk-21-jdk

COPY ./checker ${CHECKER_DATA_DIRECTORY}

COPY ./antlr-4.13.0-complete.jar ${CHECKER_DATA_DIRECTORY}/..
