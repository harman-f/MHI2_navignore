#!/bin/bash
set -e
. ../process.sh

FILES="\
de/vw/mib/asl/internal/androidauto/target/RequestHandler.java \
de/vw/mib/asl/internal/carplay/common/CarPlayModeHandling.java \
"

for j in $FILES; do
echo "Compiling $j"
${JAVA_HOME}/bin/javac -source 1.2 -target 1.2 -cp ".:${JAR}" $j
done

CLASSES=$(echo $FILES | sed -r 's:\.java:.class:g')

${JAVA_HOME}/bin/jar cvf navignore_vw.jar $CLASSES

#ssh mibw sh -l /root/.profile
#scp NavActiveIgnore.jar mibw:/mnt/app/eso/hmi/lsd/jars/

