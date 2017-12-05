FROM java:8-jdk

ENV GRADLE_VERSION 2.6


RUN apt-get update && apt-get install -y --no-install-recommends openjfx && rm -rf /var/lib/apt/lists/*

VOLUME /root/.m2
VOLUME /root/.gradle

WORKDIR /usr/bin
RUN curl -sLO https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-all.zip && \
  unzip gradle-${GRADLE_VERSION}-all.zip && \
  ln -s gradle-${GRADLE_VERSION} gradle && \
  rm gradle-${GRADLE_VERSION}-all.zip

ENV GRADLE_HOME /usr/bin/gradle
ENV PATH $PATH:$GRADLE_HOME/bin

COPY . /usr/src/app
COPY ./build.gradle .


COPY . .
EXPOSE 9080

CMD ./gradlew bootRun
