docker build . -t tramfinder --no-cache
docker run -p 80:80/tcp -d --rm tramfinder