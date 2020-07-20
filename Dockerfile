FROM openjdk:8
ADD target/otcleditor.war otcleditor.war
VOLUME /tmp
EXPOSE 2243
ENV JAVA_OPTS=""
CMD java $JAVA_OPTS -jar otcleditor.war

# to build: sudo docker build  -t ****.dkr.ecr.ap-south-1.amazonaws.com/otcleditor:1.0 .
# to run: 
#    - sudo docker run -e JAVA_OPTS="$_JAVA_OPTIONS" -v /otcleditor:/otcleditor -p 8080:8080 *****.dkr.ecr.ap-south-1.amazonaws.com/usermgmt:1.0
# to push to ECR
#    - Run 'aws configure' just once - contact Frank for Access-key and Secret-key.
#    - first authentiate with AWS - '(aws ecr get-login --no-include-email --region ap-south-1)' if not yet authenticated.
#    - sudo docker push ****.dkr.ecr.ap-south-1.amazonaws.com/otcleditor:1.0
