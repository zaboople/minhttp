java -version || goto :error
call mvn package || goto :error
java -Xmx15m -jar target\hello-1.0.0.all.jar || goto :error

goto :end
:error
echo FAILED
:end