docker volume create pgdata
docker volume inspect pgdata
docker run --name local-postgres -p 5432:5432 --restart unless-stopped -e POSTGRES_PASSWORD=postgres -v pgdata:/var/lib/postgresql/data -v .:/sampledb -d postgres
docker exec local-pgsql  pg_restore -U postgres -d dvdrental /sampledb/dvdrental.tar
