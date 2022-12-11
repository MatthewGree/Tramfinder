docker build . -t tramfinder --no-cache
docker run -p 8080:8080/tcp -d tramfinder