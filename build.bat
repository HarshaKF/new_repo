@echo off
echo Creating directories...
if not exist "target" mkdir target

echo Compiling Java files...
javac -d target -cp "mysql-connector-j-8.0.27.jar" src/main/java/com/justglance/db/DBConnection.java

echo Creating JAR file...
cd target
jar cvfe tuition-website.jar com.justglance.db.DBConnection com/justglance/db/DBConnection.class
cd ..

echo Build complete! JAR file is in target/tuition-website.jar
pause
