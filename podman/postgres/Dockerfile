FROM docker.io/library/postgres:15

#COPY postgresql.conf /usr/share/postgresql/15/

#RUN mkdir -p /etc/postgresql/15/main
#COPY postgresql.conf /etc/postgresql/15/main

#RUN mkdir -p /etc/postgresql/15.1/main
#COPY postgresql.conf /etc/postgresql/15.1/main

#COPY postgresql.conf /etc/postgresql

#RUN sed -i -e "s/wal_level = replica/wal_level = logical/" /usr/share/postgresql/postgresql.conf.sample
#RUN sed -i -e "s/wal_level = replica/wal_level = logical/" /usr/share/postgresql/postgresql.conf.sample.dpkg
#RUN sed -i -e "s/wal_level = replica/wal_level = logical/" /usr/share/postgresql/postgresql.conf.sample.dpkg

CMD [ "postgres", "-c", "wal_level=logical" ]
