javac \
    -bootclasspath freemarker-2.3.23.jar:greendao-generator-2.2.0.jar:/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar \
    -d . \
    src/net/bradmont/openmpd/daogenerator/OpenMPDDaoGenerator.java \
    && java \
    -classpath freemarker-2.3.23.jar:greendao-generator-2.2.0.jar:/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar:. \
    net.bradmont.openmpd.daogenerator.OpenMPDDaoGenerator
